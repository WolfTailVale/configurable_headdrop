package com.example.configurableheaddrop;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Wolf;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.projectiles.ProjectileSource;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class ConfigurableHeadDrop extends JavaPlugin implements Listener {
    private FileConfiguration cfg;
    private boolean onlyWhenKilledByPlayer;
    private boolean giveToKiller;
    private boolean playerHeadsEnabled;
    private double defaultMobChance;
    private double defaultPlayerChance;
    private Map<String, Double> mobChances;
    private Map<String, Boolean> mobEnabled;
    private Random random = new Random();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        reloadSettings();
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("ConfigurableHeadDrop enabled");
    }

    @Override
    public void onDisable() {
        getLogger().info("ConfigurableHeadDrop disabled");
    }

    private void reloadSettings() {
        cfg = getConfig();
        playerHeadsEnabled = cfg.getBoolean("player-heads.enabled", true);
        onlyWhenKilledByPlayer = cfg.getBoolean("player-heads.only-when-killed-by-player", true);
        giveToKiller = cfg.getBoolean("player-heads.give-to-killer", true);
        defaultPlayerChance = cfg.getDouble("player-heads.default-chance-percent", 100.0);
        defaultMobChance = cfg.getDouble("mob-heads.default-chance-percent", 2.0);
        mobChances = new HashMap<>();
        mobEnabled = new HashMap<>();
        if (cfg.isConfigurationSection("mob-heads.chances")) {
            for (String key : cfg.getConfigurationSection("mob-heads.chances").getKeys(false)) {
                mobChances.put(key.toUpperCase(Locale.ROOT),
                        cfg.getDouble("mob-heads.chances." + key, defaultMobChance));
            }
        }
        if (cfg.isConfigurationSection("mob-heads.enabled")) {
            for (String key : cfg.getConfigurationSection("mob-heads.enabled").getKeys(false)) {
                mobEnabled.put(key.toUpperCase(Locale.ROOT), cfg.getBoolean("mob-heads.enabled." + key, true));
            }
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity instanceof Player) {
            handlePlayerHead((Player) entity, event);
        } else {
            handleMobHead(entity, event);
        }
    }

    private void handlePlayerHead(Player dead, EntityDeathEvent event) {
        if (!playerHeadsEnabled) return;
        Player killer = getKillerPlayer(dead);
        if (onlyWhenKilledByPlayer && killer == null)
            return;
        double chance = cfg.getDouble("player-heads.default-chance-percent", defaultPlayerChance);
        // per-player override by name (optional)
        if (cfg.isSet("player-heads.chances." + dead.getName())) {
            chance = cfg.getDouble("player-heads.chances." + dead.getName(), chance);
        }
        if (!roll(chance))
            return;

        ItemStack head = new ItemStack(Material.PLAYER_HEAD, 1);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        if (meta != null) {
            meta.setOwningPlayer(dead);
            meta.displayName(Component.text(dead.getName() + "'s Head"));
            head.setItemMeta(meta);
        }

        if (giveToKiller && killer != null) {
            Map<Integer, ItemStack> leftover = killer.getInventory().addItem(head);
            if (!leftover.isEmpty()) {
                killer.getWorld().dropItemNaturally(killer.getLocation(), head);
            }
        } else {
            event.getDrops().add(head);
        }
    }

    private void handleMobHead(LivingEntity entity, EntityDeathEvent event) {
        String type = entity.getType().name();
        // allow server owners to disable specific mob head drops via config
        boolean allowed = mobEnabled.getOrDefault(type, true);
        if (!allowed)
            return;
        double chance = mobChances.getOrDefault(type, defaultMobChance);
        if (!roll(chance))
            return;
        Material headMaterial = getHeadMaterialFor(entity.getType());
        if (headMaterial == null)
            return;
        ItemStack head = new ItemStack(headMaterial, 1);
        if (head.getItemMeta() instanceof SkullMeta sm && headMaterial == Material.PLAYER_HEAD) {
            sm.displayName(Component.text(entity.getType().name() + " Head"));
            head.setItemMeta(sm);
        }

        Player killer = getKillerPlayer(entity);
        if (giveToKiller && killer != null) {
            Map<Integer, ItemStack> leftover = killer.getInventory().addItem(head);
            if (!leftover.isEmpty()) {
                killer.getWorld().dropItemNaturally(killer.getLocation(), head);
            }
        } else {
            event.getDrops().add(head);
        }
    }

    private Player getKillerPlayer(Entity entity) {
        EntityDamageEvent last = entity.getLastDamageCause();
        if (last instanceof EntityDamageByEntityEvent) {
            EntityDamageByEntityEvent ede = (EntityDamageByEntityEvent) last;
            Entity damager = ede.getDamager();
            if (damager instanceof Player)
                return (Player) damager;
            if (damager instanceof Projectile) {
                Projectile proj = (Projectile) damager;
                ProjectileSource shooter = proj.getShooter();
                if (shooter instanceof Player)
                    return (Player) shooter;
            }
            if (damager instanceof Wolf) {
                Wolf wolf = (Wolf) damager;
                if (wolf.getOwner() instanceof Player)
                    return (Player) wolf.getOwner();
            }
        }
        return null;
    }

    private boolean roll(double percent) {
        if (percent <= 0)
            return false;
        if (percent >= 100)
            return true;
        return random.nextDouble() * 100.0 < percent;
    }

    private Material getHeadMaterialFor(EntityType type) {
        // Map EntityType to the appropriate head/skull material where available.
        // Returns null when there is no built-in head item for the entity.
        switch (type) {
            case CREEPER:
                return Material.CREEPER_HEAD;
            case SKELETON:
            case STRAY:
                return Material.SKELETON_SKULL;
            case WITHER_SKELETON:
                return Material.WITHER_SKELETON_SKULL;
            case ZOMBIE:
            case HUSK:
            case ZOMBIE_VILLAGER:
                return Material.ZOMBIE_HEAD;
            case ENDER_DRAGON:
                return Material.DRAGON_HEAD;
            case WITHER:
                return Material.WITHER_SKELETON_SKULL;
            case PLAYER:
                return Material.PLAYER_HEAD;
            case PIGLIN:
            case PIGLIN_BRUTE:
                // No specific piglin head in vanilla; use player head as fallback
                return Material.PLAYER_HEAD;
            default:
                // Many mobs do not have dedicated head items in vanilla (e.g., cow, pig,
                // chicken)
                return null;
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("headdrop")) {
            if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
                if (!sender.hasPermission("configurableheaddrop.reload")) {
                    sender.sendMessage(LegacyComponentSerializer.legacySection().serialize(
                            Component.text("You don't have permission to run this command.", NamedTextColor.RED)));
                    return true;
                }
                reloadConfig();
                reloadSettings();
                sender.sendMessage(LegacyComponentSerializer.legacySection()
                        .serialize(Component.text("ConfigurableHeadDrop config reloaded.", NamedTextColor.GREEN)));
                return true;
            }
            sender.sendMessage(LegacyComponentSerializer.legacySection()
                    .serialize(Component.text("Usage: /headdrop reload", NamedTextColor.YELLOW)));
            return true;
        }
        return false;
    }
}

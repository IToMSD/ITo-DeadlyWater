package pl.polsatgranie.itomsd.deadlyWater;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public final class DeadlyWater extends JavaPlugin implements Listener {

    private List<String> world_list;
    private boolean instantDeath;
    private double damagePerTick;

    private WorldGuardPlugin worldGuard;

    @Override
    public void onEnable() {
        worldGuard = (WorldGuardPlugin) Bukkit.getPluginManager().getPlugin("WorldGuard");

        if (worldGuard == null) {
            getLogger().severe("WorldGuard not found! Disabling plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        Metrics metrics = new Metrics(this, 22935);
        this.getLogger().info("""
                
                ------------------------------------------------------------
                |                                                          |
                |      _  _______        __     __    _____   ____         |
                |     | ||___ ___|      |  \\   /  |  / ____| |  _ \\        |
                |     | |   | |   ___   | |\\\\ //| | | (___   | | \\ \\       |
                |     | |   | |  / _ \\  | | \\_/ | |  \\___ \\  | |  ) )      |
                |     | |   | | | (_) | | |     | |  ____) | | |_/ /       |
                |     |_|   |_|  \\___/  |_|     |_| |_____/  |____/        |
                |                                                          |
                |                                                          |
                ------------------------------------------------------------
                |                 +==================+                     |
                |                 |    DeadlyWater   |                     |
                |                 |------------------|                     |
                |                 |        1.0       |                     |
                |                 |------------------|                     |
                |                 |  PolsatGraniePL  |                     |
                |                 +==================+                     |
                ------------------------------------------------------------
                """);
        saveDefaultConfig();
        FileConfiguration config = getConfig();

        world_list = config.getStringList("world_list");
        instantDeath = config.getBoolean("instant_death");
        damagePerTick = config.getDouble("damage_per_tick");

        getCommand("dwreload").setAliases(List.of("deadlywaterreload"));

        Bukkit.getPluginManager().registerEvents(this, this);
        DeadlyWaterFlag.registerFlags();
    }

    @Override
    public void onDisable() {
        this.getLogger().info("Goodbye!");
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        World world = player.getWorld();

        if (isWorldAffected(world.getName())) {
            if (player.hasPermission("itomsd.deadlywater.bypass")) {
                return;
            }

            if (player.isInWater()) {
                if (instantDeath) {
                    player.setHealth(0.0);
                } else {
                    player.damage(damagePerTick);
                }
            }
        } else if (isRegionAffected(player)){
            if (player.hasPermission("itomsd.deadlywater.bypass")) {
                return;
            }

            if (player.isInWater()) {
                if (instantDeath) {
                    player.setHealth(0.0);
                } else {
                    player.damage(damagePerTick);
                }
            }
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("dwreload")) {
            if (sender.hasPermission("itomsd.deadlywater.reload")) {
                reloadConfig();
                FileConfiguration config = getConfig();

                world_list = config.getStringList("world_list");
                instantDeath = config.getBoolean("instant_death");
                damagePerTick = config.getDouble("damage_per_tick");

                sender.sendMessage(ChatColor.translateAlternateColorCodes('&',config.getString("plugin_reloaded")));
                return true;
            } else {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&',getConfig().getString("no_permission")));
            }
        }
        return false;
    }

    private boolean isWorldAffected(String worldName) {
        return world_list.contains(worldName);
    }

    private boolean isRegionAffected(Player player) {
        Location loc = BukkitAdapter.adapt(player.getLocation());
        com.sk89q.worldedit.world.World world = BukkitAdapter.adapt(player.getWorld());

        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager regionManager = container.get(world);

        if (regionManager != null) {
            ApplicableRegionSet regions = regionManager.getApplicableRegions(loc.toVector().toBlockPoint());
            for (ProtectedRegion region : regions) {
                if (region.getFlag(DeadlyWaterFlag.DEADLY_WATER) == StateFlag.State.ALLOW) {
                    return true;
                }
            }
        }
        return false;
    }


}

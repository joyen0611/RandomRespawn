package org.example.pluhin.randomrespawn;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.Random;
public final class RandomRespawn extends JavaPlugin implements Listener {
    private final File file = new File(getDataFolder(),"config.yml");
    private final YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
    @Override
    public void onEnable() {
        // Plugin startup logic
        if (!getDataFolder().exists()) getDataFolder().mkdir();
        if (config.get("range") == null) config.set("range", 100);

        try {
            config.save(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("RandomRespawn is enabled.");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        try {
            config.save(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        getLogger().info("RandomRespawn is disabled.");
    }

    @EventHandler
    public void onPlayerSpawnEvent(PlayerSpawnLocationEvent event) {
        Player player = event.getPlayer();
        String uuid = player.getUniqueId().toString();
        int range = config.getInt("range");

        if (!player.hasPlayedBefore()){
            Location location = getRandomLocation(player.getWorld(), range);

            config.set("log."+uuid+".world",location.getWorld().getName());
            config.set("log."+uuid+".x",location.getX());
            config.set("log."+uuid+".y",location.getY());
            config.set("log."+uuid+".z",location.getZ());

            event.setSpawnLocation(location);
        } else if (config.get("log."+uuid) == null) {
            Location location = getRandomLocation(player.getWorld(), range);

            config.set("log."+uuid+".world",location.getWorld().getName());
            config.set("log."+uuid+".x",location.getX());
            config.set("log."+uuid+".y",location.getY());
            config.set("log."+uuid+".z",location.getZ());
        }
    }

    @EventHandler
    public void onPlayerRespawnEvent(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        String uuid = player.getUniqueId().toString();

        if (player.getRespawnLocation() != null) return;
        World world = Bukkit.getWorld(Objects.requireNonNull(config.get("log."+uuid+".world")).toString());
        double x = config.getDouble("log."+uuid+".x");
        double y = config.getDouble("log."+uuid+".y");
        double z = config.getDouble("log."+uuid+".z");

        event.setRespawnLocation(new Location(world,x,y,z));
    }
    private Location getRandomLocation(World world, int range) {
        Random random = new Random();
        int x = random.nextInt(range * 2) - range;
        int z = random.nextInt(range * 2) - range;
        int y = world.getHighestBlockYAt(x, z); // Y 좌표는 해당 위치의 최고 블록 바로 위

        return new Location(world, x + 0.5, y + 1, z + 0.5);
    }
}

package com.homesgui;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class HomeManager {

    public static class Home {
        public int slot;
        public String name;
        public String world;
        public double x, y, z;
        public float yaw, pitch;
        public String icon;

        public Location toLocation() {
            World w = Bukkit.getWorld(world);
            if (w == null) return null;
            return new Location(w, x, y, z, yaw, pitch);
        }
    }

    private static String base(UUID uuid) {
        return "players." + uuid.toString() + ".homes";
    }

    public static List<Home> getHomes(Player player) {
        List<Home> homes = new ArrayList<>();
        FileConfiguration cfg = HomesGUI.getInstance().getHomesConfig();
        ConfigurationSection section = cfg.getConfigurationSection(base(player.getUniqueId()));
        if (section == null) return homes;

        for (String key : section.getKeys(false)) {
            ConfigurationSection h = section.getConfigurationSection(key);
            if (h == null) continue;
            Home home = new Home();
            home.slot = Integer.parseInt(key);
            home.name = h.getString("name", "Home " + key);
            home.world = h.getString("world");
            home.x = h.getDouble("x");
            home.y = h.getDouble("y");
            home.z = h.getDouble("z");
            home.yaw = (float) h.getDouble("yaw");
            home.pitch = (float) h.getDouble("pitch");
            home.icon = h.getString("icon", "OAK_SIGN");
            homes.add(home);
        }
        return homes;
    }

    public static Home getHomeBySlot(Player player, int slot) {
        for (Home h : getHomes(player)) {
            if (h.slot == slot) return h;
        }
        return null;
    }

    public static Home getHomeByName(Player player, String name) {
        for (Home h : getHomes(player)) {
            if (h.name.equalsIgnoreCase(name)) return h;
        }
        return null;
    }

    public static int countHomes(Player player) {
        return getHomes(player).size();
    }

    public static boolean hasFreeSlot(Player player) {
        return countHomes(player) < HomesGUI.MAX_HOMES;
    }

    public static int firstFreeSlot(Player player) {
        List<Home> homes = getHomes(player);
        for (int i = 1; i <= HomesGUI.MAX_HOMES; i++) {
            final int slot = i;
            boolean taken = homes.stream().anyMatch(h -> h.slot == slot);
            if (!taken) return slot;
        }
        return -1;
    }

    public static void setHome(Player player, int slot, String name, Location loc) {
        FileConfiguration cfg = HomesGUI.getInstance().getHomesConfig();
        String path = base(player.getUniqueId()) + "." + slot;
        cfg.set(path + ".name", name);
        cfg.set(path + ".world", loc.getWorld().getName());
        cfg.set(path + ".x", loc.getX());
        cfg.set(path + ".y", loc.getY());
        cfg.set(path + ".z", loc.getZ());
        cfg.set(path + ".yaw", (double) loc.getYaw());
        cfg.set(path + ".pitch", (double) loc.getPitch());
        if (cfg.getString(path + ".icon") == null) {
            cfg.set(path + ".icon", "OAK_SIGN");
        }
        HomesGUI.getInstance().saveHomesConfig();
    }

    public static void renameHome(Player player, int slot, String newName) {
        FileConfiguration cfg = HomesGUI.getInstance().getHomesConfig();
        cfg.set(base(player.getUniqueId()) + "." + slot + ".name", newName);
        HomesGUI.getInstance().saveHomesConfig();
    }

    public static void setIcon(Player player, int slot, String materialName) {
        FileConfiguration cfg = HomesGUI.getInstance().getHomesConfig();
        cfg.set(base(player.getUniqueId()) + "." + slot + ".icon", materialName);
        HomesGUI.getInstance().saveHomesConfig();
    }

    public static void deleteHome(Player player, int slot) {
        FileConfiguration cfg = HomesGUI.getInstance().getHomesConfig();
        cfg.set(base(player.getUniqueId()) + "." + slot, null);
        HomesGUI.getInstance().saveHomesConfig();
    }
}

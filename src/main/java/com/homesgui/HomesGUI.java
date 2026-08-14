package com.homesgui;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public class HomesGUI extends JavaPlugin {

    private static HomesGUI instance;
    private File homesFile;
    private FileConfiguration homesConfig;

    public static final int MAX_HOMES = 5;

    @Override
    public void onEnable() {
        instance = this;

        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }

        loadHomesConfig();

        getCommand("home").setExecutor(new HomeCommand());
        getCommand("homes").setExecutor(new HomeCommand());
        getCommand("sethome").setExecutor(new SetHomeCommand());
        getCommand("delhome").setExecutor(new DelHomeCommand());

        getLogger().info("HomesGUI enabled - " + MAX_HOMES + " homes per player.");
    }

    @Override
    public void onDisable() {
        saveHomesConfig();
    }

    public static HomesGUI getInstance() {
        return instance;
    }

    private void loadHomesConfig() {
        homesFile = new File(getDataFolder(), "homes.yml");

        if (!homesFile.exists()) {
            try {
                homesFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        homesConfig = YamlConfiguration.loadConfiguration(homesFile);
    }

    public FileConfiguration getHomesConfig() {
        return homesConfig;
    }

    public void saveHomesConfig() {
        try {
            homesConfig.save(homesFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

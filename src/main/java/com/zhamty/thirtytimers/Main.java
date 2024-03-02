package com.zhamty.thirtytimers;

import com.zhamty.thirtytimers.commands.AdminCommand;
import com.zhamty.thirtytimers.commands.MainCommand;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public final class Main extends JavaPlugin {
    public static Main instance;
    public FileConfiguration toggles = null;
    ConfigManager confManager;
    Timer timer = new Timer();
    private File togglesFile = null;
    int bstatsPluginId = 21201;
    Metrics metrics;

    @Override
    public void onLoad() {
        confManager = new ConfigManager(this);
        confManager.updateConfig();
    }

    @Override
    public void onEnable() {
        instance = this;
        registerToggles();
        registerCommands();
        metrics = new Metrics(this, bstatsPluginId);
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")){
            new PAPIExpansion(this).register();
        }
        timer.start();
    }

    @Override
    public void reloadConfig() {
        super.reloadConfig();
        if (confManager == null) return;
        confManager.updateConfig();
        timer.start();
    }

    public Timer getTimer() {
        return timer;
    }

    public ConfigManager getConfManager() {
        return confManager;
    }

    public void registerCommands() {
        CommandMap commandMap = null;
        try {
            Field f = Bukkit.getPluginManager().getClass().getDeclaredField("commandMap");
            f.setAccessible(true);
            commandMap = (CommandMap) f.get(Bukkit.getPluginManager());
        } catch (NoSuchFieldException | IllegalAccessException | IllegalArgumentException | SecurityException e) {
            e.printStackTrace();
        }
        assert commandMap != null;

        String main = getConfig().getString("config.main_command", "30timers");
        String admin = getConfig().getString("config.admin_command", "30timersadmin");
        commandMap.register(main, new MainCommand(this, main));
        commandMap.register(admin, new AdminCommand(this, admin));
    }

    public FileConfiguration getToggles() {
        if (toggles == null) {
            reloadToggles();
        }
        return toggles;
    }

    public void reloadToggles() {
        if (toggles == null) {
            togglesFile = new File(getDataFolder() + "/saved_data/", "toggles.yml");
        }
        toggles = YamlConfiguration.loadConfiguration(togglesFile);
        Reader defConfigStream = new InputStreamReader(
                Objects.requireNonNull(this.getResource("toggles.yml")), StandardCharsets.UTF_8
        );
        toggles.setDefaults(YamlConfiguration.loadConfiguration(defConfigStream));
    }

    public void saveToggles() {
        try {
            toggles.save(togglesFile);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void registerToggles() {
        togglesFile = new File(this.getDataFolder() + "/saved_data/", "toggles.yml");
        if (!togglesFile.exists()) {
            this.getToggles().options().copyDefaults(true);
            saveToggles();
        }
    }

    @Override
    public void onDisable() {
        if (timer != null)
            timer.stop();
    }
}

package com.zhamty.thirtytimers;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.zhamty.thirtytimers.commands.AdminCommand;
import com.zhamty.thirtytimers.commands.MainCommand;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
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

import static com.zhamty.thirtytimers.Utils.getVersionIndex;

/**
 * Main plugin class
 */
public final class Main extends JavaPlugin {
    public static Main instance;
    public FileConfiguration toggles = null;
    ConfigManager confManager;
    Timer timer = new Timer();
    private File togglesFile = null;
    int bstatsPluginId = 21201;
    Metrics metrics;
    ProtocolManager protocolManager;
    BukkitAudiences adventure;

    /**
     * On load plugin logic (Used by bukkit)
     */
    @Override
    public void onLoad() {
        instance = this;
        confManager = new ConfigManager(this);
        confManager.updateConfig();
    }

    public BukkitAudiences adventure(){
        if(this.adventure == null) {
            throw new IllegalStateException("Tried to access Adventure when the plugin was disabled!");
        }
        return this.adventure;
    }

    /**
     * On enable plugin logic (Used by bukkit)
     */
    @Override
    public void onEnable() {
        registerToggles();
        registerCommands();
        metrics = new Metrics(this, bstatsPluginId);
        this.adventure = BukkitAudiences.create(this);
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")){
            confManager.hasPlaceholderAPI = true;
            new PAPIExpansion(this).register();
        }
        if (Bukkit.getPluginManager().isPluginEnabled("Oraxen")){
            confManager.hasOraxen = true;
        }
        if (Bukkit.getPluginManager().isPluginEnabled("ItemsAdder")){
            confManager.hasItemsAdder = true;
        }
        if (Bukkit.getPluginManager().isPluginEnabled("ProtocolLib")){
            protocolManager = ProtocolLibrary.getProtocolManager();
        } else if (getVersionIndex(1) < 10) {
            getLogger().severe(
                    "ProcolLib is needed for 30Timers in Minecraft 1.9 or earlier. Disabling 30Timers"
            );
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        timer.start();
        getLogger().info("--------------------");
        getLogger().info("30Timers enabled");
        getLogger().info("PlaceholderAPI: " + confManager.hasPlaceholderAPI);
        getLogger().info("Oraxen: " + confManager.hasOraxen);
        getLogger().info("ItemsAdder: " + confManager.hasItemsAdder);
        getLogger().info("--------------------");

    }


    /**
     * Reload plugin config (used internally)
     */
    @Override
    public void reloadConfig() {
        super.reloadConfig();
        if (confManager == null) return;
        confManager.updateConfig();
        if (Bukkit.getPluginManager().isPluginEnabled("30Timers"))
            timer.start();
    }

    /**
     * Get current active timer
     * @return current active timer
     */
    public Timer getTimer() {
        return timer;
    }

    /**
     * Get current config manager
     * @return current config manager
     */
    public ConfigManager getConfManager() {
        return confManager;
    }

    void registerCommands() {
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
        commandMap.register(main, new MainCommand(this, main, null));
        commandMap.register(admin, new AdminCommand(this, admin, "30timers.admin"));
    }

    /**
     * Get player timer toggles
     * @return Player timer toggles
     */
    public FileConfiguration getToggles() {
        if (toggles == null) {
            reloadToggles();
        }
        return toggles;
    }

    /**
     * Reload toggles from file
     */
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

    /**
     * Save toggles to file
     */
    public void saveToggles() {
        try {
            toggles.save(togglesFile);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Load toggles from file
     */
    public void registerToggles() {
        togglesFile = new File(this.getDataFolder() + "/saved_data/", "toggles.yml");
        if (!togglesFile.exists()) {
            this.getToggles().options().copyDefaults(true);
            saveToggles();
        }
    }

    /**
     * On disable plugin logic (Used by bukkit)
     */
    @Override
    public void onDisable() {
        if (timer != null) {
            timer.stop();
            this.timer = null;
        }
        if(this.adventure != null) {
            this.adventure.close();
            this.adventure = null;
        }
    }
}

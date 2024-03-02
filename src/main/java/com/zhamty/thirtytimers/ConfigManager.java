package com.zhamty.thirtytimers;

import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

public class ConfigManager {
    YamlConfiguration defaultConfig;
    Main plugin;
    public static ConfigManager instance;
    boolean hasPlaceholderAPI;

    public ConfigManager(Main plugin){
        instance = this;
        this.plugin = plugin;
        plugin.saveDefaultConfig();

        InputStream configInputStream = plugin.getResource("config.yml");
        assert configInputStream != null;
        Reader configReader = new InputStreamReader(configInputStream);

        this.defaultConfig = YamlConfiguration.loadConfiguration(configReader);
    }

    public void updateConfig(){
        defaultConfig.getValues(true).forEach((key, value) -> {
            if (!getConfig().contains(key, true)){
                getConfig().set(key, value);
            }
        });
        plugin.saveConfig();
    }

    public WorldListType getWorldListType(){
        String typeStr = getConfig().getString("worlds.list_type");
        WorldListType type;
        try {
            assert typeStr != null;
            type = WorldListType.valueOf(typeStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            type = WorldListType.NONE;
        }
        return type;
    }
    public FileConfiguration getConfig(){
        return plugin.getConfig();
    }

    public String getFormattedString(String path, Player player){
        String string = getConfig().getString(path);
        assert string != null;
        return Utils.getFormattedString(string, player);
    }

    public boolean isWorldInList(World world) {
        return isWorldInList(world.getName());
    }

    public boolean isWorldInList(String worldName) {
        return getConfig().getStringList("worlds.list").contains(worldName);
    }

    public boolean canGetRandomItem(Player player){
        boolean toggled = plugin.getToggles().getBoolean(player.getName(), true);
        boolean gamemodeDisallowed = getConfig().getStringList("disabled_gamemodes").contains(player.getGameMode().toString());
        if (!toggled || gamemodeDisallowed) return false;

        if (getWorldListType() == WorldListType.WHITELIST && isWorldInList(player.getWorld())){
            return true;
        } else if (getWorldListType() == WorldListType.BLACKLIST && !isWorldInList(player.getWorld())){
            return true;
        } else {
            return getWorldListType() == WorldListType.NONE;
        }
    }

    public boolean showActionbar(int remainingTime){
        return getConfig().getBoolean("allow_action_bar") && getConfig().getInt("action_bar_seconds") >= remainingTime;
    }

    public String getActionBarText(int remainingTime, Player player){
        String message = getFormattedString("messages.random_items.next_item_action_bar", player);
        return message.replaceAll("%SECONDS%", String.valueOf(remainingTime));
    }

    public boolean toggleRandomItems(Player player){
        boolean newValue = !plugin.getToggles().getBoolean(player.getName(), true);
        plugin.getToggles().set(player.getName(), newValue);
        if (newValue){

            player.sendMessage(getFormattedString("messages.random_items.enable", player));
        } else {
            player.sendMessage(getFormattedString("messages.random_items.disable", player));
        }
        return newValue;
    }

    public boolean toggleRandomItems(boolean newValue, Player player){
        plugin.getToggles().set(player.getName(), newValue);
        return newValue;
    }

    public enum WorldListType {
        WHITELIST, BLACKLIST, NONE
    }
}



package com.zhamty.thirtytimers;

import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

/**
 * 30Timers config manager
 */
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

    /**
     * Add new entries to old config files
     */
    public void updateConfig(){
        getConfig().setDefaults(new MemoryConfiguration());
        defaultConfig.getValues(true).forEach((key, value) -> {
            if (!getConfig().contains(key)){
                getConfig().set(key, value);
            }
        });
        getConfig().setDefaults(defaultConfig);
        plugin.saveConfig();
    }

    /**
     * Get the world list type set in config.yml
     * @return WHITELIST, BLACKLIST or NONE
     */
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

    /**
     * Get main plugin config
     * @return Plugin config
     */
    public FileConfiguration getConfig(){
        return plugin.getConfig();
    }

    /**
     * Get formatted string from config path (colors and PlaceholderAPI)
     * @param path Setting path
     * @param player Player to parse PlaceholderAPI
     * @return Formatted string
     */
    public String getFormattedString(String path, Player player){
        return getFormattedString(path, player, null);
    }

    /**
     * Get formatted string from config path (colors and PlaceholderAPI)
     * @param path Setting path
     * @param sender CommandSender (parsed as null if it's not a Player) to parse PlaceholderAPI
     * @return Formatted string
     */
    public String getFormattedString(String path, CommandSender sender){
        Player player = null;
        if (sender instanceof Player) {
            player = (Player) sender;
        }
        return getFormattedString(path, player);
    }

    /**
     * Get formatted string from config path (colors, custom placeholders and PlaceholderAPI)
     * @param path Setting path
     * @param player Player to parse PlaceholderAPI
     * @param placeholders Custom placeholders hashmap. For example: %COMMAND%: "30timers"
     * @return Formatted string
     */
    public String getFormattedString(String path, Player player, HashMap<String, String> placeholders){
        String string = getConfig().getString(path);
        assert string != null;
        if (placeholders == null) {
            return Utils.getFormattedString(string, player);
        }
        for (Map.Entry<String, String> placeholder : placeholders.entrySet()){
            string = string.replaceAll("%"+placeholder.getKey()+"%", placeholder.getValue());
        }
        return Utils.getFormattedString(string, player);
    }

    /**
     * Send a formatted string from config path to a CommandSender (colors and PlaceholderAPI)
     * @param sender CommandSender to send formatted string
     * @param path String path
     */
    public void sendFormattedString(CommandSender sender, String path){
        sendFormattedString(sender, path, null);
    }

    /**
     * Send a formatted string from config path to a CommandSender (colors, custom placeholders and PlaceholderAPI)
     * @param sender CommandSender to send formatted string
     * @param path String path
     * @param placeholders Custom placeholders hashmap. For example: %COMMAND%: "30timers"
     */
    public void sendFormattedString(CommandSender sender, String path, HashMap<String, String> placeholders){
        Player player = null;
        if (sender instanceof Player){
            player = (Player) sender;
        }
        String message = getFormattedString(path, player, placeholders);
        Utils.sendFormattedMessage(sender, message);
    }

    /**
     * Checks if a world is in config.yml world list
     * @param world World object
     * @return if world is in list
     */
    public boolean isWorldInList(World world) {
        return isWorldInList(world.getName());
    }

    /**
     * Checks if a world is in config.yml world list
     * @param worldName The world name as string
     * @return if world is in list
     */
    public boolean isWorldInList(String worldName) {
        return getConfig().getStringList("worlds.list").contains(worldName);
    }

    /**
     * Check if a player can get a random item.
     * Players cannot get a random item when they disabled it, their gamemode is disallowed, they are dead or not in a
     * world that allows them to get the random item.
     * @param player player to check
     * @return if the player can get a random item
     */
    public boolean canGetRandomItem(Player player){
        boolean toggled = plugin.getToggles().getBoolean(player.getName(), true);
        boolean gamemodeDisallowed = getConfig().getStringList("disabled_gamemodes")
                .contains(player.getGameMode().toString());
        boolean dead = player.isDead();
        if (!toggled || gamemodeDisallowed || dead) return false;

        if (getWorldListType() == WorldListType.WHITELIST && isWorldInList(player.getWorld())){
            return true;
        } else if (getWorldListType() == WorldListType.BLACKLIST && !isWorldInList(player.getWorld())){
            return true;
        } else {
            return getWorldListType() == WorldListType.NONE;
        }
    }

    /**
     * Checks if the action bar has to be shown.
     * Action Bar is not shown when disabled or when the seconds needed to be shown have no passed yet
     * @param remainingTime remaining timer time
     * @return if action bar has to be shown
     */
    public boolean showActionbar(int remainingTime){
        return getConfig().getBoolean("allow_action_bar")
                && getConfig().getInt("action_bar_seconds") >= remainingTime;
    }

    /**
     * Get the action bar string parsed with colors, PlaceholderAPI and remaining seconds.
     * @param remainingTime remaining timer time
     * @param player player to parse (PlaceholderAPI)
     * @return formatted action bar string
     */
    public String getActionBarText(int remainingTime, Player player){
        String message = getFormattedString("messages.random_items.next_item_action_bar", player);
        return message.replaceAll("%SECONDS%", String.valueOf(remainingTime));
    }

    /**
     * Toggle random items for a player
     * @param player Player to be toggled
     * @return new value
     */
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
    /**
     * Toggle random items for a player to a set value
     * @param newValue Value to be set
     * @param player Player to be toggled
     * @return new value
     */
    public boolean toggleRandomItems(boolean newValue, Player player){
        plugin.getToggles().set(player.getName(), newValue);
        return newValue;
    }

    /**
     * World list types that can be set from config.yml
     */
    public enum WorldListType {
        WHITELIST, BLACKLIST, NONE
    }
}



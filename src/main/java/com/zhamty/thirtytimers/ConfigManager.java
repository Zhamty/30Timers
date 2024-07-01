package com.zhamty.thirtytimers;

import dev.lone.itemsadder.api.CustomStack;
import io.th0rgal.oraxen.api.OraxenItems;
import io.th0rgal.oraxen.items.ItemBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.zhamty.thirtytimers.Utils.*;

/**
 * 30Timers config manager
 */
public class ConfigManager {
    YamlConfiguration defaultConfig;
    Main plugin;
    public static ConfigManager instance;
    boolean hasPlaceholderAPI;
    boolean hasItemsAdder;
    boolean hasOraxen;

    public ConfigManager(Main plugin) {
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
    public void updateConfig() {
        getConfig().setDefaults(new MemoryConfiguration());
        defaultConfig.getValues(true).forEach((key, value) -> {
            if (!getConfig().contains(key)) {
                getConfig().set(key, value);
            }
        });
        getConfig().setDefaults(defaultConfig);
        plugin.saveConfig();
    }

    /**
     * Get the world list type set in config.yml
     *
     * @return WHITELIST, BLACKLIST or NONE
     */
    public ListType getWorldListType() {
        String typeStr = getConfig().getString("worlds.list_type");
        ListType type;
        try {
            assert typeStr != null;
            type = ListType.valueOf(typeStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            type = ListType.NONE;
        }
        return type;
    }

    /**
     * Get main plugin config
     *
     * @return Plugin config
     */
    public FileConfiguration getConfig() {
        return plugin.getConfig();
    }

    /**
     * Get formatted string from config path (colors and PlaceholderAPI)
     *
     * @param path   Setting path
     * @param player Player to parse PlaceholderAPI
     * @return Formatted string
     * @deprecated Use getFormattedComponent instead. Will be removed in future releases
     */
    public String getFormattedString(String path, Player player) {
        return LegacyComponentSerializer.legacySection().serialize(getFormattedComponent(path, player));
    }
    /**
     * Get formatted string from config path (colors and PlaceholderAPI)
     *
     * @param path   Setting path
     * @param sender CommandSender (parsed as null if it's not a Player) to parse PlaceholderAPI
     * @return Formatted string
     * @deprecated Use getFormattedComponent instead. Will be removed in future releases
     */
    public String getFormattedString(String path, CommandSender sender) {
        Player player = null;
        if (sender instanceof Player) {
            player = (Player) sender;
        }
        return LegacyComponentSerializer.legacySection().serialize(getFormattedComponent(path, player));
    }

    /**
     * Get formatted string from config path (colors, custom placeholders and PlaceholderAPI)
     *
     * @param path         Setting path
     * @param player       Player to parse PlaceholderAPI
     * @param placeholders Custom placeholders hashmap. For example: %COMMAND%: "30timers"
     * @return Formatted string
     * @deprecated Use getFormattedComponent instead. Will be removed in future releases
     */
    public String getFormattedString(String path, Player player, HashMap<String, String> placeholders) {
        return LegacyComponentSerializer.legacySection().serialize(getFormattedComponent(path, player, placeholders));
    }

    /**
     * Get formatted string from config path (minimessage, legacy and PlaceholderAPI) as an Adventure component
     *
     * @param path         Setting path
     * @param player       Player to parse PlaceholderAPI
     * @return Formatted component
     */
    public Component getFormattedComponent(String path, Player player) {
        return getFormattedComponent(path, player, null);
    }

    /**
     * Get formatted string from config path (minimessage, legacy, custom placeholders and PlaceholderAPI) as an
     * Adventure component
     *
     * @param path         Setting path
     * @param player       Player to parse PlaceholderAPI
     * @param placeholders Custom placeholders hashmap. For example: %COMMAND%: "30timers"
     * @return Formatted component
     */
    public Component getFormattedComponent(String path, Player player, HashMap<String, String> placeholders) {
        String string = getConfig().getString(path);
        return Utils.getFormattedComponent(string, player, placeholders);
    }

    /**
     * Send a formatted string from config path to a CommandSender (colors and PlaceholderAPI)
     *
     * @param sender CommandSender to send formatted string
     * @param path   String path
     */
    public void sendFormattedString(CommandSender sender, String path) {
        sendFormattedString(sender, path, null);
    }

    /**
     * Send a formatted string from config path to a CommandSender (colors, custom placeholders and PlaceholderAPI)
     *
     * @param sender       CommandSender to send formatted string
     * @param path         String path
     * @param placeholders Custom placeholders hashmap. For example: %COMMAND%: "30timers"
     */
    public void sendFormattedString(CommandSender sender, String path, HashMap<String, String> placeholders) {
        sendFormattedMessage(sender, getConfig().getString(path), placeholders);
    }

    /**
     * Checks if a world is in config.yml world list
     *
     * @param world World object
     * @return if world is in list
     */
    public boolean isWorldInList(World world) {
        return isWorldInList(world.getName());
    }

    /**
     * Checks if a world is in config.yml world list
     *
     * @param worldName The world name as string
     * @return if world is in list
     */
    public boolean isWorldInList(String worldName) {
        return getConfig().getStringList("worlds.list").contains(worldName);
    }

    /**
     * Gets the item list type
     * @return item list type
     */
    public ListType getItemListType() {
        try {
            return ListType.valueOf(getConfig().getString("items_list.list_type"));
        } catch (IllegalArgumentException ex) {
            return ListType.NONE;
        }
    }

    /**
     * Get the list of items that is in the config (whether it's whitelist, blacklist or none)
     * @return list of items that is in the config
     */
    public List<ItemStack> getListItems() {
        List<ItemStack> list = new ArrayList<>();
        for (String matStr : Main.instance.getConfig().getStringList("items_list.list")) {
            Material material = Material.getMaterial(matStr);
            if (material == null) continue;
            try {
                list.add(new ItemStack(material, 1));
            } catch (IllegalArgumentException ex) {
                continue;
            }
        }
        return list;
    }

    /**
     * Get the list of items that can be given to players (including custom items).
     * @return List of items that can be given to players
     */
    public List<ItemStack> getGiveableItems() {
        ListType listType = getItemListType();
        List<ItemStack> list = new ArrayList<>();

        if (listType == ListType.WHITELIST) {
            list = getListItems();
        } else {
            for (Material material : Material.values()) {
                try {
                    list.add(new ItemStack(material, 1));
                } catch (IllegalArgumentException ex) {
                    continue;
                }
            }
            if (listType == ListType.BLACKLIST) {
                list.removeAll(getListItems());
            }
        }

        list.addAll(getCustomItems());
        list.removeIf(itemStack -> !isItem(itemStack.getType())
                || isAir(itemStack.getType())
                || !isEnabledByFeature(itemStack.getType())
        );
        return list;
    }

    /**
     * Get ItemStacks of config.yml's custom items from providers like Oraxen and Itemsadder
     *
     * @return Custom items' ItemStacks
     */
    public List<ItemStack> getCustomItems() {
        List<ItemStack> list = new ArrayList<>();
        if (!hasOraxen && !hasItemsAdder) return list;

        for (String customID : Main.instance.getConfig().getStringList("items_list.custom")) {
            if (hasOraxen) {
                ItemBuilder stack = OraxenItems.getItemById(customID);
                if (stack != null) {
                    list.add(stack.build());
                }
            }
            if (hasItemsAdder) {
                CustomStack stack = CustomStack.getInstance(customID);
                if (stack != null) {
                    list.add(stack.getItemStack());
                }
            }
        }
        return list;
    }

    /**
     * Check if a player can get a random item.
     * Players cannot get a random item when they disabled it, their gamemode is disallowed, they are dead or not in a
     * world that allows them to get the random item.
     *
     * @param player player to check
     * @return if the player can get a random item
     */
    public boolean canGetRandomItem(Player player) {
        boolean toggled = plugin.getToggles().getBoolean(player.getName(), true);
        boolean gamemodeDisallowed = getConfig().getStringList("disabled_gamemodes")
                .contains(player.getGameMode().toString());
        boolean dead = player.isDead();
        if (!toggled || gamemodeDisallowed || dead) return false;

        if (getWorldListType() == ListType.WHITELIST && isWorldInList(player.getWorld())) {
            return true;
        } else if (getWorldListType() == ListType.BLACKLIST && !isWorldInList(player.getWorld())) {
            return true;
        } else {
            return getWorldListType() == ListType.NONE;
        }
    }

    /**
     * Checks if the action bar has to be shown.
     * Action Bar is not shown when disabled or when the seconds needed to be shown have no passed yet
     *
     * @param remainingTime remaining timer time
     * @return if action bar has to be shown
     */
    public boolean showActionbar(int remainingTime) {
        return getConfig().getBoolean("allow_action_bar")
                && getConfig().getInt("action_bar_seconds") >= remainingTime;
    }

    /**
     * Get the action bar string parsed with colors, PlaceholderAPI and remaining seconds.
     *
     * @param remainingTime remaining timer time
     * @param player        player to parse (PlaceholderAPI)
     * @return formatted action bar string
     * @deprecated Use getActionBarComponent instead. Will be removed in future releases
     */
    public String getActionBarText(int remainingTime, Player player) {
        return LegacyComponentSerializer.legacySection().serialize(getActionBarComponent(remainingTime, player));
    }

    /**
     * Get the action bar string parsed with minimessage, legacy, PlaceholderAPI and remaining seconds as
     * an Adventure component
     *
     * @param remainingTime remaining timer time
     * @param player        player to parse (PlaceholderAPI)
     * @return formatted action bar component
     */
    public Component getActionBarComponent(int remainingTime, Player player) {
        String message = getConfig().getString("messages.random_items.next_item_action_bar");
        assert message != null;
        message = message.replace("%SECONDS%", String.valueOf(remainingTime));
        return Utils.getFormattedComponent(message, player);
    }

    /**
     * Toggle random items for a player
     *
     * @param player Player to be toggled
     * @return new value
     */
    public boolean toggleRandomItems(Player player) {
        boolean newValue = !plugin.getToggles().getBoolean(player.getName(), true);
        plugin.getToggles().set(player.getName(), newValue);
        if (newValue) {

            player.sendMessage(getFormattedString("messages.random_items.enable", player));
        } else {
            player.sendMessage(getFormattedString("messages.random_items.disable", player));
        }
        return newValue;
    }

    /**
     * Toggle random items for a player to a set value
     *
     * @param newValue Value to be set
     * @param player   Player to be toggled
     * @return new value
     */
    public boolean toggleRandomItems(boolean newValue, Player player) {
        plugin.getToggles().set(player.getName(), newValue);
        return newValue;
    }

    /**
     * List types that can be set from config.yml
     */
    public enum ListType {
        WHITELIST, BLACKLIST, NONE
    }
}



package com.zhamty.thirtytimers;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.google.gson.JsonObject;
import me.clip.placeholderapi.PlaceholderAPI;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

/**
 * Some utility methods
 */
public class Utils {
    /**
     * Send an action bar to a player.
     * Spigot wasn't able to send action bars without using NMS until MC 1.10.
     * This function is used to send an action bar in any version 1.8+.
     * ProtocolLib is needed for 1.9.4 and earlier.
     * @param player player to send
     * @param text text to be sent
     */
    public static void sendActionBar(Player player, String text){
        if (getVersionIndex(1) < 10) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("text", text);
            WrappedChatComponent component = WrappedChatComponent.fromJson(jsonObject.toString());

            PacketContainer packet = Main.instance.protocolManager.createPacket(PacketType.Play.Server.CHAT);
            packet.getChatTypes().write(0, EnumWrappers.ChatType.GAME_INFO);
            if (packet.getBytes().size() == 1)
                packet.getBytes().write(0, (byte) 2);
            packet.getChatComponents().write(0, component);

            packet.setMeta("abm_filtered_packet", true);
            Main.instance.protocolManager.sendServerPacket(player, packet);
            return;
        }
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(text));
    }

    /**
     * Function to detect in which version is the server running
     * @param index 0 = major, 1 = minor, 2 = patch
     * @return The version number by its index
     */
    public static int getVersionIndex(int index){
        String string = Bukkit.getServer().getVersion().split("\\.")[index];
        string = string.replaceAll("[()]", "");
        return Integer.parseInt(string);
    }

    /**
     * Check if a material is an item.
     * Spigot introduced this in 1.12.2 (org.bukkit.Material#isItem).
     * This function was created to support older versions.
     * @param material Material to check
     * @return If material is an item
     */
    public static boolean isItem(Material material) {
        if (getVersionIndex(1) > 12 || (getVersionIndex(1) == 12 && getVersionIndex(2) == 2)){
            return material.isItem();
        }
        switch (material.name()) {
            //<editor-fold desc="case isItem">
            case "ACACIA_DOOR":
            case "BED_BLOCK":
            case "BEETROOT_BLOCK":
            case "BIRCH_DOOR":
            case "BREWING_STAND":
            case "BURNING_FURNACE":
            case "CAKE_BLOCK":
            case "CARROT":
            case "CAULDRON":
            case "COCOA":
            case "CROPS":
            case "DARK_OAK_DOOR":
            case "DAYLIGHT_DETECTOR_INVERTED":
            case "DIODE_BLOCK_OFF":
            case "DIODE_BLOCK_ON":
            case "DOUBLE_STEP":
            case "DOUBLE_STONE_SLAB2":
            case "ENDER_PORTAL":
            case "END_GATEWAY":
            case "FIRE":
            case "FLOWER_POT":
            case "FROSTED_ICE":
            case "GLOWING_REDSTONE_ORE":
            case "IRON_DOOR_BLOCK":
            case "JUNGLE_DOOR":
            case "LAVA":
            case "MELON_STEM":
            case "NETHER_WARTS":
            case "PISTON_EXTENSION":
            case "PISTON_MOVING_PIECE":
            case "PORTAL":
            case "POTATO":
            case "PUMPKIN_STEM":
            case "PURPUR_DOUBLE_SLAB":
            case "REDSTONE_COMPARATOR_OFF":
            case "REDSTONE_COMPARATOR_ON":
            case "REDSTONE_LAMP_ON":
            case "REDSTONE_TORCH_OFF":
            case "REDSTONE_WIRE":
            case "SIGN_POST":
            case "SKULL":
            case "SPRUCE_DOOR":
            case "STANDING_BANNER":
            case "STATIONARY_LAVA":
            case "STATIONARY_WATER":
            case "SUGAR_CANE_BLOCK":
            case "TRIPWIRE":
            case "WALL_BANNER":
            case "WALL_SIGN":
            case "WATER":
            case "WOODEN_DOOR":
            case "WOOD_DOUBLE_STEP":
            //</editor-fold>
                return false;
            default:
                return true;
        }
    }
    /**
     * Check if a material is air.
     * Spigot introduced this in 1.13 (org.bukkit.Material#isAir).
     * This function was created to support older versions.
     * @param material Material to check
     * @return If material is air
     */
    public static boolean isAir(Material material) {
        if(getVersionIndex(1) < 13) {
            return material == Material.AIR;
        }
        switch (material) {
            //<editor-fold desc="case isAir">
            case AIR:
            case CAVE_AIR:
            case VOID_AIR:
            case LEGACY_AIR:
            //</editor-fold>
                return true;
            default:
                return false;
        }
    }

    /**
     * Check if a material is enabled by current features.
     * Spigot introduced this in 1.20 (org.bukkit.Material#isEnabledByFeature).
     * This function was created to support older versions.
     * @param item Material to check
     * @return If material is enabled by current features
     */
    public static Boolean isEnabledByFeature(Material item){
        if (getVersionIndex(1) < 20){
            return true;
        }
        return item.isEnabledByFeature(Bukkit.getServer().getWorlds().get(0));
    }

    /**
     * Parse a string (Colors and PlaceholderAPI).
     * @param string string to format
     * @param player player to parse PlaceholderAPI
     * @return formatted string
     */
    public static String getFormattedString(String string, Player player){
        String formattedString = ChatColor.translateAlternateColorCodes('&', string);
        if (Main.instance.confManager.hasPlaceholderAPI) {
            formattedString = PlaceholderAPI.setPlaceholders(player, formattedString);
        }
        return formattedString;
    }

    /**
     * Parse a string (Colors, custom placeholders and PlaceholderAPI).
     * @param string string to format
     * @param player player to parse PlaceholderAPI
     * @param placeholders placeholders to parse
     * @return formatted string
     */
    public static String getFormattedString(String string, Player player, HashMap<String, String> placeholders){
        if (placeholders == null) return getFormattedString(string, player);
        for (Map.Entry<String, String> placeholder : placeholders.entrySet()){
            string = string.replaceAll("%"+placeholder.getKey()+"%", placeholder.getValue());
        }
        return getFormattedString(string, player);
    }

    /**
     * Tries to get a good item name.
     * @param item item to get name
     * @return Item name attemp
     */
    public static String getItemName(ItemStack item){
        return item.getType().name().replaceAll("_", " ").toLowerCase();
    }

    /**
     * Check if player inventory is full.
     * Used to know if random items have to be dropped or added to inventory.
     * @param p player to check
     * @return If inventory is full
     */
    public static boolean isInventoryFull(Player p) {
        for (ItemStack itemStack : p.getInventory().getStorageContents()) {
            if (itemStack == null) return false;
        }
        return true;
    }

    /**
     * Sends a formatted message to a CommandSender (colors and PlaceholderAPI).
     * @param to commandSender that will receive the message
     * @param message message to be sent
     */
    public static void sendFormattedMessage(CommandSender to, String message){
        sendFormattedMessage(to, message, null);
    }

    /**
     * Sends a formatted message to a CommandSender (colors, custom placeholders and PlaceholderAPI).
     * @param to CommandSender that will receive the message
     * @param message message to be sent
     * @param placeholders custom placeholders to parse
     */
    public static void sendFormattedMessage(CommandSender to, String message, HashMap<String, String> placeholders){
        Player toPlayer = null;
        if (to instanceof Player)
            toPlayer = (Player) to;
        to.sendMessage(getFormattedString(message, toPlayer, placeholders));
    }
}

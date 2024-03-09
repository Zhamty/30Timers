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
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class Utils {
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
    public static int getVersionIndex(int index){
        String string = Bukkit.getServer().getVersion().split("\\.")[index];
        string = string.replaceAll("[()]", "");
        return Integer.parseInt(string);
    }
    public static boolean isItem(Material material) {
        if (getVersionIndex(1) > 12 || (getVersionIndex(1) == 12 && getVersionIndex(2) == 2)){
            return material.isItem();
        }
        switch (material.name()) {
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
                return false;
            default:
                return true;
            }
        
    }
    public static boolean isAir(Material material) {
        if(getVersionIndex(1) < 13) {
            return material == Material.AIR;
        }
        switch (material) {
            case AIR:
            case CAVE_AIR:
            case VOID_AIR:
            case LEGACY_AIR:
                return true;
            default:
                return false;
        }
    }

    public static Boolean isEnabledByFeature(Material item){
        if (getVersionIndex(1) < 20){
            return true;
        }
        return item.isEnabledByFeature(Bukkit.getServer().getWorlds().get(0));
    }

    public static String getFormattedString(String string, Player player){
        String formattedString = ChatColor.translateAlternateColorCodes('&', string);
        if (Main.instance.confManager.hasPlaceholderAPI) {
            formattedString = PlaceholderAPI.setPlaceholders(player, formattedString);
        }
        return formattedString;
    }


    public static String getItemName(ItemStack item){
        return item.getType().name().replaceAll("_", " ").toLowerCase();
    }
    public static boolean isInventoryFull(Player p) {
        int i = 0;
        for (ItemStack itemStack : p.getInventory().getContents()) {
            i++;
            if (i >= 36)
                break;

            if (itemStack == null)
                return false;
        }
        return true;
    }




}

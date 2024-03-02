package com.zhamty.thirtytimers;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class Utils {

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

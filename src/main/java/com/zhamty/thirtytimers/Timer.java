package com.zhamty.thirtytimers;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitTask;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class Timer {
    int initialTime = 30;
    int remainingTime = 30;
    boolean running;
    BukkitTask timerTask;
    public HashMap<String, ItemStack> playerItems = new HashMap<>();

    public int getRemainingTime(){
        return remainingTime;
    }
    public int getInitialTime(){
        return initialTime;
    }

    public void start(){
        running = true;
        initialTime = ConfigManager.instance.getConfig().getInt("time_between_items", 30);
        remainingTime = initialTime;
        if (timerTask != null && !timerTask.isCancelled()) timerTask.cancel();
        timerTask = Bukkit.getScheduler().runTaskTimerAsynchronously(Main.instance, this::second, 0L, 20L);
    }

    void second(){
        if (!running) return;

        remainingTime -= 1;

        if (remainingTime <= 0) {
            playerItems.clear();
        }
        for (Player player : Bukkit.getServer().getOnlinePlayers()){
            if (!ConfigManager.instance.canGetRandomItem(player)) continue;

            if (ConfigManager.instance.showActionbar(remainingTime)) {
                String message = ConfigManager.instance.getActionBarText(remainingTime, player);
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy(message));
            }

            if (remainingTime > 0) continue;
            ItemStack item = giveRandomItem(player);
            playerItems.put(player.getName(), item);
        }
        if (remainingTime > 0) return;
        remainingTime = initialTime;
    }

    public void pause(){
        running = false;
    }

    public void resume(){
        running = true;
    }

    public void stop(){
        running = false;
        remainingTime = initialTime;
        if (timerTask == null) return;
        timerTask.cancel();
    }

    void dropItemSynchronously(Player player, ItemStack item){
        Bukkit.getScheduler().runTask(Main.instance, () ->
                player.getWorld().dropItem(player.getLocation(), item)
        );
    }

    ItemStack giveRandomItem(Player player){
        ItemStack randomItem = getRandomItem();
        if (!Utils.isInventoryFull(player)) player.getInventory().addItem(randomItem);
        else dropItemSynchronously(player, randomItem);

        String message = ConfigManager.instance.getFormattedString("messages.random_items.on_item_receive_chat", player);
        message = message.replaceAll("%ITEM%", Utils.getItemName(randomItem));
        player.sendMessage(message);
        return randomItem;
    }

    public static ItemStack getRandomItem() {
        SecureRandom sr = new SecureRandom();
        List<Material> items = Arrays.asList(Material.values());
        Material item = items.get(sr.nextInt(items.size()));
        if (!item.isEnabledByFeature(Bukkit.getServer().getWorlds().get(0))
                || !item.isItem()
                || item.isAir()
                || Main.instance.getConfig().getStringList("blacklisted_items").contains(item.name()))
            return getRandomItem();

        ItemStack itemStack = new ItemStack(item, 1);
        if(itemStack.getType().equals(Material.ENCHANTED_BOOK)) {
            ItemMeta meta = itemStack.getItemMeta();
            SecureRandom sr2 = new SecureRandom();

            List<Enchantment> enchantments = Registry.ENCHANTMENT.stream().toList();
            Enchantment e = enchantments.get(sr2.nextInt(enchantments.size()));

            assert meta != null;
            meta.addEnchant(e, 1, true);
            itemStack.setItemMeta(meta);
        }
        return itemStack;
    }

    public ItemStack getLastItem(OfflinePlayer player){
        return playerItems.get(player.getName());
    }

    public boolean isRunning() {
        return running;
    }
}

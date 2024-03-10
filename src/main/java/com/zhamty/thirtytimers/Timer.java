package com.zhamty.thirtytimers;

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

import static com.zhamty.thirtytimers.Utils.*;

/**
 * Random items timer manager
 */
public class Timer {
    int initialTime = 30;
    int remainingTime = 30;
    boolean running;
    BukkitTask timerTask;
    HashMap<String, ItemStack> playerItems = new HashMap<>();

    /**
     * Get remaining time
     * @return remaining time
     */
    public int getRemainingTime(){
        return remainingTime;
    }

    /**
     * Get initial time
     * @return initial time (default = 30)
     */
    public int getInitialTime(){
        return initialTime;
    }

    /**
     * Start the timer
     */
    public void start(){
        initialTime = ConfigManager.instance.getConfig().getInt("time_between_items", 30);
        stop();
        running = true;
        timerTask = Bukkit.getScheduler().runTaskTimerAsynchronously(Main.instance, this::second, 0L, 20L);
    }

    /**
     * Start the timer with option to broadcast
     * @param broadcast should broadcast?
     */
    public void start(boolean broadcast){
        start();
        if (broadcast){
            Main.instance.getServer().broadcastMessage(Main.instance.getConfManager()
                    .getFormattedString("messages.random_items.enable_global", null));
        }
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
                sendActionBar(player, message);
            }

            if (remainingTime > 0) continue;
            ItemStack item = giveRandomItem(player);
            playerItems.put(player.getName(), item);
        }
        if (remainingTime > 0) return;
        remainingTime = initialTime;
    }

    /**
     * Pause the timer
     */
    public void pause(){
        running = false;
    }

    /**
     * Resume the timer
     */
    public void resume(){
        running = true;
    }

    /**
     * Stop the timer
     */
    public void stop(){
        running = false;
        remainingTime = initialTime;
        if (timerTask == null) return;
        timerTask.cancel();
    }

    /**
     * Stop the timer with option to broadcast
     * @param broadcast should broadcast?
     */
    public void stop(boolean broadcast){
        stop();
        if (broadcast){
            Main.instance.getServer().broadcastMessage(Main.instance.getConfManager()
                    .getFormattedString("messages.random_items.disable_global", null));
        }
    }

    /**
     * Toggle the timer (start or stop)
     */
    public void toggleStop(){
        toggleStop(false);
    }
    /**
     * Toggle the timer (start or stop) with option to broadcast
     * @param broadcast should broadcast?
     */
    public void toggleStop(boolean broadcast){
        if(running){
            stop(broadcast);
            return;
        }
        start(broadcast);
    }

    /**
     * Toggle the timer (pause or resume)
     */
    public void togglePause(){
        if(running){
            pause();
            return;
        }
        resume();
    }

    void dropItemSynchronously(Player player, ItemStack item){
        Bukkit.getScheduler().runTask(Main.instance, () ->
                player.getWorld().dropItem(player.getLocation(), item)
        );
    }

    ItemStack giveRandomItem(Player player){
        ItemStack randomItem = getRandomItem();

        if (!Utils.isInventoryFull(player))
            player.getInventory().addItem(randomItem);
        else dropItemSynchronously(player, randomItem);

        String message = ConfigManager.instance.getFormattedString(
                "messages.random_items.on_item_receive_chat", player);
        message = message.replaceAll("%ITEM%", Utils.getItemName(randomItem));
        player.sendMessage(message);

        return randomItem;
    }

    /**
     * Get a random item
     * @return a random item
     */
    public static ItemStack getRandomItem() {
        SecureRandom sr = new SecureRandom();
        List<Material> items = Arrays.asList(Material.values());
        Material item = items.get(sr.nextInt(items.size()));

        if (!isEnabledByFeature(item)
                || !isItem(item)
                || isAir(item)
                || Main.instance.getConfig().getStringList("blacklisted_items").contains(item.name()))
            return getRandomItem();

        ItemStack itemStack = new ItemStack(item, 1);
        if(itemStack.getType().equals(Material.ENCHANTED_BOOK)) {
            ItemMeta meta = itemStack.getItemMeta();
            sr = new SecureRandom();

            Enchantment[] enchantments = (Enchantment[]) Registry.ENCHANTMENT.stream().toArray();
            Enchantment e = enchantments[sr.nextInt(enchantments.length)];

            assert meta != null;
            meta.addEnchant(e, 1, true);
            itemStack.setItemMeta(meta);
        }
        return itemStack;
    }

    /**
     * Get a player last given item
     * @param player player to get last given item
     * @return last given item
     */
    public ItemStack getLastItem(OfflinePlayer player){
        return playerItems.get(player.getName());
    }

    /**
     * Is the timer running
     * @return is the timer running
     */
    public boolean isRunning() {
        return running;
    }
}

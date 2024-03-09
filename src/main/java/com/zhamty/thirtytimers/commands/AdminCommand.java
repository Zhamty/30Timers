package com.zhamty.thirtytimers.commands;

import com.zhamty.thirtytimers.Main;
import com.zhamty.thirtytimers.Timer;
import com.zhamty.thirtytimers.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class AdminCommand extends Command implements CommandExecutor {
    public Main plugin;

    public AdminCommand(Main plugin, String name) {
        super(name);
        this.plugin = plugin;
        this.setPermission("30timers.admin");
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {

        String subcommand;
        try {
            subcommand = args[0];
        } catch (IndexOutOfBoundsException ex) {
            subcommand = "help";
        }

        Player player_sender = null;
        try {
            player_sender = (Player) sender;
        } catch (Exception ignore) {
        }
        switch (subcommand) {
            case "help":
                for (String line : plugin.getConfig().getStringList("messages.commands.help_admin")) {
                    String parsedLine = Utils.getFormattedString(line.replaceAll("%COMMAND%",
                            plugin.getConfManager().getFormattedString("admin_command", player_sender)
                    ), player_sender);
                    sender.sendMessage(parsedLine);
                }
                break;
            case "toggle":
                if (args.length == 1) {
                    if (!sender.hasPermission("30timers.toggle.global")) return false;

                    Timer timer = plugin.getTimer();

                    if (!timer.isRunning()) {
                        timer.start();
                        String message = plugin.getConfManager()
                                .getFormattedString("messages.random_items.enable_global", player_sender);
                        plugin.getServer().broadcastMessage(message);
                    } else {
                        timer.stop();
                        String message = plugin.getConfManager()
                                .getFormattedString("messages.random_items.disable_global", player_sender);
                        plugin.getServer().broadcastMessage(message);
                    }

                    break;
                }
                if (args.length == 3) {
                    if (!sender.hasPermission("30timers.toggle.others")) return false;

                    Player player = Bukkit.getPlayer(args[1]);
                    boolean newToggle = args[2].equalsIgnoreCase("on");
                    assert player != null;
                    plugin.getConfManager().toggleRandomItems(newToggle, player);
                    if (newToggle) {
                        sender.sendMessage(plugin.getConfManager()
                                .getFormattedString("messages.commands.toggle.enable_player", player_sender)
                                .replaceAll("%PLAYER%", player.getDisplayName()));
                    } else {
                        sender.sendMessage(plugin.getConfManager()
                                .getFormattedString("messages.commands.toggle.disable_player", player_sender)
                                .replaceAll("%PLAYER%", player.getDisplayName()));
                    }

                    break;
                }
                if (args.length == 2 && (args[1].equalsIgnoreCase("on")
                        || args[1].equalsIgnoreCase("off"))) {

                    if (!sender.hasPermission("30timers.toggle.global")) return false;
                    Timer timer = plugin.getTimer();
                    boolean oldValue = timer.isRunning();
                    boolean newValue = args[1].equalsIgnoreCase("on");
                    if (oldValue == newValue) {
                        sender.sendMessage(plugin.getConfManager()
                                .getFormattedString("messages.commands.toggle.nothing_changed", player_sender));
                        return true;
                    }
                    if (!timer.isRunning()) {
                        timer.start();
                        plugin.getServer().broadcastMessage(plugin.getConfManager()
                                .getFormattedString("messages.random_items.enable_global", player_sender));
                    } else {
                        timer.stop();
                        plugin.getServer().broadcastMessage(plugin.getConfManager()
                                .getFormattedString("messages.random_items.disable_global", player_sender));
                    }
                    break;
                }
                if (!sender.hasPermission("30timers.toggle.others")) return false;
                Player player = Bukkit.getPlayer(args[1]);
                assert player != null;
                boolean newToggle = plugin.getConfManager().toggleRandomItems(player);
                if (newToggle) {
                    sender.sendMessage(plugin.getConfManager()
                            .getFormattedString("messages.commands.toggle.enable_player", player_sender)
                            .replaceAll("%PLAYER%", player.getDisplayName()));
                } else {
                    sender.sendMessage(plugin.getConfManager()
                            .getFormattedString("messages.commands.toggle.disable_player", player_sender)
                            .replaceAll("%PLAYER%", player.getDisplayName()));
                }
                break;
            case "reload":
                if (!sender.hasPermission("30timers.reload")) return false;
                sender.sendMessage(plugin.getConfManager()
                        .getFormattedString("messages.commands.reload.reloading", player_sender));
                plugin.reloadConfig();
                plugin.reloadToggles();
                sender.sendMessage(plugin.getConfManager()
                        .getFormattedString("messages.commands.reload.reloaded", player_sender));
                break;
            default:
                sender.sendMessage(plugin.getConfManager()
                        .getFormattedString("messages.commands.unknown", player_sender)
                        .replaceAll("%COMMAND%", Objects.requireNonNull(plugin.getConfig().getString("admin_command"))));
                return false;
        }
        return true;
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String label, String[] args) {
        return onCommand(sender, this, label, args);
    }
}

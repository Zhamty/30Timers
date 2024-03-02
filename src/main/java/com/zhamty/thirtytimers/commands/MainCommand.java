package com.zhamty.thirtytimers.commands;

import com.zhamty.thirtytimers.Main;
import com.zhamty.thirtytimers.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class MainCommand extends Command implements CommandExecutor {
    public Main plugin;

    public MainCommand(Main plugin, String name) {
        super(name);
        this.plugin = plugin;
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
        } catch (Exception ignore) { }
        switch (subcommand) {
            case "help":
                for (String line : plugin.getConfig().getStringList("messages.commands.help")) {
                    String parsedLine = line.replaceAll("%COMMAND%", plugin.getConfManager().
                            getFormattedString("main_command", player_sender));
                    sender.sendMessage(parsedLine);
                }
                break;
            case "toggle":
                if (sender.hasPermission("30timers.toggle.own")) {
                    boolean status = !plugin.getToggles().getBoolean(sender.getName(), true);
                    if (status) {
                        sender.sendMessage(plugin.getConfManager()
                                .getFormattedString("messages.random_items.enable", player_sender));
                    } else {
                        sender.sendMessage(plugin.getConfManager()
                                .getFormattedString("messages.random_items.disable", player_sender));
                    }
                    plugin.getToggles().set(sender.getName(), status);
                    plugin.saveToggles();
                }
                break;
            case "time":
                if (plugin.getTimer().getRemainingTime() == 1) {
                    sender.sendMessage(plugin.getConfManager()
                            .getFormattedString("messages.commands.time.singular", player_sender));
                    break;
                }
                sender.sendMessage(plugin.getConfManager()
                        .getFormattedString("messages.commands.time.plural", player_sender)
                        .replaceAll("%TIME%", String.valueOf(plugin.getTimer().getRemainingTime())));
                break;
            case "about":
                sender.sendMessage(Utils.getFormattedString("&f----- &b&l30Timers &f-----\n"
                        + "&bPlugin created by &fZhamty\n"
                        + "&bVersion: &f" + plugin.getDescription().getVersion() + "\n"
                        + "&f----- &b&l30Timers &f-----", player_sender));
                break;
            default:
                sender.sendMessage(plugin.getConfManager()
                        .getFormattedString("messages.commands.unknown", player_sender)
                        .replaceAll("%COMMAND%", Objects.requireNonNull(plugin.getConfig().getString("main_command"))));
                return false;
        }
        return true;
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String label, String[] args) {
        return onCommand(sender, this, label, args);
    }
}

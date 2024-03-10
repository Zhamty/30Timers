package com.zhamty.thirtytimers.commands;

import com.zhamty.thirtytimers.Main;
import com.zhamty.thirtytimers.Utils;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;

/**
 * 30timers common command
 */
public class Command extends org.bukkit.command.Command {
    String helpLinesPath() { return ""; }
    Main plugin;

    public Command(Main plugin, String name, @Nullable String permission) {
        super(name);
        this.plugin = plugin;
        this.setPermission(permission);
    }

    public boolean onCommand(@NotNull CommandSender sender, @NotNull String subcommand, @NotNull String[] args) {
        HashMap<String, String> placeholders = new HashMap<>();
        if (subcommand.equals("help")) {
            List<String> lines = plugin.getConfig().getStringList(helpLinesPath());
            placeholders.put("COMMAND", getName());
            Utils.sendFormattedMessage(sender, String.join("\n", lines), placeholders);
            return true;
        }
        return false;
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String label, String[] args) {
        String subcommand = "help";
        if (args.length > 0){
            subcommand = args[0].toLowerCase();
        }
        return onCommand(sender, subcommand, args);
    }
}

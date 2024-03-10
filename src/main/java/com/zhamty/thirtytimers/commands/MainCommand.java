package com.zhamty.thirtytimers.commands;

import com.zhamty.thirtytimers.Main;
import com.zhamty.thirtytimers.Utils;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

/**
 * 30Timers main command (/30timers)
 */
public class MainCommand extends Command {
    @Override
    String helpLinesPath(){ return "messages.commands.help"; }
    public MainCommand(Main plugin, String name, @Nullable String permission) {
        super(plugin, name, permission);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull String subcommand, @NotNull String[] args) {
        if (super.onCommand(sender, subcommand, args)) return true;
        HashMap<String, String> placeholders = new HashMap<>();
        switch(subcommand) {
            case "toggle":
                if (sender.hasPermission("30timers.toggle.own")) {
                    boolean status = !plugin.getToggles().getBoolean(sender.getName(), true);

                    if (status)
                        plugin.getConfManager().sendFormattedString(sender, "messages.random_items.enable");
                    else
                        plugin.getConfManager().sendFormattedString(sender, "messages.random_items.disable");

                    plugin.getToggles().set(sender.getName(), status);
                    plugin.saveToggles();
                }
                return true;
            case "time":
                if (plugin.getTimer().getRemainingTime() == 1) {
                    plugin.getConfManager().sendFormattedString(sender, "messages.commands.time.singular");
                    return true;
                }
                placeholders.put("TIME", String.valueOf(plugin.getTimer().getRemainingTime()));
                plugin.getConfManager().sendFormattedString(sender,
                        "messages.commands.time.plural", placeholders);
                return true;
            case "about":
                Utils.sendFormattedMessage(sender,
                        "&f----- &b&l30Timers &f-----\n"
                                + "&bPlugin created by &fZhamty\n"
                                + "&bVersion: &f" + plugin.getDescription().getVersion() + "\n"
                                + "&f----- &b&l30Timers &f-----"
                );
                return true;
        }
        placeholders.put("COMMAND", getName());
        plugin.getConfManager().sendFormattedString(sender, "messages.commands.unknown", placeholders);
        return false;
    }
}

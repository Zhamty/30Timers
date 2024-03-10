package com.zhamty.thirtytimers.commands;

import com.zhamty.thirtytimers.Main;
import com.zhamty.thirtytimers.Timer;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

/**
 * 30timers admin command (/30timersadmin)
 */
public class AdminCommand extends Command {
    @Override
    String helpLinesPath(){ return "messages.commands.help_admin"; }
    public AdminCommand(Main plugin, String name, @Nullable String permission) {
        super(plugin, name, permission);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull String subcommand, @NotNull String[] args) {
        if (super.onCommand(sender, subcommand, args)) return true;

        HashMap<String, String> placeholders = new HashMap<>();
        switch (subcommand) {
            case "toggle":
                //<editor-fold desc="/<command> toggle"
                Timer timer = plugin.getTimer();
                if (args.length == 1 && sender.hasPermission("30timers.toggle.global")) {
                    timer.toggleStop(true);
                    return true;
                }
                //</editor-fold>
                //<editor-fold desc="/<command> toggle <player> <on/off>">
                if (args.length == 3 && sender.hasPermission("30timers.toggle.others")) {
                    Player player = Bukkit.getPlayer(args[1]);
                    boolean newToggle = args[2].equalsIgnoreCase("on");
                    assert player != null;
                    plugin.getConfManager().toggleRandomItems(newToggle, player);

                    placeholders.put("PLAYER", player.getDisplayName());
                    String path = "messages.commands.toggle.disable_player";
                    if (newToggle)
                        path = "messages.commands.toggle.enable_player";

                    plugin.getConfManager().sendFormattedString(sender, path, placeholders);
                    return true;
                }
                //</editor-fold>
                if (args.length != 2) return false;
                //<editor-fold desc="/<command> toggle <on/off>">
                if (sender.hasPermission("30timers.toggle.global")
                        && (args[1].equalsIgnoreCase("on")
                        || args[1].equalsIgnoreCase("off"))) {

                    boolean oldValue = timer.isRunning();
                    boolean newValue = args[1].equalsIgnoreCase("on");

                    if (oldValue == newValue) {
                        plugin.getConfManager().sendFormattedString(sender,
                                "messages.commands.toggle.nothing_changed");
                        return true;
                    }
                    timer.toggleStop(true);
                    return true;

                }
                //</editor-fold>
                //<editor-fold desc="/<command> toggle <player>">
                if (!sender.hasPermission("30timers.toggle.others")) return false;
                Player player = Bukkit.getPlayer(args[1]);
                assert player != null;
                boolean newToggle = plugin.getConfManager().toggleRandomItems(player);

                placeholders.put("PLAYER", player.getDisplayName());
                String path = "messages.commands.toggle.disable_player";
                if (newToggle)
                    path = "messages.commands.toggle.enable_player";

                plugin.getConfManager().sendFormattedString(sender, path, placeholders);
                return true;
            //</editor-fold>
            case "reload":
                if (!sender.hasPermission("30timers.reload")) return false;
                plugin.getConfManager().sendFormattedString(sender, "messages.commands.reload.reloading");
                plugin.reloadConfig();
                plugin.reloadToggles();
                plugin.getConfManager().sendFormattedString(sender, "messages.commands.reload.reloaded");
                return true;
        }
        placeholders.put("COMMAND", getName());
        plugin.getConfManager().sendFormattedString(sender, "messages.commands.unknown", placeholders);
        return false;
    }
}

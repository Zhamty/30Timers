package com.zhamty.thirtytimers;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * PlaceholderAPI 30Timers internal Expansion
 */
public class PAPIExpansion extends PlaceholderExpansion {
    private final Main plugin;
    public PAPIExpansion(Main plugin) {
        this.plugin = plugin;
    }
    @Override
    public @NotNull String getIdentifier() {
        return "30timers";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Zhamty";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        if (params.equalsIgnoreCase("time_remaining")) {
            return String.valueOf(plugin.getTimer().getRemainingTime());
        }
        if (params.equalsIgnoreCase("time_initial")) {
            return String.valueOf(plugin.getTimer().getInitialTime());
        }
        if (params.equalsIgnoreCase("item")) {
            if (player == null) return null;
            return Utils.getItemName(plugin.getTimer().getLastItem(player));
        }
        if (params.equalsIgnoreCase("toggled")) {
            if (player == null) return null;
            return String.valueOf(plugin.getToggles().getBoolean(Objects.requireNonNull(player.getName())));
        }
        return null;
    }
}

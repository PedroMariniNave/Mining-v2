package com.zpedroo.voltzmining.hooks;

import com.zpedroo.voltzmining.VoltzMining;
import com.zpedroo.voltzmining.data.PlayerData;
import com.zpedroo.voltzmining.managers.DataManager;
import com.zpedroo.voltzmining.utils.formatter.NumberFormatter;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;

public class PlaceholderAPIHook extends PlaceholderExpansion {

    private VoltzMining voltzMining;

    public PlaceholderAPIHook(VoltzMining voltzMining) {
        this.voltzMining = voltzMining;
        this.register();
    }

    public String getAuthor() {
        return voltzMining.getDescription().getAuthors().toString();
    }

    public String getIdentifier() {
        return "mining";
    }

    public String getVersion() {
        return voltzMining.getDescription().getVersion();
    }

    public String onPlaceholderRequest(Player player, String identifier) {
        PlayerData data = DataManager.getInstance().load(player);
        return switch (identifier.toUpperCase()) {
            case "AVAILABLE" -> NumberFormatter.getInstance().format(data.getAvailableBlocks());
            case "BROKEN" -> NumberFormatter.getInstance().format(data.getBrokenBlocks());
            case "PICKAXE_LEVEL" -> format(data.getPickaxe().getLevel());
            default -> null;
        };
    }

    private String format(Integer value) {
        DecimalFormat formatter = (DecimalFormat) NumberFormat.getInstance(Locale.US);
        DecimalFormatSymbols symbols = formatter.getDecimalFormatSymbols();
        symbols.setGroupingSeparator('.');
        formatter.setDecimalFormatSymbols(symbols);

        return formatter.format(value);
    }
}
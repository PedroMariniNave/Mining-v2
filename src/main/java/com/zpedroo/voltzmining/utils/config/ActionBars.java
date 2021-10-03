package com.zpedroo.voltzmining.utils.config;

import com.zpedroo.voltzmining.utils.FileUtils;
import org.bukkit.ChatColor;

public class ActionBars {

    public static final String GENERAL_BAR = getColored(FileUtils.get().getString(FileUtils.Files.CONFIG, "ActionBars.general-bar"));

    private static String getColored(String str) {
        return ChatColor.translateAlternateColorCodes('&', str);
    }
}
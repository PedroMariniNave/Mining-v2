package com.zpedroo.voltzmining.utils.config;

import com.zpedroo.voltzmining.utils.FileUtils;
import org.bukkit.ChatColor;

public class Titles {

    public static final String[] UPGRADE_PICKAXE = new String[] {
            getColored(FileUtils.get().getString(FileUtils.Files.CONFIG, "Titles.upgrade-pickaxe.title")),
            getColored(FileUtils.get().getString(FileUtils.Files.CONFIG, "Titles.upgrade-pickaxe.subtitle"))
    };

    private static String getColored(String str) {
        return ChatColor.translateAlternateColorCodes('&', str);
    }
}
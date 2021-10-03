package com.zpedroo.voltzmining.utils.config;

import com.zpedroo.voltzmining.utils.FileUtils;
import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.List;

public class Messages {

    public static final String MAX_LEVEL = getColored(FileUtils.get().getString(FileUtils.Files.CONFIG, "Messages.max-level"));

    public static final String INVALID_AMOUNT = getColored(FileUtils.get().getString(FileUtils.Files.CONFIG, "Messages.invalid-amount"));

    public static final String INSUFFICIENT_BLOCKS = getColored(FileUtils.get().getString(FileUtils.Files.CONFIG, "Messages.insufficient-blocks"));

    public static final String NEED_SPACE = getColored(FileUtils.get().getString(FileUtils.Files.CONFIG, "Messages.need-space"));

    public static final List<String> CHOOSE_AMOUNT = getColored(FileUtils.get().getStringList(FileUtils.Files.CONFIG, "Messages.choose-amount"));

    public static final List<String> SUCCESSFUL_PURCHASED = getColored(FileUtils.get().getStringList(FileUtils.Files.CONFIG, "Messages.successful-purchased"));

    public static final String THROW_COOLDOWN = getColored(FileUtils.get().getString(FileUtils.Files.CONFIG, "Messages.throw-cooldown"));

    public static final String SECOND = getColored(FileUtils.get().getString(FileUtils.Files.CONFIG, "Time-Formatter.second"));

    public static final String SECONDS = getColored(FileUtils.get().getString(FileUtils.Files.CONFIG, "Time-Formatter.seconds"));

    public static final String MINUTE = getColored(FileUtils.get().getString(FileUtils.Files.CONFIG, "Time-Formatter.minute"));

    public static final String MINUTES = getColored(FileUtils.get().getString(FileUtils.Files.CONFIG, "Time-Formatter.minutes"));

    public static final String HOUR = getColored(FileUtils.get().getString(FileUtils.Files.CONFIG, "Time-Formatter.hour"));

    public static final String HOURS = getColored(FileUtils.get().getString(FileUtils.Files.CONFIG, "Time-Formatter.hours"));

    public static final String DAY = getColored(FileUtils.get().getString(FileUtils.Files.CONFIG, "Time-Formatter.day"));

    public static final String DAYS = getColored(FileUtils.get().getString(FileUtils.Files.CONFIG, "Time-Formatter.days"));

    public static final String NOW = getColored(FileUtils.get().getString(FileUtils.Files.CONFIG, "Time-Formatter.now"));

    private static String getColored(String str) {
        return ChatColor.translateAlternateColorCodes('&', str);
    }

    private static List<String> getColored(List<String> list) {
        List<String> colored = new ArrayList<>(list.size());
        for (String str : list) {
            colored.add(getColored(str));
        }

        return colored;
    }
}
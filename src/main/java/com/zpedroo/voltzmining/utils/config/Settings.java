package com.zpedroo.voltzmining.utils.config;

import com.zpedroo.voltzmining.utils.FileUtils;
import org.bukkit.ChatColor;

import java.util.List;

public class Settings {

    public static final String COMMAND = FileUtils.get().getString(FileUtils.Files.CONFIG, "Settings.command");

    public static final List<String> ALIASES = FileUtils.get().getStringList(FileUtils.Files.CONFIG, "Settings.aliases");

    public static final String ADMIN_PERMISSION = FileUtils.get().getString(FileUtils.Files.CONFIG, "Settings.admin-permission");

    public static final Long SAVE_INTERVAL = FileUtils.get().getLong(FileUtils.Files.CONFIG, "Settings.save-interval");

    public static final Long REWARDS_INTERVAL = FileUtils.get().getLong(FileUtils.Files.CONFIG, "Settings.rewards-interval");

    public static final Double UPGRADE_CHANCE = FileUtils.get().getDouble(FileUtils.Files.CONFIG, "Settings.upgrade-chance");

    public static final Long THROW_COOLDOWN = FileUtils.get().getLong(FileUtils.Files.CONFIG, "Settings.throw-cooldown");

    public static final String REWARD_SEPARATOR = ChatColor.translateAlternateColorCodes('&', FileUtils.get().getString(FileUtils.Files.CONFIG, "Settings.reward-separator"));
}
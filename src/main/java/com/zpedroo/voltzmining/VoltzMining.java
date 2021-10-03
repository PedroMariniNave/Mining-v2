package com.zpedroo.voltzmining;

import com.zpedroo.voltzmining.commands.MiningCmd;
import com.zpedroo.voltzmining.hooks.PlaceholderAPIHook;
import com.zpedroo.voltzmining.listeners.MiningListeners;
import com.zpedroo.voltzmining.listeners.PlayerChatListener;
import com.zpedroo.voltzmining.listeners.PlayerGeneralListeners;
import com.zpedroo.voltzmining.listeners.RegionGeneralListeners;
import com.zpedroo.voltzmining.managers.DataManager;
import com.zpedroo.voltzmining.managers.InventoryManager;
import com.zpedroo.voltzmining.managers.PickaxeManager;
import com.zpedroo.voltzmining.managers.PlayerManager;
import com.zpedroo.voltzmining.mysql.DBConnection;
import com.zpedroo.voltzmining.tasks.RewardsTask;
import com.zpedroo.voltzmining.tasks.SaveTask;
import com.zpedroo.voltzmining.utils.FileUtils;
import com.zpedroo.voltzmining.utils.cooldown.Cooldown;
import com.zpedroo.voltzmining.utils.formatter.NumberFormatter;
import com.zpedroo.voltzmining.utils.formatter.TimeFormatter;
import com.zpedroo.voltzmining.utils.menus.Menus;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandMap;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.List;
import java.util.logging.Level;

import static com.zpedroo.voltzmining.utils.config.Settings.*;

public class VoltzMining extends JavaPlugin {

    private static VoltzMining instance;
    public static VoltzMining get() { return instance; }

    public void onEnable() {
        instance = this;
        new FileUtils(this);

        if (!isMySQLEnabled(getConfig())) {
            getLogger().log(Level.SEVERE, "MySQL are disabled! You need to enable it.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        new DBConnection(getConfig());
        new NumberFormatter(getConfig());
        new DataManager();
        new InventoryManager();
        new PlayerManager();
        new PickaxeManager();
        new Cooldown();
        new TimeFormatter();
        new Menus();
        new RewardsTask(this);
        new SaveTask(this);

        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new PlaceholderAPIHook(this);
        }

        registerCommand(COMMAND, ALIASES, new MiningCmd());
        registerListeners();
    }

    public void onDisable() {
        if (!isMySQLEnabled(getConfig())) return;

        PlayerManager.getInstance().restoreAllInventories();
        try {
            DataManager.getInstance().saveAll();
            DBConnection.getInstance().closeConnection();
        } catch (Exception ex) {
            getLogger().log(Level.SEVERE, "An error occurred while trying to save data!");
        }
    }

    private void registerCommand(String command, List<String> aliases, CommandExecutor executor) {
        try {
            Constructor<PluginCommand> constructor = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
            constructor.setAccessible(true);

            PluginCommand pluginCmd = constructor.newInstance(command, this);
            pluginCmd.setAliases(aliases);
            pluginCmd.setExecutor(executor);

            Field field = Bukkit.getPluginManager().getClass().getDeclaredField("commandMap");
            field.setAccessible(true);
            CommandMap commandMap = (CommandMap) field.get(Bukkit.getPluginManager());
            commandMap.register(getName().toLowerCase(), pluginCmd);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new MiningListeners(), this);
        getServer().getPluginManager().registerEvents(new PlayerChatListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerGeneralListeners(), this);
        getServer().getPluginManager().registerEvents(new RegionGeneralListeners(), this);
    }

    private Boolean isMySQLEnabled(FileConfiguration file) {
        if (!file.contains("MySQL.enabled")) return false;

        return file.getBoolean("MySQL.enabled");
    }
}
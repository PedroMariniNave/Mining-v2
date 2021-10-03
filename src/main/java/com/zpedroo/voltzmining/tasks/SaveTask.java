package com.zpedroo.voltzmining.tasks;

import com.zpedroo.voltzmining.managers.DataManager;
import com.zpedroo.voltzmining.mysql.DBConnection;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import static com.zpedroo.voltzmining.utils.config.Settings.*;

public class SaveTask extends BukkitRunnable {

    public SaveTask(Plugin plugin) {
        this.runTaskTimerAsynchronously(plugin, SAVE_INTERVAL * 20, SAVE_INTERVAL * 20);
    }

    @Override
    public void run() {
        DataManager.getInstance().saveAll();
        DataManager.getInstance().getCache().setTopBrokenBlocks(DBConnection.getInstance().getDBManager().getTopBrokenBlocks());
    }
}
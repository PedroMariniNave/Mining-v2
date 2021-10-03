package com.zpedroo.voltzmining.managers;

import com.zpedroo.voltzmining.data.PlayerData;
import com.zpedroo.voltzmining.data.cache.DataCache;
import com.zpedroo.voltzmining.mysql.DBConnection;
import com.zpedroo.voltzmining.objects.Upgrader;
import com.zpedroo.voltzmining.utils.FileUtils;
import com.zpedroo.voltzmining.utils.builder.ItemBuilder;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;

public class DataManager {

    private static DataManager instance;
    public static DataManager getInstance() { return instance; }

    private DataCache dataCache;

    public DataManager() {
        instance = this;
        this.dataCache = new DataCache();
        this.loadUpgraders();
    }

    public PlayerData load(Player player) {
        PlayerData data = dataCache.getData(player);
        if (data == null) {
            data = DBConnection.getInstance().getDBManager().load(player);
            dataCache.getPlayerData().put(player, data);
        }

        return data;
    }

    public void save(Player player) {
        PlayerData data = load(player);
        if (data == null) return;
        if (!data.isQueueUpdate()) return;

        DBConnection.getInstance().getDBManager().save(data);
        data.setQueueUpdate(false);
    }

    public void saveAll() {
        new HashSet<>(dataCache.getPlayerData().keySet()).forEach(this::save);
    }

    private void loadUpgraders() {
        FileUtils.Files file = FileUtils.Files.CONFIG;

        for (String upgraderName : FileUtils.get().getSection(file, "Upgraders")) {
            Integer upgrade = FileUtils.get().getInt(file, "Upgraders." + upgraderName + ".upgrade");
            ItemStack item = ItemBuilder.build(FileUtils.get().getFile(file).get(), "Upgraders." + upgraderName, new String[]{
                    "{upgrade}"
            }, new String[]{
                    upgrade.toString()
            }).build();

            dataCache.getUpgraders().put(upgraderName.toUpperCase(), new Upgrader(item, upgrade));
        }
    }

    public DataCache getCache() {
        return dataCache;
    }
}
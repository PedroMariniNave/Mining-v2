package com.zpedroo.voltzmining.listeners;

import com.zpedroo.voltzmining.data.PlayerData;
import com.zpedroo.voltzmining.managers.DataManager;
import com.zpedroo.voltzmining.managers.InventoryManager;
import com.zpedroo.voltzmining.managers.PlayerManager;
import de.netzkronehd.WGRegionEvents.events.RegionEnterEvent;
import de.netzkronehd.WGRegionEvents.events.RegionLeaveEvent;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class RegionGeneralListeners implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEnter(RegionEnterEvent event) {
        if (!DataManager.getInstance().getCache().getMineRegions().contains(event.getRegion().getId())) return;

        Player player = event.getPlayer();
        PlayerData data = DataManager.getInstance().load(player);
        if (data == null) return;

        PlayerManager.getInstance().storeInventory(player);
        player.getInventory().addItem(data.getPickaxe().build());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onLeave(RegionLeaveEvent event) {
        if (!DataManager.getInstance().getCache().getMineRegions().contains(event.getRegion().getId())) return;

        Player player = event.getPlayer();
        PlayerData data = DataManager.getInstance().load(player);
        if (data == null) return;
        if (data.getLaunchPickaxeTask() != null) data.getLaunchPickaxeTask().cancelTask(false);

        ItemStack[] playerItems = player.getInventory().getContents();
        List<ItemStack> toGive = new ArrayList<>(playerItems.length);

        for (ItemStack item : playerItems) {
            if (item == null || item.getType().equals(Material.AIR)) continue;
            if (item.isSimilar(data.getPickaxe().build())) continue;

            toGive.add(item);
        }

        player.getInventory().clear();
        PlayerManager.getInstance().restoreInventory(player);

        if (toGive.isEmpty()) return;

        for (ItemStack item : toGive) {
            int freeSpace = InventoryManager.getInstance().getFreeSpace(player, item);
            if (freeSpace >= item.getAmount()) {
                player.getInventory().addItem(item);
                continue;
            }

            player.getWorld().dropItemNaturally(player.getLocation(), item);
        }
    }
}

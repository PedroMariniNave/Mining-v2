package com.zpedroo.voltzmining.managers;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class PlayerManager {

    private static PlayerManager instance;
    public static PlayerManager getInstance() { return instance; }

    private Map<Player, StoredInventory> storedInventories;

    public PlayerManager() {
        instance = this;
        this.storedInventories = new HashMap<>(256);
    }

    public void updatePickaxe(Player player, ItemStack oldPickaxe, ItemStack newPickaxe) {
        for (int slot = 0; slot < player.getInventory().getSize(); ++slot) {
            ItemStack item = player.getInventory().getItem(slot);
            if (item == null || item.getType().equals(Material.AIR)) continue;
            if (!item.isSimilar(oldPickaxe)) continue;

            player.getInventory().setItem(slot, newPickaxe);
            break;
        }
    }

    public void storeInventory(Player player) {
        storedInventories.put(player, new StoredInventory(player.getInventory().getContents(), player.getInventory().getArmorContents()));
        player.getInventory().clear();
    }

    public void restoreInventory(Player player) {
        StoredInventory storedInventory = storedInventories.remove(player);
        if (storedInventory == null) return;

        player.getInventory().setContents(storedInventory.getContents());
        player.getInventory().setArmorContents(storedInventory.getArmor());
    }

    public void restoreAllInventories() {
        new HashSet<>(storedInventories.keySet()).forEach(this::restoreInventory);
    }

    public Map<Player, StoredInventory> getStoredInventories() {
        return storedInventories;
    }

    private static class StoredInventory {

        private ItemStack[] contents;
        private ItemStack[] armor;

        public StoredInventory(ItemStack[] contents, ItemStack[] armor) {
            this.contents = contents;
            this.armor = armor;
        }

        public ItemStack[] getContents() {
            return contents;
        }

        public ItemStack[] getArmor() {
            return armor;
        }
    }
}
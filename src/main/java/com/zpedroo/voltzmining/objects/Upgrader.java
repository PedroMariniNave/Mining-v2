package com.zpedroo.voltzmining.objects;

import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.inventory.ItemStack;

public class Upgrader {

    private ItemStack item;
    private Integer upgrade;

    public Upgrader(ItemStack item, Integer upgrade) {
        this.item = item;
        this.upgrade = upgrade;
    }

    public ItemStack getItem() {
        NBTItem nbt = new NBTItem(item.clone());
        nbt.setInteger("PickaxeUpgrader", upgrade);

        return nbt.getItem();
    }

    public Integer getUpgrade() {
        return upgrade;
    }
}
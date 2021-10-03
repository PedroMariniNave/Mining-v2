package com.zpedroo.voltzmining.data;

import com.zpedroo.voltzmining.objects.Enchant;
import com.zpedroo.voltzmining.objects.Pickaxe;
import com.zpedroo.voltzmining.tasks.ThrowPickaxeTask;

import java.math.BigInteger;
import java.util.UUID;

public class PlayerData {

    private UUID uuid;
    private BigInteger availableBlocks;
    private BigInteger brokenBlocks;
    private Pickaxe pickaxe;
    private ThrowPickaxeTask throwPickaxeTask;
    private Boolean update;

    public PlayerData(UUID uuid, BigInteger availableBlocks, BigInteger brokenBlocks, Pickaxe pickaxe) {
        this.uuid = uuid;
        this.availableBlocks = availableBlocks;
        this.brokenBlocks = brokenBlocks;
        this.pickaxe = pickaxe;
        this.update = false;
    }

    public UUID getUUID() {
        return uuid;
    }

    public BigInteger getAvailableBlocks() {
        return availableBlocks;
    }

    public BigInteger getBrokenBlocks() {
        return brokenBlocks;
    }

    public Pickaxe getPickaxe() {
        return pickaxe;
    }

    public ThrowPickaxeTask getLaunchPickaxeTask() {
        return throwPickaxeTask;
    }

    public Boolean isQueueUpdate() {
        return update;
    }

    public void addBlocks(BigInteger amount) {
        this.availableBlocks = availableBlocks.add(amount);
        this.brokenBlocks = brokenBlocks.add(amount);
        this.update = true;
    }

    public void removeAvailableBlocks(BigInteger amount) {
        this.availableBlocks = availableBlocks.subtract(amount);
        this.update = true;
    }

    public void setPickaxeEnchant(Enchant enchant, Integer level) {
        this.pickaxe.setEnchantLevel(enchant, level);
        this.update = true;
    }

    public void setLaunchPickaxeTask(ThrowPickaxeTask throwPickaxeTask) {
        this.throwPickaxeTask = throwPickaxeTask;
    }

    public void setQueueUpdate(Boolean update) {
        this.update = update;
    }
}
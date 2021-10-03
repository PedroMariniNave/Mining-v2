package com.zpedroo.voltzmining.objects;

import org.bukkit.Material;

import java.math.BigInteger;
import java.util.*;

public class MineBlock {

    private Material type;
    private Map<Reward, BigInteger> rewards;

    public MineBlock(Material type, Map<Reward, BigInteger> rewards) {
        this.type = type;
        this.rewards = rewards;
    }

    public Material getType() {
        return type;
    }

    public Map<Reward, BigInteger> getRewards() {
        return rewards;
    }
}
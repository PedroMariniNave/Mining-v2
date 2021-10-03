package com.zpedroo.voltzmining.utils.cooldown;

import org.bukkit.entity.Player;

import java.util.*;

public class Cooldown {

    private static Cooldown instance;
    public static Cooldown getInstance() { return instance; }

    private final Map<UUID, Long> cooldowns;

    public Cooldown() {
        instance = this;
        this.cooldowns = new HashMap<>(64);
    }

    public void addCooldown(Player player, Long expire) {
        cooldowns.put(player.getUniqueId(), expire);
    }

    public Boolean isInCooldown(Player player) {
        if (cooldowns.containsKey(player.getUniqueId())) {
            Long expire = cooldowns.get(player.getUniqueId());
            if (System.currentTimeMillis() >= expire) cooldowns.remove(player.getUniqueId());
        }

        return cooldowns.containsKey(player.getUniqueId());
    }

    public Long getCooldown(Player player) {
        if (!cooldowns.containsKey(player.getUniqueId())) return 0L;

        return cooldowns.get(player.getUniqueId());
    }
}
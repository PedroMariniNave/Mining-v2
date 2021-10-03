package com.zpedroo.voltzmining.tasks;

import com.google.common.collect.Table;
import com.zpedroo.voltzmining.managers.DataManager;
import com.zpedroo.voltzmining.objects.Reward;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.math.BigInteger;
import java.util.HashSet;

import static com.zpedroo.voltzmining.utils.config.Settings.*;

public class RewardsTask extends BukkitRunnable {

    private Plugin plugin;

    public RewardsTask(Plugin plugin) {
        this.plugin = plugin;
        this.runTaskTimerAsynchronously(plugin, REWARDS_INTERVAL * 20, REWARDS_INTERVAL * 20);
    }

    @Override
    public void run() {
        Table<Player, Reward, BigInteger> playerRewards = DataManager.getInstance().getCache().getPlayerRewards();

        new HashSet<>(playerRewards.rowMap().keySet()).forEach(player -> {
            for (Reward reward : playerRewards.rowMap().get(player).keySet()) {
                BigInteger amount = playerRewards.row(player).get(reward);

                for (String cmd : reward.getCommands()) {
                    plugin.getServer().getScheduler().runTaskLater(plugin, () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), StringUtils.replaceEach(cmd, new String[]{
                            "{player}",
                            "{amount}"
                    }, new String[]{
                            player.getName(),
                            amount.toString()
                    })), 0L);
                }
            }
        });

        playerRewards.clear();
    }
}
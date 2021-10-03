package com.zpedroo.voltzmining.tasks;

import com.zpedroo.voltzmining.VoltzMining;
import com.zpedroo.voltzmining.data.PlayerData;
import com.zpedroo.voltzmining.managers.DataManager;
import com.zpedroo.voltzmining.objects.MineBlock;
import com.zpedroo.voltzmining.objects.Reward;
import com.zpedroo.voltzmining.utils.config.ActionBars;
import com.zpedroo.voltzmining.utils.formatter.NumberFormatter;
import de.ancash.actionbar.ActionBarAPI;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static com.zpedroo.voltzmining.utils.config.Settings.*;

public class ThrowPickaxeTask extends BukkitRunnable {

    private Player player;
    private EulerAngle eulerAngle;
    private EulerAngle eulerAngleNew;
    private Vector vector;
    private ArmorStand armorStand;
    private Integer distance;
    private Double limit;
    private Block block;
    private MineBlock mineBlock;
    private Map<Reward, BigInteger> rewards;
    private BigInteger blocks;

    public ThrowPickaxeTask(Player player, Location destination, ArmorStand armorStand, Double limit) {
        this.player = player;
        this.vector = destination.subtract(player.getLocation()).toVector();
        this.armorStand = armorStand;
        this.distance = 0;
        this.limit = limit;
        this.blocks = BigInteger.ZERO;
        this.rewards = new HashMap<>(4);
        this.runTaskTimer(VoltzMining.get(), 1L, 1L);
    }

    @Override
    public void run() {
        if (player == null) {
            cancelTask(false);
            return;
        }

        eulerAngle = armorStand.getRightArmPose();
        eulerAngleNew = eulerAngle.add(1D, 0D, 0D);
        armorStand.setRightArmPose(eulerAngleNew);

        block = armorStand.getLocation().getBlock();
        mineBlock = DataManager.getInstance().getCache().getBlock(block.getType().toString());
        // if mineBlock == null, the pickaxe are out the mine.

        if (++distance >= limit || (mineBlock == null && block.getType() != Material.AIR)) {
            cancelTask(true);
        } else {
            armorStand.teleport(armorStand.getLocation().add(vector.normalize()));
        }

        for (int xOff = -1; xOff <= 1; ++xOff) {
            for (int yOff = -1; yOff <= 1; ++yOff) {
                for (int zOff = -1; zOff <= 1; ++zOff) {
                    block = armorStand.getLocation().getBlock().getRelative(xOff, yOff, zOff);
                    if (block.getType() == Material.AIR) continue;

                    mineBlock = DataManager.getInstance().getCache().getBlock(block.getType().toString());
                    if (mineBlock == null) continue;

                    Bukkit.getServer().getPluginManager().callEvent(new BlockBreakEvent(block, player));
                    block.getWorld().playSound(block.getLocation(), Sound.BLOCK_ANVIL_BREAK, 0.2f, 10f);
                    block.breakNaturally();

                    this.blocks = blocks.add(BigInteger.ONE);

                    for (Map.Entry<Reward, BigInteger> entry : mineBlock.getRewards().entrySet()) {
                        Reward reward = entry.getKey();
                        BigInteger amount = entry.getValue();

                        rewards.put(reward, rewards.getOrDefault(reward, BigInteger.ZERO).add(amount));
                    }
                }
            }
        }

        if (blocks.signum() <= 0) return;

        StringBuilder builder = new StringBuilder(rewards.size());

        new HashSet<>(rewards.keySet().stream().toList()).forEach(reward -> {
            BigInteger amount = rewards.get(reward);

            DataManager.getInstance().getCache().addRewardAmount(player, reward, amount);

            if (!builder.isEmpty()) builder.append(REWARD_SEPARATOR);

            builder.append(StringUtils.replaceEach(reward.getDisplay(), new String[]{
                    "{amount}"
            }, new String[]{
                    NumberFormatter.getInstance().format(amount)
            }));
        });

        ActionBarAPI.sendActionBar(player, StringUtils.replaceEach(ActionBars.GENERAL_BAR, new String[]{
                "{blocks}",
                "{rewards}"
        }, new String[]{
                NumberFormatter.getInstance().format(blocks),
                builder.toString()
        }));
    }

    public void cancelTask(Boolean giveItem) {
        this.cancel();
        armorStand.getWorld().playSound(armorStand.getLocation(), Sound.ENTITY_ITEM_BREAK, 0.3f, 10f);
        armorStand.remove();

        if (player == null) return;

        PlayerData data = DataManager.getInstance().load(player);
        if (data == null) return;

        data.setLaunchPickaxeTask(null);

        if (giveItem) player.getInventory().setItemInMainHand(data.getPickaxe().build());
    }
}

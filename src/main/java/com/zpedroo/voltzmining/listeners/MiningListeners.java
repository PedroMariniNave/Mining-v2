package com.zpedroo.voltzmining.listeners;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.zpedroo.voltzmining.data.PlayerData;
import com.zpedroo.voltzmining.managers.DataManager;
import com.zpedroo.voltzmining.managers.PlayerManager;
import com.zpedroo.voltzmining.objects.Enchant;
import com.zpedroo.voltzmining.objects.MineBlock;
import com.zpedroo.voltzmining.objects.Reward;
import com.zpedroo.voltzmining.utils.EnchantType;
import com.zpedroo.voltzmining.utils.config.ActionBars;
import com.zpedroo.voltzmining.utils.config.Titles;
import com.zpedroo.voltzmining.utils.formatter.NumberFormatter;
import de.ancash.actionbar.ActionBarAPI;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.math.BigInteger;
import java.util.*;

import static com.zpedroo.voltzmining.utils.config.Settings.*;

public class MiningListeners implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBreak(BlockBreakEvent event) {
        Location location = BukkitAdapter.adapt(event.getBlock().getLocation());
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        ApplicableRegionSet regions = container.createQuery().getApplicableRegions(location);

        for (ProtectedRegion region : regions) {
            if (!DataManager.getInstance().getCache().getOreRegions().contains(region.getId())) continue;

            Player player = event.getPlayer();
            PlayerData data = DataManager.getInstance().load(player);
            ItemStack item = event.getPlayer().getItemInHand().clone();
            if (!item.getType().toString().endsWith("_PICKAXE") && data.getLaunchPickaxeTask() == null) return;

            MineBlock mineBlock = DataManager.getInstance().getCache().getBlock(event.getBlock().getType().toString());
            if (mineBlock == null) return;

            BigInteger blocks = BigInteger.ONE;
            Map<Reward, BigInteger> rewards = new HashMap<>(mineBlock.getRewards());

            Integer fortune = data.getPickaxe().getEnchantLevel(getEnchant(EnchantType.LOOT_BONUS_BLOCKS.getName()));
            Integer superArea = data.getPickaxe().getEnchantLevel(getEnchant(EnchantType.SUPER_AREA.getName()));
            Integer thunder = data.getPickaxe().getEnchantLevel(getEnchant(EnchantType.THUNDER.getName()));

            if (newChance() <= getEnchant(EnchantType.SUPER_AREA.getName()).getIncreasePerLevel() * superArea) {
                for (int xOff = -1; xOff <= 1; ++xOff) {
                    for (int yOff = -1; yOff <= 1; ++yOff) {
                        for (int zOff = -1; zOff <= 1; ++zOff) {
                            Block blockFound = event.getBlock().getRelative(xOff, yOff, zOff);
                            MineBlock mineBlockFound = DataManager.getInstance().getCache().getBlock(blockFound.getType().toString());
                            if (mineBlockFound == null) continue;

                            blocks = blocks.add(BigInteger.ONE);

                            for (Map.Entry<Reward, BigInteger> entry : mineBlockFound.getRewards().entrySet()) {
                                Reward reward = entry.getKey();
                                BigInteger amount = entry.getValue();

                                rewards.put(reward, rewards.getOrDefault(reward, BigInteger.ZERO).add(amount));
                            }

                            blockFound.breakNaturally();
                        }
                    }
                }
            } else if (newChance() <= getEnchant(EnchantType.THUNDER.getName()).getIncreasePerLevel() * thunder) {
                Bukkit.getWorld(event.getBlock().getWorld().getName()).spigot().strikeLightningEffect(event.getBlock().getLocation(), true);
                for (int yOff = -50; yOff <= 50; ++yOff) {
                    Block blockFound = event.getBlock().getRelative(0, yOff, 0);
                    MineBlock mineBlockFound = DataManager.getInstance().getCache().getBlock(blockFound.getType().toString());
                    if (mineBlockFound == null) continue;

                    blocks = blocks.add(BigInteger.ONE);

                    for (Map.Entry<Reward, BigInteger> entry : mineBlockFound.getRewards().entrySet()) {
                        Reward reward = entry.getKey();
                        BigInteger amount = entry.getValue();

                        rewards.put(reward, rewards.getOrDefault(reward, BigInteger.ZERO).add(amount));
                    }

                    blockFound.breakNaturally();
                }
            }

            if (fortune > 0) {
                for (Map.Entry<Reward, BigInteger> entry : rewards.entrySet()) {
                    Reward reward = entry.getKey();
                    BigInteger amount = entry.getValue();

                    rewards.put(reward, amount.add(amount.multiply(BigInteger.valueOf(fortune))));
                }
            }

            if (newChance() <= UPGRADE_CHANCE) {
                Random random = new Random();
                Enchant toUpgrade = null;
                Map<String, Enchant> enchants = DataManager.getInstance().getCache().getEnchants();
                int number = random.nextInt(enchants.size());
                int i = -1;
                for (Enchant enchant : enchants.values()) {
                    if (++i < number) continue;
                    if (data.getPickaxe().getEnchantLevel(enchant) >= enchant.getMaxLevel()) continue;

                    toUpgrade = enchant;
                    break;
                }

                if (toUpgrade == null) return;

                final ItemStack oldPickaxe = data.getPickaxe().build();

                data.setPickaxeEnchant(toUpgrade, data.getPickaxe().getEnchantLevel(toUpgrade) + 1);
                if (data.getLaunchPickaxeTask() == null) {
                    ItemStack newPickaxe = data.getPickaxe().build();

                    PlayerManager.getInstance().updatePickaxe(player, oldPickaxe, newPickaxe);
                }

                player.sendTitle(Titles.UPGRADE_PICKAXE[0], Titles.UPGRADE_PICKAXE[1]);
            }

            data.addBlocks(blocks);

            if (data.getLaunchPickaxeTask() != null) return;

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
    }

    private Double newChance() {
        return new Random().nextDouble() * 100D;
    }

    private Enchant getEnchant(String enchant) {
        return DataManager.getInstance().getCache().getEnchant(enchant);
    }
}
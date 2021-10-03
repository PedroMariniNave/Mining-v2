package com.zpedroo.voltzmining.data.cache;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.zpedroo.voltzmining.data.PlayerData;
import com.zpedroo.voltzmining.mysql.DBConnection;
import com.zpedroo.voltzmining.objects.*;
import com.zpedroo.voltzmining.utils.FileUtils;
import com.zpedroo.voltzmining.utils.builder.ItemBuilder;
import com.zpedroo.voltzmining.utils.formatter.NumberFormatter;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.math.BigInteger;
import java.util.*;

public class DataCache {

    private Table<Player, Reward, BigInteger> playerRewards;
    private Map<Player, PlayerData> playerData;
    private Map<String, Upgrader> upgraders;
    private Map<String, Enchant> enchants;
    private Map<String, Reward> rewards;
    private Map<String, MineBlock> blocks;
    private Set<String> mineRegions;
    private Set<String> oreRegions;
    private List<ShopItem> shopItems;
    private List<PlayerData> topBrokenBlocks;

    public DataCache() {
        this.playerData = new HashMap<>(256);
        this.upgraders = new HashMap<>(8);
        this.playerRewards = HashBasedTable.create();
        this.enchants = loadEnchants();
        this.rewards = loadRewards();
        this.blocks = loadBlocks();
        this.mineRegions = loadMineRegions();
        this.oreRegions = loadOreRegions();
        this.shopItems = loadShopItems();
    }

    public Table<Player, Reward, BigInteger> getPlayerRewards() {
        return playerRewards;
    }

    public BigInteger getRewardAmount(Player player, Reward reward) {
        return playerRewards.row(player).getOrDefault(reward, BigInteger.ZERO);
    }

    public void addRewardAmount(Player player, Reward reward, BigInteger amount) {
        playerRewards.row(player).put(reward, getRewardAmount(player, reward).add(amount));
    }

    public Map<Player, PlayerData> getPlayerData() {
        return playerData;
    }

    public Map<String, Upgrader> getUpgraders() {
        return upgraders;
    }

    public Map<String, Enchant> getEnchants() {
        return enchants;
    }

    public Map<String, Reward> getRewards() {
        return rewards;
    }

    public Map<String, MineBlock> getBlocks() {
        return blocks;
    }

    public Set<String> getMineRegions() {
        return mineRegions;
    }

    public Set<String> getOreRegions() {
        return oreRegions;
    }

    public List<ShopItem> getShopItems() {
        return shopItems;
    }

    public List<PlayerData> getTopBrokenBlocks() {
        if (topBrokenBlocks == null) {
            this.topBrokenBlocks = DBConnection.getInstance().getDBManager().getTopBrokenBlocks();
        }
        
        return topBrokenBlocks;
    }

    public PlayerData getData(Player player) {
        if (!playerData.containsKey(player)) return null;

        return playerData.get(player);
    }

    public MineBlock getBlock(String block) {
        if (!blocks.containsKey(block.toUpperCase())) return null;

        return blocks.get(block.toUpperCase());
    }

    public Enchant getEnchant(String enchant) {
        if (!enchants.containsKey(enchant.toUpperCase())) return null;

        return enchants.get(enchant.toUpperCase());
    }

    private Map<String, Enchant> loadEnchants() {
        FileUtils.Files file = FileUtils.Files.CONFIG;

        Set<String> enchants = FileUtils.get().getSection(file, "Enchants");
        Map<String, Enchant> ret = new HashMap<>(enchants.size());

        for (String enchant : enchants) {
            Double increasePerLevel = FileUtils.get().getDouble(file, "Enchants." + enchant + ".increase-per-level");
            Integer maxLevel = FileUtils.get().getInt(file, "Enchants." + enchant + ".max-level");
            Integer initialLevel = FileUtils.get().getInt(file, "Enchants." + enchant + ".initial-level");

            ret.put(enchant.toUpperCase(), new Enchant(enchant, increasePerLevel, maxLevel, initialLevel));
        }

        return ret;
    }

    private Map<String, Reward> loadRewards() {
        FileUtils.Files file = FileUtils.Files.CONFIG;

        Set<String> rewards = FileUtils.get().getSection(file, "Rewards");
        Map<String, Reward> ret = new HashMap<>(rewards.size());

        for (String rewardName : rewards) {
            String display = ChatColor.translateAlternateColorCodes('&', FileUtils.get().getString(file, "Rewards." + rewardName + ".display"));
            List<String> commands = FileUtils.get().getStringList(file, "Rewards." + rewardName + ".commands");

            ret.put(rewardName.toUpperCase(), new Reward(rewardName, display, commands));
        }

        return ret;
    }

    private Map<String, MineBlock> loadBlocks() {
        FileUtils.Files file = FileUtils.Files.CONFIG;

        Set<String> blocks = FileUtils.get().getSection(file, "Blocks");
        Map<String, MineBlock> ret = new HashMap<>(blocks.size());

        for (String block : blocks) {
            Material material = Material.getMaterial(block.toUpperCase());

            List<String> blockRewardsList = FileUtils.get().getStringList(file, "Blocks." + block + ".rewards");
            Map<Reward, BigInteger> blockRewards = new HashMap<>(blockRewardsList.size());

            for (String blockReward : blockRewardsList) {
                String[] split = blockReward.split(",");
                Reward reward = rewards.get(split[0].toUpperCase());
                if (reward == null) continue;

                BigInteger amount = NumberFormatter.getInstance().filter(split[1]);
                if (amount.signum() < 0) continue;

                blockRewards.put(reward, amount);
            }

            ret.put(block.toUpperCase(), new MineBlock(material, blockRewards));
        }

        return ret;
    }

    private Set<String> loadMineRegions() {
        FileUtils.Files file = FileUtils.Files.CONFIG;

        return new HashSet<>(FileUtils.get().getStringList(file, "Regions.mine"));
    }

    private Set<String> loadOreRegions() {
        FileUtils.Files file = FileUtils.Files.CONFIG;

        return new HashSet<>(FileUtils.get().getStringList(file, "Regions.ores"));
    }

    private List<ShopItem> loadShopItems() {
        FileUtils.Files file = FileUtils.Files.SHOP;

        Set<String> shopItems = FileUtils.get().getSection(file, "Inventory.items");
        List<ShopItem> ret = new ArrayList<>(shopItems.size());

        for (String str : shopItems) {
            if (str == null) continue;

            BigInteger price = new BigInteger(FileUtils.get().getString(file, "Inventory.items." + str + ".price", "0"));
            Integer defaultAmount = FileUtils.get().getInt(file, "Inventory.items." + str + ".default-amount", 1);
            ItemStack display = ItemBuilder.build(FileUtils.get().getFile(file).get(), "Inventory.items." + str + ".display", new String[]{
                    "{price}"
            }, new String[]{
                    NumberFormatter.getInstance().format(price)
            }).build();
            ItemStack shopItem = null;
            if (FileUtils.get().getFile(file).get().contains("Inventory.items." + str + ".shop-item")) {
                shopItem = ItemBuilder.build(FileUtils.get().getFile(file).get(), "Inventory.items." + str + ".shop-item").build();
            }
            List<String> commands = FileUtils.get().getStringList(file, "Inventory.items." + str + ".commands");

            ret.add(new ShopItem(price, defaultAmount, display, shopItem, commands));
        }

        return ret;
    }

    public void setTopBrokenBlocks(List<PlayerData> topBrokenBlocks) {
        this.topBrokenBlocks = topBrokenBlocks;
    }
}
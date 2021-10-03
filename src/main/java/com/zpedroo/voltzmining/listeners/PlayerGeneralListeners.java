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
import com.zpedroo.voltzmining.objects.Pickaxe;
import com.zpedroo.voltzmining.tasks.ThrowPickaxeTask;
import com.zpedroo.voltzmining.utils.EnchantType;
import com.zpedroo.voltzmining.utils.config.Messages;
import com.zpedroo.voltzmining.utils.config.Settings;
import com.zpedroo.voltzmining.utils.cooldown.Cooldown;
import com.zpedroo.voltzmining.utils.formatter.TimeFormatter;
import de.tr7zw.nbtapi.NBTItem;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import java.util.Random;
import java.util.concurrent.TimeUnit;

public class PlayerGeneralListeners implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onQuit(PlayerQuitEvent event) {
        DataManager.getInstance().save(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPickaxeInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getItem() == null || !event.getItem().getType().toString().endsWith("_PICKAXE")) return;

        Player player = event.getPlayer();
        if (Cooldown.getInstance().isInCooldown(player)) {
            player.sendMessage(StringUtils.replaceEach(Messages.THROW_COOLDOWN, new String[]{
                    "{cooldown}"
            }, new String[]{
                    TimeFormatter.getInstance().format(Cooldown.getInstance().getCooldown(player) - System.currentTimeMillis())
            }));
            return;
        }

        PlayerData data = DataManager.getInstance().load(player);
        Enchant throwEnchant = DataManager.getInstance().getCache().getEnchant(EnchantType.THROW.getName());
        Integer enchantLevel = data.getPickaxe().getEnchantLevel(throwEnchant);
        if (enchantLevel <= 0) return;

        Location location = BukkitAdapter.adapt(event.getPlayer().getLocation());
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        ApplicableRegionSet regions = container.createQuery().getApplicableRegions(location);

        for (ProtectedRegion region : regions) {
            if (!DataManager.getInstance().getCache().getMineRegions().contains(region.getId())) continue;

            event.setCancelled(true);

            ItemStack item = event.getItem();
            Double limit = enchantLevel * throwEnchant.getIncreasePerLevel();
            org.bukkit.Location destination = player.getLocation().clone().add(player.getLocation().getDirection().multiply(10));
            ArmorStand armorStand = player.getWorld().spawn(getBlockBehindPlayer(player).clone().add(0D, 1.25D, 0D), ArmorStand.class);
            armorStand.setVisible(false);
            armorStand.setGravity(false);
            armorStand.setArms(true);
            armorStand.setInvulnerable(true);
            armorStand.addEquipmentLock(EquipmentSlot.HAND, ArmorStand.LockType.REMOVING_OR_CHANGING);
            armorStand.setItemInHand(item);
            armorStand.setRightArmPose(new EulerAngle(0, 0, new Random().nextInt(5) + 60));

            item.setAmount(0);
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 0.3f, 0.3f);

            Cooldown.getInstance().addCooldown(player, System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(Settings.THROW_COOLDOWN));
            data.setLaunchPickaxeTask(new ThrowPickaxeTask(player, destination, armorStand, limit));
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Player player = event.getPlayer();
        ItemStack item = event.getHand() == EquipmentSlot.HAND ? player.getInventory().getItemInMainHand() : player.getInventory().getItemInOffHand();
        if (item.getType().equals(Material.AIR)) return;

        NBTItem nbt = new NBTItem(item);
        if (!nbt.hasKey("PickaxeUpgrader")) return;

        event.setCancelled(true);

        Integer toUpgrade = nbt.getInteger("PickaxeUpgrader");

        PlayerData data = DataManager.getInstance().load(player);
        Pickaxe pickaxe = data.getPickaxe();

        boolean upgraded = false;

        final ItemStack oldPickaxe = pickaxe.build();

        for (Enchant enchant : pickaxe.getEnchants().keySet()) {
            if (pickaxe.getEnchantLevel(enchant) >= enchant.getMaxLevel()) continue;

            int newLevel = pickaxe.getEnchantLevel(enchant) + toUpgrade;
            if (newLevel > enchant.getMaxLevel()) newLevel = enchant.getMaxLevel();

            pickaxe.setEnchantLevel(enchant, newLevel);
            upgraded = true;
        }

        if (!upgraded) {
            player.sendMessage(Messages.MAX_LEVEL);
            return;
        }

        item.setAmount(item.getAmount() - 1);

        ItemStack newPickaxe = pickaxe.build();
        PlayerManager.getInstance().updatePickaxe(player, oldPickaxe, newPickaxe);

        player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 10f, 0.5f);
    }

    public org.bukkit.Location getBlockBehindPlayer(Player player) {
        Vector inverseDirectionVec = player.getLocation().getDirection().normalize().multiply(-1);

        return player.getLocation().add(inverseDirectionVec);
    }
}
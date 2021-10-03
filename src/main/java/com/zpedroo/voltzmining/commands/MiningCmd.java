package com.zpedroo.voltzmining.commands;

import com.zpedroo.voltzmining.managers.DataManager;
import com.zpedroo.voltzmining.managers.InventoryManager;
import com.zpedroo.voltzmining.objects.Upgrader;
import com.zpedroo.voltzmining.utils.config.Settings;
import com.zpedroo.voltzmining.utils.formatter.NumberFormatter;
import com.zpedroo.voltzmining.utils.menus.Menus;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.math.BigInteger;

public class MiningCmd implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = sender instanceof Player ? (Player) sender : null;

        if (args.length >= 4) {
            switch (args[0].toUpperCase()) {
                case "UPGRADER":
                    if (!sender.hasPermission(Settings.ADMIN_PERMISSION)) break;

                    Player target = Bukkit.getPlayer(args[1]);
                    if (target == null) break;

                    Upgrader upgrader = DataManager.getInstance().getCache().getUpgraders().get(args[2].toUpperCase());
                    if (upgrader == null) break;

                    BigInteger amount = NumberFormatter.getInstance().filter(args[3]);

                    if (amount.signum() <= 0) amount = BigInteger.ONE;
                    if (amount.compareTo(BigInteger.valueOf(2304)) > 0) amount = BigInteger.valueOf(2304);

                    ItemStack item = upgrader.getItem();
                    item.setAmount(amount.intValue());

                    if (InventoryManager.getInstance().getFreeSpace(target, item) >= amount.intValue()) {
                        target.getInventory().addItem(item);
                        return true;
                    }

                    target.getWorld().dropItemNaturally(target.getLocation(), item);
                    return true;
            }
        }

        if (args.length > 0) {
            switch (args[0].toUpperCase()) {
                case "TOP":
                    if (player == null) break;

                    Menus.getInstance().openTopMenu(player);
                    return true;
                case "SHOP":
                    if (player == null) break;

                    Menus.getInstance().openShopMenu(player);
                    return true;
            }
        }

        if (player == null) return true;

        Menus.getInstance().openMainMenu(player);
        return false;
    }
}
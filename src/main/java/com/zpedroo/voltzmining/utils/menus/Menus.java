package com.zpedroo.voltzmining.utils.menus;

import com.zpedroo.voltzmining.data.PlayerData;
import com.zpedroo.voltzmining.listeners.MiningListeners;
import com.zpedroo.voltzmining.listeners.PlayerChatListener;
import com.zpedroo.voltzmining.listeners.PlayerGeneralListeners;
import com.zpedroo.voltzmining.listeners.RegionGeneralListeners;
import com.zpedroo.voltzmining.managers.DataManager;
import com.zpedroo.voltzmining.managers.PlayerManager;
import com.zpedroo.voltzmining.objects.Enchant;
import com.zpedroo.voltzmining.objects.ShopItem;
import com.zpedroo.voltzmining.utils.FileUtils;
import com.zpedroo.voltzmining.utils.builder.InventoryBuilder;
import com.zpedroo.voltzmining.utils.builder.InventoryUtils;
import com.zpedroo.voltzmining.utils.builder.ItemBuilder;
import com.zpedroo.voltzmining.utils.config.Messages;
import com.zpedroo.voltzmining.utils.formatter.NumberFormatter;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Menus {

    private static Menus instance;
    public static Menus getInstance() { return instance; }

    private InventoryUtils inventoryUtils;

    private ItemStack nextPageItem;
    private ItemStack previousPageItem;

    public Menus() {
        instance = this;
        this.inventoryUtils = new InventoryUtils();
        this.nextPageItem = ItemBuilder.build(FileUtils.get().getFile(FileUtils.Files.CONFIG).get(), "Next-Page").build();
        this.previousPageItem = ItemBuilder.build(FileUtils.get().getFile(FileUtils.Files.CONFIG).get(), "Previous-Page").build();
    }

    public void openMainMenu(Player player) {
        FileUtils.Files file = FileUtils.Files.MAIN;

        String title = ChatColor.translateAlternateColorCodes('&', FileUtils.get().getString(file, "Inventory.title"));
        int size = FileUtils.get().getInt(file, "Inventory.size");

        Inventory inventory = Bukkit.createInventory(null, size, title);

        List<String> placeholders = new ArrayList<>(12);
        List<String> replaces = new ArrayList<>(12);

        PlayerData data = DataManager.getInstance().load(player);

        for (Map.Entry<Enchant, Integer> entry : data.getPickaxe().getEnchants().entrySet()) {
            placeholders.add("{" + entry.getKey().getName().toLowerCase() + "}");
            replaces.add(entry.getValue().toString());
        }

        placeholders.add("{player}");
        placeholders.add("{broken_blocks}");
        placeholders.add("{available_blocks}");

        replaces.add(player.getName());
        replaces.add(NumberFormatter.getInstance().format(data.getBrokenBlocks()));
        replaces.add(NumberFormatter.getInstance().format(data.getAvailableBlocks()));

        for (String str : FileUtils.get().getSection(file, "Inventory.items")) {
            ItemStack item = null;
            String action = null;
            if (FileUtils.get().getFile(file).get().contains("Inventory.items." + str + ".join") && FileUtils.get().getFile(file).get().contains("Inventory.items." + str + ".left")) {
                boolean mining = PlayerManager.getInstance().getStoredInventories().containsKey(player);

                String typeToGet = mining ? "left" : "join";

                item = ItemBuilder.build(FileUtils.get().getFile(file).get(), "Inventory.items." + str + "." + typeToGet, placeholders.toArray(new String[placeholders.size()]), replaces.toArray(new String[replaces.size()])).build();
                action = FileUtils.get().getString(file, "Inventory.items." + str + "." + typeToGet + ".action");
            } else {
                item = ItemBuilder.build(FileUtils.get().getFile(file).get(), "Inventory.items." + str, placeholders.toArray(new String[placeholders.size()]), replaces.toArray(new String[replaces.size()])).build();
                action = FileUtils.get().getString(file, "Inventory.items." + str + ".action");
            }
            int slot = FileUtils.get().getInt(file, "Inventory.items." + str + ".slot");

            if (StringUtils.contains(action, ":")) {
                String[] split = action.split(":");
                String command = split.length > 1 ? split[1] : null;
                if (command == null) continue;

                switch (split[0].toUpperCase()) {
                    case "PLAYER" -> inventoryUtils.addAction(inventory, slot, () -> {
                        player.chat("/" + command);
                    }, InventoryUtils.ActionType.ALL_CLICKS);

                    case "CONSOLE" -> inventoryUtils.addAction(inventory, slot, () -> {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), StringUtils.replaceEach(command, new String[]{
                                "{player}"
                        }, new String[]{
                                player.getName()
                        }));
                    }, InventoryUtils.ActionType.ALL_CLICKS);
                }
            }

            switch (action.toUpperCase()) {
                case "SHOP":
                    inventoryUtils.addAction(inventory, slot, () -> {
                        openShopMenu(player);
                    }, InventoryUtils.ActionType.ALL_CLICKS);
                    break;
                case "TOP":
                    inventoryUtils.addAction(inventory, slot, () -> {
                        openTopMenu(player);
                    }, InventoryUtils.ActionType.ALL_CLICKS);
                    break;
            }

            inventory.setItem(slot, item);
        }

        player.openInventory(inventory);
    }

    public void openShopMenu(Player player) {
        FileUtils.Files file = FileUtils.Files.SHOP;

        String title = ChatColor.translateAlternateColorCodes('&', FileUtils.get().getString(file, "Inventory.title"));
        int size = FileUtils.get().getInt(file, "Inventory.size");

        Inventory inventory = Bukkit.createInventory(null, size, title);

        List<ShopItem> shopItems = DataManager.getInstance().getCache().getShopItems();

        int i = -1;
        String[] slots = FileUtils.get().getString(file, "Inventory.item-slots").replace(" ", "").split(",");
        List<ItemBuilder> builders = new ArrayList<>(shopItems.size());
        for (ShopItem item : shopItems) {
            if (item == null) continue;
            if (++i >= slots.length) i = 0;

            ItemStack display = item.getDisplay().clone();
            int slot = Integer.parseInt(slots[i]);
            List<InventoryUtils.Action> actions = new ArrayList<>(1);

            actions.add(new InventoryUtils.Action(InventoryUtils.ActionType.ALL_CLICKS, slot, () -> {
                player.closeInventory();
                PlayerChatListener.getPlayerChat().put(player, new PlayerChatListener.PlayerChat(player, item));
                for (String msg : Messages.CHOOSE_AMOUNT) {
                    if (msg == null) continue;

                    player.sendMessage(StringUtils.replaceEach(msg, new String[]{
                            "{item}",
                            "{price}"
                    }, new String[]{
                            item.getDisplay().hasItemMeta() ? item.getDisplay().getItemMeta().hasDisplayName() ? item.getDisplay().getItemMeta().getDisplayName() : item.getDisplay().getType().toString() : item.getDisplay().getType().toString(),
                            NumberFormatter.getInstance().format(item.getPrice())
                    }));
                }
            }));

            builders.add(ItemBuilder.build(display, slot, actions));
        }

        int nextPageSlot = FileUtils.get().getInt(file, "Inventory.next-page-slot");
        int previousPageSlot = FileUtils.get().getInt(file, "Inventory.previous-page-slot");

        InventoryBuilder.build(player, inventory, title, builders, nextPageSlot, previousPageSlot, nextPageItem, previousPageItem);
    }

    public void openTopMenu(Player player) {
        FileUtils.Files file = FileUtils.Files.TOP;

        String title = ChatColor.translateAlternateColorCodes('&', FileUtils.get().getString(file, "Inventory.title"));
        int size = FileUtils.get().getInt(file, "Inventory.size");

        Inventory inventory = Bukkit.createInventory(null, size, title);

        int pos = 0;
        String[] slots = FileUtils.get().getString(file, "Inventory.slots").replace(" ", "").split(",");

        for (PlayerData data : DataManager.getInstance().getCache().getTopBrokenBlocks()) {
            ItemStack item = ItemBuilder.build(FileUtils.get().getFile(FileUtils.Files.TOP).get(), "Item", new String[]{
                    "{player}",
                    "{pos}",
                    "{broken_blocks}",
                    "{available_blocks}"
            }, new String[]{
                    Bukkit.getOfflinePlayer(data.getUUID()).getName(),
                    String.valueOf(++pos),
                    NumberFormatter.getInstance().format(data.getBrokenBlocks()),
                    NumberFormatter.getInstance().format(data.getAvailableBlocks())
            }).build();

            int slot = Integer.parseInt(slots[pos - 1]);

            inventoryUtils.addAction(inventory, slot, null, InventoryUtils.ActionType.ALL_CLICKS); // cancel click

            inventory.setItem(slot, item);
        }

        player.openInventory(inventory);
    }
}
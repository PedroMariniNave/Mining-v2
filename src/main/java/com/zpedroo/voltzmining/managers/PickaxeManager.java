package com.zpedroo.voltzmining.managers;

import com.zpedroo.voltzmining.objects.Enchant;
import com.zpedroo.voltzmining.objects.Pickaxe;
import com.zpedroo.voltzmining.utils.FileUtils;
import com.zpedroo.voltzmining.utils.builder.ItemBuilder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Map;

public class PickaxeManager {

    private static PickaxeManager instance;
    public static PickaxeManager getInstance() { return instance; }

    public PickaxeManager() {
        instance = this;
    }

    public Pickaxe getDefaultPickaxe() {
        ItemStack item = ItemBuilder.build(FileUtils.get().getFile(FileUtils.Files.CONFIG).get(), "Pickaxe").build();
        ItemMeta meta = item.getItemMeta();
        Map<Enchant, Integer> enchants = new HashMap<>(DataManager.getInstance().getCache().getEnchants().size());
        for (Enchant enchant : DataManager.getInstance().getCache().getEnchants().values()) {
            Integer level = enchant.getInitialLevel();
            enchants.put(enchant, level);
        }

        return new Pickaxe(item.getType(), meta != null ? meta.hasDisplayName() ? item.getItemMeta().getDisplayName() : item.getType().toString() : item.getType().toString(), enchants);
    }
}
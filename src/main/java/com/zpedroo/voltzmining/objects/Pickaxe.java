package com.zpedroo.voltzmining.objects;

import com.zpedroo.voltzmining.utils.FileUtils;
import com.zpedroo.voltzmining.utils.builder.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Pickaxe {

    private Material type;
    private String name;
    private Map<Enchant, Integer> enchants;

    public Pickaxe(Material type, String name, Map<Enchant, Integer> enchants) {
        this.type = type;
        this.name = name;
        this.enchants = enchants;
    }

    public Material getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public Map<Enchant, Integer> getEnchants() {
        return enchants;
    }

    public Integer getEnchantLevel(Enchant enchant) {
        if (!enchants.containsKey(enchant)) return 0;

        return enchants.get(enchant);
    }

    public Integer getLevel() {
        Integer level = 0;
        for (Enchant enchant : enchants.keySet()) {
            if (enchant.getMaxLevel() <= 0) continue;

            level += getEnchantLevel(enchant);
        }

        return level;
    }

    public void setEnchantLevel(Enchant enchant, Integer level) {
        this.enchants.put(enchant, level);
    }

    public ItemStack build() {
        List<String> placeholders = new ArrayList<>(enchants.size());
        List<String> replaces = new ArrayList<>(enchants.size());

        for (Map.Entry<Enchant, Integer> entry : enchants.entrySet()) {
            placeholders.add("{" + entry.getKey().getName().toLowerCase() + "}");
            replaces.add(entry.getValue().toString());
        }

        ItemStack item = ItemBuilder.build(FileUtils.get().getFile(FileUtils.Files.CONFIG).get(), "Pickaxe", placeholders.toArray(new String[placeholders.size()]), replaces.toArray(new String[replaces.size()])).build();
        ItemMeta meta = item.getItemMeta();

        if (meta != null) meta.setDisplayName(name);
        if (type != null) item.setType(type);

        return item;
    }
}
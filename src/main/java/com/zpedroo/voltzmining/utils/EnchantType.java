package com.zpedroo.voltzmining.utils;

import com.zpedroo.voltzmining.managers.DataManager;
import com.zpedroo.voltzmining.objects.Enchant;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;

public abstract class EnchantType extends Enchantment {

    public EnchantType(NamespacedKey key) {
        super(key);
    }

    public static final Enchant SUPER_AREA = DataManager.getInstance().getCache().getEnchant("super_area");

    public static final Enchant THUNDER = DataManager.getInstance().getCache().getEnchant("thunder");

    public static final Enchant THROW = DataManager.getInstance().getCache().getEnchant("throw");
}
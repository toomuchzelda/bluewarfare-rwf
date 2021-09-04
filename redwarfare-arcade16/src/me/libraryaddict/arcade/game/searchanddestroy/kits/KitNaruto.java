package me.libraryaddict.arcade.game.searchanddestroy.kits;

import me.libraryaddict.arcade.game.searchanddestroy.abilities.NarutoAbility;
import me.libraryaddict.core.inventory.utils.ItemBuilder;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class KitNaruto extends SnDKit {
    public KitNaruto() {
        super("Naruto", new String[]
                {
                        "naruto"
                }, new NarutoAbility());

        setItems(new ItemStack(Material.IRON_SWORD), new ItemStack(Material.SUGAR_CANE));
    }

    @Override
    public Material[] getArmorMats() {
        return null;
    }

    @Override
    public ItemStack[] getArmor() {
        return new ItemStack[]
                {
                        new ItemBuilder(Material.LEATHER_BOOTS).setColor(Color.BLUE).build(),
                        new ItemBuilder(Material.LEATHER_LEGGINGS).setColor(Color.ORANGE).build(),
                        new ItemBuilder(Material.LEATHER_CHESTPLATE).setColor(Color.ORANGE).build(),
                        new ItemBuilder(Material.GOLDEN_HELMET).build()
                };
    }

    @Override
    public Material getMaterial() {
        return Material.BIRCH_LEAVES;
    }


}

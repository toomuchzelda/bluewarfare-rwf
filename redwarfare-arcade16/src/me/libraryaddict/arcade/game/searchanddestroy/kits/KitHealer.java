package me.libraryaddict.arcade.game.searchanddestroy.kits;

import me.libraryaddict.arcade.game.searchanddestroy.abilities.HealerAbility;
import me.libraryaddict.arcade.kits.KitAvailibility;
import me.libraryaddict.core.inventory.utils.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;


public class KitHealer extends SnDKit {

    public static final ItemStack JUMP = new ItemBuilder(Material.COOKED_CHICKEN).setTitle("Jump forward").build();

    public KitHealer() {
        super("Jumper", KitAvailibility.Free, new String[]
                {
                        "this kit has frost walker and can fly"
                }, new HealerAbility());

        setItems(new ItemBuilder(Material.WOODEN_SWORD).addEnchantment(Enchantment.DAMAGE_ALL, 3).build(),
                new ItemStack(JUMP), new ItemStack(Material.SNOWBALL, 64));

    }

    @Override
    public ItemStack[] getArmor() {
        return new ItemStack[]
                {
                        new ItemBuilder(Material.LEATHER_BOOTS).addEnchantment(Enchantment.FROST_WALKER, 1).addEnchantment(Enchantment.PROTECTION_FALL, 900).build(),
                        new ItemStack(Material.LEATHER_LEGGINGS),
                        new ItemStack(Material.GOLDEN_CHESTPLATE),
                        new ItemStack(Material.GOLDEN_HELMET)
                };
    }

    @Override
    public Material[] getArmorMats() {
        return null;
        //return new Material[]
        //{
        //		Material.LEATHER_LEGGINGS, Material.GOLD_CHESTPLATE,
        //		Material.GOLD_HELMET
        //	};
    }

    @Override
    public Material getMaterial() {
        return Material.BEACON;
    }
}

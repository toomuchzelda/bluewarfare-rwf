package me.libraryaddict.arcade.game.searchanddestroy.kits;

import me.libraryaddict.arcade.game.searchanddestroy.abilities.ButcherAbility;
import me.libraryaddict.core.inventory.utils.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

/**
 * This class manages how Butcher will show up in the Kit HUD and what items it will recieve
 * This file should be in your kits folder
 *
 * @author birdy117
 */
public class Butcher extends SnDKit {


    public Butcher() {
        super("Butcher", new String[]
                {
                        "A self-sustainable combat kit that is able to heal through cannibalistic means. Chop up your enemies into delectable chops to heal yourself",
                        "With relativley weaker defenses, this kit will receive raw porkchops upon a kill and spawns with only 2. These porkchops will heal you for 4 health.",
                        "by birdy117"
                }, new ButcherAbility());

        setPrice(150);

        setItems(new ItemBuilder(Material.IRON_AXE).addEnchantment(Enchantment.DAMAGE_ALL, 1).build(),
                new ItemStack(Material.PORKCHOP, 2));
    }

    @Override
    public Material[] getArmorMats() {
        return new Material[]
                {
                        Material.IRON_BOOTS, Material.CHAINMAIL_LEGGINGS, Material.CHAINMAIL_CHESTPLATE, Material.IRON_HELMET
                };
    }

    @Override
    public ItemStack getIcon() {
        return new ItemBuilder(Material.IRON_AXE).addEnchantment(Enchantment.DAMAGE_ALL, 2).build();
    }

    @Override
    public Material getMaterial() {
        return null;
    }

}
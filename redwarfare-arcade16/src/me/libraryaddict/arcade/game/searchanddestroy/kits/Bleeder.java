package me.libraryaddict.arcade.game.searchanddestroy.kits;

import me.libraryaddict.arcade.game.searchanddestroy.abilities.BleederAbility2;
import me.libraryaddict.core.inventory.utils.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 * This class manages how Bleeder will show up in the Kit HUD and what items it will recieve.
 * This file should be in your kits folder
 *
 * @author birdy117
 */
public class Bleeder extends SnDKit {


    public Bleeder() {
        super("Bleeder", new String[]
                {
                        "An offensive kit with less defenses and offensive power, but with the ability to temporarily cut an enemies max health in half!",
                        "Enemies that are bleeding will only be able to recover up to 50% of their health.",
                        "Inspired by Lexplosion, by birdy117"
                }, new BleederAbility2());

        setPrice(400);

        setItems(new ItemStack(Material.IRON_SWORD));
    }

    @Override
    public Material[] getArmorMats() {
        return new Material[]
                {
                        Material.CHAINMAIL_BOOTS, Material.CHAINMAIL_LEGGINGS, Material.GOLDEN_CHESTPLATE,
                        Material.CHAINMAIL_HELMET
                };
    }

    @Override
    public Material getMaterial() {
        return Material.BEETROOT_SOUP;
    }

}

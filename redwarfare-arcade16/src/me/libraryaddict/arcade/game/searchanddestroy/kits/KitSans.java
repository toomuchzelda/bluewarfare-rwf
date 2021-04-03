package me.libraryaddict.arcade.game.searchanddestroy.kits;

import me.libraryaddict.arcade.game.searchanddestroy.abilities.SansAbility;
import me.libraryaddict.core.C;
import me.libraryaddict.core.inventory.utils.ItemBuilder;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

public class KitSans extends SnDKit {

    private ItemStack _throwBone = new ItemBuilder(Material.BONE_BLOCK).setTitle(C.Bold + "throwable bone").build();

    public KitSans() {
        super("sans", new String[]
                {
                        "heya. you've been busy, huh? ... so, i've got a question for ya. do you think even the worst person can change...? that everyone can be a good person, if they just try? heh heh heh heh... well, here's a better question. do you wanna have a bad time? cause if you take another step forward... you are REALLY not going to like what happens next. welp. sorry, old lady. this is why i never make promises."
                }, new SansAbility());

        setPrice(1);

        setItems(new ItemBuilder(Material.BONE).setTitle("bone").addEnchantment(Enchantment.DIG_SPEED, 3).build(), new ItemStack(_throwBone));
    }

    @Override
    public ItemStack[] getArmor() {
        return new ItemStack[]
                {
                        new ItemBuilder(Material.LEATHER_BOOTS).setColor(Color.PURPLE).build(),
                        new ItemBuilder(Material.LEATHER_LEGGINGS).setColor(Color.BLACK).build(),
                        new ItemBuilder(Material.LEATHER_CHESTPLATE).setColor(Color.BLUE).build(),
                        new ItemBuilder(Material.SKELETON_SKULL).build()
                };
    }

    @Override
    public Material[] getArmorMats() {
        return null;
    }

    @Override
    public Material getMaterial() {
        return Material.BONE;
    }

}

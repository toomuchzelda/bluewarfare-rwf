package me.libraryaddict.arcade.game.searchanddestroy.kits;

import me.libraryaddict.arcade.game.searchanddestroy.abilities.GhostAbility;
import me.libraryaddict.arcade.game.searchanddestroy.abilities.WraithAbility;
import me.libraryaddict.core.inventory.utils.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class KitWraith extends SnDKit {

    public KitWraith(JavaPlugin plugin) {
        super("Wraith", new String[]
                {
                        "A kit that is extremely similar to what Ghost is, however this kit functions differently!", "",
                        "Rather than a sword, it has a Power 3 bow, and is easily visible when shooting."
                }, new GhostAbility(plugin), new WraithAbility());

        setPrice(200);

        setItems(new ItemBuilder(Material.BOW).addEnchantment(Enchantment.ARROW_INFINITE, 1)
                .addEnchantment(Enchantment.ARROW_DAMAGE, 1).build(), new ItemStack(Material.ARROW), new ItemBuilder(Material.WOODEN_AXE).setTitle("Sword?").build());
    }

    public boolean canTeleportTo() {
        return false;
    }

    @Override
    public Material[] getArmorMats() {
        return new Material[4];
    }

    @Override
    public Material getMaterial() {
        return Material.TIPPED_ARROW;
    }

}

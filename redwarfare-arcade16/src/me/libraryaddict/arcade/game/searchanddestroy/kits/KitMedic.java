package me.libraryaddict.arcade.game.searchanddestroy.kits;

import me.libraryaddict.arcade.game.searchanddestroy.abilities.MedicAbility;
import me.libraryaddict.core.C;
import me.libraryaddict.core.inventory.utils.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class KitMedic extends SnDKit {
    public KitMedic() {
        super("Medic", new String[]
                {
                        "This supportive kit can heal allies with its wand, but it has a limit of 25 hearts per player.",
                        "After that, they can only heal minorly by giving regeneration.",
                        "To heal more efficiently, medics can see the overall health of every player over their heads.", "",
                        C.DRed + C.Bold + "This kit loses its healing ability in small games with less than 10 players"
                }, new MedicAbility());

        setItems(new ItemStack(Material.IRON_SWORD), new ItemBuilder(Material.BLAZE_ROD).setTitle(C.Gold).build(),
                new ItemStack(Material.GOLDEN_APPLE, 20));
    }

    @Override
    public Material[] getArmorMats() {
        return new Material[]
                {
                        Material.GOLDEN_BOOTS, Material.GOLDEN_LEGGINGS, Material.GOLDEN_CHESTPLATE, Material.GOLDEN_HELMET
                };
    }

    @Override
    public Material getMaterial() {
        return Material.GOLDEN_APPLE;
    }

    @Override
    public boolean isBalancedTeams() {
        return true;
    }

}

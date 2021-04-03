package me.libraryaddict.arcade.game.survivalgames;

import me.libraryaddict.core.inventory.BasicInventory;
import me.libraryaddict.core.inventory.utils.ItemLayout;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;

public class ShopInventory extends BasicInventory {
    public ShopInventory(Player player, SurvivalGamesItems sgItems) {
        super(player, "Shop", 54);

        ItemLayout layout = new ItemLayout("OOXXXXXOO", "OXXXOXXXO", "XXOOOOOXX", "OXXXOXXXO", "OOXXOXXOO");

        ArrayList<ItemStack> items = new ArrayList<ItemStack>();
        // items.add(Pair.of(new ItemStack(Material.EXP_BOTTLE), 0));
    }

}

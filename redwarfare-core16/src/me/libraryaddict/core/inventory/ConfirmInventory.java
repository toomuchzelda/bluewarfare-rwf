package me.libraryaddict.core.inventory;

import me.libraryaddict.core.C;
import me.libraryaddict.core.inventory.utils.IButton;
import me.libraryaddict.core.inventory.utils.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public class ConfirmInventory extends BasicInventory {
    public ConfirmInventory(Player player, ItemStack displayItem, Runnable onPurchase, Runnable onCancel) {
        super(player, "Confirm Purchase");

        addItem(4, displayItem);

        addButton(20, new ItemBuilder(Material.GREEN_WOOL, 1).setTitle(C.Green + C.Bold + "CONFIRM").build(), new IButton() {
            @Override
            public boolean onClick(ClickType clickType) {
                onPurchase.run();
                return true;
            }
        });

        addButton(24, new ItemBuilder(Material.RED_WOOL, 1).setTitle(C.DRed + C.Bold + "CANCEL").build(), new IButton() {
            @Override
            public boolean onClick(ClickType clickType) {
                onCancel.run();
                return true;
            }
        });
    }

}

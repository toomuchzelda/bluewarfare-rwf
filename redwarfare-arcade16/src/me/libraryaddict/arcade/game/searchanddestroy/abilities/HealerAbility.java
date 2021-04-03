package me.libraryaddict.arcade.game.searchanddestroy.abilities;

import me.libraryaddict.arcade.game.searchanddestroy.kits.KitHealer;
import me.libraryaddict.arcade.kits.Ability;
import me.libraryaddict.core.inventory.utils.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public class HealerAbility extends Ability {

    private ItemStack _jump = new ItemBuilder(Material.COOKED_CHICKEN).setTitle("Jump forward").build();

    @Override
    public void registerAbility() {
        for (Player player : getPlayers(true)) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1));
        }
    }

    @EventHandler
    private void jump(PlayerInteractEvent event) {
        ItemStack item = event.getItem();
        Player player = event.getPlayer();

        if (item == null || !item.isSimilar(KitHealer.JUMP))
            return;

        Vector velocity = player.getVelocity().add(player.getLocation().getDirection());

        player.setVelocity(velocity);
    }


}

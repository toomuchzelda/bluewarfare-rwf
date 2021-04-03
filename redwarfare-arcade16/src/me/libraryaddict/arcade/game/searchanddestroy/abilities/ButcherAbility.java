package me.libraryaddict.arcade.game.searchanddestroy.abilities;

import me.libraryaddict.arcade.kits.Ability;
import me.libraryaddict.core.C;
import me.libraryaddict.core.damage.CustomDamageEvent;
import me.libraryaddict.core.inventory.utils.ItemBuilder;
import me.libraryaddict.core.utils.UtilEnt;
import me.libraryaddict.core.utils.UtilInv;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;

/**
 * This class manages the Butcher's Abilities
 * This file should be in your abilities folder
 *
 * @author birdy117
 */
public class ButcherAbility extends Ability {

    /*
     * Easy to access Butcher stats for balancing.
     */

    /*
     * The amount of health a raw porkchop heals
     */
    private final static double PORKCHOP_HEAL = 4.0;

    /*
     * Gives the Butcher 2.0 hearts when right clicking a raw porkchop
     */
    @EventHandler
    public void eatHumanFlesh(PlayerInteractEvent event) {
        //If the game isn't ongoing, then do nothing
        if (!isLive())
            return;

        //If the player doesn't right click the raw prokchop, then do nothing
        if (!UtilInv.isItem(event.getItem(), Material.PORKCHOP))
            return;

        //if the player doesn't have the ability, do nothing
        if (!hasAbility(event.getPlayer()))
            return;

        //If the player isn't alive, do nothing
        if (!isAlive(event.getPlayer())) {
            return;
        }

        //If the player is denied to use the item in hand, do nothing
        if (event.useItemInHand() == Result.DENY)
            return;

        //If you have 17-20 health, do nothing    
        if (event.getPlayer().getHealth() > 16.0) {
            return;
        }

        //Heals the player by 2.0 hearts
        UtilEnt.heal(event.getPlayer(), PORKCHOP_HEAL);
        UtilInv.remove(event.getPlayer(), Material.PORKCHOP, 1);

        //Plays the muching sound
        event.getPlayer().getWorld().playSound(event.getPlayer().getLocation(), Sound.ENTITY_GENERIC_EAT, 1.4F, 0);

        //Sends a message to the player
        event.getPlayer().sendMessage(C.Red + "The spectators look at you with disgust.");

        event.setCancelled(true);
    }

    /*
     * Gives the Butcher a porkchop on kill
     */
    @EventHandler
    public void onKill(CustomDamageEvent event) {
        if (!isLive())
            return;

        if (!event.isPlayerDamager() || !hasAbility(event.getPlayerDamager()))
            return;

        Player player = event.getPlayerDamager();

        if (event.getPlayerDamagee().getHealth() == 0) {
            UtilInv.addItem(player, new ItemBuilder(Material.PORKCHOP).setTitle(event.getPlayerDamagee().getName() + "'s Remains").build());
        }


    }
}

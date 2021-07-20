package me.libraryaddict.arcade.game.searchanddestroy.abilities;

import me.libraryaddict.arcade.kits.Ability;
import me.libraryaddict.core.C;
import me.libraryaddict.core.damage.AttackType;
import me.libraryaddict.core.damage.CustomDamageEvent;
import me.libraryaddict.core.time.TimeEvent;
import me.libraryaddict.core.time.TimeType;
import me.libraryaddict.core.utils.UtilInv;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.MiscDisguise;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.AbstractArrow.PickupStatus;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;

import java.text.DecimalFormat;

public class SansAbility extends Ability {

    @EventHandler
    public void onTick(TimeEvent event) {
        if (event.getType() != TimeType.TICK)
            return;

        if (!isLive())
            return;

        for (Player player : getPlayers(true)) {
            float playerExp = player.getExp();
            float expToGive = 0;

            if (player.isSprinting()) {
                expToGive = -0.005F;
            } else {
                expToGive = 0.005F;
            }

            if (expToGive < 0)
                expToGive += (expToGive * player.getLevel() * 0.02);

            if (expToGive < 0 && playerExp <= 0 && player.getLevel() == 0)
                continue;

            playerExp += expToGive;

            if (playerExp <= 0) {
                if (player.getLevel() > 0) {
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_BURP, 1, 0);

                    //     onChange(player, false);

                    player.setLevel(player.getLevel() - 1);

                    playerExp = 1;
                } else {
                    playerExp = 0;
                }
            } else if (playerExp >= 1) {
                if (player.getLevel() < 0) {
                    player.setLevel(player.getLevel() + 1);
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 0);

                    //       onChange(player, true);

                    playerExp = 0;
                } else {
                    playerExp = 1;
                }
            }

            player.setExp(playerExp);
        }
    }

    @EventHandler
    public void dodgeAttack(CustomDamageEvent event) {
        if (event.getFinalDamager() instanceof Player && event.getDamagee() instanceof Player) {
            if (!hasAbility((Player) event.getDamagee())) {
                return;
            }

            //sans is damagee
            Player player = event.getPlayerDamagee();
            Player attacker = (Player) event.getFinalDamager();

            if (player.getExp() < 0.12F) {
                player.setExp(0F);

                double damageNumber = event.getDamage() / 2;
                //		String damageTitle = (damageNumber + "");
                attacker.sendTitle(" ", (C.Bold + C.Red + new DecimalFormat("#.#").format(Math.floor(damageNumber * 10) / 10)), 3, 10, 0);
                return;
            }

            player.sendMessage("dodged");

            attacker.sendTitle(" ", C.Bold + "MISS", 0, 20, 0);

            player.setExp(player.getExp() - 0.12F);

            event.setCancelled(true);
        } else {
            return;
        }
    }

    @EventHandler
    public void onDamage(CustomDamageEvent event) {
        if (!hasAbility(event.getPlayerDamager()))
            return;

        if (!event.getAttackType().isMelee())
            return;

        if (!UtilInv.isHolding(event.getPlayerDamager(), Material.BONE))
            return;

        event.setFinalDamage(2);
    }

    @EventHandler
    public void onShoot(PlayerInteractEvent event) {
        if (!hasAbility(event.getPlayer()))
            return;

        Player player = event.getPlayer();

        if (!UtilInv.isHolding(player, Material.BONE_BLOCK))
            return;

        if (player.getExp() < 0.12F) {
            player.sendMessage(C.Red + "You don't have enough energy!");
            return;
        }

        Vector vec = player.getLocation().getDirection();
        vec.multiply(1.1);
        Arrow arrow = player.launchProjectile(Arrow.class, vec);
        //	arrow.setBounce(true);

        //	arrow.setVelocity(vec);
        arrow.setShooter(player);
        arrow.setPickupStatus(PickupStatus.DISALLOWED);
        arrow.setGravity(true);

        MiscDisguise boneDisguise = new MiscDisguise(DisguiseType.getType(EntityType.DROPPED_ITEM), 352);

        //	DisguiseAPI.disguiseToAll(arrow, boneDisguise);
        boneDisguise.setEntity(arrow);
        boneDisguise.startDisguise();
        player.setExp(player.getExp() - 0.12F);
    }

    @EventHandler
    public void arrowDamage(CustomDamageEvent event) {
    	if(event.getFinalDamager() instanceof Player)
    	{
	        Player player = (Player) event.getFinalDamager();
	        if (!hasAbility(player))
	            return;
	
	        if (!(event.getDamager() instanceof Arrow) && event.getAttackType() != AttackType.PROJECTILE)
	            return;
	
	        event.setFinalDamage(2);
	    }
    }
}


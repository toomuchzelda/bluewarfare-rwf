package me.libraryaddict.arcade.game.searchanddestroy.abilities;

import me.libraryaddict.arcade.game.GameTeam;
import me.libraryaddict.arcade.kits.Ability;
import me.libraryaddict.core.C;
import me.libraryaddict.core.Pair;
import me.libraryaddict.core.damage.AttackType;
import me.libraryaddict.core.damage.CustomDamageEvent;
import me.libraryaddict.core.damage.CustomDamageEvent.DamageRunnable;
import me.libraryaddict.core.damage.DamageManager;
import me.libraryaddict.core.data.ParticleColor;
import me.libraryaddict.core.time.TimeEvent;
import me.libraryaddict.core.time.TimeType;
import me.libraryaddict.core.utils.UtilMath;
import me.libraryaddict.core.utils.UtilParticle;
import me.libraryaddict.core.utils.UtilPlayer;
import me.libraryaddict.core.utils.UtilTime;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * This class manages everything to do with Porcupine's Unique Ability
 * This file should be in your abilities folder
 *
 * @author birdy117
 */
public class PorcupineAbility extends Ability {

    /*
     * The maximum amount of damage a Porcupine can reflect back
     */
    private static final double reflectDamageCap = 4.0;
    /*
     * The amount of true damage a Porcupine will deflect (initially set to 1.0).
     */
    public double reflectDamage = 1.0;

    /*
     * Easy to access Porcupine Stats for balancing
     */
    /*
     * An ArrayList used to keep track of the arrows within a porc
     */
    private ArrayList<Pair<String, Long>> _arrows = new ArrayList<Pair<String, Long>>();
    /*
     * Creates a new AttackType that deals with the death message of someone dying from the modded Thorns.
     * This also what gives the reflection damage to ignore armor and deal no KB
     */
    private AttackType PORC_THORNS = new AttackType("Super Thorns", "%Killed% died trying to get past %Killer%'s pointy quils").setIgnoreRate().setNoKnockback();

    /**
     * This method handles the reflection damage dealt to enemies.
     *
     * @param event
     */
    @EventHandler
    public void onDamage(CustomDamageEvent event) {
        if (!isLive()) {
            return;
        }

        Player player = event.getPlayerDamagee();

        Player enemy = event.getPlayerDamager();

        if (player == null) {
            return;
        }

        if (!hasAbility(player))
            return;

        if (!isAlive(player))
            return;

        if (event.getAttackType() != AttackType.MELEE || event.getAttackType() != AttackType.PROJECTILE) {
            return;
        }

        DamageManager gameDmgMngr = this.getGame().getDamageManager();
        CustomDamageEvent porcThorns = gameDmgMngr.createEvent(enemy, PORC_THORNS, reflectDamage, player);
        enemy.playSound(enemy.getLocation(), Sound.ENCHANT_THORNS_HIT, 1.4F, 1.7F);
        gameDmgMngr.callDamage(porcThorns);

    }

    /**
     * This method handles each time a porc gets hit with an arrow.
     *
     * @param event
     */
    @EventHandler(ignoreCancelled = true)
    public void onArrowHit(CustomDamageEvent event) {
        event.addRunnable(new DamageRunnable(getKit().getName() + " Hit") {
            @Override
            public void run(CustomDamageEvent event2) {
                if (!isLive())
                    return;

                Player player = event.getPlayerDamagee();

                if (player == null) {
                    return;
                }

                if (!hasAbility(player))
                    return;

                if (!isAlive(player))
                    return;

                GameTeam team = getGame().getTeam(player);

                if (team == null)
                    return;

                ParticleColor color = team.getSettings().getParticleColor();
                ArrayList<Player> enemies = UtilPlayer.getPlayers();

                enemies.remove(player);

                if (event.getAttackType() == AttackType.PROJECTILE) {
                    for (int i = 0; i < 40; i++) {
                        UtilParticle.playParticle(player.getLocation()
                                .add(UtilMath.rr(-0.4, .4), UtilMath.rr(0, 1.9), UtilMath.rr(-0.4, .4)), color);
                    }
                }

                if (!(event.getDamager() instanceof Arrow)) {
                    return;
                }

                if (reflectDamage < reflectDamageCap) {
                    player.sendMessage(C.Yellow + "You were hit by an arrow! You feel your quils begin to rise!");
                    player.playSound(player.getLocation(), Sound.ENTITY_CAT_HURT, 1.4F, 1.7F);
                    reflectDamage++;
                } else {
                    player.sendMessage(C.Gold + "Your quils are fully exposed!");
                    player.playSound(player.getLocation(), Sound.ENTITY_CAT_HURT, 1.4F, 1.7F);
                }
                _arrows.add(Pair.of(player.getName(), System.currentTimeMillis()));
            }
        });
    }

    /**
     * Manages each second a porc has arrows in them.
     *
     * @param event
     */
    @EventHandler
    public void onUpdate(TimeEvent event) {
        if (event.getType() != TimeType.TICK) {
            return;
        }

        if (!isLive())
            return;

        while (!_arrows.isEmpty() && UtilTime.elasped(_arrows.get(0).getValue(), 10000)) {
            Player player = Bukkit.getPlayerExact(_arrows.remove(0).getKey());

            if (player == null) {
                continue;
            }

            if (!isAlive(player))
                continue;

            UtilPlayer.setArrowDespawnTimer(player, 2000);

            int arrows = UtilPlayer.getArrowsInBody(player) - 1;

            String message = "An arrow has disappeared. ";

            if (arrows > 0) {
                message += "You have " + arrows + " arrow" + (arrows != 1 ? "s" : "") + " stuck in you! Your quils begin to ease.";
                if (reflectDamage > 1.0) {
                    reflectDamage--;
                }
            } else {
                message += "No remaining arrows stuck in you! Your quils have fully subsided!";
            }

            player.sendMessage(C.Yellow + message);

            UtilPlayer.setArrowsInBody(player, arrows);

            if (arrows <= 0) {
                Iterator<Pair<String, Long>> itel = _arrows.iterator();

                while (itel.hasNext()) {
                    if (itel.next().getKey().equals(player.getName())) {
                        itel.remove();
                    }
                }
            }
        }
    }
}

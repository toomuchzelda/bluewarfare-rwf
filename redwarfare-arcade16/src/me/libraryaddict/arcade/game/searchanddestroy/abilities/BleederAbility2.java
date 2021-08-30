package me.libraryaddict.arcade.game.searchanddestroy.abilities;

import me.libraryaddict.arcade.kits.Ability;
import me.libraryaddict.core.C;
import me.libraryaddict.core.Pair;
import me.libraryaddict.core.damage.CustomDamageEvent;
import me.libraryaddict.core.data.ParticleColor;
import me.libraryaddict.core.time.TimeEvent;
import me.libraryaddict.core.time.TimeType;
import me.libraryaddict.core.utils.UtilEnt;
import me.libraryaddict.core.utils.UtilMath;
import me.libraryaddict.core.utils.UtilParticle;
import me.libraryaddict.core.utils.UtilTime;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * This class manages Bleeders Ability
 * This file should be in your abilities folder
 *
 * @author birdy117
 */
public class BleederAbility2 extends Ability {

    // An arraylist used to keep track of all players that are "bleeding"
    // Bleeding players have 50% of their HP turned into Absorption Hearts
    List<Pair<Long, Player>> bleeding = new ArrayList<Pair<Long, Player>>();

    /**
     * This method handles each time a player gets hit by a Bleeder
     *
     * @param event
     */
    @EventHandler
    public void onDamage(CustomDamageEvent event) {
        if (!hasAbility(event.getPlayerDamager()))
            return;

        if (!event.getAttackType().isMelee())
            return;


        if (!isAlive(event.getDamagee()))
            return;

        if (!event.isLivingDamagee())
            return;

        LivingEntity enemy = event.getLivingDamagee();

        bleed(event.getDamager(), enemy);
    }


    /**
     * This method tries to bleed the enemy after being hit
     *
     * @param damager
     * @param damagee
     */
    public void bleed(Entity damager, Entity damagee) {

        Player enemy = (Player) damagee;

        for (Pair<Long, Player> ent : bleeding) {
            if (ent.getValue().equals(enemy)) {
                return;
            }
        }

        enemy.playSound(enemy.getLocation(), Sound.ENTITY_GHAST_HURT, 1.4F, 0);

        double maxHp = enemy.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue();
        double actualHp = enemy.getHealth();

        if (actualHp > maxHp / 2) {
            UtilEnt.setAbsorptionHearts(enemy, actualHp - maxHp / 2);
        }

        enemy.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(maxHp / 2);
        enemy.sendMessage(ChatColor.RED + damager.getName() + " casued you to bleed! You lost half of your max health!");
        for (int i = 0; i < 8; i++) {
            UtilParticle.playParticle(
                    enemy.getLocation().add(UtilMath.rr(0.3), 1 + UtilMath.rr(0.5), UtilMath.rr(0.3)),
                    ParticleColor.DARK_RED);
        }
        bleeding.add(Pair.of(System.currentTimeMillis(), enemy));
    }

    /**
     * Manages each second a Player is bleeding
     *
     * @param event
     */
    @EventHandler
    public void onBleedTick(TimeEvent event) {
        if (event.getType() != TimeType.TICK)
            return;

        if (!isLive())
            return;

        Iterator<Pair<Long, Player>> itel = bleeding.iterator();

        while (itel.hasNext()) {
            Pair<Long, Player> entry = itel.next();

            Player entity = entry.getValue();

            if (!entity.isValid()) {
                itel.remove();
                continue;
            }

            // The player will remain at half health for 30 seconds
            // Not sure if the number right here is equal to 30 seconds in game
            if (!UtilTime.elasped(entry.getKey(), 30000)) {
                UtilParticle.playParticle(
                        entity.getLocation().add(UtilMath.rr(0.3), 1 + UtilMath.rr(0.5), UtilMath.rr(0.3)),
                        ParticleColor.DARK_RED);
                return;
            }

            // After 30 seconds, it will remove them from the bleed list and give them their
            // original health back
            itel.remove();
            entity.sendMessage(C.Green + "The bleeding has finally stopped!");
            entity.getAttribute(Attribute.GENERIC_MAX_HEALTH)
                    .setBaseValue(entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue() * 2);
            if (UtilEnt.getAbsorptionHearts(entity) > 0) {
                UtilEnt.heal(entity, UtilEnt.getAbsorptionHearts(entity));
                UtilEnt.setAbsorptionHearts(entity, 0);
            }
        }
    }

}

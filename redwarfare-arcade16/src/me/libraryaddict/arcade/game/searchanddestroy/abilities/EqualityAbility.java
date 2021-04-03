package me.libraryaddict.arcade.game.searchanddestroy.abilities;

import me.libraryaddict.arcade.kits.Ability;
import me.libraryaddict.core.damage.CustomDamageEvent;
import org.bukkit.event.EventHandler;

public class EqualityAbility extends Ability {
    @EventHandler
    public void onDamage(CustomDamageEvent event) {
        if (!event.isPlayerDamagee())
            return;

        if (!hasAbility(event.getPlayerDamagee()) && !hasAbility(event.getPlayerDamager()))
            return;

        if (!event.getAttackType().isMelee() && !event.getAttackType().isProjectile())
            return;

        event.setFinalDamage(3);
    }
}

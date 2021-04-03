package me.libraryaddict.arcade.game.searchanddestroy.abilities;

import me.libraryaddict.arcade.kits.Ability;
import me.libraryaddict.core.condition.ConditionManager;
import me.libraryaddict.core.damage.CustomDamageEvent;
import me.libraryaddict.core.damage.CustomDamageEvent.DamageRunnable;
import me.libraryaddict.core.utils.UtilInv;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class VenomAbility extends Ability {
    @EventHandler
    public void onDamage(CustomDamageEvent event) {
        if (!event.isPlayerDamager())
            return;

        if (!hasAbility(event.getPlayerDamager()))
            return;

        if (!UtilInv.isHolding(event.getPlayerDamager(), EquipmentSlot.HAND, Material.IRON_SWORD))
            return;

        event.addRunnable(new DamageRunnable("Venom") {
            public void run(CustomDamageEvent event2) {
                ConditionManager.addPotion(new PotionEffect(PotionEffectType.POISON, 60, 0), event.getDamagee(),
                        event.getPlayerDamager(), true);
                ConditionManager.addPotion(new PotionEffect(PotionEffectType.SLOW, 60, 0), event.getDamagee(),
                        event.getPlayerDamager(), true);
            }
        });
    }

}

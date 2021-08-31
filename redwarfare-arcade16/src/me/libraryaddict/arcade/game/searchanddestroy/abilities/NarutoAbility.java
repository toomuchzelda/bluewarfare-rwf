package me.libraryaddict.arcade.game.searchanddestroy.abilities;

import me.libraryaddict.arcade.kits.Ability;
import me.libraryaddict.core.C;
import me.libraryaddict.core.damage.AttackType;
import me.libraryaddict.core.explosion.CustomExplosion;
import me.libraryaddict.core.utils.UtilInv;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.MiscDisguise;
import org.bukkit.Material;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;

public class NarutoAbility extends Ability {

    int cooldown = 0;

    @EventHandler
    public void onShoot(PlayerInteractEvent event) {
        if (!hasAbility(event.getPlayer())) {
            return;
        }

        if (!UtilInv.isHolding(event.getPlayer(), Material.SUGAR_CANE)) {
            return;
        }

        Player player = event.getPlayer();

        if (cooldown > 0) {
            player.sendMessage(C.Red + "You can throw again in " + cooldown + " seconds!");
            return;
        }

        Vector vec = player.getLocation().getDirection();
        Arrow arrow = player.launchProjectile(Arrow.class, vec);
        arrow.setShooter(player);
        arrow.setPickupStatus(AbstractArrow.PickupStatus.DISALLOWED);
        arrow.setSilent(true);

        MiscDisguise thing = new MiscDisguise(DisguiseType.SNOWBALL);
        thing.setEntity(arrow);
        thing.startDisguise();
    }

    @EventHandler
    public void arrowLand(ProjectileHitEvent event) {
        if (event.getEntity().getShooter() instanceof Player && event.getEntity() instanceof Arrow) {
            Player player = (Player) event.getEntity().getShooter();
            if (hasAbility(player)) {
                Arrow arrow = (Arrow) event.getEntity();

                new CustomExplosion(event.getEntity().getLocation(), 6.5F, AttackType.EXPLOSION).
                setDamageBlocks(false).setMaxDamage(2)
                        .setDamager(player).setIgnoreNonLiving(true).customExplode();

                arrow.remove();
            } else {
                return;
            }
        } else {
            return;
        }
    }
}

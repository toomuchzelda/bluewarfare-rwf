package me.libraryaddict.arcade.game.searchanddestroy.killstreak.streaks;

import me.libraryaddict.arcade.game.GameTeam;
import me.libraryaddict.arcade.game.searchanddestroy.SearchAndDestroy;
import me.libraryaddict.arcade.game.searchanddestroy.killstreak.StreakBase;
import me.libraryaddict.core.damage.CustomDamageEvent;
import me.libraryaddict.core.damage.CustomDamageEvent.DamageRunnable;
import me.libraryaddict.core.inventory.utils.ItemBuilder;
import me.libraryaddict.core.time.TimeEvent;
import me.libraryaddict.core.time.TimeType;
import me.libraryaddict.core.utils.UtilError;
import me.libraryaddict.core.utils.UtilMath;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftIronGolem;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map.Entry;

public class IronGolemKillstreak extends StreakBase {
    private HashMap<Entity, MeleeAttackGoal> _melee = new HashMap<Entity, MeleeAttackGoal>();
    private Field _timer;

    public IronGolemKillstreak(SearchAndDestroy manager) {
        super(manager, "iron golem");
        try {
            //                                                  followingTargetEvenIfNotSeen
            _timer = MeleeAttackGoal.class.getDeclaredField("c");
            _timer.setAccessible(true);
        } catch (Exception ex) {
            UtilError.handle(ex);
        }
    }

    @Override
    public Material getFallingMaterial() {
        return Material.IRON_BLOCK;
    }

    @Override
    public ItemStack getItem() {
        ItemBuilder builder = new ItemBuilder(Material.IRON_BLOCK, 1);
        builder.setTitle("Summon Iron Golem");
        return builder.build();
    }

    @Override
    public int getKillsRequired() {
        return 8;
    }

    @EventHandler
    public void onDamageCorrection(CustomDamageEvent event) {
        Entity entity = event.getDamager();

        if (entity == null || !(entity instanceof IronGolem)) {
            return;
        }

        event.setInitDamage("Damage Correction", 13 + UtilMath.r(2));

        event.addRunnable(new DamageRunnable("Iron Golem") {

            @Override
            public void run(CustomDamageEvent event2) {
                if (!_melee.containsKey(entity)) {
                    return;
                }

                try {
                    MeleeAttackGoal goal = _melee.get(entity);

                    _timer.set(goal, 30);
                } catch (Exception ex) {
                    UtilError.handle(ex);
                }
            }
        });
    }

    @EventHandler(ignoreCancelled = true)
    public void onDamageKnockback(CustomDamageEvent event) {
        if (!(event.getDamager() instanceof IronGolem))
            return;

        event.setKnockback(new Vector(0, 1, 0));
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onDeath(EntityDeathEvent event) {
        event.getDrops().clear();
    }

    @Override
    public void onLanded(Player player, Block block) {
        IronGolem ironGolem = (IronGolem) block.getWorld().spawnEntity(block.getLocation().add(0.5, 0, 0.5),
                EntityType.IRON_GOLEM);

        ironGolem.setMaxHealth(40);
        ironGolem.setHealth(20);
        GameTeam team = _manager.getTeam(player);

        team.addToTeam(ironGolem, player);

        ironGolem.setCustomName(team.getColoring() + player.getName() + "'s Iron Golem");
        ironGolem.setCustomNameVisible(true);

        net.minecraft.world.entity.animal.IronGolem golem = ((CraftIronGolem) ironGolem).getHandle();

        try {
            Field goalSelector = Mob.class.getDeclaredField("goalSelector");
            goalSelector.setAccessible(true);

            Field targetSelector = Mob.class.getDeclaredField("targetSelector");
            targetSelector.setAccessible(true);

            Field b = GoalSelector.class.getDeclaredField("NO_GOAL");
            b.setAccessible(true);

            Field c = GoalSelector.class.getDeclaredField("lockedFlags");
            c.setAccessible(true);

            GoalSelector goal = (GoalSelector) goalSelector.get(golem);
            GoalSelector target = (GoalSelector) targetSelector.get(golem);
            PathfinderSelector selector = new PathfinderSelector(_manager, team);

            ((LinkedHashSet) b.get(goal)).clear();
            ((LinkedHashSet) c.get(goal)).clear();
            ((LinkedHashSet) b.get(target)).clear();
            ((LinkedHashSet) c.get(target)).clear();

            MeleeAttackGoal melee = new MeleeAttackGoal(golem, 1.0D, false);
            // melee[1] = new MeleeAttackGoal(golem, 1.0D, true);

            _melee.put(ironGolem, melee);

            goal.addGoal(0, new FloatGoal(golem));
            goal.addGoal(2, melee);
            goal.addGoal(5, new MoveTowardsRestrictionGoal(golem, 1.0D));
            //goal.a(6, new PathfinderGoalMoveThroughVillage(golem, 1.0D, false));
            goal.addGoal(6, new MoveThroughVillageGoal(golem, 1.0D, false, 0, null));
            goal.addGoal(7, new RandomStrollGoal(golem, 1.0D));
            goal.addGoal(8, new LookAtPlayerGoal(golem, net.minecraft.world.entity.player.Player.class, 8.0F));
            goal.addGoal(8, new RandomLookAroundGoal(golem));

            //target.a(1, new PathfinderGoalHurtByTarget(golem, true));
            target.addGoal(1, new HurtByTargetGoal(golem));
            target.addGoal(2, new NearestAttackableTargetGoal(golem,
                    net.minecraft.world.entity.player.Player.class, 0, true, false, selector));
            target.addGoal(2, new NearestAttackableTargetGoal(golem,
                    Mob.class, 0, false, false, selector));
        } catch (Exception ex) {
            UtilError.handle(ex);
        }
    }

    @EventHandler
    public void onMeleeTick(TimeEvent event) {
        if (event.getType() != TimeType.TICK) {
            return;
        }

        Iterator<Entry<Entity, MeleeAttackGoal>> itel = _melee.entrySet().iterator();

        while (itel.hasNext()) {
            Entry<Entity, MeleeAttackGoal> entry = itel.next();

            Entity entity = entry.getKey();

            if (!entity.isValid()) {
                itel.remove();
                continue;
            }
        }
    }

}

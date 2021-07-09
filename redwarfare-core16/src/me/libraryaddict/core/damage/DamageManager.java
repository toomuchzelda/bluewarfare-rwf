package me.libraryaddict.core.damage;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;
import me.libraryaddict.core.C;
import me.libraryaddict.core.Pair;
import me.libraryaddict.core.combat.CombatEvent;
import me.libraryaddict.core.combat.CombatManager;
import me.libraryaddict.core.condition.ConditionManager;
import me.libraryaddict.core.damage.CustomDamageEvent.DamageRunnable;
import me.libraryaddict.core.plugin.MiniPlugin;
import me.libraryaddict.core.time.TimeEvent;
import me.libraryaddict.core.time.TimeType;
import me.libraryaddict.core.utils.*;
import net.minecraft.server.v1_16_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.EntityEffect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityCombustByEntityEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

public class DamageManager extends MiniPlugin {
    private CombatManager _combatManager;
    private Method _damageArmor;
    private Method _deathSound;
    private Method _hurtSound;
    private Method _pitch;
    private Method _radius;
    //				damagee EID,     damager eid, tick,    damage
    private HashMap<Integer, HashMap<Integer, Pair<Integer, Double>>> _tickHit = new HashMap<>();
    //				damagee, tick
    private HashMap<Integer, Integer> _altNoDamageTicks = new HashMap<>();
    
    //to store attacks that come 1 tick early and execute 1 tick later
    //private HashMap<Integer, EntityDamageEvent> _attackBuffer = new HashMap<>();

    public DamageManager(JavaPlugin plugin, CombatManager combatManager) {
        super(plugin, "Damage Manager");

        for (DamageCause cause : DamageCause.values()) {
            AttackType attack = AttackType.getAttack(cause);

            if (attack == null || attack == AttackType.UNKNOWN) {
                System.err.print("The DamageCause '" + cause.name() + "' has not been registered as an attack type");
            }
        }

        _combatManager = combatManager;

        try {
            _damageArmor = EntityLiving.class.getDeclaredMethod("damageArmor", DamageSource.class, float.class);
            _damageArmor.setAccessible(true);


            _deathSound = EntityLiving.class.getDeclaredMethod("getSoundDeath");
            _deathSound.setAccessible(true);

            _hurtSound = EntityLiving.class.getDeclaredMethod("getSoundHurt", DamageSource.class);
            _hurtSound.setAccessible(true);

            _radius = EntityLiving.class.getDeclaredMethod("getSoundVolume");
            _radius.setAccessible(true);

            _pitch = EntityLiving.class.getDeclaredMethod("dH");
            _pitch.setAccessible(true);
        } catch (Exception ex) {
            UtilError.handle(ex);
        }

        ProtocolLibrary.getProtocolManager()
                .addPacketListener(new PacketAdapter(getPlugin(), ListenerPriority.LOW, PacketType.Play.Server.ENTITY_VELOCITY) {
                    @Override
                    public void onPacketSending(PacketEvent event) {
                        event.setPacket(event.getPacket().shallowClone());

                        StructureModifier<Integer> ints = event.getPacket().getIntegers();

                        Player player = UtilPlayer.getPlayer(ints.read(0));

                        if (player == null || player.isGliding())
                            return;

                        Vector vec = reduceVelocity(player, ints.read(1) / 8000D, ints.read(2) / 8000D, ints.read(3) / 8000D);

                        ints.write(1, (int) (vec.getX() * 8000));
                        ints.write(2, (int) (vec.getY() * 8000));
                        ints.write(3, (int) (vec.getZ() * 8000));
                    }
                });
    }

    public void addHit(Entity damager, Entity damagee, double damage) {
        HashMap<Integer, Pair<Integer, Double>> records;

        if ((records = _tickHit.get(damagee.getEntityId())) == null) {
            _tickHit.put(damagee.getEntityId(), records = new HashMap<Integer, Pair<Integer, Double>>());
        }

        records.put(damager.getEntityId(), Pair.of(UtilTime.currentTick, damage));
    }

    private void applyKnockback(CustomDamageEvent event) {
        if (event.getFinalKnockback().length() <= 0) {
            return;
        }

        UtilEnt.velocity(event.getDamagee(), event.getFinalKnockback(), false);

        ConditionManager.addFall(event.getDamagee(), event.getFinalDamager());
    }
    
//    @EventHandler
//    public void clickDebug(PlayerInteractEntityEvent event) {
//    	if(event.getPlayer().isSneaking())
//    	{
//	    	event.getRightClicked().setFireTicks(500);
//	    	Bukkit.broadcastMessage("setrightclicked to " + event.getRightClicked().getFireTicks());
//    	}
//    	else
//    	{
//    		Bukkit.broadcastMessage(event.getRightClicked().getName() + " FT: " + event.getRightClicked().getFireTicks());
//    	}
//    }

    public void callDamage(CustomDamageEvent event) {
        Bukkit.getPluginManager().callEvent(event);

        AttackType attack = event.getAttackType();

        if (event.isCancelled()) {
            if (attack.isFall()) {
                event.getDamagee().setFallDistance(-1);
            }

            return;
        }

        if (attack == AttackType.CUSTOM || attack == AttackType.UNKNOWN) {
            System.err.println("Can't handle " + event.getDamagee() + " " + event.getDamager() + " " + attack.getName());
            Thread.dumpStack();
        }

        if (event.getDamage() < 0) {
            System.err.println("Error while handling damage for " + attack + " " + event.getDamagee());
            event.printDebug();
            Thread.dumpStack();
        }

        double damage = Math.max(0, event.getDamage());

        Entity entity = event.getDamagee();
        // if (entity instanceof Player && ((Player) entity).getName().equals("libraryaddict"))
        // event.printDebug();

        if (event.getDamager() instanceof Projectile && attack == AttackType.PROJECTILE) {
            if (event.getDamager() instanceof Arrow) {
                if (entity instanceof Player) {
                    UtilPlayer.setArrowsInBody((Player) entity, UtilPlayer.getArrowsInBody((Player) entity) + 1);
                }

                if (event.getFinalDamager() instanceof Player) {
                    ((Player) event.getFinalDamager()).playSound(event.getFinalDamager().getLocation(),
                            Sound.ENTITY_ARROW_HIT_PLAYER, 2, 1);
                }
            }

            event.getDamager().remove();
        }

        double modDamage = 0;

        if (!event.isIgnoreRate() && !attack.isInstantDeath() && event.isLivingDamagee()) {
            LivingEntity living = event.getLivingDamagee();
            //Bukkit.broadcastMessage("In callDamage " + living.getName() + " has " + living.getNoDamageTicks()
            //		+ " NDT");

            if (living.getNoDamageTicks() > living.getMaximumNoDamageTicks() / 2.0F) {
                modDamage = living.getLastDamage();

                if (damage <= modDamage + 0.001) {
                    if (attack.isExplosion() && event.getFinalKnockback().length() > living.getVelocity().length()) {
                        applyKnockback(event);
                    }

                    if (attack.isMelee() && event.isLivingDamager() && canHit(event.getLivingDamager(), living, damage)
                            && entity instanceof Damageable) {
                        addHit(event.getLivingDamager(), living, damage);

                        _combatManager.getCreateCombatLog(event.getDamagee())
                                .addEvent(new CombatEvent(event, damage - modDamage, true));
                    }

                    return;
                }
            }

            if (event.isLivingDamager()) {
                addHit(event.getLivingDamager(), living, damage);
            }
        }

        applyKnockback(event);

        event.runRunnables(true);

        if (!(entity instanceof Damageable))
            return;

        _combatManager.getCreateCombatLog(event.getDamagee()).addEvent(new CombatEvent(event, damage - modDamage, false));

        Damageable damageable = (Damageable) entity;
        boolean doDeath = damageable.getHealth() + UtilEnt.getAbsorptionHearts(entity) <= damage;

        if (!attack.isInstantDeath() && (!doDeath || !(entity instanceof Player))) {
            if (damageable instanceof ArmorStand) {
                if (damage > 0) {
                    entity.remove();
//                    ((CraftArmorStand) damageable).getHandle().die();
                }
            } else if (event.isLivingDamagee() && !doDeath) {
                LivingEntity living = event.getLivingDamagee();

                EntityLiving nms = ((CraftLivingEntity) living).getHandle();
                nms.aw = 1.5F;

                if (!event.isIgnoreRate() && living.getNoDamageTicks() > living.getMaximumNoDamageTicks() / 2.0F) {
                    damageEntity(living, damage - living.getLastDamage(), false);
                } else {
                    damageEntity(living, damage, true);

                    living.setNoDamageTicks(living.getMaximumNoDamageTicks());
                    nms.hurtTicks = 10;
                }

                living.setLastDamage(damage);

                if (!attack.isIgnoreArmor()) {
                    try {
                        _damageArmor.invoke(nms, DamageSource.GENERIC, (float) damage);
                    } catch (Exception e) {
                        UtilError.handle(e);
                    }
                }
            } else {
                damageable.damage(damage);

                if (damageable instanceof Player)
                    System.err.println("Handling damage wrong!!");
                // damageable.setHealth(damageable.getHealth() - damage);
                // playDamage(damageable);
            }
        } else {
            playDamage(damageable);
            handleDeath((Player) entity);
        }

        event.runRunnables(false);
        
//        if(event.getDamager() instanceof Arrow)
//        {
//        	Arrow arrow = (Arrow) event.getDamager();
//        	if (arrow.getFireTicks() > -1)
//            {
//            	event.getDamagee().setFireTicks(500);
//            	Bukkit.broadcastMessage("setfiretick manually, damageeFT is: " + event.getDamagee().getFireTicks());
//            }
//        }
    }

    public boolean canHit(Entity damager, LivingEntity damagee, double damage) {
        HashMap<Integer, Pair<Integer, Double>> records;

        if ((records = _tickHit.get(damagee.getEntityId())) == null) {
            return true;
        }

        Pair<Integer, Double> pair;

        if ((pair = records.get(damager.getEntityId())) == null)
            return true;

        int damageTicks = UtilTime.currentTick - pair.getKey();

        //previously less than or equal to
        if (damageTicks < damagee.getMaximumNoDamageTicks() / 2F)
            return false;

        if (damage <= pair.getValue() + 0.001)
            return false;

        return true;
    }

    public boolean canAttemptHit(Entity damager, LivingEntity damagee) {
        HashMap<Integer, Pair<Integer, Double>> records;

        if ((records = _tickHit.get(damagee.getEntityId())) == null) {
            return true;
        }

        Pair<Integer, Double> pair;

        if ((pair = records.get(damager.getEntityId())) == null)
            return true;

        int damageTicks = UtilTime.currentTick - pair.getKey();

        //previously less than or equal to
        if (damageTicks < damagee.getMaximumNoDamageTicks() / 2F)
            return false;

        return true;
    }

    public CustomDamageEvent createEvent(Entity entity, AttackType attack, double damage, Entity cause) {
        return createEvent(entity, attack, damage, cause,
                cause instanceof Projectile && ((Projectile) cause).getShooter() instanceof Entity
                        ? (Entity) ((Projectile) cause).getShooter() : null);
    }

    public CustomDamageEvent createEvent(Entity entity, AttackType attack, double damage, Entity cause, Entity realCause) {
        return createEvent(entity, attack, damage, cause, realCause, false);
    }

    public CustomDamageEvent createEvent(Entity entity, AttackType attack, double damage, Entity cause, Entity realCause,
                                         boolean ignoreRate) {
        CustomDamageEvent event = new CustomDamageEvent(entity, attack, damage);

        event.setDamager(cause);

        if (realCause != null) {
            event.setRealDamager(realCause);
        }

        event.setIgnoreRate(ignoreRate);

        if (cause != null && attack.isKnockback()) {
            event.setCalculateKB(true);

            if (cause instanceof Player && ((Player) cause).isSprinting()) {
                event.addKnockMult("Sprint", 1);

                event.addRunnable(new DamageRunnable("Sprinting") {
                    @Override
                    public void run(CustomDamageEvent event2) {
                        ((Player) cause).setSprinting(false);

                        EntityPlayer player = ((CraftPlayer) cause).getHandle();

                        Vec3D currentMot = player.getMot();
                        //player.motX *= 0.6;
                        //player.motZ *= 0.6;
                        player.setMot(currentMot.getX() * 0.6, currentMot.getY(), currentMot.getZ() * 0.6);
                    }
                });
            }
        }

        return event;
    }

    private void damageEntity(Damageable entity, double damage, boolean broadcastDamage) {
        if (!(entity instanceof LivingEntity)) {
            entity.damage(damage);
            return;
        }

        //EntityLiving nms = ((CraftLivingEntity) entity).getHandle();

        if (broadcastDamage) {
            entity.playEffect(EntityEffect.HURT);
            // nms.world.broadcastEntityEffect(nms, (byte) 2);
        }

        double abs = UtilEnt.getAbsorptionHearts(entity);

        if (abs > 0) {
            abs -= damage;

            UtilEnt.setAbsorptionHearts((LivingEntity) entity, Math.max(abs, 0));

            damage = -abs;
        }

        if (damage > 0) {
            double newHealth = entity.getHealth() - damage;

            if (newHealth <= 0) {
                UtilError.log("Somehow setting entity to less than 0 health?");
            }

            if (entity instanceof Player) {
                ((Player) entity).setExhaustion(Math.min(((Player) entity).getExhaustion() + 5, 40)); // TODO
            }

            entity.setHealth((float) newHealth);
        }

        if (broadcastDamage) {
            playDamage(entity);
        }
    }

    private void handleDeath(Player player) {
        CustomDeathEvent event = new CustomDeathEvent(player, _combatManager.getCreateCombatLog(player).clone());

        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled())
            return;

        if (event.isAdvertiseDeath()) {
            CustomDamageEvent combatEvent = event.getCombatLog().getLastEvent().getEvent();

            Bukkit.broadcastMessage(combatEvent.getAttackType().getDeathMessage(C.Yellow, combatEvent.getDamagee(),
                    combatEvent.getFinalDamager(), combatEvent.getDamager()));
        }

        player.damage(999);
    }

    public CustomDamageEvent newDamage(Entity entity, AttackType attack, double damage) {
        CustomDamageEvent event = new CustomDamageEvent(entity, attack, damage);

        callDamage(event);

        return event;
    }

    public CustomDamageEvent newDamage(Entity entity, AttackType attack, double damage, Entity cause) {
        return newDamage(entity, attack, damage, cause,
                cause instanceof Projectile && ((Projectile) cause).getShooter() instanceof Entity
                        ? (Entity) ((Projectile) cause).getShooter() : null);
    }

    public CustomDamageEvent newDamage(Entity entity, AttackType attack, double damage, Entity cause, Entity realCause) {
        return newDamage(entity, attack, damage, cause, realCause, false);
    }

    public CustomDamageEvent newDamage(Entity entity, AttackType attack, double damage, Entity cause, Entity realCause,
                                       boolean ignoreRate) {
        CustomDamageEvent event = createEvent(entity, attack, damage, cause, realCause, ignoreRate);

        callDamage(event);

        return event;
    }

    public CustomDamageEvent newDamage(Entity entity, AttackType attack, double damage, Entity cause, Entity realCause,
                                       boolean ignoreRate, Vector velocity) {
        CustomDamageEvent event = createEvent(entity, attack, damage, cause, realCause, ignoreRate);
        event.setKnockback(velocity);

        callDamage(event);

        return event;
    }

    public CustomDamageEvent newDamage(Entity entity, AttackType attack, double damage, Vector velocity) {
        return newDamage(entity, attack, damage, null, null, false, velocity);
    }

    //figure out the living entity's current fake noDamageTicks and return it
    public int getAltNoDamageTicks(LivingEntity living)
    {
    	//if they havent been hit put them in the record
    	if(_altNoDamageTicks.get(living.getEntityId()) == null)
    	{
    		_altNoDamageTicks.put(living.getEntityId(), UtilTime.currentTick);
    		return 0;
    	}
    	
    	int difference = UtilTime.currentTick - _altNoDamageTicks.get(living.getEntityId());
    	int noDamageTicks = living.getMaximumNoDamageTicks() - difference;
    	
    	if(noDamageTicks < 0)
    		noDamageTicks = 0;
    	
    	return noDamageTicks;
    }
    
    public void setAltNoDamageTicks(LivingEntity living, int noDamageTicks)
    {
    	int offset = 20 - noDamageTicks;
    	//since it's getted by 20 put the 'last hit' in the past (or future if larger than 20)
    	int toPut = UtilTime.currentTick - offset;
    	
    	_altNoDamageTicks.put(living.getEntityId(), toPut);
    }
    
    //onDamage used to be an EventHandler
    //the reason this method exists is because in 1.12 EntityDamageEvent was called everytime
    //something *attempted* to hit something else, even if it did no damage, ie 1 player clicking 10times/second
    //and only dealing damage 2times/second
    //in 1.16 its only called when the entity actually takes damage and not on attempted damage.
    //i might be blind but i couldnt find an alternative Event to use so I wrote this solution using a
    //seperate noDamageTick (NDT) counter and this method. the entity's NDT is set to 0 every time so
    //they can be attempted to be hit immediately afterwards everytime, but the seperately counted NDT
    // is used as their noDamageTicks value before passing the event on to onDamage and eventually callDamage
    // so they're not able to be damaged every tick
    @EventHandler
    public void onDamageCaller(EntityDamageEvent event)
    {
    	LivingEntity living;
    	if(event.getEntity() instanceof LivingEntity)
    	{
    		living = (LivingEntity) event.getEntity();
    		living.setNoDamageTicks(getAltNoDamageTicks(living));
    		//Bukkit.broadcastMessage(living.getName() + "has " + getAltNoDamageTicks(living) + " NDT");
    		
    		onDamage(event);
    		
    		//Bukkit.broadcastMessage("After onDamage called " + living.getName() + " has "
    		//		+ living.getNoDamageTicks() + " NDT");
    		setAltNoDamageTicks(living, living.getNoDamageTicks());
    		//Bukkit.broadcastMessage("and " + living.getName() + " aNDT is " + getAltNoDamageTicks(living));
    		
    		//allow the event to be called again immediately after
    		living.setNoDamageTicks(0);
    		//Bukkit.broadcastMessage(living.getName() + " NDT set to " + living.getNoDamageTicks());
    		//Bukkit.broadcastMessage("---------------------");
    	}
    	else
    	{
    		onDamage(event);
    	}
    }
    
    public void onDamage(EntityDamageEvent event) {
        if (event.getCause() == DamageCause.CUSTOM) {
            if (event.getEntityType() == EntityType.PLAYER)
                Thread.dumpStack();

            return;
        }

        event.setCancelled(true);

        AttackType attackType = AttackType.getAttack(event.getCause());
        double damage = event.getDamage();
        Entity damager = null;

        if (event instanceof EntityDamageByEntityEvent) {
            EntityDamageByEntityEvent damageEvent = (EntityDamageByEntityEvent) event;

            damager = damageEvent.getDamager();

            if (damager instanceof Player && attackType == AttackType.MELEE) {
                ItemStack item = ((Player) damager).getInventory().getItemInMainHand();

                damage = UtilInv.getDamage(item);

                if (event.getEntity() instanceof LivingEntity
                        && !canAttemptHit(damager, (LivingEntity) event.getEntity())) {
                    return;
                }
                else
                {
                	 ((Player) damager).sendMessage("ndt: " + ((LivingEntity) event.getEntity()).getNoDamageTicks());
                }
            }
        }

        /* if (!attackType.isInstantDeath() && attackType.isMelee() && event.getEntity() instanceof LivingEntity)
        {
        LivingEntity living = (LivingEntity) event.getEntity();

        if (living.getNoDamageTicks() > living.getMaximumNoDamageTicks() / 2.0F)
        {
            if (damage <= living.getLastDamage() + 0.001)
            {
                if (!(damager instanceof LivingEntity) || !canHit((LivingEntity) damager, living, damage))
                {
                    return;
                }
            }
        }
        }*/

        CustomDamageEvent newEvent = newDamage(event.getEntity(), attackType, damage, damager);

        if (!(newEvent.getDamagee() instanceof Damageable)) {
            event.setCancelled(newEvent.isCancelled());
        }
    }

    @EventHandler
    public void onDamageTickCleanup(TimeEvent event) {
        if (event.getType() != TimeType.MIN)
            return;

        Iterator<Entry<Integer, HashMap<Integer, Pair<Integer, Double>>>> itel = _tickHit.entrySet().iterator();

        while (itel.hasNext()) {
            Entry<Integer, HashMap<Integer, Pair<Integer, Double>>> entry = itel.next();

            entry.getValue().values().removeIf(pair -> pair.getKey() >= 200);
            // the above replaces the following:
//            Iterator<Entry<Integer, Pair<Integer, Double>>> itel2 = entry.getValue().entrySet().iterator();
//
//            while (itel2.hasNext()) {
//                Entry<Integer, Pair<Integer, Double>> entry2 = itel2.next();
//
//                if (entry2.getValue().getKey() < 200)
//                    continue;
//
//                itel2.remove();
//            }
            if (!entry.getValue().isEmpty())
                continue;

            itel.remove();
        }
        
        Iterator<Entry<Integer, Integer>> itel2 = _altNoDamageTicks.entrySet().iterator();
        
        while (itel2.hasNext()){
        	Entry<Integer, Integer> entry = itel2.next();
        	int tick = UtilTime.currentTick - entry.getValue();
        	
        	//just cover every case except if its >= 200
        	if(!(tick < 200))
        	{
        		itel2.remove();
        	}
        	
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        event.setDeathMessage(null);
        event.setDroppedExp(0);
        event.setKeepInventory(true);
        event.setKeepLevel(false);
        event.setNewExp(0);
        event.setNewLevel(0);
        event.setNewTotalExp(0);
    }

    @EventHandler
    public void onEntityCombust(EntityCombustByEntityEvent event) {
        if (!(event.getCombuster() instanceof Player || event.getCombuster() instanceof Arrow))
            return;

        event.setCancelled(true);
    }

    public void playDamage(Damageable entity) {
        if (!(entity instanceof LivingEntity)) {
            return;
        }

        if (entity.getHealth() > 0)
            entity.playEffect(EntityEffect.HURT);
        // I think bukkit can handle death sounds itself
        // TODO check
        // the above replaces the following:
        EntityLiving nms = ((CraftLivingEntity) entity).getHandle();


        try {
            SoundEffect effect;
            if (entity.getHealth() <= 0.0F) {
                effect = (SoundEffect) _deathSound.invoke(nms);
            } else {
                effect = (SoundEffect) _hurtSound.invoke(nms, (DamageSource) null);
            }

            if (effect != null) {
                nms.playSound(effect, (float) _radius.invoke(nms), (float) _pitch.invoke(nms));
            }
        } catch (Exception ex) {
            UtilError.handle(ex);
        }
    }

    private float getFrictionFactor(Material material) {
        switch (material) {
            case BLUE_ICE:
                return 0.989f;
            case FROSTED_ICE:
            case ICE:
            case PACKED_ICE:
                return 0.98f;
            case SLIME_BLOCK:
                return 0.8f;
            default: // all other blocks are 0.6
                return 0.6f;
        }
    }

    private Vector reduceVelocity(LivingEntity entity, double motX, double motY, double motZ) {
        float frictionIGuess = 0.91f;
        if (entity.isOnGround()) {
            frictionIGuess *= getFrictionFactor(entity.getLocation().getBlock().getRelative(BlockFace.DOWN).getType());
        }

        //unsure if not climbing or climbing
        if (UtilEnt.isClimbing(entity)) {
            motX = UtilMath.clamp(motX, -0.15, 0.15); // that many digits is overkill
            motZ = UtilMath.clamp(motZ, -0.15, 0.15);
            entity.setFallDistance(0);

            if (motY < -0.15) {
                motY = -0.15;
            }

            boolean flag = entity instanceof Player && ((Player) entity).isSneaking();

            if (flag && motY < 0.0) {
                motY = 0;
            }

            if (((CraftEntity) entity).getHandle().positionChanged) {
                motY = 0.2;
            }
        }

        motY -= 0.08;

        if (entity.hasPotionEffect(PotionEffectType.LEVITATION)) {
            motY += (0.05 * (UtilEnt.getPotion(entity, PotionEffectType.LEVITATION).getAmplifier() + 1) - motY) * 0.2;
        }

        // System.out.println(MinecraftServer.currentTick + " Pre 3. " + motZ);

        motY *= 0.98;
        motX *= frictionIGuess;
        motZ *= frictionIGuess;
        // System.out.println(MinecraftServer.currentTick + " 3. " + motZ);

        return new Vector(motX, motY, motZ);
    }

}

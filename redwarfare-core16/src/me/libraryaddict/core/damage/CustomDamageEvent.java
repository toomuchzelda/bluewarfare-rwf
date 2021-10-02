package me.libraryaddict.core.damage;

import me.libraryaddict.core.Pair;
import me.libraryaddict.core.condition.ConditionManager;
import me.libraryaddict.core.utils.UtilEnt;
import me.libraryaddict.core.utils.UtilError;
import me.libraryaddict.core.utils.UtilMath;
import me.libraryaddict.core.utils.UtilParticle;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class CustomDamageEvent extends Event implements Cancellable {
	private static final HandlerList _handlers = new HandlerList();
	private AttackType _attackType;
	private ArrayList<Pair<String, Double>> _baseDamage = new ArrayList<Pair<String, Double>>();
	private boolean _cancelled;
	private ArrayList<Pair<DamageMod, Double>> _damageMods = new ArrayList<Pair<DamageMod, Double>>();
	private ArrayList<Pair<DamageMod, Double>> _damageMults = new ArrayList<Pair<DamageMod, Double>>();
	private Entity _damager;
	private Entity _entity;
	private Double _finalDamage;
	private boolean _ignoreConditions;
	private boolean _ignoreRate;
	private Vector _knockback = new Vector();
	private HashMap<String, Vector> _knockbackMods = new HashMap<String, Vector>();
	private HashMap<String, Double> _knockbackMult = new HashMap<String, Double>();
	private Entity _realDamager;
	private ArrayList<DamageRunnable> _runnables = new ArrayList<DamageRunnable>();
	private boolean _calculateKB;

	//for switching kb modes
	//1 for 1.8, 2 for my recreation, 0 for lib.
	public static int kbMode = 1;
	public static boolean sprintCancel = true;
	public static boolean reduceVel = false;
	public static boolean velPacket = true;
	public static boolean attackRate = true;
	public static boolean useAv = true;
	public static boolean useImpulse = true;

	public static double yVal = 0.4;
	public static double xzVal = 0.4;
	public static double yMult = 0.1;
	public static double xzMult = 1;

	public CustomDamageEvent(Entity damagee, AttackType attackType, double damage) {
		_attackType = attackType;
		_entity = damagee;
		_ignoreRate = attackType.isIgnoreRate();

		setInitDamage("Initial Damage", damage);
	}

	public static HandlerList getHandlerList() {
		return _handlers;
	}

	public void setCalculateKB(boolean calculate) {
		_calculateKB = calculate;
	}

	private Vector recalculateKnockback()
	{
		switch(kbMode)
		{
		case 0:
			return recalculateKnockbackLib();

		case 1:
			return recalculateKnockbackAdjusted();

		case 2:
			return recKnockbackMyWay();

		default:
			return recalculateKnockbackLib();
		}
	}

	private Vector recKnockbackMyWay()
	{
		Vector vec = new Vector();

		if(getDamager() != null)
		{

			double xOffset = getDamager().getLocation().getX() - getDamagee().getLocation().getX();
			double y = 0.6d;
			double zOffset = getDamager().getLocation().getZ() - getDamagee().getLocation().getZ();

			double dist = xOffset * xOffset + zOffset * zOffset;

			while (!Double.isFinite(dist) || dist < 0.0001)
			{
				xOffset = UtilMath.rr(-0.01, 0.01);
				zOffset = UtilMath.rr(-0.01, 0.01);
				dist = xOffset * xOffset + zOffset * zOffset;
			}

			dist = Math.sqrt(dist);

			if(_calculateKB)
			{
				Vector punch = new Vector();
				double currentY = getDamagee().getVelocity().getY();

				punch.setX(-(xOffset / dist * y));
				punch.setZ(-(zOffset / dist * y));

				if(getDamagee().isOnGround())
				{
					punch.multiply(1.5);
					punch.setY(0.425);
				}
				else
				{
					punch.setY((currentY / 2) + y);
					if(punch.getY() > 0.4)
						punch.setY(0.4);
				}

				vec.add(punch);
			}

			double level = 0;

			for (double value : _knockbackMult.values())
			{
				level += value;
			}

			if(level > 0)
			{
				Entity damager = getDamager();
				Vector kbEnch;

				double xKb = (double) -Math.sin(damager.getLocation().getYaw() * 3.1415927F / 180.0f) * (float) level * 0.5f;
				double yKb = 0.1d;
				double zKb = (double) Math.cos(damager.getLocation().getYaw() * 3.1415927F / 180.0f) * (float) level * 0.5f;

				kbEnch = new Vector(xKb, yKb, zKb);
				vec.add(kbEnch);
			}
		}

		return vec;
	}

	private Vector recalculateKnockbackAdjusted()
	{
		Vector toReturn = _knockback.clone();
		
        if (getDamager() != null)
        {
        	Vector offset;
        	if(getDamager() instanceof Projectile && getAttackType() == AttackType.PROJECTILE)
        	{
        		offset = getDamager().getLocation().getDirection();
        		offset.setZ(-offset.getZ());
        	}
        	else
        	{
        		offset = getDamager().getLocation().toVector().subtract(getDamagee().getLocation().toVector());
        	}

            double xDist = offset.getX();
            double zDist = offset.getZ();

            while (!Double.isFinite(xDist * xDist + zDist * zDist) || xDist * xDist + zDist * zDist < 0.0001)
            {
                xDist = UtilMath.rr(-0.01, 0.01);
                zDist = UtilMath.rr(-0.01, 0.01);
            }

            double dist = Math.sqrt(xDist * xDist + zDist * zDist);

            if (_calculateKB)
            {
                Vector vec = getDamagee().getVelocity();

                vec.setX(vec.getX() / 2);
                vec.setY(vec.getY() / 2);
                vec.setZ(vec.getZ() / 2);

                vec.add(new Vector(-(xDist / dist * xzVal), yVal, -(zDist / dist * xzVal)));

                if(vec.getY() > yVal)
                	vec.setY(yVal);
                
                toReturn.add(vec);
                
                double level = 0;

                for (double value : _knockbackMult.values())
                {
                    level += value;
                }

                if (level != 0)
                {
                	level *= xzMult;
                    level /= 2;

                    Entity damager = getDamager();
    				Vector kbEnch;

    				double xKb = (double) -Math.sin(damager.getLocation().getYaw() * 3.1415927F / 180.0f) * (float) level;
    				double zKb = (double) Math.cos(damager.getLocation().getYaw() * 3.1415927F / 180.0f) * (float) level;

    				kbEnch = new Vector(xKb, yMult, zKb);
    				toReturn.add(kbEnch);
                }
            }
        }

        return toReturn;
	}

	private Vector recalculateKnockbackLib()
    {
        Vector toReturn = _knockback.clone();

        if (getDamager() != null)
        {
            Vector offset = getDamager().getLocation().toVector().subtract(getDamagee().getLocation().toVector());

            double xDist = offset.getX();
            double zDist = offset.getZ();

            while (!Double.isFinite(xDist * xDist + zDist * zDist) || xDist * xDist + zDist * zDist < 0.0001)
            {
                xDist = UtilMath.rr(-0.01, 0.01);
                zDist = UtilMath.rr(-0.01, 0.01);
            }

            double dist = Math.sqrt(xDist * xDist + zDist * zDist);

            if (_calculateKB)
            {
                Vector vec = getDamagee().getVelocity();

                vec.setX(vec.getX() / 2);
                vec.setY(vec.getY() / 2);
                vec.setZ(vec.getZ() / 2);

                vec.add(new Vector(-(xDist / dist * xzVal), yVal, -(zDist / dist * xzVal)));

                toReturn.add(vec);
            }

            double level = 0;

            for (double value : _knockbackMult.values())
            {
                level += value;
            }

            if (level != 0)
            {
            	level *= xzMult;
                level /= 2;

                toReturn.add(new Vector(-(xDist / dist * level), yMult, -(zDist / dist * level)));
            }
        }

        return toReturn;
    }

	public void addDamage(DamageMod damageMod, double damage) {
		removeDamageMod(damageMod);

		_damageMods.add(Pair.of(damageMod, damage));
	}

	public void addKnockback(String name, Vector vector) {
		_knockbackMods.put(name, vector);
	}

	public void addKnockMult(String type, double mod) {
		_knockbackMult.put(type, mod);
	}

	public void addMultiplier(DamageMod damageMod, double mult) {
		if (!_damageMults.isEmpty()) {
			throw new IllegalArgumentException("There is already a damage multiplier! " + _damageMults.iterator().next());
		}

		_damageMults.add(Pair.of(damageMod, mult));
	}

	public void addRunnable(DamageRunnable runnable) {
		_runnables.remove(runnable);

		_runnables.add(runnable);
	}

	public void addRunnables(ArrayList<DamageRunnable> list) {
		_runnables.addAll(list);
	}

	private void calculateArmor() {
		if (!isLivingDamagee() || getAttackType().isIgnoreArmor()) {
			return;
		}

		float armorRating = UtilEnt.getArmorRating(getLivingDamagee());

		addDamage(DamageMod.ARMOR, -(getDamageInternal(false, DamageMod.ARMOR, DamageMod.ARMOR_ENCHANTS) * (armorRating / 25D)));
	}

	private void calculateEnchants() {
		AttackType attack = getAttackType();

		removeDamageMod(DamageMod.ARMOR_ENCHANTS);

		if (isLivingDamagee()) {
			ItemStack[] equips = getLivingDamagee().getEquipment().getArmorContents();

			if (isPlayerDamagee()) {
				equips = getPlayerDamagee().getInventory().getArmorContents();
			}

			float total = 0;
			float totalExplosion = 0;

			for (ItemStack item : equips) {
				if (item == null || item.getType() == Material.AIR) {
					continue;
				}

				Map<Enchantment, Integer> enchants = item.getEnchantments();

				for (Entry<Enchantment, Integer> entry : enchants.entrySet()) {
					Enchantment enchant = entry.getKey();
					int level = entry.getValue();
					float f = (6 + (level * level)) / 3F;

					if (enchant.equals(Enchantment.PROTECTION_ENVIRONMENTAL) && !attack.isIgnoreArmor()) {
						f *= 0.75;
					} else if (enchant.equals(Enchantment.PROTECTION_FIRE) && attack.isBurn()) {
						f *= 1.25;
					} else if (enchant.equals(Enchantment.PROTECTION_EXPLOSIONS) && attack.isExplosion()) {
						f *= 1.5;

						totalExplosion += level;
					} else if (enchant.equals(Enchantment.PROTECTION_PROJECTILE) && attack.isProjectile()) {
						f *= 1.5;
					} else if (enchant.equals(Enchantment.PROTECTION_FALL) && attack.isFall()) {
						f *= 2.5;
					} else {
						f = 0;
					}

					total += f;
				}
			}

			if (total > 20) {
				total = 20;
			}

			addDamage(DamageMod.ARMOR_ENCHANTS, -(getDamageInternal(false, DamageMod.ARMOR_ENCHANTS) * (total / 25D)));

			if (attack.isExplosion() && totalExplosion > 0) {
				// setInitKnockback(getKnockbackStrength() - (totalExplosion * 0.15));
			}
		}

		if (isLivingDamager() && attack.isMelee()) {
			ItemStack item = isPlayerDamager() ? getPlayerDamager().getInventory().getItemInMainHand()
					: getLivingDamager().getEquipment().getItemInMainHand();

			if (item != null && item.getType() != Material.AIR) {
				Map<Enchantment, Integer> enchants = item.getEnchantments();

				for (Entry<Enchantment, Integer> entry : enchants.entrySet()) {
					Enchantment enchant = entry.getKey();
					int level = entry.getValue();

					if (enchant.equals(Enchantment.DAMAGE_ALL)) {
						addDamage(DamageMod.WEAPON_ENCHANTS, 1.25 * level);

						addRunnable(new DamageRunnable("Damage Particles") {
							@Override
							public void run(CustomDamageEvent event2) {
								Location loc = event2.getDamagee().getLocation();

								if (event2.isLivingDamagee()) {
									loc.add(0, event2.getLivingDamagee().getEyeHeight() / 2, 0);
								}

								for (int i = 0; i < 12; i++)
									UtilParticle.playParticle(Particle.CRIT_MAGIC, loc, UtilMath.rr(-1, 1),
											UtilMath.rr(-1, 1), UtilMath.rr(-1, 1));
							}
						});
					} else if (enchant.equals(Enchantment.FIRE_ASPECT)) {
						addRunnable(new DamageRunnable("Fire Aspect") {
							@Override
							public void run(CustomDamageEvent event2) {
								ConditionManager.setFire(getDamagee(), AttackType.FIRE_ASPECT, getFinalDamager(), level * 80,
										false);
							}
						});
					} else if (enchant.equals(Enchantment.KNOCKBACK)) {
						//nerf kb enchants from vanilla op
						addKnockMult("Knockback", level * 0.5);
					}
				}
			}
		}

		if (getAttackType() == AttackType.PROJECTILE && getDamager() instanceof Arrow) {
			if (getDamager().getFireTicks() > 0) {
				addRunnable(new DamageRunnable("Burning Arrow") {
					@Override
					public void run(CustomDamageEvent event2) {
						ConditionManager.setFire(getDamagee(), AttackType.FIRE_BOW, getFinalDamager(), 100, false);
					}
				});
			}

			int knockback = ((Arrow) getDamager()).getKnockbackStrength();

			removeKnockback("Arrow Knockback");

			Vector vec = getFinalKnockback();
			//credit to Onnet - created pythagoras theorem.
			double dist = Math.sqrt(vec.getX() * vec.getX() + vec.getZ() * vec.getZ());

			if (knockback > 0 && dist > 0) {
				Vector newVector = new Vector(vec.getX() * knockback * 0.6 / dist, 0.1, vec.getZ() * knockback * 0.6 / dist);

				addKnockback("Arrow Knockback", newVector);
			}
		}
	}

	public AttackType getAttackType() {
		return _attackType;
	}

	public void setAttackType(AttackType attackType) {
		_attackType = attackType;
	}

	public double getDamage(DamageMod... ignore) {
		calculateArmor();
		calculateEnchants();

		if (_finalDamage != null)
			return _finalDamage;

		return getDamageInternal(true, ignore);
	}

	public Entity getDamagee() {
		return _entity;
	}

	private double getDamageInternal(boolean doMultiplier, DamageMod... ignore) {
		double damage = getInitialDamage();

		loop:

			for (Pair<DamageMod, Double> entry : _damageMods) {
				for (DamageMod ignoreModifier : ignore) {
					if (entry.getKey().isParent(ignoreModifier))
						continue loop;
				}

				damage += entry.getValue();
			}

		if (doMultiplier) {
			loop:

				for (Entry<DamageMod, Double> entry : _damageMults) {
					for (DamageMod ignoreModifier : ignore) {
						if (entry.getKey().isParent(ignoreModifier))
							continue loop;
					}

					damage = damage * entry.getValue();
				}
		}

		return damage;
	}

	public Entity getDamager() {
		return _damager;
	}

	public CustomDamageEvent setDamager(Entity damager) {
		_damager = damager;

		if (damager instanceof Player) {
			_realDamager = damager;
		} else if (damager instanceof Tameable) {
			AnimalTamer tamer = ((Tameable) damager).getOwner();

			if (tamer instanceof Entity)
				_realDamager = (Entity) tamer;
		} else if (damager instanceof Projectile) {
			ProjectileSource entity = ((Projectile) damager).getShooter();

			if (entity instanceof Entity) {
				_realDamager = (Entity) entity;
			}
		}

		return this;
	}

	public Entity getFinalDamager() {
		return _realDamager == null ? _damager : _realDamager;
	}

	public Vector getFinalKnockback() {
		Vector vec = recalculateKnockback();

		for (Vector v : _knockbackMods.values()) {
			vec.add(v);
		}

		if (vec.length() > 0.1 && vec.getY() <= 0.1) {
			vec.setY(0.1);
		}

		return vec;
	}

	@Override
	public HandlerList getHandlers() {
		return _handlers;
	}

	public double getInitialDamage() {
		return _finalDamage == null ? _baseDamage.get(_baseDamage.size() - 1).getValue() : _finalDamage;
	}

	public LivingEntity getLivingDamagee() {
		return getDamagee() instanceof LivingEntity ? (LivingEntity) getDamagee() : null;
	}

	public LivingEntity getLivingDamager() {
		return getDamager() instanceof LivingEntity ? (LivingEntity) getDamager() : null;
	}

	public Player getPlayerDamagee() {
		return getDamagee() instanceof Player ? (Player) getDamagee() : null;
	}

	public Player getPlayerDamager() {
		return getDamager() instanceof Player ? (Player) getDamager() : null;
	}

	public ArrayList<DamageRunnable> getRunnables() {
		return _runnables;
	}

	@Override
	public boolean isCancelled() {
		return _cancelled;
	}

	@Override
	public void setCancelled(boolean cancelled) {
		_cancelled = cancelled;
	}

	public boolean isIgnoreConditions() {
		return _ignoreConditions;
	}

	public void setIgnoreConditions(boolean ignoreConditions) {
		_ignoreConditions = ignoreConditions;
	}

	public boolean isIgnoreRate() {
		return _ignoreRate;
	}

	public CustomDamageEvent setIgnoreRate(boolean ignoreRate) {
		_ignoreRate = ignoreRate;

		return this;
	}

	public boolean isLivingDamagee() {
		return getDamagee() instanceof LivingEntity;
	}

	public boolean isLivingDamager() {
		return getDamager() instanceof LivingEntity;
	}

	public boolean isPlayerDamagee() {
		return getDamagee() instanceof Player;
	}

	public boolean isPlayerDamager() {
		return getDamager() instanceof Player;
	}

	public void printDebug() {
		System.out.println("================");
		System.out.println("Printing CustomDamageEvent");
		System.out.print("Attack: " + getAttackType().getName());
		System.out.print("Damagee: " + getDamagee());
		System.out.print("Damager: " + getDamager());
		System.out.print("Real Damager: " + getFinalDamager());

		for (Pair<String, Double> pair : _baseDamage) {
			System.out.print("Base Damage: " + pair.getKey() + " - " + pair.getValue());
		}

		System.out.println("Final Damage: " + getDamage());

		for (Entry<DamageMod, Double> knockback : _damageMods) {
			System.out.print("Damage Mod: " + knockback.getKey() + ": " + knockback.getValue());
		}

		for (Entry<DamageMod, Double> knockback : _damageMults) {
			System.out.print("Damage Mult: " + knockback.getKey() + ": " + knockback.getValue());
		}

		System.out.println("Knockback: " + _knockback);

		for (Entry<String, Vector> knockback : _knockbackMods.entrySet()) {
			System.out.print("Knockback Mod: " + knockback.getKey() + ": " + knockback.getValue());
		}
	}

	public void removeDamageMod(DamageMod damageMod) {
		Iterator<Pair<DamageMod, Double>> itel = _damageMods.iterator();

		while (itel.hasNext()) {
			Pair<DamageMod, Double> pair = itel.next();

			if (pair.getKey() != damageMod && !pair.getKey().equals(damageMod))
				continue;

			itel.remove();
		}
	}

	public void removeKnockback(String name) {
		_knockbackMods.remove(name);
	}

	public void runRunnables(boolean predamage) {
		for (DamageRunnable runnable : _runnables) {
			try {
				if (predamage != runnable.isPreDamage())
					continue;

				runnable.run(this);
			} catch (Exception ex) {
				UtilError.handle(ex);
			}
		}
	}

	public CustomDamageEvent setDamager(Entity damager, Entity realDamager) {
		setDamager(damager);

		_realDamager = realDamager;

		return this;
	}

	public void setFinalDamage(double finalDamage) {
		_finalDamage = finalDamage;
	}

	public void setInitDamage(String newDamage, double initDamage) {
		_baseDamage.add(Pair.of(newDamage, initDamage));
	}

	public void setKnockback(Vector knockbackDirection) {
		if (!Double.isFinite(knockbackDirection.getX()) || !Double.isFinite(knockbackDirection.getY())
				|| !Double.isFinite(knockbackDirection.getZ())) {
			UtilError.handle(new Exception("Illegal double"));
			return;
		}

		_knockback = knockbackDirection;
		_calculateKB = false;
	}

	public void setRealDamager(Entity entity) {
		_realDamager = entity;
	}

	public static abstract class DamageRunnable {
		private String _name;

		public DamageRunnable(String name) {
			_name = name;
		}

		public boolean isPreDamage() {
			return true;
		}

		@Override
		public boolean equals(Object object) {
			return object instanceof DamageRunnable && ((DamageRunnable) object).getName().equals(getName());
		}

		public String getName() {
			return _name;
		}

		public abstract void run(CustomDamageEvent event2);
	}

}

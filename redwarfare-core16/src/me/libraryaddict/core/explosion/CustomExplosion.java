package me.libraryaddict.core.explosion;

import me.libraryaddict.core.damage.AttackType;
import me.libraryaddict.core.damage.CustomDamageEvent;
import me.libraryaddict.core.damage.DamageManager;
import me.libraryaddict.core.hologram.Hologram;
import me.libraryaddict.core.utils.UtilPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.item.enchantment.ProtectionEnchantment;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Bukkit;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_17_R1.block.CraftBlock;
import org.bukkit.craftbukkit.v1_17_R1.event.CraftEventFactory;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import com.google.common.collect.Sets;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.function.Predicate;

public class CustomExplosion extends Explosion
{
	private AttackType _attackType;
	private float _blockExplosionSize;
	private boolean _createFire;
	private float _damage;
	private boolean _damageBlocks = true;
	private boolean _damageBlocksEqually;
	private boolean _dropItems = true;
	private boolean _ignoreNonLiving;
	private boolean _ignoreRate = true;
	private float _maxDamage = 1000;
	private org.bukkit.entity.LivingEntity _owner;
	private AttackType _selfAttackType;
	private float _size;
	private boolean _useCustomDamage;
	//private World _world;
	private Level _world;
	private org.bukkit.World _bukkitWorld;
	private double posX, posY, posZ;
	private Location _loc;
	private ExplosionDamageCalculator explosionDamageCalculator = new ExplosionDamageCalculator();
	
	public CustomExplosion(Location loc, float explosionSize, AttackType attackType) {
		//    super(((CraftWorld) loc.getWorld()).getHandle(), null, loc.getX(), loc.getY(), loc.getZ(), explosionSize, false,
		//            false);
		
		super(((CraftWorld) loc.getWorld()).getHandle(), null, null, null, loc.getX(), loc.getY(), loc.getZ(),
				explosionSize, false, Explosion.BlockInteraction.NONE);
		
		posX = loc.getX();
		posY = loc.getY();
		posZ = loc.getZ();
		_loc = loc;
		
		_world = ((CraftWorld) loc.getWorld()).getHandle();
		_bukkitWorld = loc.getWorld();
		_blockExplosionSize = _size = explosionSize;
		_attackType = attackType;
		_selfAttackType = attackType;
	}
	
	public CustomExplosion(Location loc, float explosionSize, AttackType attackType, AttackType selfAttackType) {
		this(loc, explosionSize, attackType);
		
		_selfAttackType = selfAttackType;
		_bukkitWorld = loc.getWorld();
		_loc = loc;
	}
	
	//from NMS Explosion
	@Override
	public void explode() {
		this._world.gameEvent(this.source, GameEvent.EXPLODE, new BlockPos(this.posX, this.posY, this.posZ));
		Set<BlockPos> set = Sets.newHashSet();
		boolean flag = true;
		
		int i;
		int j;
		for(int k = 0; k < 16; ++k) {
			for(i = 0; i < 16; ++i) {
				for(j = 0; j < 16; ++j) {
					if (k == 0 || k == 15 || i == 0 || i == 15 || j == 0 || j == 15) {
						double d0 = (double)((float)k / 15.0F * 2.0F - 1.0F);
						double d1 = (double)((float)i / 15.0F * 2.0F - 1.0F);
						double d2 = (double)((float)j / 15.0F * 2.0F - 1.0F);
						double d3 = Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
						d0 /= d3;
						d1 /= d3;
						d2 /= d3;
						float f = this._blockExplosionSize * (0.7F + this._world.random.nextFloat() * 0.6F);
						double d4 = this.posX;
						double d5 = this.posY;
						double d6 = this.posZ;
						
						for(float var21 = 0.3F; f > 0.0F; f -= 0.22500001F) {
							BlockPos blockposition = new BlockPos(d4, d5, d6);
							BlockState iblockdata = this._world.getBlockState(blockposition);
							FluidState fluid = this._world.getFluidState(blockposition);
							if (!this._world.isInWorldBounds(blockposition)) {
								break;
							}
							
							Optional<Float> optional = this.explosionDamageCalculator.
									getBlockExplosionResistance(this, this._world, blockposition, iblockdata, fluid);
							if (optional.isPresent()) {
								f -= ((Float)optional.get() + 0.3F) * 0.3F;
							}
							
							if (f > 0.0F && this.explosionDamageCalculator.shouldBlockExplode(
									this, this._world, blockposition, iblockdata, f)) {
								set.add(blockposition);
							}
							
							d4 += d0 * 0.30000001192092896D;
							d5 += d1 * 0.30000001192092896D;
							d6 += d2 * 0.30000001192092896D;
						}
					}
				}
			}
		}
		
		this.getToBlow().addAll(set);
		float f2 = this._size * 2.0F;
		i = Mth.floor(this.posX - (double)f2 - 1.0D);
		j = Mth.floor(this.posX + (double)f2 + 1.0D);
		int l = Mth.floor(this.posY - (double)f2 - 1.0D);
		int i1 = Mth.floor(this.posY + (double)f2 + 1.0D);
		int j1 = Mth.floor(this.posZ - (double)f2 - 1.0D);
		int k1 = Mth.floor(this.posZ + (double)f2 + 1.0D);
		List<Entity> list = this._world.getEntities(this.source, new AABB((double)i, (double)l, (double)j1, (double)j, (double)i1, (double)k1));
		Vec3 vec3d = new Vec3(this.posX, this.posY, this.posZ);
		
		for(int l1 = 0; l1 < list.size(); ++l1) {
			Entity entity = (Entity)list.get(l1);
			
			if (!(entity.getBukkitEntity() instanceof LivingEntity) && _ignoreNonLiving)
				continue;
			
			double d7 = (double) (Math.sqrt(entity.distanceToSqr(vec3d)) / this._size);
			
			if (d7 <= 1.0D) {
				double d8 = entity.getX() - this.posX;
				double d9 = (entity instanceof PrimedTnt ? entity.getY() : entity.getEyeY()) - this.posY;
				double d10 = entity.getZ() - this.posZ;
				double d11 = Math.sqrt(d8 * d8 + d9 * d9 + d10 * d10);
				
				if (d11 != 0.0D) {
					d8 /= d11;
					d9 /= d11;
					d10 /= d11;
					
					//performs a raytrace that determines the percentage of solid blocks between the two
					double d12 = (double) getSeenPercent(vec3d, entity);
					double d13 = (1.0D - d7) * d12;
					float damage;
					
					if (_useCustomDamage) {
						damage = Math.max(0, (int) ((_damage * d12) * (d11 / _size)));
					} else {
						damage = (int) ((d13 * d13 + d13) / 2.0D * 8.0D * this._size + 1.0D);
						damage = Math.min(damage, _maxDamage);
					}
					
					if(entity.getBukkitEntity() instanceof Damageable) {
						
						DamageManager manager = ExplosionManager.explosionManager.getDamageManager();
						
						CustomDamageEvent event = manager.createEvent(entity.getBukkitEntity(),
								entity.getBukkitEntity() == _owner ? _selfAttackType : _attackType,
								damage, _owner);
						
						event.setIgnoreRate(_ignoreRate);
						
						Vector vec = new Vector(d8 * d13, d9 * d13, d10 * d13);
						
						event.setKnockback(vec);
						manager.callDamage(event);
					} else {
						CraftEventFactory.entityDamage = this.source;
						entity.hurt(DamageSource.explosion(this), damage);
						CraftEventFactory.entityDamage = null;
					}
					
					if(((entity instanceof net.minecraft.world.entity.player.Player)) &&
							(!((net.minecraft.world.entity.player.Player) entity).getAbilities().invulnerable)) {
						this.getHitPlayers().put((net.minecraft.world.entity.player.Player) entity,
								new Vec3(d8 * d13, d9 * d13, d10 * d13));
					}
				}
			}
		}
	}
	
	//used in CustomExplosion.explode()
	public void newExplode()
	{
		//create ArrayList of entities that will be involved in this explosion
		ArrayList<org.bukkit.entity.Entity> entities = new ArrayList<org.bukkit.entity.Entity>();
		
		//check if every entity in the world is inside the explosion. if they are, add them to
		//the ArrayList entities
		for(org.bukkit.entity.Entity entity : _bukkitWorld.getEntities())
		{
			double eX = entity.getLocation().getX();
			double eY = entity.getLocation().getY();
			double eZ = entity.getLocation().getZ();
			
			double radius = (double) _size;
			
			if(eX >= posX - radius && eX <= posX + radius)
			{
				if(eY >= posY - radius && eY <= posY + radius)
				{
					if(eZ >= posZ - radius && eZ <= posZ + radius)
					{
						entities.add(entity);
					}
				}
			}
		}
		
		//for every entity inside the explosion
		//if there are blocks between the explosion and victim, do nothing (they're protected from the explosion)
		//otherwise TODO the rest of this method
		for(org.bukkit.entity.Entity damagee : entities)
		{
			Vector direction = damagee.getLocation().add(0.0, 0.2, 0.0).toVector().subtract(_loc.toVector());
			
			Vector headDirection;
			
			if(damagee instanceof LivingEntity)
			{
				LivingEntity living = (LivingEntity) damagee;
				headDirection = living.getLocation().add(0.0, living.getEyeHeight(), 0.0).toVector()
						.subtract(_loc.toVector());
			}
			else
			{
				headDirection = damagee.getLocation().add(0.0, damagee.getHeight(), 0.0).toVector()
						.subtract(_loc.toVector());
			}
			
			
			//make if it hits the head or the body then print it hit that part respectively
			
			RayTraceResult rayTrace = _bukkitWorld.rayTrace(_loc, direction, _size,
					FluidCollisionMode.NEVER, true, 0, Predicate.isEqual(damagee));
			
			RayTraceResult rayTraceHead = _bukkitWorld.rayTrace(_loc, headDirection, _size,
					FluidCollisionMode.NEVER, true, 0, Predicate.isEqual(damagee));
			
			if (damagee instanceof Player)
			{
				Bukkit.broadcastMessage(damagee.getName());
			}
			else
			{
				Bukkit.broadcastMessage(damagee.getType().toString());
			}
			
			if(rayTrace != null && damagee instanceof Player)
			{
				if(rayTrace.getHitEntity() != null)
				{
					if(rayTrace.getHitEntity().equals(damagee))
						Bukkit.broadcastMessage("Hit damagee's feet");
				}
				else if(rayTrace.getHitBlock() != null)
				{
					Bukkit.broadcastMessage("Feet hit block" + rayTrace.getHitBlock().getType().toString());
				}
			}
			else if(rayTrace == null)
			{
				Bukkit.broadcastMessage("Feet raytrace null");
			}
			
			if(rayTraceHead != null && damagee instanceof Player)
			{
				if(rayTraceHead.getHitEntity() != null)
				{
					if(rayTraceHead.getHitEntity().equals(damagee))
						Bukkit.broadcastMessage("Hit damagee's head");
				}
				else if(rayTraceHead.getHitBlock() != null)
				{
					Bukkit.broadcastMessage("Head hit block" + rayTraceHead.getHitBlock().getType().toString());
				}
			}
			else if(rayTraceHead == null)
			{
				Bukkit.broadcastMessage("Head raytrace null");
			}
    		
    		/*
    		if(rayTrace != null && damagee instanceof Player && rayTrace.getHitEntity() != null)
    		{
    			if( rayTrace.getHitEntity().equals(damagee))
    			{
	    			//Bukkit.broadcastMessage(rayTrace.toString());
	    			Bukkit.broadcastMessage("Hit player " + damagee.getName());
	    			Bukkit.broadcastMessage("Hit position: " + rayTrace.getHitPosition().toString());
	    			Bukkit.broadcastMessage("Damagee position: " + damagee.getLocation().toVector().toString());
	    			Bukkit.broadcastMessage("Player/hit position diff: " + damagee.getLocation().toVector()
	    					.subtract(rayTrace.getHitPosition()).toString());
	    			counter++;
	    			Bukkit.broadcastMessage("------------" + counter);
    			}
    		}
    		else if (rayTrace != null && damagee instanceof Player && rayTrace.getHitBlock() != null)
    		{
    			Bukkit.broadcastMessage("Hit block " + rayTrace.getHitBlock().getType().toString());
    			Bukkit.broadcastMessage("Blockface: " + rayTrace.getHitBlockFace().toString());
    			Bukkit.broadcastMessage("Hit position: " + rayTrace.getHitPosition().toString());
    			Bukkit.broadcastMessage("------------" + counter);
    		}
    		else if (damagee instanceof Player)
    		{
    			Bukkit.broadcastMessage("raytrace is null");
    		}	*/
		}
		
		//    	for(org.bukkit.entity.Entity e : entities)
		//    	{
		//    		if(e instanceof Player)
		//    		{
		//    			Bukkit.broadcastMessage(e.getName());
		//    		}
		//    		else
		//    		{
		//    			Bukkit.broadcastMessage(e.getType().toString());
		//    		}
		//    	}
		
		
	}
	
	@Override
	public void finalizeExplosion(boolean flag) {
		Location loc = new Location(_world.getWorld(), posX, posY, posZ);
		
		_world.getWorld().playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 4F,
				(1.0F + (this._world.random.nextFloat() - this._world.random.nextFloat()) * 0.2F) * 0.7F);
		
		if ((this._blockExplosionSize >= 2.0F) && (this._damageBlocks)) {
			_world.getWorld().spawnParticle(org.bukkit.Particle.EXPLOSION_LARGE, loc, 50);
		} else {
			_world.getWorld().spawnParticle(org.bukkit.Particle.EXPLOSION_NORMAL, loc, 50);
		}
		
		if (_damageBlocks) {
			org.bukkit.World bworld = this._world.getWorld();
			
			ArrayList<org.bukkit.block.Block> blockList = new ArrayList<>();
			
			for (int i1 = this.getToBlow().size() - 1; i1 >= 0; i1--) {
				BlockPos cpos = this.getToBlow().get(i1);
				
				org.bukkit.block.Block bblock = bworld.getBlockAt(cpos.getX(), cpos.getY(), cpos.getZ());
				
				if (bblock.getType() != org.bukkit.Material.AIR) {
					blockList.add(bblock);
				}
			}
			
			ExplosionEvent event = new ExplosionEvent(this, blockList);
			
			this._world.getCraftServer().getPluginManager().callEvent(event);
			
			//this.getBlocks().clear();
			this.clearToBlow();
			
			for (org.bukkit.block.Block bblock : event.getBlocks()) {
				BlockPos coords = new BlockPos(bblock.getX(), bblock.getY(), bblock.getZ());
				this.getToBlow().add(coords);
			}
			
			if (getToBlow().isEmpty()) {
				this.wasCanceled = true;
				return;
			}
			
			//            if (_fallingBlockExplosion)
			//            {
			//            	Collection<org.bukkit.block.Block> blocks = event.getBlocks();
			//
			//            	if (blocks.size() > _maxFallingBlocks)
			//            	{
			//            		blocks = new ArrayList<org.bukkit.block.Block>(blocks);
			//
			//            		Collections.shuffle((ArrayList) blocks);
			//
			//            		int toRemove = blocks.size() - _maxFallingBlocks;
			//
			//            		for (int i = 0; i < toRemove; i++)
			//            		{
			//            			blocks.remove(0);
			//            		}
			//            	}
			//
			//            	_explosion.BlockExplosion(blocks, new Location(_world.getWorld(), posX, posY, posZ), false, false);
			//            } end of comment block here
			
			for (BlockPos blockposition : this.getToBlow()) {
				//								getType
				BlockState block = this._world.getBlockState(blockposition);
				
				if (flag) {
					double d0 = blockposition.getX() + this._world.random.nextFloat();
					double d1 = blockposition.getY() + this._world.random.nextFloat();
					double d2 = blockposition.getZ() + this._world.random.nextFloat();
					double d3 = d0 - this.posX;
					double d4 = d1 - this.posY;
					double d5 = d2 - this.posZ;
					double d6 = Math.sqrt(d3 * d3 + d4 * d4 + d5 * d5);
					
					d3 /= d6;
					d4 /= d6;
					d5 /= d6;
					double d7 = 0.5D / (d6 / this._blockExplosionSize + 0.1D);
					
					d7 *= (this._world.random.nextFloat() * this._world.random.nextFloat() + 0.3F);
					d3 *= d7;
					d4 *= d7;
					d5 *= d7;
					bworld.spawnParticle(org.bukkit.Particle.EXPLOSION_NORMAL, (d0 + this.posX) / 2.0D,
							(d1 + this.posY) / 2.0D, (d2 + this.posZ) / 2.0D, 1, d3, d4, d5);
					//                    this._world.addParticle(EnumParticle.EXPLOSION_NORMAL, (d0 + this.posX * 1.0D) / 2.0D,
					//                            (d1 + this.posY * 1.0D) / 2.0D, (d2 + this.posZ * 1.0D) / 2.0D, d3, d4, d5, new int[0]);
					bworld.spawnParticle(org.bukkit.Particle.SMOKE_NORMAL, d0, d1, d2, 1, d3, d4, d5);
					//                    this._world.addParticle(EnumParticle.SMOKE_NORMAL, d0, d1, d2, d3, d4, d5, new int[0]);
				}
				
				if (block.getMaterial() != Material.AIR) {
					if (block.getBlock().dropFromExplosion(this) && _dropItems) {
						CraftBlock.at(this._world, blockposition).breakNaturally();
						//                        block.getBlock().dropNaturally(this._world, blockposition, this._world.getType(blockposition),
						//                                _blockExplosionSize, 0);
					}
					
					this._world.setBlock(blockposition, Blocks.AIR.defaultBlockState(), 3);
					block.getBlock().wasExploded(this._world, blockposition, this);
				}
			}
		}
		
		if (this._createFire) {
			Random random = new Random();
			for (BlockPos blockposition : this.getToBlow()) {
				if (this._world.getBlockState(blockposition).isAir() && random.nextInt(3) == 0 &&
						// what the fuck is i
						// copied from 1.16 Explosion
						this._world.getBlockState(blockposition.down()).isSolidRender(this._world, blockposition.down())) {
					if (!CraftEventFactory.callBlockIgniteEvent(this._world, blockposition.getX(), blockposition.getY(),
							blockposition.getZ(), this).isCancelled())
						this._world.setBlockAndUpdate(blockposition, Blocks.FIRE.defaultBlockState());
				}
			}
		}
	}
	
	public CustomExplosion customExplode() {
        /*new BukkitRunnable()
        {
        	long started = System.currentTimeMillis();

        	public void run()
        	{
        		if (UtilTime.elasped(started, 20000))
        		{
        			this.cancel();
        		}

        		UtilParticle.playParticle(ParticleType.FLAME, new Location(_world.getWorld(), posX, posY, posZ));
        	}
        }.runTaskTimer(Bukkit.getPluginManager().getPlugins()[0], 0, 5);*/
		// Explode
		//newExplode();
		//a();
		//a(true);
		//newExplode();
		explode();
		finalizeExplosion(true);
		
		return this;
	}
	
	public float getSize() {
		return _size;
	}
	
	public void setAttackType(AttackType attackType) {
		_attackType = attackType;
	}
	
	public CustomExplosion setBlockExplosionSize(float explosionSize) {
		_blockExplosionSize = explosionSize;
		
		return this;
	}
	
	public CustomExplosion setBlocksDamagedEqually(boolean damageEqually) {
		_damageBlocksEqually = damageEqually;
		
		return this;
	}
	
	public CustomExplosion setDamageBlocks(boolean damageBlocks) {
		_damageBlocks = damageBlocks;
		
		return this;
	}
	
	public CustomExplosion setDamager(org.bukkit.entity.Player player) {
		_owner = player;
		
		return this;
	}
	
	public CustomExplosion setDropItems(boolean dropItems) {
		_dropItems = dropItems;
		
		return this;
	}
	
	/**
	 * Center of explosion does this much damage
	 */
	public CustomExplosion setExplosionDamage(float damage) {
		_damage = damage;
		_useCustomDamage = true;
		
		return this;
	}
	
	public CustomExplosion setIgnoreNonLiving(boolean ignoreNonLiving) {
		_ignoreNonLiving = ignoreNonLiving;
		
		return this;
	}
	
	public CustomExplosion setIgnoreRate(boolean ignoreRate) {
		_ignoreRate = ignoreRate;
		
		return this;
	}
	
	public void setIncinderary(boolean fire) {
		_createFire = fire;
	}
	
	public CustomExplosion setMaxDamage(float maxDamage) {
		_maxDamage = maxDamage;
		
		return this;
	}
}

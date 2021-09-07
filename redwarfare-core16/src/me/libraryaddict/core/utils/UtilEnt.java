package me.libraryaddict.core.utils;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.comphenix.protocol.wrappers.WrappedDataWatcher.WrappedDataWatcherObject;

import me.libraryaddict.core.Pair;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Tag;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Ladder;
import org.bukkit.block.data.type.TrapDoor;
import org.bukkit.entity.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class UtilEnt {
    public static PacketContainer getInvisPacket(Entity entity) {
        return getInvisPacket(entity.getEntityId(), new WrappedDataWatcher(entity));
    }

    public static PacketContainer getInvisPacket(int entityId) {
        return getInvisPacket(entityId, new WrappedDataWatcher());
    }

    private static final int INDEX = MetaIndex.ENTITY_META.getIndex();
    public static PacketContainer getInvisPacket(int entityId, WrappedDataWatcher watcher) {
        byte original = watcher.hasIndex(INDEX) ? watcher.getByte(INDEX) : (byte) 0;
        watcher.setObject(MetaIndex.ENTITY_META.getIndex(), WrappedDataWatcher.Registry.get(Byte.class),
                (byte) (original | 32));

        PacketContainer container = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.ENTITY_METADATA);
        container.getIntegers().write(0, entityId);
        container.getWatchableCollectionModifier().write(0, watcher.getWatchableObjects());

        return container;
    }

    public static EnumWrappers.ItemSlot getOtherEnum(EquipmentSlot slot) {
        switch (slot) {
            case HAND:
                return EnumWrappers.ItemSlot.MAINHAND;
            case OFF_HAND:
                return EnumWrappers.ItemSlot.OFFHAND;
            case HEAD:
                return EnumWrappers.ItemSlot.HEAD;
            case CHEST:
                return EnumWrappers.ItemSlot.CHEST;
            case LEGS:
                return EnumWrappers.ItemSlot.LEGS;
            case FEET:
                return EnumWrappers.ItemSlot.FEET;
        }
        return null;
    }
    
    public static PacketContainer getEquipmentPacket(Entity entity, List<Pair<EquipmentSlot, ItemStack>> list) {
        return getEquipmentPacket(entity.getEntityId(), list);
    }

    public static PacketContainer getEquipmentPacket(int entityId, List<Pair<EquipmentSlot, ItemStack>> list) {
        PacketContainer container = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.ENTITY_EQUIPMENT);
        container.getIntegers().write(0, entityId);
        container.getSlotStackPairLists().write(0, list.stream()
                .map(pair -> new com.comphenix.protocol.wrappers.Pair<>(getOtherEnum(pair.getKey()), pair.getValue()))
                .collect(Collectors.toList()));

        return container;
    }

    public static PacketContainer getEquipmentPacket(Entity entity, EquipmentSlot slot, ItemStack stack) {
        return getEquipmentPacket(entity.getEntityId(), slot, stack);
    }

    public static PacketContainer getEquipmentPacket(int entityId, EquipmentSlot slot, ItemStack stack) {
        PacketContainer container = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.ENTITY_EQUIPMENT);
        container.getIntegers().write(0, entityId);
        //protocollib throwing exception trying to use non-existent empty constructor for SingletonList
        //container.getSlotStackPairLists().write(0,
        //        Collections.singletonList(new com.comphenix.protocol.wrappers.Pair<>(getOtherEnum(slot), stack))
        //);
    
        ArrayList<com.comphenix.protocol.wrappers.Pair<EnumWrappers.ItemSlot, ItemStack>> list = new ArrayList();
        list.add(new com.comphenix.protocol.wrappers.Pair<>(getOtherEnum(slot), stack));
        container.getSlotStackPairLists().write(0, list);
        
        return container;
    }
    
    public static PacketContainer getMetadataPacket(Entity entity)
    {
    	//https://www.spigotmc.org/threads/entitymetadata-packet-via-protocollib-modifies-entity-on-server.512100/
    	WrappedDataWatcher metadata = WrappedDataWatcher.getEntityWatcher(entity);
    	PacketContainer packet = new PacketContainer(PacketType.Play.Server.ENTITY_METADATA);
    	packet.getIntegers().write(0, entity.getEntityId());
    	packet.getWatchableCollectionModifier().write(0, metadata.getWatchableObjects());
    	
    	return packet;
    }

    public static double getAbsorptionHearts(Entity entity) {
        if (!(entity instanceof LivingEntity))
            return 0;

        return ((LivingEntity) entity).getAbsorptionAmount();
    }

    public static int getArmorRating(LivingEntity entity) {
        return (int) entity.getAttribute(Attribute.GENERIC_ARMOR).getValue();
    }

    public static Arrow.PickupStatus getArrowPickupStatus(Entity entity) {
        if (!(entity instanceof Arrow))
            return null;

        return ((Arrow) entity).getPickupStatus();
    }

    //removed because they suck
    /*public static Pair<Entity, Block> getHit(Entity movingEntity) {
        return getHit(movingEntity, null);
    }	*/

    //this is 1.16.4 and filled with errors.
    /*public static Pair<Entity, Block> getHit(Entity movingEntity, Predicate<Entity> predicate) {
        net.minecraft.server.v1_16_R3.Entity nms = ((CraftEntity) movingEntity).getHandle();
        
        Vec3D motion = nms.getMot();
        Vec3D vec3d = new Vec3D(nms.locX(), nms.locY(), nms.locZ());
        Vec3D vec3d1 = new Vec3D(nms.locX() + motion.getX(), nms.locY() + motion.getY(), nms.locZ() + motion.getZ());
        MovingObjectPosition movingobjectposition = nms.world.rayTrace(vec3d, vec3d1);

        vec3d = new Vec3D(nms.locX, nms.locY, nms.locZ);
        vec3d1 = new Vec3D(nms.locX + nms.motX, nms.locY + nms.motY, nms.locZ + nms.motZ);

        if (movingobjectposition != null) {
            vec3d1 = new Vec3D(movingobjectposition.pos.x, movingobjectposition.pos.y, movingobjectposition.pos.z);
        }

        net.minecraft.server.v1_16_R3.Entity entity = null;
        List list = nms.world.getEntities(nms, nms.getBoundingBox().a(nms.motX, nms.motY, nms.motZ).g(1.0D));

        double d5 = 0.0D;

        for (int j = 0; j < list.size(); j++) {
            net.minecraft.server.v1_16_R3.Entity entity1 = (net.minecraft.server.v1_16_R3.Entity) list.get(j);

            if (predicate != null ? predicate.apply(entity1.getBukkitEntity()) :
                    (entity1.isInteractable() || entity1 instanceof EntityItem)) {
                AxisAlignedBB axisalignedbb = entity1.getBoundingBox().g(0.30000001192092896D);

                MovingObjectPosition movingobjectposition1 = axisalignedbb.b(vec3d, vec3d1);

                if (movingobjectposition1 != null) {
                    double d6 = vec3d.distanceSquared(movingobjectposition1.pos);
                    if ((d6 < d5) || (d5 == 0.0D)) {
                        entity = entity1;
                        d5 = d6;
                    }
                }
            }
        }

        Entity hitEntity = entity != null ? entity.getBukkitEntity() : null;
        Block block = movingobjectposition != null && movingobjectposition.a() != null ? movingEntity.getWorld()
                .getBlockAt(movingobjectposition.a().getX(), movingobjectposition.a().getY(),
                        movingobjectposition.a().getZ()) : null;

        return Pair.of(hitEntity, block);
    }	*/

    public static String getName(Entity entity) {
        if (entity == null)
            return "Unknown";

        if (entity instanceof Player)
            return entity.getName();

        if (entity.getCustomName() != null)
            return entity.getCustomName();

        return entity.getType().getKey().getKey();
    }

    @SuppressWarnings("deprecation")
    public static int getNewEntityId() {
        return Bukkit.getUnsafe().nextEntityId();
    }


    public static PotionEffect getPotion(Entity entity, PotionEffectType potionType) {
        if (!(entity instanceof LivingEntity))
            return null;

        return ((LivingEntity) entity).getPotionEffect(potionType);
    }

    public static double heal(Entity entity, double health) {
        if (!(entity instanceof Damageable)) {
            return 0;
        }

        Damageable ent = (Damageable) entity;

        double toHeal = ent.getMaxHealth() - ent.getHealth();

        toHeal = Math.min(toHeal, health);

        ent.setHealth(ent.getHealth() + toHeal);

        return toHeal;
    }

    public static boolean isGrounded(Entity entity) {
        return entity.isOnGround();

        /*for (Block block : UtilBlock.getBlocks(entity.getLocation().subtract(0.3, 0.2, 0.3),
                entity.getLocation().add(0.3, 0, 0.3)))
        {
        	if (!UtilBlock.nonSolid(block))
        		return true;
        }*/
    }

    public static void setAbsorptionHearts(LivingEntity entity, double hearts) {
        entity.setAbsorptionAmount(hearts);
    }

    // this method isn't used at all
//    public static void setArrowLived(Entity entity, int lived) {
//        if (!(entity instanceof Arrow))
//            return;
//
//        try {
//            EntityArrow arrow = ((CraftArrow) entity).getHandle();
//
//            Field field = EntityArrow.class.getDeclaredField("despawnCounter");
//            field.setAccessible(true);
//
//            field.setInt(arrow, lived);
//        } catch (Exception ex) {
//            UtilError.handle(ex);
//        }
//    }

    public static void setArrowPickupStatus(Entity entity, Arrow.PickupStatus newStatus) {
        if (!(entity instanceof Arrow))
            return;

        ((Arrow) entity).setPickupStatus(newStatus);
    }

    public static void velocity(Entity ent, Vector vec, boolean groundBoost) {
        if (isGrounded(ent) && groundBoost && vec.getY() <= 0.01) {
            vec.setY(0.2);
        }

        if (!Double.isFinite(vec.getX()) || !Double.isFinite(vec.getY()) || !Double.isFinite(vec.getZ())) {
            UtilError.handle(new Exception("Illegal double"));
            return;
        }

        if (ent instanceof LivingEntity) {
            // vec = reduceVelocity((LivingEntity) ent, vec.getX(), vec.getY(), vec.getZ());
        }

        ent.setVelocity(vec);
    }

    // copy the nms isClimbing method to assist future migration
    public static boolean isClimbing(LivingEntity entity) {
        if (entity instanceof Player && ((Player) entity).getGameMode() == GameMode.SPECTATOR) {
            return false;
        } else {
            Block block = entity.getLocation().getBlock();
            if (Tag.CLIMBABLE.isTagged(block.getType())) {
                return true;
            } else if (block.getState() instanceof TrapDoor) {
                TrapDoor trapdoor = (TrapDoor) block.getState();
                if (trapdoor.isOpen() && block.getRelative(BlockFace.DOWN).getState() instanceof Ladder) {
                    Ladder ladder = (Ladder) block.getRelative(BlockFace.DOWN).getState();
                    return ladder.getFacing() == trapdoor.getFacing();
                }
            }
        }
        return false;
    }

}

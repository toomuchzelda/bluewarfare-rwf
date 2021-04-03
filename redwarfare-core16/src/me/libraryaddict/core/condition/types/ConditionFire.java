package me.libraryaddict.core.condition.types;

import me.libraryaddict.core.damage.AttackType;
import org.bukkit.entity.Entity;

public class ConditionFire extends Condition {
    public ConditionFire(Entity entity, AttackType attackType, Entity cause, int expires) {
        super("Fire", entity, attackType, cause, expires);
    }

    @Override
    public boolean hasExpired() {
        return super.hasExpired() || getVictim().getFireTicks() == 0;
    }

}

package me.libraryaddict.core.condition;

import me.libraryaddict.core.condition.types.Condition;
import org.bukkit.entity.Entity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ConditionEvent extends Event implements Cancellable {
    private static final HandlerList _handlers = new HandlerList();
    private boolean _cancelled;
    private Condition _condition;
    private Entity _victim;

    public ConditionEvent(Entity entity, Condition condition) {
        _victim = entity;
        _condition = condition;
    }

    public static HandlerList getHandlerList() {
        return _handlers;
    }

    public Condition getCondition() {
        return _condition;
    }

    @Override
    public HandlerList getHandlers() {
        return _handlers;
    }

    public Entity getVictim() {
        return _victim;
    }

    @Override
    public boolean isCancelled() {
        return _cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        _cancelled = cancel;
    }

}

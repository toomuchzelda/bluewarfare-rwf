package me.libraryaddict.core.server;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class MergeEvent extends Event implements Cancellable {
    private static final HandlerList _handlers = new HandlerList();
    private boolean _cancelled;

    public static HandlerList getHandlerList() {
        return _handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return _handlers;
    }

    @Override
    public boolean isCancelled() {
        return _cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        _cancelled = cancelled;
    }
}

package me.libraryaddict.core.explosion;

import org.bukkit.block.Block;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.ArrayList;

public class ExplosionEvent extends Event implements Cancellable {
    private static final HandlerList _handlers = new HandlerList();
    private ArrayList<Block> _blocks = new ArrayList<Block>();
    private boolean _cancelled;
    private CustomExplosion _explosion;

    public ExplosionEvent(CustomExplosion explosion, ArrayList<Block> blocks) {
        _explosion = explosion;
        _blocks = blocks;
    }

    public static HandlerList getHandlerList() {
        return _handlers;
    }

    public ArrayList<Block> getBlocks() {
        return _blocks;
    }

    public CustomExplosion getExplosion() {
        return _explosion;
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
    public void setCancelled(boolean cancel) {
        _cancelled = cancel;
    }

}

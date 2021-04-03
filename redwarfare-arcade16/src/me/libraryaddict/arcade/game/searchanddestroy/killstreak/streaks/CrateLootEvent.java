package me.libraryaddict.arcade.game.searchanddestroy.killstreak.streaks;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;

public class CrateLootEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private boolean _cancelled;
    private ArrayList<ItemStack> _loot;
    private Player _player;

    public CrateLootEvent(Player player, ArrayList<ItemStack> items) {
        _player = player;
        _loot = items;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public ArrayList<ItemStack> getLoot() {
        return _loot;
    }

    public Player getPlayer() {
        return _player;
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

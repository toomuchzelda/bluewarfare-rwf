package me.libraryaddict.core.player.events;

import me.libraryaddict.network.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Notifies listeners to store their data before the player quits
 */
public class PlayerUnloadEvent extends Event {
    private static final HandlerList _handlers = new HandlerList();
    private ConcurrentLinkedQueue _pending = new ConcurrentLinkedQueue();
    private Player _player;
    private PlayerData _playerData;

    public PlayerUnloadEvent(Player player, PlayerData data) {
        _player = player;
        _playerData = data;
    }

    public static HandlerList getHandlerList() {
        return _handlers;
    }

    public PlayerData getData() {
        return _playerData;
    }

    @Override
    public HandlerList getHandlers() {
        return _handlers;
    }

    public Player getPlayer() {
        return _player;
    }

    public boolean isPending() {
        return _pending.peek() != null;
    }

    public void registerIntent(Object obj) {
        _pending.add(obj);
    }

    public void removeIntent(Object obj) {
        _pending.remove(obj);
    }
}

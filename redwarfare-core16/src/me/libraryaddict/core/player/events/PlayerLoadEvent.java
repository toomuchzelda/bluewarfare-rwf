package me.libraryaddict.core.player.events;

import me.libraryaddict.network.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerLoadEvent extends Event {
    private static final HandlerList _handlers = new HandlerList();
    private Player _player;
    private PlayerData _playerData;

    public PlayerLoadEvent(Player player, PlayerData data) {
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
}

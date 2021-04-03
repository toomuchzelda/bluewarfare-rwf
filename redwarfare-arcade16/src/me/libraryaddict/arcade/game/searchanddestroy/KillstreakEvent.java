package me.libraryaddict.arcade.game.searchanddestroy;

import me.libraryaddict.arcade.events.DeathEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class KillstreakEvent extends Event {
    private static final HandlerList _handlers = new HandlerList();
    private boolean _assist;
    private DeathEvent _deathEvent;
    private int _newKillstreak;
    private Player _player;

    public KillstreakEvent(Player player, DeathEvent event, int newKillstreak, boolean assist) {
        _player = player;
        _newKillstreak = newKillstreak;
        _deathEvent = event;
        _assist = assist;
    }

    public static HandlerList getHandlerList() {
        return _handlers;
    }

    public DeathEvent getDeath() {
        return _deathEvent;
    }

    @Override
    public HandlerList getHandlers() {
        return _handlers;
    }

    public int getKillstreak() {
        return _newKillstreak;
    }

    public Player getPlayer() {
        return _player;
    }

    public boolean isAssist() {
        return _assist;
    }

}

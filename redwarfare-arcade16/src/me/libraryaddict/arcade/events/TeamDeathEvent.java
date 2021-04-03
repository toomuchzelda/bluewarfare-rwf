package me.libraryaddict.arcade.events;

import me.libraryaddict.arcade.game.GameTeam;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class TeamDeathEvent extends Event implements Cancellable {
    private static final HandlerList _handlers = new HandlerList();
    private boolean _cancelled;
    private GameTeam _team;

    public TeamDeathEvent(GameTeam team) {
        _team = team;
    }

    public static HandlerList getHandlerList() {
        return _handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return _handlers;
    }

    public GameTeam getTeam() {
        return _team;
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

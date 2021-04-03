package me.libraryaddict.arcade.events;

import me.libraryaddict.arcade.managers.GameState;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class GameStateEvent extends Event {
    private static final HandlerList _handlers = new HandlerList();
    private GameState _gameState;

    public GameStateEvent(GameState newGameState) {
        _gameState = newGameState;
    }

    public static HandlerList getHandlerList() {
        return _handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return _handlers;
    }

    public GameState getState() {
        return _gameState;
    }
}

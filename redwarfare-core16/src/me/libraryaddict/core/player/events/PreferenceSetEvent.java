package me.libraryaddict.core.player.events;

import me.libraryaddict.network.Pref;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PreferenceSetEvent<T> extends Event {
    private static final HandlerList _handlers = new HandlerList();
    private Player _player;
    private Pref<T> _preference;
    private T _value;

    public PreferenceSetEvent(Player player, Pref<T> pref, T newValue) {
        _player = player;
        _preference = pref;
        _value = newValue;
    }

    public static HandlerList getHandlerList() {
        return _handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return _handlers;
    }

    public Player getPlayer() {
        return _player;
    }

    public Pref<T> getPreference() {
        return _preference;
    }

    public T getValue() {
        return _value;
    }

}
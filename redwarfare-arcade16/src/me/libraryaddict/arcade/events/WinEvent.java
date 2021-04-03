package me.libraryaddict.arcade.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.ArrayList;
import java.util.UUID;

public class WinEvent extends Event {
    private static final HandlerList _handlers = new HandlerList();
    private ArrayList<UUID> _losers;
    private ArrayList<UUID> _winners;

    public WinEvent(ArrayList<UUID> winners, ArrayList<UUID> losers) {
        _winners = winners;
        _losers = losers;
    }

    public static HandlerList getHandlerList() {
        return _handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return _handlers;
    }

    public ArrayList<UUID> getLosers() {
        return _losers;
    }

    public ArrayList<UUID> getWinners() {
        return _winners;
    }
}

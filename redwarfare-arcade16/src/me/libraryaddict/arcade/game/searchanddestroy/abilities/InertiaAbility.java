package me.libraryaddict.arcade.game.searchanddestroy.abilities;

import me.libraryaddict.arcade.kits.Ability;
import me.libraryaddict.core.time.TimeEvent;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.UUID;

public class InertiaAbility extends Ability {
    private HashMap<UUID, Vector> _movement = new HashMap<UUID, Vector>();
    private HashMap<UUID, Location> _lastLocation = new HashMap<UUID, Location>();


    @EventHandler
    public void onTick(TimeEvent event) {
        for (Player player : getPlayers(true)) {

        }
    }
}

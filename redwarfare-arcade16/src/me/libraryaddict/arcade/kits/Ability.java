package me.libraryaddict.arcade.kits;

import me.libraryaddict.arcade.game.Game;
import me.libraryaddict.arcade.managers.ArcadeManager;
import me.libraryaddict.core.utils.UtilPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import com.comphenix.protocol.reflect.FieldAccessException;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

public abstract class Ability implements Listener {
    private Kit _kit;

    public Game getGame() {
        return getManager().getGame();
    }

    public Kit getKit() {
        return _kit;
    }

    public void setKit(Kit owningKit) {
        _kit = owningKit;
    }

    public ArcadeManager getManager() {
        return _kit.getManager();
    }

    public ArrayList<Player> getPlayers() {
        ArrayList<Player> players = new ArrayList<Player>();

        for (Player player : UtilPlayer.getPlayers()) {
            if (!hasAbility(player))
                continue;

            players.add(player);
        }

        return players;
    }

    public ArrayList<Player> getPlayers(boolean alive) {
        ArrayList<Player> players = new ArrayList<Player>();

        for (Player player : getGame().getPlayers(alive)) {
            if (!hasAbility(player))
                continue;

            players.add(player);
        }

        return players;
    }

    public JavaPlugin getPlugin() {
        return getManager().getPlugin();
    }

    public boolean hasAbility(Player player) {
        return player != null && _kit.usingKit(player);
    }

    public boolean isAlive(Entity entity) {
        return getManager().getGame().isAlive(entity);
    }

    public boolean isLive() {
        return getGame().isLive();
    }

    public void registerAbility() {
    }
    
    //for giving abilities per player, for respawn-enabled games (koth)
    public void giveAbility(Player player) throws InvocationTargetException, FieldAccessException {
    }
    
    //same as above, for removing
    public void removeAbility(Player player) {
    }
    
    public void unregisterAbility() {
    }
}

package me.libraryaddict.arcade.game.searchanddestroy.killstreak;

import me.libraryaddict.arcade.game.searchanddestroy.KillstreakEvent;
import me.libraryaddict.arcade.game.searchanddestroy.SearchAndDestroy;
import me.libraryaddict.arcade.game.searchanddestroy.killstreak.streaks.*;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

import java.util.ArrayList;

public class KillstreakManager {
    private ArrayList<StreakBase> _killstreaks = new ArrayList<StreakBase>();
    private SearchAndDestroy _searchAndDestroy;

    public KillstreakManager(SearchAndDestroy searchAndDestroy) {
        _searchAndDestroy = searchAndDestroy;

        _killstreaks.add(new CompassKillstreak(searchAndDestroy));
        _killstreaks.add(new SupplyCrateKillstreak(searchAndDestroy));
        _killstreaks.add(new WolvesKillstreak(searchAndDestroy));
        _killstreaks.add(new IronGolemKillstreak(searchAndDestroy));
        _killstreaks.add(new NapalmKillstreak(searchAndDestroy));
    }

    public void onKillstreak(KillstreakEvent event) {
        Player player = event.getPlayer();

        if (!_searchAndDestroy.isAlive(player))
            return;

        int kills = event.getKillstreak();

        for (StreakBase killstreak : _killstreaks) {
            if (killstreak.hasKillstreak(kills)) {
                killstreak.giveKillstreak(player, kills);
            }
        }
    }

    public void register() {
        for (StreakBase killstreak : _killstreaks) {
            _searchAndDestroy.registerListener(killstreak);
        }
    }

    public void unregister() {
        for (StreakBase killstreak : _killstreaks) {
            HandlerList.unregisterAll(killstreak);
        }
    }

}

package me.libraryaddict.core.preference.commands;

import me.libraryaddict.core.command.SimpleCommand;
import me.libraryaddict.core.preference.PreferenceManager;
import me.libraryaddict.core.rank.Rank;
import me.libraryaddict.core.ranks.PlayerRank;
import org.bukkit.entity.Player;

import java.util.Collection;

public class CommandPreference extends SimpleCommand {
    private PreferenceManager _preferences;

    public CommandPreference(PreferenceManager preferences) {
        super(new String[]
                {
                        "preferences", "preference", "prefs", "pref"
                }, Rank.ALL);

        _preferences = preferences;
    }

    @Override
    public void onTab(Player player, PlayerRank rank, String[] args, String token, Collection<String> completions) {
    }

    @Override
    public void runCommand(Player player, PlayerRank rank, String alias, String[] args) {
        _preferences.openInventory(player);
    }

}

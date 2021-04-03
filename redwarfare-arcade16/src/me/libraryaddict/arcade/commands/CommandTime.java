package me.libraryaddict.arcade.commands;

import me.libraryaddict.arcade.game.Game;
import me.libraryaddict.arcade.managers.GameManager;
import me.libraryaddict.core.C;
import me.libraryaddict.core.command.SimpleCommand;
import me.libraryaddict.core.rank.Rank;
import me.libraryaddict.core.ranks.PlayerRank;
import org.bukkit.entity.Player;

import java.util.Collection;

public class CommandTime extends SimpleCommand {
    private GameManager _gameManager;

    public CommandTime(GameManager gameManager) {
        super("time", Rank.ALL);

        _gameManager = gameManager;
    }

    @Override
    public void onTab(Player player, PlayerRank rank, String[] args, String token, Collection<String> completions) {
    }

    @Override
    public void runCommand(Player player, PlayerRank rank, String alias, String[] args) {
        Game game = _gameManager.getGame();

        if (!game.isLive()) {
            player.sendMessage(C.Red + "The game isn't in progress");
            return;
        }

        game.sendTimeProgress(player);
    }

}

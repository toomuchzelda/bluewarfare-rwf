package me.libraryaddict.arcade.commands;

import me.libraryaddict.arcade.game.GameTeam;
import me.libraryaddict.arcade.managers.ArcadeManager;
import me.libraryaddict.core.C;
import me.libraryaddict.core.command.SimpleCommand;
import me.libraryaddict.core.rank.Rank;
import me.libraryaddict.core.ranks.PlayerRank;
import me.libraryaddict.core.utils.UtilInv;
import me.libraryaddict.core.utils.UtilItem;
import me.libraryaddict.core.utils.UtilNumber;
import me.libraryaddict.core.utils.UtilPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

public class CommandGiveItem extends SimpleCommand {
    private ArcadeManager _arcadeManager;

    public CommandGiveItem(ArcadeManager arcadeManager) {
        super(new String[]
                {
                        "give", "item", "i"
                }, Rank.OWNER);

        _arcadeManager = arcadeManager;
    }

    @Override
    public void onTab(Player player, PlayerRank rank, String[] args, String token, Collection<String> completions) {
        if (rank.hasRank(Rank.OWNER) && args.length == 0) {
            completions.addAll(getPlayers(token));

            if ("all".startsWith(token.toLowerCase())) {
                completions.add("All");
            }
        }

        if (args.length > 1)
            return;

        List<String> items = UtilItem.getCompletions(token, false);

        completions.addAll(items);
    }

    @Override
    public void runCommand(Player player, PlayerRank rank, String alias, String[] args) {
        if (args.length == 0) {
            player.sendMessage(C.Red + "/" + alias + " <ItemStack>");

            if (rank.hasRank(Rank.OWNER)) {
                player.sendMessage(C.Red + "/" + alias + " <Player> <ItemStack>");
            }

            return;
        }

        Predicate<Player> toReceive = null;

        if (args[0].equalsIgnoreCase("All")) {
            toReceive = p -> true;
        } else if (args.length > 1) {

            for (GameTeam team : _arcadeManager.getGame().getTeams()) {
                if (!team.getName().split(" ")[0].equalsIgnoreCase(args[0])) {
                    toReceive = arg -> team.isInTeam(arg) && team.isAlive(arg);
                    break;
                }
            }

            if (toReceive == null) {
                Player p = Bukkit.getPlayer(args[0]);
                if (p != null)
                    toReceive = Predicate.isEqual(p);
            }
        }

        if (toReceive != null)
            args = Arrays.copyOfRange(args, 1, args.length);
        else
            toReceive = Predicate.isEqual(player);

        if (args.length > 3) {
            player.sendMessage(C.Red + "Too many arguments were given!");
            return;
        }

        Material item = UtilItem.getItem(args[0]);
        int amount;

        if (item == null) {
            player.sendMessage(C.Red + "Unable to find the item " + args[0]);
            return;
        }

        if (args.length > 1) {
            if (!UtilNumber.isParsableInt(args[1])) {
                player.sendMessage(C.Red + "Cannot parse '" + args[1] + "' to a number!");
                return;
            } else {
                amount = Integer.parseInt(args[1]);
            }
        } else {
            amount = item.getMaxStackSize();
        }

        amount = Math.min(item.getMaxStackSize() * 36, amount);

        ItemStack itemstack = new ItemStack(item, amount);

        int people = 0;

        for (Player p : UtilPlayer.getPlayers()) {
            if (!toReceive.test(p))
                continue;

            UtilInv.addItem(p, itemstack);

            people++;

            p.sendMessage(C.Blue + "Given " + UtilItem.getName(itemstack) + " x " + amount
                    + (player != toReceive ? " by " + player.getName() : ""));
        }

        player.sendMessage(C.Blue + "Given " + people + " people " + UtilItem.getName(itemstack) + " x " + amount);
    }

}

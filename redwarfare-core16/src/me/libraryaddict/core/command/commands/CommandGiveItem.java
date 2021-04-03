package me.libraryaddict.core.command.commands;

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

public class CommandGiveItem extends SimpleCommand {
    public CommandGiveItem() {
        super(new String[]
                {
                        "give", "item", "i"
                }, Rank.OWNER);
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

        boolean all = args[0].equalsIgnoreCase("All");

        Player toReceive = args.length > 1 ? Bukkit.getPlayer(args[0]) : null;

        if (toReceive == null && !all)
            toReceive = player;
        else
            args = Arrays.copyOfRange(args, 1, args.length);

        if (toReceive != player && !rank.hasRank(Rank.OWNER)) {
            player.sendMessage(C.Red + "You are not allowed to do that");
            return;
        }

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

        if (all) {
            for (Player p : UtilPlayer.getPlayers()) {
                UtilInv.addItem(p, itemstack);

                if (p == player)
                    continue;

                p.sendMessage(C.Blue + "Given " + UtilItem.getName(itemstack) + " x " + amount);
            }

            player.sendMessage(C.Blue + "Given everyone " + UtilItem.getName(itemstack) + " x " + amount);
        } else {
            UtilInv.addItem(toReceive, itemstack);

            toReceive.sendMessage(C.Blue + "Given " + UtilItem.getName(itemstack) + " x " + amount
                    + (player != toReceive ? " by " + player.getName() : ""));

            if (player != toReceive) {
                player.sendMessage(C.Blue + "Given " + toReceive.getName() + " " + UtilItem.getName(itemstack) + " x " + amount);
            }
        }
    }

}

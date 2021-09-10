package me.libraryaddict.arcade.game.searchanddestroy.abilities;

import me.libraryaddict.arcade.Arcade;
import me.libraryaddict.arcade.events.EquipmentEvent;
import me.libraryaddict.arcade.game.Game;
import me.libraryaddict.arcade.game.GameTeam;
import me.libraryaddict.arcade.game.TeamGame;
import me.libraryaddict.arcade.game.searchanddestroy.kits.KitGhost;
import me.libraryaddict.arcade.game.searchanddestroy.kits.KitSpy;
import me.libraryaddict.arcade.game.searchanddestroy.kits.KitWraith;
import me.libraryaddict.arcade.kits.Ability;
import me.libraryaddict.arcade.kits.Kit;
import me.libraryaddict.core.C;
import me.libraryaddict.core.chat.ChatEvent;
import me.libraryaddict.core.scoreboard.FakeScoreboard;
import me.libraryaddict.core.scoreboard.FakeTeam;
import net.minecraft.server.level.ServerPlayer;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.scoreboard.Team.Option;
import org.bukkit.scoreboard.Team.OptionStatus;

import com.google.common.base.Predicate;

import java.util.ArrayList;
import java.util.logging.Level;

public class SpyAbility extends Ability {
	@EventHandler(priority = EventPriority.MONITOR)
	public void onChat(ChatEvent event) {
		Player player = event.getPlayer();

		GameTeam team = getGame().getTeam(player);

		if (team == null)
			return;

		if (hasAbility(player)) {
			event.setCancelled(true);

			Bukkit.getLogger().log(Level.INFO, event.getFinalUncensored());

			for (Player receiver : event.getRecipients()) {
				GameTeam hisTeam = getGame().getTeam(receiver);

				event.setDisplayName((hisTeam != null ? hisTeam : team).getColoring() + player.getName());

				if (receiver == event.getPlayer()) {
					receiver.sendMessage(event.getFinalUncensored());
				} else {
					receiver.sendMessage(event.getFinalCensored());
				}
			}
		} else {
			event.setDisplayName(team.getColoring() + event.getDisplayName());
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onHatEvent(EquipmentEvent event) {
		if (!hasAbility(event.getWearer()))
			return;

		ItemStack item = ((TeamGame) getGame()).getCosmeticGearItem(event.getViewer(), getGame().getTeam(event.getViewer()),
				event.getSlot());

		if (item == null)
			return;

		if (event.getSlot() == EquipmentSlot.FEET && item.getItemMeta() instanceof LeatherArmorMeta) {
			LeatherArmorMeta meta = (LeatherArmorMeta) item.getItemMeta();

			Color color = meta.getColor();
			meta.setColor(color.mixColors(color, color, color, Color.GRAY));

			item.setItemMeta(meta);
		}

		event.setHat(item);
	}

	//main spy scoreboard registering done in SearchAndDestroy.setupScoreboards()
	// this is for players respawning that have picked kit spy and weren't before
	// this method assumes everything is setup already by that^ method
	public void giveAbility(Player player)
	{
		Game game = getManager().getGame();

		//put them on special spy team visible to each team, except their own!
		for(GameTeam observerTeam : game.getTeams())
		{
			FakeScoreboard board = game.getScoreboard().getScoreboard(observerTeam.getName());
			//for(GameTeam renderedTeam : game.getTeams())
			//{
			//spies shouldn't be on same spy team as the team of the scoreboard
			if(game.getTeam(player) != observerTeam)
			{
				board.getTeam(observerTeam.getName() + "Spy").addPlayer(player);
			}
			//}
		}
	}

	public void removeAbility(Player player)
	{
		Game game = getManager().getGame();

		for(GameTeam observerTeam : game.getTeams())
		{
			//FakeScoreboard board = game.getScoreboard().getScoreboard(observerTeam.getName());
			FakeScoreboard board = game.getScoreboard().getScoreboard(observerTeam.getName());
			//FakeTeam team = board.getTeam(observerTeam.getName() + "Spy");
			//team.removePlayer(player.getName());
			//FakeTeam realTeam = board.getTeam(observerTeam.getName());
			//realTeam.addPlayer(player);

			//remove them from all other team scoreboard spyTeams (They shouldn't be in their own
			// scoreboard team's spyTeam)
			if(game.getTeam(player) != observerTeam)
				board.getTeam(observerTeam.getName() + "Spy").removePlayer(player.getName());

			//re-add them to all team scoreboard's realTeams
			for(GameTeam renderedTeam : game.getTeams())
			{
				FakeTeam realTeam = board.getTeam(renderedTeam.getName());
				realTeam.addPlayer(player);
			}
		}
	}
}

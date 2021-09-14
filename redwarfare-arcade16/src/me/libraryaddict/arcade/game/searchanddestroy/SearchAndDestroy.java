package me.libraryaddict.arcade.game.searchanddestroy;

import com.comphenix.protocol.ProtocolLib;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.google.common.base.Predicate;
import me.libraryaddict.arcade.events.DeathEvent;
import me.libraryaddict.arcade.events.GameStateEvent;
import me.libraryaddict.arcade.events.TeamDeathEvent;
import me.libraryaddict.arcade.game.GameOption;
import me.libraryaddict.arcade.game.GameTeam;
import me.libraryaddict.arcade.game.TeamGame;
import me.libraryaddict.arcade.game.searchanddestroy.abilities.GhostAbility;
import me.libraryaddict.arcade.game.searchanddestroy.killstreak.KillstreakManager;
import me.libraryaddict.arcade.game.searchanddestroy.kits.*;
import me.libraryaddict.arcade.kits.Ability;
import me.libraryaddict.arcade.kits.Kit;
import me.libraryaddict.arcade.managers.ArcadeManager;
import me.libraryaddict.arcade.managers.GameManager;
import me.libraryaddict.arcade.managers.GameState;
import me.libraryaddict.core.C;
import me.libraryaddict.core.ServerType;
import me.libraryaddict.core.damage.AttackType;
import me.libraryaddict.core.data.TeamSettings;
import me.libraryaddict.core.inventory.utils.ItemBuilder;
import me.libraryaddict.core.map.WorldData;
import me.libraryaddict.core.scoreboard.FakeScoreboard;
import me.libraryaddict.core.scoreboard.FakeTeam;
import me.libraryaddict.core.time.TimeEvent;
import me.libraryaddict.core.time.TimeType;
import me.libraryaddict.core.utils.*;
import me.libraryaddict.core.utils.UtilParticle.ViewDist;
import me.libraryaddict.disguise.LibsDisguises;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Team.Option;
import org.bukkit.scoreboard.Team.OptionStatus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class SearchAndDestroy extends TeamGame {
	private ArrayList<TeamBomb> _bombs = new ArrayList<TeamBomb>();
	private long _deathTimer = 600000;
	private KillstreakManager _killstreakManager;
	private long _lastDeath;
	private ArrayList<GameTeam> _lastManStanding = new ArrayList<GameTeam>();
	private int _poisonStage;
	private AttackType BOMB_EXPLODE = new AttackType("Bomb Exploded", "%Killed% was caught in the explosion of their bomb")
			.setInstantDeath();
	private AttackType END_OF_GAME = new AttackType("End of Game", "%Killed% was unable of escape the strings of time")
			.setIgnoreArmor().setNoKnockback();
	
	//changed in registerBombs()/registerHills()
	private SNDMapType _mapType = SNDMapType.SND;
	private ArrayList<Hill> _hills = new ArrayList<Hill>();
	private Hill _activeHill = null;
	private HashMap<GameTeam, Integer> _kothScore = new HashMap<>();
	private boolean randomHillOrder = false;
	
	public SearchAndDestroy(ArcadeManager arcadeManager) {
		super(arcadeManager, ServerType.SearchAndDestroy);
		
		setKits(new KitTrooper(), new KitBerserker(), new KitDemolitions(), new KitDwarf(), new KitExplosive(),
				new KitGhost(getManager().getPlugin()), new KitJuggernaut(getManager().getPlugin()), new KitLongbow(),
				new KitMedic(), new KitPyro(), new KitRewind(), new KitShortbow(), /*new KitSpy(),*/ new KitTeleporter(),
				new KitVampire(), new KitVenom(), new KitWarper(), new KitNinja(), new KitFrost(), new KitSkinner(),
				new KitWraith(getManager().getPlugin()), new KitBleeder(), new KitHealer(),
				new KitSans(), new KitSacrificial(), new KitNaruto());
		
		_killstreakManager = new KillstreakManager(this);
		
		setOption(GameOption.STEAK_HEALTH, 8D);
		setOption(GameOption.HATS, true);
		setOption(GameOption.INFORM_KILL_ASSIST, true);
		setOption(GameOption.TABLIST_KILLS, true);
		setOption(GameOption.COLOR_CHAT_NAMES, false);
	}
	
	private void checkLastMan() {
		new BukkitRunnable() {
			public void run() {
				if (!isLive())
					return;
				
				for (GameTeam team : getTeams()) {
					if (_lastManStanding.contains(team))
						continue;
					
					ArrayList<Player> players = team.getPlayers(true);
					
					if (players.size() != 1)
						continue;
					
					_lastManStanding.add(team);
					
					Player player = players.get(0);
					
					for (ItemStack item : UtilInv.getNonClonedInventory(player)) {
						if (item.getType() != Material.BLAZE_POWDER)
							continue;
						
						FuseType.BOMB_ARMING.setLevel(item, 10);
					}
					
					player.updateInventory();
					
					Announce(team.getColoring() + player.getName() + " is last man standing!");
				}
			}
		}.runTask(getManager().getPlugin());
	}
	
	public void drawScoreboard() {
		if(_mapType == SNDMapType.SND)
			drawSNDScoreboard();
		else if(_mapType == SNDMapType.KOTH)
			drawKothScoreboard();
	}
	
	private void drawSNDScoreboard()
	{
		FakeScoreboard board = getManager().getScoreboard().getMainScoreboard();
		
		ArrayList<GameTeam> teams = getTeams(true);
		
		Collections.sort(teams, GameTeam.COMPARE_PLAYERS);
		
		ArrayList<String> lines = new ArrayList<String>();
		
		Iterator<GameTeam> itel = teams.iterator();
		Iterator<TeamBomb> bombItel = getBombs().stream().filter((bomb) -> !bomb.isOwned()).iterator();
		
		while (itel.hasNext()) {
			GameTeam team = itel.next();
			
			lines.add(team.getColoring() + C.Bold + team.getName());
			
			lines.add(team.getPlayers(true).size() + " alive");
			
			for (TeamBomb bomb : getBombs()) {
				if (!bomb.isOwned() || bomb.getTeam() != team) {
					continue;
				}
				
				if (bomb.isArmed()) {
					String disarm = bomb.getDisarmStatus();
					
					if (disarm == null) {
						lines.add(team.getColoring() + "Bomb " + C.Bold + bomb.getTimeLeft());
					} else {
						lines.add(team.getColoring() + disarm + " " + team.getColoring() + C.Bold + bomb.getTimeLeft());
					}
				} else {
					lines.add("Bomb is Safe");
				}
			}
			
			if (itel.hasNext() || bombItel.hasNext())
				lines.add("");
		}
		
		while (bombItel.hasNext()) {
			TeamBomb bomb = bombItel.next();
			
			if (!bomb.isArmed())
				lines.add(C.Bold + "Nuke");
			else
				lines.add(bomb.getTeam().getColoring() + C.Bold + "Nuke " + bomb.getTimeLeft());
		}
		
		if (lines.size() > 15) {
			while (lines.contains(""))
				lines.remove("");
		}
		
		if (!lines.isEmpty() && lines.get(lines.size() - 1).equals(""))
			lines.remove(lines.size() - 1);
		
		board.setSidebar(lines);
	}
	
	private void drawKothScoreboard()
	{
		FakeScoreboard board = getManager().getScoreboard().getMainScoreboard();
		
		ArrayList<GameTeam> teams;
		if(_mapType == SNDMapType.KOTH)
			teams = getTeams();
		else
			teams = getTeams(true);
		
		Collections.sort(teams, GameTeam.COMPARE_PLAYERS);
		
		ArrayList<String> lines = new ArrayList<String>();
		
		Iterator<GameTeam> itel = teams.iterator();
		//Iterator<TeamBomb> bombItel = getBombs().stream().filter((bomb) -> !bomb.isOwned()).iterator();
		
		while (itel.hasNext()) {
			GameTeam team = itel.next();
			
			lines.add(team.getColoring() + C.Bold + team.getName());
			//C.Reset + ": " + team.getPlayers().size() + " players");
			int score = _kothScore.get(team);
			lines.add(team.getColoring() + C.Bold + "Score: " + score / 20);
			lines.add(team.getPlayers().size() + " players");
			
			
			//approx. appearance
			/*
			 * Team (Bold, coloured in team colour)
			 * Score: (team's score) (Bold, team coloured)
			 * (amount of players) players
			 * ....repeat for each team
			 */
			
			if (itel.hasNext())
				lines.add("");
		}
		
		lines.add(C.Bold + "Active Hill: " + _activeHill.getName());
		int timeLeft;
		if(isLive())
			timeLeft = _activeHill.getHillTime() - getGameTime();
		else
			timeLeft = _activeHill.getHillTime();
		lines.add(C.Bold + "Time Left: " + C.Yellow + (timeLeft));
		
		if (lines.size() > 15) {
			while (lines.contains(""))
				lines.remove("");
		}
		
		if (!lines.isEmpty() && lines.get(lines.size() - 1).equals(""))
			lines.remove(lines.size() - 1);
		
		board.setSidebar(lines);
	}
	
	public ArrayList<TeamBomb> getBombs() {
		return _bombs;
	}
	
	@Override
	public int getCreditsKill() {
		return 0;
	}
	
	@Override
	public int getCreditsLose() {
		return 1;
	}
	
	@Override
	public int getCreditsWin() {
		return 3;
	}
	
	public boolean isEndGame() {
		return UtilTime.elasped(getStateChanged(), _deathTimer + 60000);
	}
	
	@EventHandler
	public void onBombInteract(PlayerInteractEntityEvent event) {
		if (!isLive())
			return;
		
		Player player = event.getPlayer();
		
		if (!isAlive(player))
			return;
		
		for (TeamBomb bomb : getBombs()) {
			if (!bomb.isArmed())
				continue;
			
			if (bomb.getBomb() != event.getRightClicked()) {
				continue;
			}
			
			bomb.onInteract(player, UtilInv.getHolding(player, Material.BLAZE_POWDER));
		}
	}
	
	@EventHandler
	public void onBombInteract(PlayerInteractEvent event) {
		if (!isLive())
			return;
		
		if (event.useItemInHand() == Result.DENY)
			return;
		
		if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
			return;
		
		Player player = event.getPlayer();
		
		if (!isAlive(player))
			return;
		
		for (TeamBomb bomb : getBombs()) {
			if (!bomb.getBlock().equals(event.getClickedBlock())) {
				continue;
			}
			
			bomb.onInteract(player, event.getItem());
		}
	}
	
	@EventHandler
	public void onBombTick(TimeEvent event) {
		if (event.getType() != TimeType.TICK)
			return;
		
		if (!isLive())
			return;
		
		Collections.sort(getBombs(), new Comparator<TeamBomb>() {
			
			@Override
			public int compare(TeamBomb o1, TeamBomb o2) {
				return Long.compare(o1.getFused(), o2.getFused());
			}
		});
		
		ArrayList<TeamBomb> toCheck = new ArrayList<TeamBomb>(getBombs());
		
		while (!toCheck.isEmpty()) {
			TeamBomb bomb = toCheck.remove(0);
			
			if (!getBombs().contains(bomb))
				continue;
			
			bomb.tickBomb();
			
			if (!bomb.isArmed())
				continue;
			
			if (bomb.getTimeLeft() > 0)
				continue;
			
			onExplode(bomb);
		}
	}
	
	@EventHandler
	public void onDeath(DeathEvent event) {
		checkLastMan();
		
		_lastDeath = System.currentTimeMillis();
	}
	
	public void onExplode(TeamBomb teamBomb) {
		Location loc = teamBomb.getBomb().getLocation();
		
		loc.getWorld().playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 10000, 0);
		UtilParticle.playParticle(Particle.EXPLOSION_HUGE, loc, ViewDist.LONGER);
		
		Iterator<TeamBomb> itel = getBombs().iterator();
		
		while (itel.hasNext()) {
			TeamBomb bomb = itel.next();
			
			if (bomb.getTeam() != teamBomb.getTeam())
				continue;
			
			if (!bomb.isOwned() && teamBomb.isOwned()) {
				bomb.restore();
				continue;
			}
			
			bomb.remove();
			itel.remove();
		}
		
		for (Block block : UtilBlock.getBlocks(teamBomb.getBlock().getLocation().add(0.5, 0.5, 0.5), 6)) {
			Material mat = block.getType();
			
			//teleporter block?
			if (mat == Material.ACACIA_LOG)
				continue;
			
			if (UtilBlock.solid(mat))
				block.setType(Material.COAL_BLOCK);
			else if (mat.name().contains("SLAB") || mat.name().contains("STEP")) {
				block.setType(Material.BLACKSTONE_SLAB);
			}
		}
		
		if (teamBomb.isOwned())
			Announce(teamBomb.getTeam().getColoring() + teamBomb.getTeam().getName() + "'s " + C.Gold + "bomb exploded!");
		else
			Announce(teamBomb.getTeam().getColoring() + teamBomb.getTeam().getName() + "'s " + C.Gold
					+ "nuke exploded! Everyone but them annihilated!");
		
		setOption(GameOption.DEATH_MESSAGES, false);
		
		for (Player player : (teamBomb.isOwned() ? teamBomb.getTeam().getPlayers(true)
				: getPlayers(true).stream().filter((player) -> !teamBomb.getTeam().isInTeam(player))
				.collect(Collectors.toList()))) {
			getDamageManager().newDamage(player, BOMB_EXPLODE, 0);
		}
		
		setOption(GameOption.DEATH_MESSAGES, true);
	}
	
	@EventHandler
	public void onGameEnd(GameStateEvent event) {
		if (event.getState() != GameState.End && event.getState() != GameState.Dead)
			return;
		
		_killstreakManager.unregister();
	}
	
	@EventHandler
	public void onGameStart(GameStateEvent event) {
		if (event.getState() != GameState.Live)
			return;
		
		_lastDeath = System.currentTimeMillis();
		
		if (UtilMath.r(5) == 0)
			setOption(GameOption.TIME_OF_WORLD, 15000L);
		
		for (Player player : UtilPlayer.getPlayers()) {
			GameTeam team = getTeam(player);
			
			player.sendMessage(C.Gold + "You are in " + team.getColoring() + team.getName());
		}
		
		for (GameTeam team : getTeams()) {
			Announce(C.Gold + "There are " + team.getPlayers().size() + " player" + (team.getPlayers().size() == 1 ? "" : "s")
					+ " in " + team.getColoring() + team.getName());
		}
		
		if(_mapType == SNDMapType.KOTH)
			Announce(C.Gold + "The Hill is at " + _activeHill.getName());
	}
	
	@EventHandler
	public void onKillstreak(KillstreakEvent killstreakEvent) {
		_killstreakManager.onKillstreak(killstreakEvent);
	}
	
	@EventHandler
	public void onPoison(TimeEvent event) {
		if (event.getType() != TimeType.SEC) {
			return;
		}
		
		if (!isLive()) {
			return;
		}
		
		if(_mapType == SNDMapType.KOTH)
			return;
		
		if (_poisonStage == 0 && !UtilTime.elasped(getStateChanged(), _deathTimer)) {
			if (System.currentTimeMillis() - _lastDeath > 90000) {
				_deathTimer -= 3000;
			}
		}
		
		if (_poisonStage == 0 && UtilTime.elasped(getStateChanged(), _deathTimer)) {
			_poisonStage++;
			
			Announce(C.Red + "One minute until players start dying!");
			
			_deathTimer = System.currentTimeMillis() - getStateChanged();
		} else if (_poisonStage >= 1 && isEndGame()) {
			if (_poisonStage == 1) {
				_poisonStage++;
				
				Announce(C.Red + "Don't say I didn't warn you!");
				
				for (Player player : getPlayers(true)) {
					UtilInv.remove(player, Material.GOLDEN_APPLE);
					UtilInv.remove(player, Material.COOKED_BEEF);
				}
			}
			
			ArrayList<GameTeam> teams = new ArrayList<GameTeam>(getTeams());
			
			Collections.shuffle(teams);
			
			for (GameTeam team : teams) {
				for (Player player : team.getPlayers(true)) {
					double most = 0;
					
					double extraDamage = Math.max(0, ((System.currentTimeMillis() - getStateChanged()) - (60000 * 11)) / 120000D)
							+ 0.75;
					
					for (TeamBomb bomb : getBombs()) {
						if (bomb.getTeam() != team || !bomb.isOwned()) {
							continue;
						}
						
						double healthToTake = extraDamage;
						
						if (bomb.getBlock().getLocation().add(0.5, 0.5, 0.5).distance(player.getLocation()) < 15) {
							healthToTake += UtilMath.rr(1);
						} else {
							healthToTake += UtilMath.rr(0.5);
						}
						
						if (healthToTake <= most)
							continue;
						
						most = healthToTake;
					}
					
					most *= Math.max(player.getMaxHealth(), 20) / 20;
					
					getManager().getDamage().newDamage(player, END_OF_GAME, most);
					
					player.setNoDamageTicks(0);
				}
			}
		}
	}
	
	@EventHandler
	public void onKothTick(TimeEvent event)
	{
		if (event.getType() != TimeType.TICK)
			return;
		
		if(_mapType == SNDMapType.KOTH)
		{
			//show where hills are before game started
			if (!isLive())
			{
				for(Hill h : _hills)
				{
					h.drawParticles(Color.WHITE);
				}
				return;
			}
			
			Logger logger = ArcadeManager.getManager().getPlugin().getLogger();
			//logger.info("koth ticking...");
			
			//check if active hill has changed
			if (getGameTime() >= _activeHill.getHillTime())
			{
				_activeHill.setDone();
				_activeHill.setActiveHill(false);
				boolean noMoreHills = true;
				for (Hill h : _hills)
				{
					//found the next hill to use
					if (!h.isDone())
					{
						_activeHill.getStandingPlayers().clear();
						_activeHill = h;
						_activeHill.setActiveHill(true);
						noMoreHills = false;
						//Bukkit.broadcastMessage(C.Gold + "Hill has changed to " + _activeHill.getName()
						//	+ "! Go There!!!!");
						Bukkit.broadcastMessage(C.Gold + "The Hill has moved to " + _activeHill.getName());
						for (Player p : Bukkit.getOnlinePlayers())
						{
							p.sendTitle(" ", C.Gold + "The Hill has moved to " + _activeHill.getName());
							p.playSound(p.getLocation(), Sound.ENTITY_PARROT_IMITATE_ENDER_DRAGON, SoundCategory.AMBIENT, 9999, 0.5f);
						}
						break;
					}
				}
				
				//end the game
				if (noMoreHills)
				{
					setOption(GameOption.DEATH_MESSAGES, false);
					
					GameTeam winningTeam = getTeams().get(0);
					int highestScore = 0;
					for (GameTeam gameTeam : getTeams())
					{
						int score = _kothScore.get(gameTeam);
						if (score > highestScore)
						{
							highestScore = score;
							winningTeam = gameTeam;
						}
					}
					
					setOption(GameOption.DEATH_OUT, true);
					for (GameTeam team : getTeams())
					{
						if (winningTeam != team)
						{
							for (Player p : team.getPlayers())
							{
								team.setDead(p);
							}
							team.setDead(true);
						}
					}
					
					setOption(GameOption.DEATH_MESSAGES, true);
					checkGameState();
					return;
				}
			}
			
			for(Hill h : _hills) {
				h.drawHologram();
			}
			
			for (Player p : Bukkit.getOnlinePlayers())
			{
				if (isAlive(p) && _activeHill.getBoundingBox().contains(p.getBoundingBox()))
				{
					if (!_activeHill.getStandingPlayers().contains(p))
						_activeHill.addStandingPlayer(p);
					//logger.info("player " + p.getName() + " standing inside " + _activeHill.getName());
				}
				else if (_activeHill.getStandingPlayers().contains(p))
					_activeHill.removeStandingPlayer(p);
			}
			
			//logger.info(_activeHill.getStandingPlayers().toString());
			//ArrayList<GameTeam> teamsOnPoint = new ArrayList<>();
			GameTeam teamOnPoint = null;
			Color particleColor = Color.WHITE;
			if (_activeHill.getStandingPlayers().size() > 0)
			{
				for (Player p : _activeHill.getStandingPlayers())
				{
					GameTeam team = getTeam(p);
					//get first player's team
					if (teamOnPoint == null)
						teamOnPoint = team;
						//more than one team on point, reward no points and reveal ghosts
					else if (team != teamOnPoint)
					{
						teamOnPoint = null;
						_activeHill.revealGhosts();
						break;
					}
				}
				
				if (teamOnPoint != null)
				{
					int score = _kothScore.get(teamOnPoint);
					score++;
					_kothScore.put(teamOnPoint, score);
					particleColor = teamOnPoint.getColor();
				}
				//logger.info("gave team " + teamOnPoint.getName() + " one point.\nend of tick");
			}
			
			_activeHill.drawParticles(particleColor);
		}
	}
	
	//for players waiting to respawn
	@EventHandler
	public void onKothDeadTick(TimeEvent event)
	{
		if(getOption(GameOption.DEATH_OUT))
			return;
		
		if(event.getType() != TimeType.TICK)
			return;
		
		for(GameTeam team : getTeams())
		{
			ArrayList<Player> deadList = team.getPlayers(false);
			for(Player p : deadList)
			{
				long diedWhen = team.getDiedTime(p);
				long now = System.currentTimeMillis();
				//respawn after 5 seconds
				if(now - diedWhen > 5000)
				{
					p.setFlying(false);
					p.setAllowFlight(false);
					p.setCollidable(true);
					
					p.getInventory().remove(getManager().getLobby().getKitSelector());
					p.getInventory().remove(getManager().getGameManager().getKitLayout());
					p.getInventory().remove(getManager().getGameManager().getCompass());
					p.getInventory().remove(getManager().getGameManager().getNextGame());
					
					removeKillstreak(p);
					
					if (getOption(GameOption.TEAM_HOTBAR)) {
						GameManager.giveTeamLeatherItem(team, p);
					}
					
					try {
						getKit(p).applyKit(p);
						for(Ability ability : getKit(p).getAbilities())
						{
							//redo custom things done in registerAbility that aren't
							// registering packet listeners or one-time-per-game things
							// ability removals handled in GameTeam.setDead(Player)
							ability.giveAbility(p);
						}
					} catch (Exception ex) {
						UtilError.handle(ex);
					}
					
					UtilPlayer.showToAll(p);
					DisguiseUtilities.refreshTrackers(p);
					
					UtilPlayer.tele(p, team.getSpawn());
					team.removeFromDead(p);
				}
				else
				{
					long seconds = 5 - ((now - diedWhen) / 1000);
					String color;
					if(now % 1000 > 500)
						color = C.Green;
					else
						color = C.DGreen;
					p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(
							color + "Respawning in " + seconds + " seconds"
					));
				}
			}
		}
	}
	
	public Hill getActiveHill()
	{
		return _activeHill;
	}
	
	
	@EventHandler
	public void onScoreboardDraw(TimeEvent event) {
		if (event.getType() != TimeType.TICK)
			return;
		
		if (getState().isPreGame())
			return;
		
		drawScoreboard();
	}
	
	@EventHandler
	public void onTeamDeath(TeamDeathEvent event) {
		GameTeam team = event.getTeam();
		Announce(team.getColoring() + team.getName() + C.Gold + " was defeated!");
		
		Iterator<TeamBomb> itel = getBombs().iterator();
		
		while (itel.hasNext()) {
			TeamBomb bomb = itel.next();
			
			if (bomb.getTeam() != event.getTeam())
				continue;
			
			if (!bomb.isOwned()) {
				bomb.restore();
				continue;
			}
			
			bomb.remove();
			itel.remove();
		}
	}
	
	@EventHandler
	public void onTeamsCreation(GameStateEvent event) {
		if (event.getState() != GameState.Live)
			return;
		
		_killstreakManager.register();
		checkLastMan();
	}
	
	@EventHandler
	public void registerGoals(GameStateEvent event) {
		if (event.getState() != GameState.MapLoaded)
			return;
		
		Logger logger = ArcadeManager.getManager().getPlugin().getLogger();
		
		//check if it's bombs or hills first
		Iterator<Entry<String, ArrayList<String>>> iter = getData().getDataIterator();
		while(iter.hasNext())
		{
			Entry<String, ArrayList<String>> entry = iter.next();
			String key = entry.getKey();

			/*logger.info("key: " + key + "\n values:");
			for(String s : entry.getValue())
			{
				logger.info("    " + s);
			}*/
			
			if(key.startsWith("Hill"))
			{
				//logger.info("maptype koth");
				_mapType = SNDMapType.KOTH;
				
				int i = 0;
				//logger.info(key + " , iteration " + i);
				Hill hill = null;
				try {
					hill = parseHill(key, i);
				}
				catch(Exception e) {
					UtilError.handle(e);
					e.printStackTrace();
					continue;
				}
				_hills.add(hill);
				logger.info("Added Hill: " + hill.toString());
				hill.startHologram();
				//hill.drawHologram();
				i++;
			}
			else if(key.equalsIgnoreCase("RandomHillOrder"))
			{
				//randomHillOrder = Boolean.parseBoolean(entry.getValue().get(0));
				randomHillOrder = Boolean.parseBoolean(getData().getData(key).get(0));
				logger.info("randomHills: " + randomHillOrder);
			}
		}
		//logger.info("Hill1 null? snd gametype");
		if(_mapType == SNDMapType.KOTH)
		{
			//start all scores at 0
			for(GameTeam team : getTeams())
			{
				_kothScore.put(team, 0);
			}
			setOption(GameOption.DEATH_OUT, false);
			
			if(randomHillOrder)
			{
				Collections.shuffle(_hills);
			}
			
			//make each hill time += the previous
			int time = 0;
			for(Hill h : _hills)
			{
				time += h.getHillTime();
				if(time != 0)
					h.setHillTime(time);
			}
			
			_activeHill = _hills.get(0);
			_activeHill.setActiveHill(true);
			
			for(Hill h : _hills)
			{
				h.drawHologram();
			}
		}
		else// if(_mapType != SNDMapType.KOTH)
		{
			_mapType = SNDMapType.SND;
			for (TeamSettings settings : TeamSettings.values()) {
				//list of blocks listed under "Custom" with keys of TeamSettings name + " Bombs"
				ArrayList<Block> bombs = getData().getCustomBlocks(settings.name() + " Bombs");
				
				for (Block b : bombs) {
					TeamBomb bomb = new TeamBomb(this, getTeam(settings), b);
					
					_bombs.add(bomb);
					
					bomb.drawHologram();
				}
			}
		}
	}
	
	public Hill parseHill(String key, int number)
	{
		WorldData worldData = getData();
		ArrayList<String> data = worldData.getData(key);
		Hill hill;
		int i = 0;
		String name = key.replaceFirst("Hill", "");
		//need to be ordered XZ corner, then -XZ corner, in the map's config.yml
		Location corner1 = worldData.parseLoc(data.get(0));
		Location corner2 = worldData.parseLoc(data.get(1));
		int hillTime;
		try
		{
			hillTime = Integer.parseInt(data.get(2).split(",")[1]);
		}
		catch(Exception e)
		{
			hillTime = 1500;
			UtilError.handle(e);
			e.printStackTrace();
		}
		//							add 1 to XZ corner to bump it up to edge of block
		hill = new Hill(number, name, corner1.add(1, 0, 1), corner2, hillTime, getManager());
		return hill;
	}
	
	public void sendTimeProgress(Player player) {
		player.sendMessage(C.Gray + "The game has been in progress for " + UtilNumber.getTime(getGameTime(), TimeUnit.SECONDS));
		
		if (!isEndGame()) {
			player.sendMessage(C.Gray + "Poison will begin in " + UtilNumber
					.getTime((getStateChanged() + _deathTimer + 60000) - System.currentTimeMillis(), TimeUnit.MILLISECONDS));
		}
	}
	
	@Override
	public void setupScoreboards() {
		getScoreboard().discardScoreboards();
		
		FakeScoreboard main = getScoreboard().getMainScoreboard();
		
		FakeScoreboard specs = getScoreboard().createScoreboard("Spectators", (player) -> getTeam(player) == null);
		
		for (GameTeam observerTeam : getTeams()) {
			FakeTeam realTeam = specs.createTeam(observerTeam.getName());
			realTeam.setPrefix(observerTeam.getColoring());
			
			for (Player player : observerTeam.getPlayers()) {
				realTeam.addPlayer(player);
			}
		}
		
		main.addChild(specs);
		
		FakeTeam specTeam = specs.createTeam("Spectators");
		specTeam.setPrefix(C.Gray);
		
		// Create all the teams
		for (GameTeam observerTeam : getTeams()) {
			FakeScoreboard board = getScoreboard().createScoreboard(observerTeam.getName(), new Predicate<Player>() {
				@Override
				public boolean apply(Player input) {
					return observerTeam.isInTeam(input);
				}
			});
			
			main.addChild(board);
			
			ArrayList<Player> spies = new ArrayList<Player>();
			
			for (GameTeam renderedTeam : getTeams()) {
				FakeTeam realTeam = board.createTeam(renderedTeam.getName());
				FakeTeam ghostTeam = board.createTeam(renderedTeam.getName() + "Invis");
				FakeTeam spyTeam = board.createTeam(renderedTeam.getName() + "Spy");
				
				realTeam.setPrefix(renderedTeam.getColoring()+ "real");
				spyTeam.setPrefix(renderedTeam.getColoring() + "spy");
				ghostTeam.setPrefix(renderedTeam.getColoring() + "ghost");
				
				ghostTeam.setOption(Option.NAME_TAG_VISIBILITY, OptionStatus.NEVER);
				
				realTeam.setSeeInvisiblePlayers(true);
				
				for (Player player : renderedTeam.getPlayers()) {
					//					team the player is on**
					if (observerTeam == renderedTeam) {
						realTeam.addPlayer(player);
						continue;
					}
					
					Kit kit = getKit(player);
					
					if (kit instanceof KitSpy) {
						spies.add(player);
					} else if (kit instanceof KitGhost || kit instanceof KitWraith) {
						ghostTeam.addPlayer(player);
					} else {
						realTeam.addPlayer(player);
					}
					
					realTeam.addPlayer(player.getName());
				}
			}
			
			for (Player player : spies) {
				board.getTeam(observerTeam.getName() + "Spy").addPlayer(player);
			}
		}
		
		main.setSidebarTitle(C.Gold + "Teams");
	}
}

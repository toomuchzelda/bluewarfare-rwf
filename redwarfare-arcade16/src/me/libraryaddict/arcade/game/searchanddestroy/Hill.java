package me.libraryaddict.arcade.game.searchanddestroy;

import java.util.ArrayList;

import me.libraryaddict.arcade.game.GameTeam;
import me.libraryaddict.arcade.game.searchanddestroy.abilities.GhostAbility;
import me.libraryaddict.arcade.kits.Ability;
import me.libraryaddict.arcade.managers.ArcadeManager;
import me.libraryaddict.core.data.ParticleColor;
import me.libraryaddict.core.utils.UtilParticle;
import me.libraryaddict.core.utils.UtilTime;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import me.libraryaddict.core.C;
import me.libraryaddict.core.hologram.Hologram;

public class Hill
{
	private int _hillNumber;
	private String _hillName;
	private Location _xzCorner;
	private Location _oppositeCorner;
	private Hologram _hologram;
	private boolean _isActiveHill;
	private boolean _isDone;
	//maybe replaceable with a HashMap
	private ArrayList<Player> _standingPlayers;
	//area to stand within to trigger
	private BoundingBox _boundingBox;
	private int _hillTime;
	private ArcadeManager _manager;
	
	public Hill(int hillNumber, String hillName, Location xzBorder, Location oppositeCorner, int time,
				ArcadeManager manager)
	{
		_hillNumber = hillNumber;
		_hillName = hillName;
		_xzCorner = xzBorder;
		_oppositeCorner = oppositeCorner;
		_hologram = null;
		_isActiveHill = false;
		_standingPlayers = new ArrayList<>();
		this._hillTime = time;
		_isDone = false;
		_manager = manager;
		
		_boundingBox = new BoundingBox(_xzCorner.getX(), _xzCorner.getY(), _xzCorner.getZ(),
				_oppositeCorner.getX(), _oppositeCorner.getY(), _oppositeCorner.getZ());
	}
	
	public void startHologram()
	{
		Vector difference = _xzCorner.toVector().subtract(_oppositeCorner.toVector());
		double xLength = difference.getX();
		double zLength = difference.getZ();
		xLength /= 2;
		zLength /= 2;
		
		Location holoLoc = new Location(_xzCorner.getWorld(), _oppositeCorner.getX() + xLength,
				_xzCorner.getY(), _oppositeCorner.getZ() + zLength);
		
		_hologram = new Hologram(holoLoc, "Hill");
        _hologram.start();
	}
	
	public void drawHologram()
	{
		SearchAndDestroy snd = (SearchAndDestroy) _manager.getGame();
		String secondLine;
		if(_isActiveHill)
			secondLine = C.Green +"Active Hill. Stand in here!";
		else
			secondLine = C.Red + "This Hill is inactive. Go to "  + snd.getActiveHill().getName() + "!";
		_hologram.setText("Hill " + _hillName, secondLine);
	}
	
	public void drawParticles(ArrayList<GameTeam> teams)
	{
		double xLength = Math.abs(_xzCorner.getX() - _oppositeCorner.getX());
		double zLength = Math.abs(_xzCorner.getZ() - _oppositeCorner.getZ());
		
		World world = _xzCorner.getWorld();
		//get RGB as 0-255 and convert  to range 0-1
		Color color;
		if(teams.size() < 1) {
			// dont divide by 0!
			color = Color.WHITE;
		}
		else {
			int colorIndex = UtilTime.currentTick % teams.size();
			color = teams.get(colorIndex).getColor();
		}
		
		double blue = (double) color.getBlue();
		double red = (double) color.getRed();
		double green = (double) color.getGreen();
		blue /= 255;
		red /= 255;
		green /= 255;
		if(red == 0)
			red = 0.00001;
		
		//draw x lines
		Vector location = _oppositeCorner.toVector();
		location.setY(_xzCorner.getY());
		for(int x = 0; x <= xLength; x += 1)
		{
			location.setX(_oppositeCorner.getX() + x);
			//fuck Utilparticle
			world.spawnParticle(Particle.SPELL_MOB, location.getX(), location.getY(),
					location.getZ(), 0, red, green, blue, 1);
			
			world.spawnParticle(Particle.SPELL_MOB, location.getX(), location.getY(),
					location.getZ() + zLength, 0, red, green, blue, 1);
		}
		
		//draw z lines
		location.setX(_oppositeCorner.getX());
		for(int z = 0; z <= zLength; z += 1)
		{
			location.setZ(_oppositeCorner.getZ() + z);
			//fuck Utilparticle
			world.spawnParticle(Particle.SPELL_MOB, location.getX(), location.getY(),
					location.getZ(), 0, red, green, blue, 1);
			
			world.spawnParticle(Particle.SPELL_MOB, location.getX() + xLength, location.getY(),
					location.getZ(), 0, red, green, blue, 1);
		}
	}
	
	public void drawParticles(Color color) {
		double xLength = Math.abs(_xzCorner.getX() - _oppositeCorner.getX());
		double zLength = Math.abs(_xzCorner.getZ() - _oppositeCorner.getZ());
		
		World world = _xzCorner.getWorld();
		//get RGB as 0-255 and convert  to range 0-1
		double blue = (double) color.getBlue();
		double red = (double) color.getRed();
		double green = (double) color.getGreen();
		blue /= 255;
		red /= 255;
		green /= 255;
		if(red == 0)
			red = 0.00001;
		
		//draw x lines
		Vector location = _oppositeCorner.toVector();
		location.setY(_xzCorner.getY());
		for(int x = 0; x <= xLength; x += 1)
		{
			location.setX(_oppositeCorner.getX() + x);
			//fuck Utilparticle
			world.spawnParticle(Particle.SPELL_MOB, location.getX(), location.getY(),
					location.getZ(), 0, red, green, blue, 1);
			
			world.spawnParticle(Particle.SPELL_MOB, location.getX(), location.getY(),
					location.getZ() + zLength, 0, red, green, blue, 1);
		}
		
		//draw z lines
		location.setX(_oppositeCorner.getX());
		for(int z = 0; z <= zLength; z += 1)
		{
			location.setZ(_oppositeCorner.getZ() + z);
			//fuck Utilparticle
			world.spawnParticle(Particle.SPELL_MOB, location.getX(), location.getY(),
					location.getZ(), 0, red, green, blue, 1);
			
			world.spawnParticle(Particle.SPELL_MOB, location.getX() + xLength, location.getY(),
					location.getZ(), 0, red, green, blue, 1);
		}
	}
	
	public void revealGhosts()
	{
		for(Player p : getStandingPlayers())
		{
			for(Ability ability : _manager.getGame().getKit(p).getAbilities())
			{
				if(ability instanceof GhostAbility)
					((GhostAbility) ability).playRevealParticles(p);
			}
		}
	}
	
	
	public String getName()
	{
		return _hillName;
	}
	
	public boolean isActiveHill()
	{
		return _isActiveHill;
	}

	public void setActiveHill(boolean _isActiveHill)
	{
		this._isActiveHill = _isActiveHill;
	}

	public BoundingBox getBoundingBox()
	{
		return _boundingBox;
	}
	
	/*public void setBoundingBox(BoundingBox _boundingBox)
	{
		this._boundingBox = _boundingBox;
	}*/
	
	public void addStandingPlayer(Player player)
	{
		_standingPlayers.add(player);
	}
	
	public void removeStandingPlayer(Player player)
	{
		_standingPlayers.remove(player);
	}
	
	public ArrayList<Player> getStandingPlayers()
	{
		return _standingPlayers;
	}

	public int getHillTime()
	{
		return _hillTime;
	}
	
	public void setHillTime(int time)
	{
		_hillTime = time;
	}
	
	public boolean isDone()
	{
		return _isDone;
	}
	
	public void setDone()
	{
		_isDone = true;
	}

	public String toString()
	{
		String s = "";
		s += "num=" + _hillNumber;
		s += "name=" + _hillName;
		s += ",locXZ" + _xzCorner.toString() + ",locOpposite" + _oppositeCorner.toString();
		return s;
	}
}

package me.libraryaddict.arcade.game.searchanddestroy;

import java.util.ArrayList;

import me.libraryaddict.core.data.ParticleColor;
import me.libraryaddict.core.utils.UtilParticle;
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
	
	public Hill(int hillNumber, String hillName, Location xzBorder, Location oppositeCorner, int time)
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
				_oppositeCorner.getY(), _oppositeCorner.getZ() + zLength);
		
		_hologram = new Hologram(holoLoc, "Hill");
        _hologram.start();
	}
	
	public void drawHologram()
	{
		_hologram.setText("Hill " + _hillName, C.Bold + "KING: TODO");
	}
	
	public void drawParticles(Color color)
	{
		double xLength = Math.abs(_xzCorner.getX() - _oppositeCorner.getX());
		double zLength = Math.abs(_xzCorner.getZ() - _oppositeCorner.getZ());
		double y = _xzCorner.getY();
		Particle.DustOptions options = new Particle.DustOptions(color, 2);
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
		for(float x = 0; x <= xLength; x += 0.5f)
		{
			Vector location = _oppositeCorner.toVector().add(new Vector(x, 0, 0));
			location.setY(_xzCorner.getY());
			//fuck Utilparticle
			world.spawnParticle(Particle.SPELL_MOB, location.getX(), location.getY(),
					location.getZ(), 0, red, green, blue, 1);
			
			world.spawnParticle(Particle.SPELL_MOB, location.getX(), location.getY(),
					location.getZ() + zLength, 0, red, green, blue, 1);
		}
		
		for(float z = 0; z <= zLength; z += 0.5f)
		{
			Vector location = _oppositeCorner.toVector().add(new Vector(0, 0, z));
			location.setY(_xzCorner.getY());
			//fuck Utilparticle
			world.spawnParticle(Particle.SPELL_MOB, location.getX(), location.getY(),
					location.getZ(), 0, red, green, blue, 1);
			
			world.spawnParticle(Particle.SPELL_MOB, location.getX() + xLength, location.getY(),
					location.getZ(), 0, red, green, blue, 1);
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

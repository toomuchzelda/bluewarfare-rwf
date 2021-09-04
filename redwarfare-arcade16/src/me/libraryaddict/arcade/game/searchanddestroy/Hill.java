package me.libraryaddict.arcade.game.searchanddestroy;

import java.util.ArrayList;

import org.bukkit.Location;
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
	private ArrayList<Player> _standingPlayers;
	//area to stand within to trigger
	private BoundingBox _boundingBox;
	
	public Hill(int hillNumber, String hillName, Location xzBorder, Location oppositeCorner)
	{
		_hillNumber = hillNumber;
		_hillName = hillName;
		_xzCorner = xzBorder;
		_oppositeCorner = oppositeCorner;
		_hologram = null;
		_isActiveHill = false;
		_standingPlayers = new ArrayList<>();
		
		_boundingBox = new BoundingBox(_xzCorner.getX(), _xzCorner.getY(), _xzCorner.getZ(),
				_oppositeCorner.getX(), _oppositeCorner.getY() + 5, _oppositeCorner.getZ());
	}
	
	public void startHologram()
	{
		Vector difference = _xzCorner.toVector().subtract(_oppositeCorner.toVector());
		double xLength = difference.getX();
		double zLength = difference.getZ();
		xLength /= 2;
		zLength /= 2;
		
		Location holoLoc = new Location(_xzCorner.getWorld(), _oppositeCorner.getX() + xLength,
				_oppositeCorner.getY() + 5, _oppositeCorner.getZ() + zLength);
		
		_hologram = new Hologram(holoLoc, "Hill");
        _hologram.start();
	}
	
	public void drawHologram()
	{
		_hologram.setText("Hill " + _hillName, C.Bold + "KING: TODO");
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

	public String toString()
	{
		String s = "";
		s += "num=" + _hillNumber;
		s += "name=" + _hillName;
		s += ",locXZ" + _xzCorner.toString() + ",locOpposite" + _oppositeCorner.toString();
		return s;
	}
}

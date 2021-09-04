package me.libraryaddict.arcade.game.searchanddestroy;

import org.bukkit.Location;
import org.bukkit.util.Vector;

import me.libraryaddict.core.hologram.Hologram;

public class Hill
{
	private int _hillNumber;
	private Location _xzCorner;
	private Location _oppositeCorner;
	private Hologram _hologram;
	
	public Hill(int hillNumber, Location xzBorder, Location oppositeCorner)
	{
		_hillNumber = hillNumber;
		_xzCorner = xzBorder;
		_oppositeCorner = oppositeCorner;
		_hologram = null;
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
	
	public String toString()
	{
		return "num=" + _hillNumber + ",locXZ" + _xzCorner.toString() + ",locOpposite" + _oppositeCorner.toString();
	}
}

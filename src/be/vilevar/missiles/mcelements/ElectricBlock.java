package be.vilevar.missiles.mcelements;

import org.bukkit.Location;

public interface ElectricBlock {

	Location getLocation();
	long getTimeOut();
	void addTimeOut(long time);
	
}

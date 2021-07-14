package be.vilevar.missiles.mcelements.crafting;

import java.util.ArrayList;
import java.util.Iterator;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import be.vilevar.missiles.mcelements.data.MIRVData;

public class RVCraftBlock {

	public static final ArrayList<RVCraftBlock> crafts = new ArrayList<>();
	
	private final Location location;
	private MIRVData mirv;
	private Player open;
	
	public RVCraftBlock(Location loc) {
		this.location = loc;
	}
	
	public Location getLocation() {
		return this.location;
	}
	
	public MIRVData getMIRV() {
		return mirv;
	}
	
	public void setMIRV(MIRVData mirv) {
		this.mirv = mirv;
	}
	
	public boolean isOpen() {
		return open != null;
	}
	
	public void setOpen(Player open) {
		this.open = open;
	}
	
	
	
	
	public void destroy() {
		crafts.remove(this);
		if(mirv != null)
			location.getWorld().dropItem(location, mirv.toItem());
		if(open != null)
			open.closeInventory();
	}
	
	
	
	
	public static void checkDestroy(Location loc) {
		Iterator<RVCraftBlock> it = crafts.iterator();
		while(it.hasNext()) {
			RVCraftBlock craft = it.next();
			if(craft.getLocation().equals(loc)) {
				it.remove();
				craft.destroy();
				return;
			}
		}
	}
	
	public static RVCraftBlock getCraftAt(Location loc) {
		for(RVCraftBlock craft : crafts) {
			if(craft.getLocation().equals(loc)) {
				return craft;
			}
		}
		return null;
	}
}

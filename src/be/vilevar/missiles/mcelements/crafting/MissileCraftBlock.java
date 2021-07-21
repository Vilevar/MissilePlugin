package be.vilevar.missiles.mcelements.crafting;

import java.util.ArrayList;
import java.util.Iterator;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import be.vilevar.missiles.mcelements.data.BallisticMissileData;

public class MissileCraftBlock {

	public static final ArrayList<MissileCraftBlock> crafts = new ArrayList<>();
	
	private final Location location;
	private BallisticMissileData missile;
	private Player open;
	
	public MissileCraftBlock(Location loc) {
		this.location = loc;
	}
	
	public Location getLocation() {
		return this.location;
	}
	
	public BallisticMissileData getMissile() {
		return missile;
	}
	
	public void setMissile(BallisticMissileData missile) {
		this.missile = missile;
	}
	
	public boolean isOpen() {
		return open != null;
	}
	
	public void setOpen(Player open) {
		this.open = open;
	}
	
	
	
	
	public void destroy(boolean remove) {
		if(remove)
			crafts.remove(this);
		if(missile != null)
			location.getWorld().dropItem(location, missile.toItemStack());
		if(open != null)
			open.closeInventory();
	}
	
	
	
	
	public static void checkDestroy(Location loc) {
		Iterator<MissileCraftBlock> it = crafts.iterator();
		while(it.hasNext()) {
			MissileCraftBlock craft = it.next();
			if(craft.getLocation().equals(loc)) {
				it.remove();
				craft.destroy(false);
				return;
			}
		}
	}
	
	public static MissileCraftBlock getCraftAt(Location loc) {
		for(MissileCraftBlock craft : crafts) {
			if(craft.getLocation().equals(loc)) {
				return craft;
			}
		}
		return null;
	}
}

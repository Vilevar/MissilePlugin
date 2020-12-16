package be.vilevar.missiles.mcelements.data;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import be.vilevar.missiles.mcelements.CustomElementManager;
import be.vilevar.missiles.mcelements.data.persistanttype.RangefinderPersistantDataType;

public class RangefinderData {
	
	public static final double defaultRange = 100;
	
	private double range;
	private Location target;
	
	public RangefinderData(double range, Location target) {
		this.range = range;
		this.target = target;
	}
	
	public double getRange() {
		return range;
	}

	public void setRange(double range) {
		this.range = range;
	}

	public Location getTarget() {
		return target;
	}

	public void setTarget(Location target) {
		this.target = target;
	}
	
	public ItemStack toItemStack() {
		ItemStack is = CustomElementManager.RANGEFINDER.create();
		ItemMeta im = is.getItemMeta();
		im.getPersistentDataContainer().set(RangefinderPersistantDataType.RANGEFINDER_KEY, RangefinderPersistantDataType.RANGEFINDER, this);
		is.setItemMeta(im);
		return is;
	}
	
	
	
	public static RangefinderData getRangefinderData(ItemStack is) {
		if(is != null && CustomElementManager.RANGEFINDER.isParentOf(is)) {
			ItemMeta im = is.getItemMeta();
			return im.getPersistentDataContainer().getOrDefault(RangefinderPersistantDataType.RANGEFINDER_KEY,
					RangefinderPersistantDataType.RANGEFINDER, new RangefinderData(defaultRange, null));
		}
		return null;
	}
}

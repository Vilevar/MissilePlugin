package be.vilevar.missiles.mcelements.data;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import be.vilevar.missiles.mcelements.CustomElementManager;
import be.vilevar.missiles.mcelements.data.persistanttype.LaserPointerPersistantDataType;

public class LaserPointerData {
	
	public static final double defaultRange = 100;
	
	private double range;
	private Location target;
	
	public LaserPointerData(double range, Location target) {
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
		ItemStack is = CustomElementManager.LASER_POINTER.create();
		ItemMeta im = is.getItemMeta();
		im.getPersistentDataContainer().set(LaserPointerPersistantDataType.LASER_POINTER_KEY, LaserPointerPersistantDataType.LASER_POINTER, this);
		is.setItemMeta(im);
		return is;
	}
	
	
	
	public static LaserPointerData getLaserPointerData(ItemStack is) {
		if(is!=null && CustomElementManager.LASER_POINTER.isParentOf(is)) {
			ItemMeta im = is.getItemMeta();
			return im.getPersistentDataContainer().getOrDefault(LaserPointerPersistantDataType.LASER_POINTER_KEY,
					LaserPointerPersistantDataType.LASER_POINTER, new LaserPointerData(defaultRange, null));
		}
		return null;
	}
}

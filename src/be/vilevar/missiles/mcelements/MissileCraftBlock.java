package be.vilevar.missiles.mcelements;

import java.util.ArrayList;
import java.util.Iterator;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import be.vilevar.missiles.mcelements.CustomElementManager.BalisticMissileData;

public class MissileCraftBlock {

	public static final ArrayList<MissileCraftBlock> crafts = new ArrayList<>();
	
	private final Location location;
	private BalisticMissileData original;
	private BalisticMissileData result;
	private boolean open;
	private int tnt;
	private int speedFuel;
	private int rangeFuel;
	private int blaze_powder;
	
	public MissileCraftBlock(Location loc) {
		this.location = loc;
	}
	
	public Location getLocation() {
		return this.location;
	}
	
	public void resetOriginalMissile() {
		this.original = null;
	}
	
	public void resetResultMissile() {
		this.result = null;
	}
	
	public void setMissile(BalisticMissileData missile) {
		this.original = missile;
		this.result = this.original==null ? null : this.original.clone();
	}
	
	public BalisticMissileData getOriginalMissile() {
		return this.original;
	}
	
	public BalisticMissileData getResultMissile() {
		return result;
	}
	
	public boolean isOpen() {
		return open;
	}
	
	public void setOpen(boolean open) {
		this.open = open;
	}
	
	public double getRange() {
		return result == null ? 0 : result.getRange();
	}

	public float getSpeed() {
		return result == null ? 0 : result.getSpeed();
	}
	
	public float getExplosionPower() {
		return result == null ? 0 : result.getExplosionPower();
	}
	
	public double getRotatingForce() {
		return result == null ? 0 : result.getRotatingForce();
	}
	
	public double getFlightHeight() {
		return result == null ? 0 : result.getFlightHeight();
	}
	
	public double getDetectorDistance() {
		return result == null ? 0 : result.getDetectorDistance();
	}
	
	public void setRange(double range) {
		if(result!=null) {
			double d = range - result.getRange();
			result.setRange(range);
			result.setWeight(result.getWeight() + 2*(d/100));
		}
	}
	
	public void setSpeed(float speed) {
		if(result!=null) {
			float d = speed - result.getSpeed();
			result.setSpeed(speed);
			result.setWeight(result.getWeight() + 2*(d/10));
		}
	}
	
	public void setExplosionPower(float explosionPower) {
		if(result!=null) {
			float d = explosionPower - result.getExplosionPower();
			result.setExplosionPower(explosionPower);
			result.setWeight(result.getWeight() + 3*(d/10));
		}
	}
	
	public void setRotatingForce(double rotatingForce) {
		if(result!=null) {
			double d = rotatingForce - result.getRotatingForce();
			result.setRotatingForce(rotatingForce);
			result.setWeight(result.getWeight() + d/50);
		}
	}
	
	public void setFlightHeight(double flightHeight) {
		if(result!=null) {
			result.setFlightHeight(flightHeight);
		}
	}
	
	public void setDetectorDistance(double distance) {
		if(result!=null) {
			result.setDetectorDistance(distance);
		}
	}
	
	public double getWeight() {
		return result==null ? 0 : result.getWeight();
	}
	
	public double getRadius() {
		return getRotatingForce()!=0 ? getWeight()*getSpeed()*getSpeed() / getRotatingForce() : 0;
	}
	
	public int getCanTakeTNT() {
		if(this.original==null || this.result==null) return this.tnt;
		int d = (int) ((this.result.getExplosionPower() - this.original.getExplosionPower())/10);
		if(d < 0) {
			return this.tnt + d;
		} else {
			return this.tnt;
		}
	}
	
	public int getTNT() {
		return tnt;
	}
	
	public void addTNT(int n) {
		this.tnt+=n;
	}
	
	public int getCanTakeRangeFuel() {
		if(this.original==null || this.result==null) return this.tnt;
		int d = (int) ((this.result.getRange() - this.original.getRange())/100);
		if(d < 0) {
			return this.rangeFuel + d;
		} else {
			return this.rangeFuel;
		}
	}
	
	public int getCanTakeSpeedFuel() {
		if(this.original==null || this.result==null) return this.tnt;
		int d = (int) ((this.result.getSpeed() - this.original.getSpeed())/10);
		if(d < 0) {
			return this.speedFuel + d;
		} else {
			return this.speedFuel;
		}
	}
	
	public int getSpeedFuel() {
		return speedFuel;
	}
	
	public int getRangeFuel() {
		return rangeFuel;
	}
	
	public void addSpeedFuel(int n) {
		this.speedFuel+=n;
	}
	
	public void addRangeFuel(int n) {
		this.rangeFuel+=n;
	}
	
	public int getCanTakeBlazePowder() {
		if(this.original==null || this.result==null) return this.tnt;
		int d = (int) ((this.result.getRotatingForce() - this.original.getRotatingForce())/50);
		if(d < 0) {
			return this.blaze_powder + d;
		} else {
			return this.blaze_powder;
		}
	}
	
	public int getBlazePowder() {
		return blaze_powder;
	}
	
	public void addBlazePowder(int n) {
		this.blaze_powder+=n;
	}
	
	
	
	public void destroy(boolean remove) {
		if(remove)
			crafts.remove(this);
		ItemStack tnt = new ItemStack(Material.TNT, this.tnt);
		ItemStack blaze_powder = new ItemStack(Material.BLAZE_POWDER, this.blaze_powder);
		ItemStack fuel = new ItemStack(CustomElementManager.FUEL, this.rangeFuel+this.speedFuel);
		ItemStack missile = this.original==null ? new ItemStack(CustomElementManager.BALISTIC_MISSILE, 0) : this.original.toItemStack();
		this.setMissile(null);
		this.tnt = 0;
		this.blaze_powder = 0;
		this.rangeFuel = 0;
		this.speedFuel = 0;
		if(tnt.getAmount() > 0)
			this.location.getWorld().dropItem(this.location, tnt);
		if(blaze_powder.getAmount() > 0)
			this.location.getWorld().dropItem(this.location, blaze_powder);
		if(fuel.getAmount() > 0)
			this.location.getWorld().dropItem(this.location, fuel);
		if(missile.getAmount() > 0)
			this.location.getWorld().dropItem(this.location, missile);
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

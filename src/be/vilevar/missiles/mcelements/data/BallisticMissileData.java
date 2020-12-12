package be.vilevar.missiles.mcelements.data;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import be.vilevar.missiles.mcelements.CustomElementManager;
import be.vilevar.missiles.mcelements.data.persistanttype.BallisticMissilePersistantDataType;
import be.vilevar.missiles.missile.BalisticMissile;
import be.vilevar.missiles.utils.ParticleEffect;

public class BallisticMissileData implements Cloneable {
	
	public static final float defaultExplosionPower = 10;
	public static final double defaultWeight = 1;
	public static final double defaultRotatingForce = 100;
	public static final double defaultRange = 300;
	public static final float defaultSpeed = 20;
	public static final double defaultFlightHeight = 200;
	public static final double defaultDetectorDistance = 0;
	
	
	
	private float explosionPower;
	private double weight;
	private double rotatingForce;
	private double range;
	private float speed;
	private double flightHeight;
	private double detectDist;
	
	private double minRotatingForce, maxRotatingForce;
	private double minRange;
	private double minSpeed;
	
	public BallisticMissileData(float explosionPower, double weight, double rotatingForce, double range, float speed, double flightHeight,
			double detectDist) {
		this.explosionPower = explosionPower;
		this.weight = weight;
		this.rotatingForce = rotatingForce;
		this.range = range;
		this.speed = speed;
		this.flightHeight = flightHeight;
		this.detectDist = detectDist;
		this.setMinRotatingForce();
		this.setMaxRotatingForce();
		this.setMinRange();
		this.setMinSpeed();
	}

	public float getExplosionPower() {
		return explosionPower;
	}

	public void setExplosionPower(float explosionPower) {
		this.explosionPower = explosionPower;
	}

	public double getWeight() {
		return weight;
	}

	public void setWeight(double weight) {
		this.weight = weight;
		this.setMinRotatingForce();
		this.setMaxRotatingForce();
		this.setMinRange();
		this.setMinSpeed();
	}
	
	public double getRotatingForce() {
		return rotatingForce;
	}

	public void setRotatingForce(double rotatingForce) {
		this.rotatingForce = rotatingForce;
		this.setMinRange();
		this.setMinSpeed();
	}

	public double getRange() {
		return range;
	}

	public void setRange(double range) {
		this.range = range;
	}

	public float getSpeed() {
		return speed;
	}

	public void setSpeed(float speed) {
		this.speed = speed;
		this.setMinRotatingForce();
		this.setMaxRotatingForce();
		this.setMinRange();
	}

	public double getFlightHeight() {
		return flightHeight;
	}

	public void setFlightHeight(double flightHeight) {
		this.flightHeight = flightHeight;
		this.setMinRotatingForce();
	}
	
	public double getDetectorDistance() {
		return this.detectDist;
	}
	
	public void setDetectorDistance(double distance) {
		this.detectDist = Math.min(10, Math.max(0, distance));
	}
	

	
	
	public double getMinRotatingForce() {
		return minRotatingForce;
	}

	public double getMaxRotatingForce() {
		return maxRotatingForce;
	}

	public double getMinRange() {
		return minRange;
	}

	public double getMinSpeed() {
		return minSpeed;
	}

	
	
	private void setMinRotatingForce() {
		this.minRotatingForce = (this.weight * this.speed * this.speed) / (flightHeight - 4);
	}

	private void setMaxRotatingForce() {
		this.maxRotatingForce = this.weight * this.speed * this.speed;
	}

	private void setMinRange() {
		double a = 1.1 * this.weight * this.speed * this.speed;
		this.minRange = (a / this.rotatingForce) + (a / (this.rotatingForce + (BalisticMissile.GRAVITY * this.weight)));
	}

	private void setMinSpeed() {
		this.minSpeed = Math.sqrt(this.rotatingForce / this.weight);
	}
	
	
	public BalisticMissile toBalisticMissile(Location loc) {
		return new BalisticMissile(ParticleEffect.FLAME, explosionPower, weight, rotatingForce, range, speed, flightHeight, detectDist, loc);
	}
	
	public ItemStack toItemStack() {
		ItemStack is = CustomElementManager.BALLISTIC_MISSILE.create();
		ItemMeta im = is.getItemMeta();
		im.getPersistentDataContainer().set(BallisticMissilePersistantDataType.BALLISTIC_MISSILE_KEY,
				BallisticMissilePersistantDataType.BALLISTIC_MISSILE, this);
		is.setItemMeta(im);
		return is;
	}
	
	@Override
	public BallisticMissileData clone() {
		return new BallisticMissileData(explosionPower, weight, rotatingForce, range, speed, flightHeight, this.detectDist);
	}
	
	
	public static BallisticMissileData getBallisticMissileData(ItemStack is) {
		if(is!=null && CustomElementManager.BALLISTIC_MISSILE.isParentOf(is)) {
			ItemMeta im = is.getItemMeta();
			return im.getPersistentDataContainer().getOrDefault(BallisticMissilePersistantDataType.BALLISTIC_MISSILE_KEY,
					BallisticMissilePersistantDataType.BALLISTIC_MISSILE, new BallisticMissileData(defaultExplosionPower, defaultWeight, 
							defaultRotatingForce, defaultRange, defaultSpeed, defaultFlightHeight, defaultDetectorDistance));
		}
		return null;
	}
}

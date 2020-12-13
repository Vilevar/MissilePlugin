package be.vilevar.missiles.mcelements.weapons;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.AbstractArrow.PickupStatus;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import be.vilevar.missiles.mcelements.CustomItem;

public class Weapon {

	private CustomItem item;
	private int aiming;
	private int price;
	private int bullets;
	private float spread;
	private float speed;
	private double damage;
	private int knockback;
	private int pierce;
	private float pitchDecline;
	private float planeDecline;
	
	// TODO Sound
	public Weapon(CustomItem item, int aiming, int price, int bullets, float spread, float speed, double damage, int knockback, int pierce,
			float pitchDecline, float planeDecline) {
		this.aiming = aiming;
		this.item = item;
		this.price = price;
		this.bullets = bullets;
		this.spread = spread;
		this.speed = speed;
		this.damage = damage;
		this.knockback = knockback;
		this.pierce = pierce;
		this.pitchDecline = pitchDecline;
		this.planeDecline = planeDecline;
	}
	
	public int getAiming() {
		return aiming;
	}
	
	public CustomItem getItem() {
		return item;
	}
	
	public int getPrice() {
		return price;
	}
	
	public void shoot(Player p) {
		World world = p.getWorld();
		Location loc = p.getEyeLocation();
		Vector direction = loc.getDirection();
		for(int i = 0; i < this.bullets; i++) {
			Arrow arrow = world.spawnArrow(loc, direction, this.speed, this.spread);
			arrow.setShooter(p);
			arrow.setPickupStatus(PickupStatus.CREATIVE_ONLY);
			arrow.setDamage(this.damage);
			arrow.setKnockbackStrength(this.knockback);
			arrow.setPierceLevel(this.pierce);
		}
		if(this.pitchDecline != 0) {
			Location newPitch = p.getLocation();
			newPitch.setPitch(loc.getPitch() - this.pitchDecline);
			p.teleport(newPitch);
		}
		if(planeDecline != 0) {
			p.setVelocity(p.getVelocity().add(direction.normalize().setY(0).multiply(-this.planeDecline)));
		}
		// TODO Sound
	}
	
	
}

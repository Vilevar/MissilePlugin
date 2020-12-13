package be.vilevar.missiles.mcelements.weapons;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.AbstractArrow.PickupStatus;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import be.vilevar.missiles.mcelements.CustomItem;

public class Weapon {
	
	private static final float SPREAD_AIMING_IMPROVEMENT = 3.f;

	private CustomItem item;
	private CustomItem ammunition;
	private int aiming;
	private int price;
	private long recover;
	private long rechargeTime;
	private int bullets;
	private float spread;
	private float speed;
	private double damage;
	private int fireTicks;
	private int knockback;
	private int pierce;
	private float pitchDecline;
	private float planeDecline;
	private Sound sound;
	private float volume;
	private float pitch;
	
	public Weapon(
			CustomItem item, CustomItem ammunition,
			int aiming, int price, long recover, long rechargeTime,
			int bullets, float spread, float speed, double damage, int fireTicks, int knockback, int pierce,
			float pitchDecline, float planeDecline,
			Sound sound, float volume, float pitch) {
		this.item = item;
		this.ammunition = ammunition;
		this.aiming = aiming;
		this.price = price;
		this.recover = recover;
		this.rechargeTime = rechargeTime;
		this.bullets = bullets;
		this.spread = spread;
		this.speed = speed;
		this.damage = damage;
		this.fireTicks = fireTicks;
		this.knockback = knockback;
		this.pierce = pierce;
		this.pitchDecline = pitchDecline;
		this.planeDecline = planeDecline;
		this.sound = sound;
		this.volume = volume;
		this.pitch = pitch;
	}
	
	public CustomItem getItem() {
		return item;
	}
	
	public CustomItem getAmmunition() {
		return ammunition;
	}
	
	public int getAiming() {
		return aiming;
	}
	
	public int getPrice() {
		return price;
	}
	
	public long getRecover() {
		return recover;
	}
	
	public long getRechargeTime() {
		return rechargeTime;
	}
	
	public void shoot(Player p, boolean isAiming) {
		World world = p.getWorld();
		Location loc = p.getEyeLocation();
		Vector direction = loc.getDirection();
		float spread = isAiming ? Math.max(0, this.spread - SPREAD_AIMING_IMPROVEMENT) : this.spread;
		for(int i = 0; i < this.bullets; i++) {
			Arrow arrow = world.spawnArrow(loc, direction, this.speed, spread);
			arrow.setShooter(p);
			arrow.setPickupStatus(PickupStatus.CREATIVE_ONLY);
			arrow.setDamage(this.damage);
			arrow.setKnockbackStrength(this.knockback);
			arrow.setPierceLevel(this.pierce);
			arrow.setFireTicks(this.fireTicks);
		}
		if(this.pitchDecline != 0) {
			Location newPitch = p.getLocation();
			newPitch.setPitch(loc.getPitch() - this.pitchDecline);
			p.teleport(newPitch);
		}
		if(planeDecline != 0) {
			p.setVelocity(p.getVelocity().add(direction.normalize().setY(0).multiply(-this.planeDecline)));
		}
		world.playSound(loc, sound, volume, pitch);
	}
	
	
}

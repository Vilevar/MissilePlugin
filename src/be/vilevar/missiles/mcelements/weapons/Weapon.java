package be.vilevar.missiles.mcelements.weapons;

import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.entity.AbstractArrow.PickupStatus;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import be.vilevar.missiles.MainEventListener;
import be.vilevar.missiles.mcelements.CustomItem;

public class Weapon {
	
	private CustomItem item;
	private CustomItem ammunition;
	private double weight;
	private double crossDamage;
	private double crossSpeed;
	private int aiming;
	private int price;
	private long recover;
	private long rechargeTime;
	private int bullets;
	private float spread;
	private float aimingSpread;
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
			double weight, double crossDamage, double crossSpeed,
			int aiming, int price, long recover, long rechargeTime,
			int bullets, float spread, float aimingSpread, float speed,
			double damage, int fireTicks, int knockback, int pierce,
			float pitchDecline, float planeDecline,
			Sound sound, float volume, float pitch) {
		this.item = item;
		this.ammunition = ammunition;
		this.weight = weight;
		this.crossDamage = crossDamage;
		this.crossSpeed = crossSpeed;
		this.aiming = aiming;
		this.aimingSpread = aimingSpread;
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
	
	public ItemStack createItem() {
		ItemStack is = this.getItem().create();
		ItemMeta im = is.getItemMeta();
		if(this.weight != 0)
			im.addAttributeModifier(Attribute.GENERIC_MOVEMENT_SPEED,
					new AttributeModifier(UUID.randomUUID(), "weapon-weight", -this.weight, Operation.ADD_NUMBER, EquipmentSlot.HAND));
		if(this.crossDamage != 0)
			im.addAttributeModifier(Attribute.GENERIC_ATTACK_DAMAGE,
					new AttributeModifier(UUID.randomUUID(), "weapon-cross-damage", this.crossDamage, Operation.ADD_NUMBER, EquipmentSlot.HAND));
		if(this.crossSpeed != 0)
			im.addAttributeModifier(Attribute.GENERIC_ATTACK_SPEED,
					new AttributeModifier(UUID.randomUUID(), "weapon-cross-speed", this.crossSpeed, Operation.ADD_NUMBER, EquipmentSlot.HAND));
		is.setItemMeta(im);
		return is;
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
	
	public float getSpeed() {
		return speed;
	}
	
	public float getSpread() {
		return spread;
	}
	
	public float getAimingSpread() {
		return aimingSpread;
	}
	
	public Arrow[] shoot(Player p, boolean isAiming) {
		Arrow[] arrows = new Arrow[this.bullets];
		World world = p.getWorld();
		Location loc = p.getEyeLocation();
		Vector direction = loc.getDirection();
		float spread = isAiming ? this.aimingSpread : this.spread;
		for(int i = 0; i < this.bullets; i++) {
			Arrow arrow = world.spawnArrow(loc, direction, this.speed, spread);
			arrow.setShooter(p);
			arrow.setPickupStatus(PickupStatus.CREATIVE_ONLY);
			arrow.setDamage(this.damage);
			arrow.setKnockbackStrength(this.knockback);
			arrow.setPierceLevel(this.pierce);
			arrow.setFireTicks(this.fireTicks);
			arrows[i] = arrow;
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
		MainEventListener.trackSoundOrigin(loc, volume, p);
		return arrows;
	}
	
	
}

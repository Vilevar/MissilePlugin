package be.vilevar.missiles.mcelements.radar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import be.vilevar.missiles.Main;
import be.vilevar.missiles.missile.BalisticMissile;

public class MissileRadarBlock {

	public static final ArrayList<MissileRadarBlock> radars = new ArrayList<>();
	public static final int MIN_RANGE = 50, MAX_RANGE = 500;
	private static final float VOLUME = 3;
	private static final double VOLUME_RANGE = VOLUME*16;
	private static final Sound ALARM = Sound.ENTITY_ENDER_DRAGON_DEATH;
	
	private final Location location;
	private int range = 50;
	private Player owner;
	private ArrayList<Player> heared = new ArrayList<>();
	private HashMap<BalisticMissile, Double> missileDistances = new HashMap<>();
	private boolean isPlaying;
	private boolean isOpen;
	
	public MissileRadarBlock(Location location) {
		this.location = location;
	}
	
	public Location getLocation() {
		return location;
	}
	
	public int getRange() {
		return range;
	}
	
	public void setRange(int range) {
		this.range = range;
	}
	
	public Player getOwner() {
		return owner;
	}
	
	public void setOwner(Player owner) {
		this.owner = owner;
	}
	
	public boolean isOpen() {
		return isOpen;
	}
	
	public void setOpen(boolean open) {
		this.isOpen = open;
	}
	
	public void destroy(boolean remove) {
		if(remove)
			radars.remove(this);
		Iterator<Player> it = heared.iterator();
		it: while(it.hasNext()) {
			Player p = it.next();
			it.remove();
			for(MissileRadarBlock radar : radars) {
				if(radar.heared.contains(p)) {
					continue it;
				}
			}
			p.stopSound(ALARM);
		}
		if(owner!=null)
			this.location.getWorld().dropItem(this.location, this.getOwnerHead());
		ItemStack compass = new ItemStack(Material.COMPASS, (this.range - MIN_RANGE)/50);
		if(compass.getAmount()>0)
			this.location.getWorld().dropItem(this.location, compass);
	}
	
	public void checkMissiles() {
		for(BalisticMissile missile : BalisticMissile.launchedMissiles) {
			double distance = missile.getLocation().distance(location);
			if(distance <= range && missile.getLaunchedLocation().distance(location) > range) {
				if(!missileDistances.containsKey(missile)) {
					if(!isPlaying)
						this.play();
					if(owner!=null)
						this.owner.sendMessage(
								"§5[§aRadar§5] §4Attention : §6Missile §c["+missile.getID()+"]§6 repéré à §c"+Math.round(distance)+" mètres.");
					missileDistances.put(missile, distance);
				} else {
					double lastDistance = missileDistances.get(missile);
					if(Math.abs(lastDistance - distance) >= 50) {
						missileDistances.put(missile, distance);
						if(this.owner!=null) {
							this.owner.sendMessage(
								"§5[§aRadar§5] §4Attention : §6Missile §c["+missile.getID()+"]§6 repéré à §c"+Math.round(distance)+" mètres.");
						}
					}
				}
			}
		}
	}
	
	private void play() {
		if(this.isPlaying) return;
		this.isPlaying = true;
		this.location.getWorld().playSound(this.location, ALARM, VOLUME, 1);
		for(Player p : Bukkit.getOnlinePlayers()) {
			if(p.getLocation().distance(location) <= VOLUME_RANGE) {
				heared.add(p);
			}
		}
		Bukkit.getScheduler().runTaskLater(Main.i, () -> {
			this.isPlaying = false;
			this.heared.clear();
		}, 360);
	}
	
	public ItemStack getOwnerHead() {
		ItemStack skull = null;
		if(this.owner!=null) {
			skull = new ItemStack(Material.PLAYER_HEAD);
			SkullMeta sm = (SkullMeta) skull.getItemMeta();
			sm.setOwningPlayer(this.owner);
			skull.setItemMeta(sm);
		}
		return skull;
	}
	
	
	
	
	
	
	
	public static void checkDestroy(Location loc) {
		Iterator<MissileRadarBlock> it = radars.iterator();
		while(it.hasNext()) {
			MissileRadarBlock radar = it.next();
			if(radar.getLocation().equals(loc)) {
				it.remove();
				radar.destroy(false);
				return;
			}
		}
	}
	
	public static MissileRadarBlock getRadarAt(Location loc) {
		for(MissileRadarBlock radar : radars) {
			if(radar.getLocation().equals(loc)) {
				return radar;
			}
		}
		return null;
	}
}

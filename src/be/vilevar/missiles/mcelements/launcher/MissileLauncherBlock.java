package be.vilevar.missiles.mcelements.launcher;

import java.util.ArrayList;
import java.util.Iterator;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import be.vilevar.missiles.Main;
import be.vilevar.missiles.mcelements.data.BallisticMissileData;
import be.vilevar.missiles.utils.ParticleEffect;

public class MissileLauncherBlock {

	public static final ArrayList<MissileLauncherBlock> launchers = new ArrayList<>();
	private static final double smokeCenter = 5 / (4 - 2*Math.sqrt(2));
	private static final double smokeRadius = (51 + 30*Math.sqrt(2)) / 4;
	private static final double smokeMax = 2*Math.PI;
	private static final double smokeProgress = Math.PI/32;
	
	
	
	private final Location location;
	private BallisticMissileData missileData;
	private int pitch;
	private int yaw;
	private BukkitTask smokeTask;
	private Player isOpen;
	
	public MissileLauncherBlock(Location location) {
		this.location = location;
	}
	
	public Location getLocation() {
		return location;
	}
	
	public BallisticMissileData getMissileData() {
		return missileData;
	}
	
	public boolean setMissileData(BallisticMissileData missile) {
		this.missileData = missile;
		return true;
	}
	
	public int getPitch() {
		return pitch;
	}
	
	public void setPitch(int pitch) {
		this.pitch = pitch;
	}
	
	public int getYaw() {
		return yaw;
	}
	
	public void setYaw(int yaw) {
		this.yaw = yaw;
	}
	
	
	
	public boolean isOpen() {
		return isOpen != null;
	}
	
	public void setOpen(Player open) {
		this.isOpen = open;
	}
	
	
	public boolean canLaunchMissile() {
		return this.smokeTask == null && this.missileData != null;
	}
	
	public void launchMissile() {
		Location loc = this.location.clone().add(0.5, 1, 0.5);
		this.missileData.toBallisticMissile().launch(this.isOpen, loc, Math.toRadians(yaw), Math.toRadians(pitch));
		this.missileData = null;
		
		loc.add(0, -1, 0);
		this.smokeTask = Bukkit.getScheduler().runTaskTimer(Main.i, new Runnable() {
			private double r = 0;
			
			@Override
			public void run() {
				if(r > 3) {
					smokeTask.cancel();
					smokeTask = null;
					return;
				}
				for(int j = 0; j < 5 && r <= 3; j++, r += 0.1) {
					double y = r < 2 ? -Math.sqrt(smokeRadius - Math.pow(r-smokeCenter, 2)) + smokeCenter : 0;
					for(double i = 0; i < smokeMax; i+=smokeProgress) {
						double x = r*Math.cos(i);
						double z = r*Math.sin(i);
						loc.add(x, y, z);
						ParticleEffect.SMOKE_NORMAL.display(0, 0, 0, 0, 1, loc, loc.getWorld().getPlayers());
						loc.subtract(x, y, z);
					}
				}
			}
		}, 3, 1);
	}
	
	
	public void destroy(boolean remove) {
		if(remove)
			launchers.remove(this);
		if(missileData != null)
			this.location.getWorld().dropItem(this.location, this.missileData.toItemStack());
		if(isOpen != null)
			this.isOpen.closeInventory();
	}
	
	
	public ItemStack getMissileItem() {
		return this.missileData == null ? null : this.missileData.toItemStack();
	}
	
	
	
	
	
	public static void checkDestroy(Location loc) {
		Iterator<MissileLauncherBlock> it = launchers.iterator();
		while(it.hasNext()) {
			MissileLauncherBlock launcher = it.next();
			if(launcher.getLocation().equals(loc)) {
				it.remove();
				launcher.destroy(false);
				return;
			}
		}
	}
	
	public static MissileLauncherBlock getLauncherAt(Location loc) {
		for(MissileLauncherBlock launcher : launchers) {
			if(launcher.getLocation().equals(loc)) {
				return launcher;
			}
		}
		return null;
	}
}

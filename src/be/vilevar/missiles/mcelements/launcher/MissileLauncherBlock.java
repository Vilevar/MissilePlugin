package be.vilevar.missiles.mcelements.launcher;

import java.util.ArrayList;
import java.util.Iterator;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import be.vilevar.missiles.Main;
import be.vilevar.missiles.mcelements.ElectricBlock;
import be.vilevar.missiles.mcelements.data.BallisticMissileData;

public class MissileLauncherBlock implements ElectricBlock {

	public static final ArrayList<MissileLauncherBlock> launchers = new ArrayList<>();
	private static final double smokeCenter = 5 / (4 - 2*Math.sqrt(2));
	private static final double smokeRadius = (51 + 30*Math.sqrt(2)) / 4;
	private static final double smokeMax = 2*Math.PI;
	private static final double smokeProgress = Math.PI/32;
	
	
	private Main main = Main.i;
	
	private final Location location;
	private BallisticMissileData missileData;
	private int pitch = 45;
	private int yaw = 0;
	
	private long offTime;
	
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
	
	@Override
	public int getTimeOut() {
		int time = (int) (this.offTime - System.currentTimeMillis());
		if(time <= 0) {
			return 0;
		}
		return time;
	}
	
	@Override
	public void addTimeOut(long time) {
		this.offTime = System.currentTimeMillis() + this.getTimeOut() + time; 
	}
	
	
	
	public boolean isOpen() {
		return isOpen != null;
	}
	
	public void setOpen(Player open) {
		this.isOpen = open;
	}
	
	
	public boolean canLaunchMissile() {
		if(this.main.getGame() != null && !this.main.getGame().isStarted()) {
			return false;
		}
		return this.smokeTask == null && this.missileData != null;
	}
	
	public boolean launchMissile() {
		Location loc = this.location.clone().add(0.5, 1, 0.5);
		if(this.missileData.toBallisticMissile().launch(this.isOpen, loc.clone(), Math.toRadians(yaw), Math.toRadians(pitch))) {
			this.missileData = null;
			
			loc.add(0, -1, 0);
			this.smokeTask = this.main.getServer().getScheduler().runTaskTimer(this.main, new Runnable() {
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
							Main.display(Particle.SMOKE_LARGE, loc);
							loc.subtract(x, y, z);
						}
					}
				}
			}, 3, 1);
			return true;
		}
		return false;
	}
	
	
	public boolean destroy(boolean remove) {
		if(remove)
			launchers.remove(this);
		
		boolean drops = this.getTimeOut() == 0;
		
		if(missileData != null && drops)
			this.location.getWorld().dropItem(this.location, this.missileData.toItemStack());
		if(isOpen != null)
			this.isOpen.closeInventory();
		
		return drops;
	}
	
	
	public ItemStack getMissileItem() {
		return this.missileData == null ? null : this.missileData.toItemStack();
	}
	
	
	
	
	
	public static boolean checkDestroy(Location loc) {
		Iterator<MissileLauncherBlock> it = launchers.iterator();
		while(it.hasNext()) {
			MissileLauncherBlock launcher = it.next();
			if(launcher.getLocation().equals(loc)) {
				it.remove();
				return launcher.destroy(false);
			}
		}
		return true;
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

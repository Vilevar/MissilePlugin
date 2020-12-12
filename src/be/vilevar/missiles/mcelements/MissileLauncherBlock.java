package be.vilevar.missiles.mcelements;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.scheduler.BukkitTask;

import be.vilevar.missiles.Main;
import be.vilevar.missiles.mcelements.data.BallisticMissileData;
import be.vilevar.missiles.mcelements.data.LaserPointerData;
import be.vilevar.missiles.missile.BalisticMissile;
import be.vilevar.missiles.utils.ParticleEffect;

public class MissileLauncherBlock {

	public static final ArrayList<MissileLauncherBlock> launchers = new ArrayList<>();
	private static final double smokeCenter = 5 / (4 - 2*Math.sqrt(2));
	private static final double smokeRadius = (51 + 30*Math.sqrt(2)) / 4;
	private static final double smokeMax = 2*Math.PI;
	private static final double smokeProgress = Math.PI/32;
	
	
	
	private final Location location;
	private BallisticMissileData missileData;
	private BalisticMissile missile;
	private LaserPointerData laserPointer;
	private Player owner;
	private BukkitTask smokeTask;
	private boolean isOpen;
	
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
		if(this.isLaunchingConfirmed()) return false;
		this.missileData = missile;
		return true;
	}
	
	public LaserPointerData getLaserPointer() {
		return laserPointer;
	}
	
	public void setLaserPointer(LaserPointerData laserPointer) {
		this.laserPointer = laserPointer;
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
	
	public boolean isLaunchingConfirmed() {
		return this.missile!=null;
	}
	
	public boolean confirmLaunching(Player confirmer) {
		if(this.isLaunchingConfirmed()) {
			confirmer.sendMessage("§4Le tir est déjà confirmé.");
			return false;
		}
		if(this.missileData==null) {
			confirmer.sendMessage("§cIl n'y a pas de missile.");
			return false;
		}
		double a = this.missileData.getFlightHeight() - (this.getMissileDataRadius() + 2);
		if(a < this.location.getY()) {
			confirmer.sendMessage("§cLe lanceur est trop haut (coordonnées y) pour ce missile (max = "+a+").");
			return false;
		}
		if(this.laserPointer==null) {
			confirmer.sendMessage("§cIl n'y a pas de pointeur laser.");
			return false;
		}
		if(this.laserPointer.getTarget()==null) {
			confirmer.sendMessage("§cLe pointeur laser n'a pas de cible.");
			return false;
		}
		double distance = this.location.toVector().subtract(this.laserPointer.getTarget().toVector()).setY(0).length();
		if(this.missileData.getMinRange() <= distance && distance <= this.missileData.getRange()) {
			this.missile = this.missileData.toBalisticMissile(this.location.clone().add(0.5, 1, 0.5));
			return true;
		} else {
			confirmer.sendMessage("§cLa cible n'est pas dans la portée du missile.");
			return false;
		}
	}
	
	private double getMissileDataRadius() {
		return this.missileData.getWeight()*this.missileData.getSpeed()*this.missileData.getSpeed() / this.missileData.getRotatingForce();
	}
	
	public boolean canLaunchMissile() {
		return this.smokeTask==null && this.isLaunchingConfirmed();
	}
	
	public void launchMissile() {
		if(this.isLaunchingConfirmed()) {
			try {
				this.missile.launch(this.laserPointer.getTarget(), this.owner);
				Location loc = this.location.clone().add(0.5, 0, 0.5);
				this.smokeTask = Bukkit.getScheduler().runTaskTimer(Main.i, new Runnable() {
					private double r = 0;
					
					@Override
					public void run() {
						if(r > 3) {
							smokeTask.cancel();
							smokeTask = null;
							return;
						}
						for(int j = 0; j < 5 && r <= 3; j++, r+=0.1) {
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
				}, 5, 1);
				this.missile = null;
				this.missileData = null;
			} catch (Exception e) {
				throw e;
			}
		}
	}
	
	
	public void destroy(boolean remove) {
		if(remove) launchers.remove(this);
		if(missileData!=null) {
			Random random = new Random();
			if(this.laserPointer!=null && random.nextBoolean()) {
				this.location.getWorld().dropItem(this.location, this.laserPointer.toItemStack());
				this.laserPointer=null;
			}
			if(this.owner!=null && random.nextBoolean()) {
				ItemStack is = new ItemStack(Material.PLAYER_HEAD);
				SkullMeta im = (SkullMeta) is.getItemMeta();
				im.setOwningPlayer(this.owner);
				is.setItemMeta(im);
				this.location.getWorld().dropItem(this.location, is);
				this.owner = null;
			}
			(this.missile = this.isLaunchingConfirmed() ? this.missile : this.missileData.toBalisticMissile(this.location)).explode();
		}
		if(this.laserPointer!=null) {
			this.location.getWorld().dropItem(this.location, this.laserPointer.toItemStack());
			this.laserPointer=null;
		}
		if(this.owner!=null) {
			this.location.getWorld().dropItem(this.location, this.getOwnerHead());
			this.owner = null;
		}
	}
	
	
	@SuppressWarnings("deprecation")
	public ItemStack getOwnerHead() {
		ItemStack skull = null;
		if(this.owner!=null) {
			skull = new ItemStack(Material.PLAYER_HEAD);
			SkullMeta sm = (SkullMeta) skull.getItemMeta();
			sm.setOwner(this.owner.getName());
			skull.setItemMeta(sm);
		}
		return skull;
	}
	
	public ItemStack getMissileItem() {
		return this.missileData==null ? null : this.missileData.toItemStack();
	}
	
	public ItemStack getLaserPointerItem() {
		return this.laserPointer==null ? null : this.laserPointer.toItemStack();
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

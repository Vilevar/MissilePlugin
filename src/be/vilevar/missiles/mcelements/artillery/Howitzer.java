package be.vilevar.missiles.mcelements.artillery;

import static be.vilevar.missiles.Main.clamp;

import java.util.ArrayList;
import java.util.Iterator;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Stairs;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import be.vilevar.missiles.Main;
import be.vilevar.missiles.artillery.ShellPath;
import be.vilevar.missiles.artillery.Shell;
import be.vilevar.missiles.utils.ParticleEffect;
import be.vilevar.missiles.utils.Vec3d;

public class Howitzer {

	public static final ArrayList<Howitzer> howitzers = new ArrayList<>();
	private static final int minYawDegrees = -45;
	private static final int maxYawDegrees = 45;
	private static final int minYawMillirad = -99;
	private static final int maxYawMillirad = 99;
	private static final int minPitchDegrees = 20;
	private static final int maxPitchDegrees = 80;
	private static final int minPitchMillirad = -99;
	private static final int maxPitchMillirad = 99;
	private static final Sound BOOM = Sound.ENTITY_ENDER_DRAGON_DEATH;
	
	
	private Main main = Main.i;
	
	private final int direction;
	private final Location loc;
	private final Location firedLoc;
	private final Location particleLoc;
	
	private int yawDegrees = 0;
	private int yawMillirad = 0;
	private int pitchDegrees = 45;
	private int pitchMillirad = 0;
	
	private Shell shell;
	
	private Player open;
	
	public Howitzer(Block block) {
		this.loc = block.getLocation();
		
		Stairs stairs = (Stairs) block.getBlockData();
		
		Location loc = block.getLocation().add(0, 1, 0);
		switch(stairs.getFacing()) {
		case EAST:
			this.direction = 0;
			loc.add(1.1, 0, 0.5);
			this.particleLoc = loc.clone().add(1, 0, 0);
			break;
		case NORTH:
			this.direction = -90;
			loc.add(0.5, 0, -0.1);
			this.particleLoc = loc.clone().add(0, 0, -1);
			break;
		case SOUTH:
			this.direction = 90;
			loc.add(0.5, 0, 1.1);
			this.particleLoc = loc.clone().add(0, 0, 1);
			break;
		case WEST:
			this.direction = 180;
			loc.add(-0.1, 0, 0.5);
			this.particleLoc = loc.clone().add(-1, 0, 0);
			break;
		default:
			this.direction = 0;
			this.particleLoc = loc.clone();
			break;
		}
		
		this.firedLoc = loc;
	}
	
	public int getDirection() {
		return direction;
	}

	public Location getLocation() {
		return loc;
	}
	
	public int getYawDegrees() {
		return yawDegrees;
	}
	
	public void setYawDegrees(int yawDegrees) {
		this.yawDegrees = clamp(minYawDegrees, maxYawDegrees, yawDegrees);
	}

	public void addYawDegrees(int yawDegrees) {
		this.setYawDegrees(this.yawDegrees + yawDegrees);
	}

	public int getYawMillirad() {
		return yawMillirad;
	}

	public void setYawMillirad(int yawMillirad) {
		this.yawMillirad = clamp(minYawMillirad, maxYawMillirad, yawMillirad);
	}
	
	public void addYawMillirad(int yawMillirad) {
		this.setYawMillirad(this.yawMillirad + yawMillirad);
	}

	public double getYaw() {
		return Math.toRadians(this.direction + this.yawDegrees) + ((this.yawMillirad) / 1000.0);
	}
	
	public int getPitchDegrees() {
		return pitchDegrees;
	}

	public void setPitchDegrees(int pitchDegrees) {
		this.pitchDegrees = clamp(minPitchDegrees, maxPitchDegrees, pitchDegrees);
	}
	
	public void addPitchDegrees(int pitchDegrees) {
		this.setPitchDegrees(this.pitchDegrees + pitchDegrees);
	}

	public int getPitchMillirad() {
		return pitchMillirad;
	}

	public void setPitchMillirad(int pitchMillirad) {
		this.pitchMillirad = clamp(minPitchMillirad, maxPitchMillirad, pitchMillirad);
	}
	
	public void addPitchMillirad(int pitchMillirad) {
		this.setPitchMillirad(this.pitchMillirad + pitchMillirad);
	}

	public double getPitch() {
		return Math.toRadians(this.pitchDegrees) + ((this.pitchMillirad) / 1000.0);
	}
	
	
	public Shell getShell() {
		return shell;
	}

	public void setShell(Shell shell) {
		this.shell = shell;
	}
	
	
	public boolean canFire() {
		if(this.main.getGame() != null && !this.main.getGame().isStarted()) {
			return false;
		}
		return this.shell != null;
	}
	
	
	public void fire(Player gunner) {
		new ShellPath(gunner.getWorld(), this.shell, new Vec3d(firedLoc.getX(), firedLoc.getZ(), firedLoc.getY()),
				this.getPitch(), this.getYaw(), gunner).runTaskTimer(main, 1, 1);
		this.shell = null;
		gunner.getWorld().playSound(this.firedLoc, BOOM, 4.f, 1.f);
		
		main.getServer().getScheduler().runTaskLater(main, () -> {
			for(double r = 0; r <= 1; r += 0.2) {
				for(double t = 0; t <= 2*Math.PI; t += Math.PI/8.0) {
					for(double p = 0; p <= 2*Math.PI; p += Math.PI/8.0) {
						Location particle = this.particleLoc.clone().add(
								new Vector(Math.cos(t)*Math.sin(p), Math.sin(t), Math.cos(t)*Math.cos(p)).multiply(r));
						Main.display(ParticleEffect.SMOKE_NORMAL, particle);
					}
				}
			}
		}, 1);
	}
	
	
	public boolean isOpen() {
		return open != null;
	}
	
	public void setOpen(Player open) {
		this.open = open;
	}
	
	
	public void adaptForTarget(Player p, Location loc) {
		try {
			Vector target = loc.toVector().subtract(this.firedLoc.toVector());
			
			double dist = target.clone().setY(0).length();
			
			double g = Main.i.getWorldManager().getG().length();
			double s = Math.sqrt(Math.pow(this.shell.getV0(), 4) - g*(g*Math.pow(dist, 2) + 2*target.getY()*Math.pow(this.shell.getV0(), 2)));
			double maxAngle = Math.atan((Math.pow(this.shell.getV0(), 2) + s) / (g * dist));
			double minAngle = Math.atan((Math.pow(this.shell.getV0(), 2) - s) / (g * dist));
			
			if(this.testPitchAngle(maxAngle) < this.testPitchAngle(minAngle))
				this.testPitchAngle(maxAngle);
			
			double targetAngle = Math.atan2(target.getZ(), target.getX());
			
			double yawAngle = targetAngle;
			switch(this.direction) {
			case 0: // East
				yawAngle = targetAngle;
				break;
			case 90: // South
				yawAngle = targetAngle - Math.PI / 2;
				break;
			case 180: // West
				yawAngle = targetAngle - Math.signum(targetAngle) * Math.PI;
				break;
			case -90: // North
				yawAngle = targetAngle + Math.PI / 2;
				break;
			}
			
			yawAngle = Math.toDegrees(yawAngle);
			
			
			int yawDeg = Main.clamp(minYawDegrees, maxYawDegrees, (int) Math.round(yawAngle));
			int yawMillirad = Main.clamp(minPitchMillirad, maxPitchMillirad, (int) Math.round(Math.toRadians(yawAngle - yawDeg) * 1000.));
			
			this.yawDegrees = yawDeg;
			this.yawMillirad = yawMillirad;
		} catch (Exception e) {
			p.sendMessage("§cProblème survenu lors de la visée.");
		}			
	}
	
	private double testPitchAngle(double angle) {
		double degrees = Math.toDegrees(angle);
		int pitchDeg = Main.clamp(minPitchDegrees, maxPitchDegrees, (int) Math.round(degrees));
		double pitchMilliradRemind = Math.toRadians(degrees - pitchDeg) * 1000.;
		int pitchMillirad = Main.clamp(minPitchMillirad, maxPitchMillirad, (int) Math.round(pitchMilliradRemind));
		
		this.pitchDegrees = pitchDeg;
		this.pitchMillirad = pitchMillirad;
		
		return Math.abs(pitchMilliradRemind - pitchMillirad);
	}
	
	
	public static void checkDestroy(Location loc) {
		Iterator<Howitzer> it = howitzers.iterator();
		while(it.hasNext()) {
			Howitzer howitzer = it.next();
			if(howitzer.getLocation().equals(loc)) {
				it.remove();
				if(howitzer.isOpen()) {
					howitzer.open.closeInventory();
				}
				return;
			}
		}
	}
	
	public static Howitzer getHowitzerAt(Location loc) {
		for(Howitzer howitzer : howitzers) {
			if(howitzer.getLocation().equals(loc)) {
				return howitzer;
			}
		}
		return null;
	}
}

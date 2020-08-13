package be.vilevar.missiles.missile;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import be.vilevar.missiles.Main;
import be.vilevar.missiles.utils.ParticleEffect;

public class RBalisticMissile implements Cloneable {

	public static final ArrayList<RBalisticMissile> launchedMissiles = new ArrayList<>();
	
	public static final float SPEED_CONVERTER = 20;
	public static final double g = 9.806_65;
	
	private final ParticleEffect particle;
	private final double weight; // m
	private final float speed; // v
	private final double maxHeight;
	private double angle;
	
	private final float explosionPower;
	private final boolean explosionSetFire = true;
	private final boolean explosionBreakBlocks = true;
	
	private final Location location;
	
	private Location position;
	private Vector direction;
	private BukkitTask task;
	private Location target;
	private Vector toTarget2D;
	private double time;
	private boolean exploded;
	private Player launcher;
	
	public RBalisticMissile(ParticleEffect particle, float explosionPower, double weight, float speed, double maxHeight, Location location) {
		this.particle = particle;
		this.explosionPower = explosionPower;
		this.weight = weight;
		this.speed = speed;
		this.location = location;
		this.maxHeight = maxHeight;
	}
	
	public ParticleEffect getParticle() {
		return particle;
	}
	
	public double getWeight() {
		return weight;
	}
	
	public float getSpeed() {
		return speed;
	}
	
	public float getExplosionPower() {
		return explosionPower;
	}
	
	public boolean setExplosionFire() {
		return explosionSetFire;
	}
	
	public boolean breakExplosionBlocks() {
		return explosionBreakBlocks;
	}
	
	public Location getLaunchedLocation() {
		return this.location.clone();
	}
	
	public Location getLocation() {
		return position == null ? location.clone() : position.clone();
	}
	
	public Vector getDirection() {
		return direction == null ? null : direction.clone();
	}
	
	public Location getTarget() {
		return target == null ? null : target.clone();
	}
	
	public double getFlightHeight() {
		return maxHeight;
	}
	
	public boolean isLaunched() {
		return task != null;
	}
	
	public int getID() {
		return this.isLaunched() ? this.task.getTaskId() : -1;
	}
	
	public boolean hasExploded() {
		return this.exploded;
	}
	
	public void launch(Location target, Player launcher) {
		if(this.isLaunched()) throw new IllegalStateException("The missile is already launched.");
		if(this.hasExploded()) throw new IllegalStateException("The missile has already exploded.");
		this.position = this.location;
		this.target = target;
		this.toTarget2D = target.toVector().subtract(this.location.toVector());
		double dY = this.toTarget2D.getY();
		this.toTarget2D.setY(0);
		
		double uSqrt = Math.sqrt(Math.pow(this.speed, 4) - g*(g*Math.pow(this.toTarget2D.length(), 2) + 2*dY*Math.pow(this.speed, 2)));
		if(Double.isNaN(uSqrt)) {
			launcher.sendMessage("§cPortée trop longue : "+this.toTarget2D.length()+" VS "+(this.speed/g*Math.sqrt(Math.pow(this.speed, 2)-2*dY*g)));
			return;
		}
		System.out.println(uSqrt);
		double a1 = Math.atan((Math.pow(this.speed, 2) + uSqrt) / (g*this.toTarget2D.length()));
		double a2 = Math.atan((Math.pow(this.speed, 2) - uSqrt) / (g*this.toTarget2D.length()));
		
		/*
		if(a1 < 0) {
			this.angle = a2;
		} else if(a2 < 0) {
			this.angle = a1;
		} else {
			double testAngle = Math.pow(this.speed, 2)/(2*g);
			this.angle = ((testAngle*Math.pow(Math.sin(a1), 2))-this.maxHeight) <= ((testAngle*Math.pow(Math.sin(a2), 2))-this.maxHeight) ? a1 : a2;
		}*/
		this.angle = Math.max(a1, a2);
		
		System.out.println(Math.toDegrees(this.angle));
		
		this.toTarget2D = this.toTarget2D.normalize();
		this.launcher = launcher;
		
	//	this.createStartExplosion();
		
		this.task = Bukkit.getScheduler().runTaskTimer(Main.i, () -> {
			
			if(Double.isNaN(this.angle)) return;
			
			if(this.time >= 100) this.task.cancel();
			
			double t;
			for(t = last; t <= last+10; t+=.005) {
				Vector v = this.toTarget2D.clone().multiply(Math.cos(this.angle)*t*this.speed);
				v.setY(-.5*g*Math.pow(t, 2) + Math.sin(this.angle)*t*this.speed);
				this.position = this.location.clone().add(v);
				Main.display(this.particle, this.position);
				
				if(this.position.getBlock().getType().isSolid() && !this.position.getBlock().getLocation().equals(this.location)) {
					t = 0;
					this.time++;
					break;
				}
			}
			
			last = t;
			
		//	System.out.println(time+" "+Math.cos(this.angle)*time*this.speed+" ("+this.position.getX()+", "+this.position.getY()+", "+this.position.getZ()+")");
		}, 1, 1);
		
		if(this.launcher!=null)
			this.launcher.sendMessage("§6Missile §c["+this.getID()+"]§6 tiré. Cible à §a"+
					target.toVector().subtract(this.location.toVector()).setY(0).length()+"§6 mètres.");
	}
	
	private double last;
	
	private void displayParticle() {
		if(this.position.getBlock().getType().isSolid() && this.time >= 30) {
			this.task.cancel();
			this.launcher.sendMessage("Finish");
		}
		Main.display(this.particle, this.position);
	}
	
	@Override
	public RBalisticMissile clone() {
		return new RBalisticMissile(particle, explosionPower, weight, speed, maxHeight, location);
	}
	
}

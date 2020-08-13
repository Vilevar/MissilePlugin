package be.vilevar.missiles.missile;

import java.math.BigDecimal;
import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import be.vilevar.missiles.Main;
import be.vilevar.missiles.utils.ParticleEffect;

public class BalisticMissile implements Cloneable {

	public static final ArrayList<BalisticMissile> launchedMissiles = new ArrayList<>();
	
	public static final float SPEED_CONVERTER = 20;
	private static final float PRECISION = 0.2f;
	public static final double GRAVITY = 10;
	
	private final ParticleEffect particle;
	private final double weight; // m
	private final double rotatingForce; // F   -->   F = m v² / r
	private final float speed; // v
	private final double radius1; // r minimum
	private final double radius2; // r minimum with gravity
	private final double minRange;
	private final double range;
	private final float mcSpeed;
	private final double flightHeight;
	private final double detectorDistance;
	private final int n;
	private final float r;
	
	private final float explosionPower;
	private final boolean explosionSetFire = true;
	private final boolean explosionBreakBlocks = true;
	
	private final Location location;
	
	private Location position;
	private Vector direction;
	private BukkitTask task;
	private Location target;
	private Location beginLand;
	private Location beginFlight;
	private State state;
	private double nRotation = 0;
	private Vector toTarget2D;
	private boolean exploded;
	private long flightStart;
	private Player launcher;
	
	public BalisticMissile(ParticleEffect particle, float explosionPower, double weight, double rotatingForce,
			double range, float speed, double flightHeight, double detectorDistance, Location location) {
		this.particle = particle;
		this.explosionPower = explosionPower;
		this.weight = weight;
		this.rotatingForce = rotatingForce;
		this.speed = speed;
		this.mcSpeed = speed / SPEED_CONVERTER;
		double a = weight * this.speed * this.speed;
		this.radius1 = a / rotatingForce;
		this.radius2 = a / (rotatingForce + (GRAVITY * weight));
		a *= 1.1;
		this.minRange = (a / rotatingForce) + (a / (rotatingForce + (GRAVITY * weight)));
		this.range = range;
		this.location = location;
		this.flightHeight = flightHeight;
		this.detectorDistance = detectorDistance;
		BigDecimal[] decs = new BigDecimal(mcSpeed).divideAndRemainder(new BigDecimal(PRECISION));
		this.n = decs[0].intValue();
		this.r = decs[1].floatValue();
	}
	
	public ParticleEffect getParticle() {
		return particle;
	}
	
	public double getWeight() {
		return weight;
	}
	
	public double getRotatingForce() {
		return rotatingForce;
	}
	
	public float getSpeed() {
		return speed;
	}
	
	public double getMinRange() {
		return minRange;
	}
	
	public double getRange() {
		return range;
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
		return flightHeight;
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
	
	private boolean canLaunch(Location target) {
		double distance = target.toVector().subtract(this.location.clone().add(-0.5, 0, -0.5).toVector()).setY(0).length();
		return minRange <= distance && distance <= range;
	}
	
	public void launch(Location target, Player launcher) {
		if(this.isLaunched()) throw new IllegalStateException("The missile is already launched.");
		if(this.hasExploded()) throw new IllegalStateException("The missile has already exploded.");
		if(!this.canLaunch(target)) throw new IllegalArgumentException("The target is not in the range of the missile.");
		this.position = this.location;
		this.target = target;
		this.state = State.TAKE_OFF;
		this.direction = new Vector(0, 1, 0);
		this.toTarget2D = target.toVector().setY(0).subtract(this.location.toVector().setY(0)).normalize();
		(this.beginLand = target.clone().add(this.toTarget2D.clone().multiply(-radius2))).setY(flightHeight);
		this.launcher = launcher;
		this.flightStart = System.currentTimeMillis();
		
		this.task = Bukkit.getScheduler().runTaskTimer(Main.i, () -> {
			
			for(int i = 0; i < n + 1 && !exploded; i++) {
				float speed = i == n ? r : PRECISION;
				if(speed == 0) continue;
				float nextSpeed = (i+1) < n || (i==n && n>=1) ? PRECISION : r;
				
				if(this.state == State.TAKE_OFF) {
					if(this.position.getY() >= (flightHeight-radius1)) {
						this.direction = this.rotationToFlight(nRotation+=speed);
						this.state = State.BEGIN_FLIGHT;
						(this.beginFlight = this.position).setY(flightHeight-radius1);
					} else {
						this.direction.normalize().multiply(speed);
					}
				} else
				if(this.state == State.BEGIN_FLIGHT) {
					Vector v = this.rotationToFlight(nRotation+=speed);
					if(v==null) {
						v = this.toTarget2D.clone().multiply(radius1).setY(radius1);
						this.direction = this.toTarget2D.clone();
						this.state = State.FLIGHT;
						this.nRotation = 0;
					} else {
						this.direction = v;
					}
					this.position = this.beginFlight.clone().add(v);
					this.displayParticle();
					continue;
				} else
				if(this.state == State.FLIGHT) {
					double distance = this.beginLand.toVector().subtract(this.position.toVector()).setY(0).length();
					if(distance < nextSpeed) {
						this.direction = this.rotationToLand(nRotation+=speed);
						this.state = State.BEGIN_LAND;
						this.beginLand = this.position;
					} else {
						this.direction.normalize().multiply(speed);
						if(distance == nextSpeed) {
							this.state = State.BEGIN_LAND;
						}
					}
				} else
				if(this.state == State.BEGIN_LAND) {
					Vector v = this.rotationToLand(nRotation+=speed);
					if(v==null) {
						v = this.toTarget2D.clone().multiply(radius2).setY(-radius2);
						this.direction = new Vector(0, -1, 0);
						this.state = State.LANDING;
						this.nRotation = 0;
					} else {
						this.direction = v;
					}
					this.position = this.beginLand.clone().add(v);
					this.displayParticle();
					continue;
				} else
				if(this.state == State.LANDING) {
					this.direction.normalize().multiply(speed);
				}
				
				this.position.add(this.direction);
				this.displayParticle();
				
			}
		}, 1, 1);
		
		if(this.launcher!=null)
			this.launcher.sendMessage("§6Missile §c["+this.getID()+"]§6 tiré. Cible à §a"+
					target.toVector().subtract(this.location.toVector()).setY(0).length()+"§6 mètres.");
		
		launchedMissiles.add(this);
	}
	
	public void explode() {
		if(this.exploded) return;
		if(this.task!=null)
			this.task.cancel();
		Location l = this.position == null ? this.location : this.position;
	//	MegaExplosion.createExplosion(l, explosionPower, launcher);
		l.getWorld().createExplosion(l.getX(), l.getY(), l.getZ(), this.explosionPower, this.explosionSetFire, this.explosionBreakBlocks);
		this.exploded = true;
		
		if(launchedMissiles.contains(this)) {
			launchedMissiles.remove(this);
		}
		
		if(this.isLaunched() && this.launcher!=null && this.launcher.isOnline()) {
			this.launcher.sendMessage("§6Missile §c["+this.getID()+"]§6 a explosé à environ §c"+Math.round(this.position.distance(this.target))
					+ " mètres§6 de la cible.");
			this.launcher.sendMessage("§6Temps de vol : §a"+((int) ((System.currentTimeMillis() - this.flightStart) / 1000))+" secondes§6.");
		}
	}
	
	private void displayParticle() {
		Material type = this.position.getBlock().getType();
		if(type.isSolid() || type==Material.FIRE || type==Material.LAVA || this.checkDetector()) {
			this.explode();
		} else {
			for(Entity ent : this.position.getChunk().getEntities()) {
				if(ent.getLocation().distance(this.position)<=0.1) {
					this.explode();
					return;
				}
			}
			Main.display(this.particle, this.position);
		}
	}
	
	private boolean checkDetector() {
		Location loc = this.position.clone();
		Vector dir = this.getTanVector();
		for(int i = 1; i <= this.detectorDistance; i++) {
			loc.add(dir);
			Material type = loc.getBlock().getType();
			if(type.isSolid()) {
				return true;
			} else {
				for(Entity ent : this.position.getChunk().getEntities()) {
					if(ent.getLocation().distance(this.position)<=0.1) {
						return true;
					}
				}
			}
		}
		return false;
	}
	
	private Vector getTanVector() { // TODO
		return this.direction.clone().normalize();
	}
	
	private Vector rotationToFlight(double x) {
		double alpha = x/radius1;
		if(alpha >= Math.PI/2) return null;
		Vector v = this.toTarget2D.clone();
		v.multiply(radius1*(1-Math.cos(alpha))).setY(radius1*Math.sin(alpha));
		return v;
	}
	
	private Vector rotationToLand(double x) {
		double alpha = x/radius2;
		if(alpha >= Math.PI/2) return null;
		Vector v = this.toTarget2D.clone();
		v.multiply(radius2*Math.sin(alpha)).setY(-(radius2*(1-Math.cos(alpha))));
		return v;
	}
	
	private enum State {
		TAKE_OFF(true), BEGIN_FLIGHT(false), FLIGHT(true), BEGIN_LAND(false), LANDING(true);
		State(boolean stable) {}
	}
	
	@Override
	public BalisticMissile clone() {
		return new BalisticMissile(particle, this.explosionPower, weight, rotatingForce, range, speed, flightHeight, this.detectorDistance, location);
	}
}

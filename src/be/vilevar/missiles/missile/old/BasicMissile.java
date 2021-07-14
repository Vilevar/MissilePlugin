package be.vilevar.missiles.missile.old;
//
//import java.math.BigDecimal;
//import java.util.ArrayList;
//import java.util.Random;
//
//import org.bukkit.Bukkit;
//import org.bukkit.Location;
//import org.bukkit.Material;
//import org.bukkit.entity.Entity;
//import org.bukkit.entity.Player;
//import org.bukkit.scheduler.BukkitTask;
//import org.bukkit.util.Vector;
//
//import be.vilevar.missiles.Main;
//import be.vilevar.missiles.utils.ParticleEffect;
//

public class BasicMissile {
	
}

//public class BasicMissile implements Cloneable {
//
//	public static final ArrayList<BasicMissile> launchedMissiles = new ArrayList<>();
//	
//	public static final float SPEED_CONVERTER = 20;
//	private static final float PRECISION = 0.2f;
//	
//	private final ParticleEffect particle;
//	private final double weight;
//	private final double rotatingForce;
//	private final double range;
//	private final float speed;
//	private final float mcSpeed;
//	private final double minRadius;
//	private final int n;
//	private final float r;
//	
//	private final float explosionPower;
//	private final boolean explosionSetFire = true;
//	private final boolean explosionBreakBlocks = false;
//	
//	private Player launcher;
//	private Target target;
//	private BukkitTask task;
//	private boolean exploded;
//	private Location location;
//	private Vector direction;
//	private long flightStart;
//	
//	public BasicMissile(ParticleEffect particle, float explosionPower, double weight, double rotatingForce, double range, float speed) {
//		this.particle = particle;
//		this.explosionPower = explosionPower;
//		this.weight = weight;
//		this.rotatingForce = rotatingForce;
//		this.range = range;
//		this.speed = speed;
//		this.mcSpeed = speed/SPEED_CONVERTER;
//		this.minRadius = (weight * this.speed * this.speed) / rotatingForce;
//		BigDecimal[] decs = new BigDecimal(mcSpeed).divideAndRemainder(new BigDecimal(PRECISION));
//		this.n = decs[0].intValue();
//		this.r = decs[1].floatValue();
//	}
//	
//	public ParticleEffect getParticle() {
//		return particle;
//	}
//	
//	public double getWeight() {
//		return weight;
//	}
//	
//	public double getRotatingForce() {
//		return rotatingForce;
//	}
//	
//	public float getSpeed() {
//		return speed;
//	}
//	
//	public double getRange() {
//		return range;
//	}
//	
//	public float getExplosionPower() {
//		return explosionPower;
//	}
//	
//	public boolean setExplosionFire() {
//		return explosionSetFire;
//	}
//	
//	public boolean breakExplosionBlocks() {
//		return explosionBreakBlocks;
//	}
//	
//	public Location getLocation() {
//		return location == null ? null : location.clone();
//	}
//	
//	public Vector getDirection() {
//		return direction == null ? null : direction.clone();
//	}
//	
//	public Target getTarget() {
//		return target;
//	}
//	
//	public boolean isLaunched() {
//		return task != null;
//	}
//	
//	public int getID() {
//		return this.isLaunched() ? this.task.getTaskId() : -1;
//	}
//	
//	public boolean hasExploded() {
//		return this.exploded;
//	}
//	
//	private boolean canLaunch(Location target) {
//		// todo
//		return true;
//	}
//	
//	Random rand = new Random();
//	
//	public void launch(Target target, Player launcher) {
//		if(this.isLaunched()) throw new IllegalStateException("The missile is already launched.");
//		if(this.hasExploded()) throw new IllegalStateException("The missile has already exploded.");
//		if(!this.canLaunch(target.getTarget())) throw new IllegalArgumentException("The target is not in the range of the missile.");
//		this.target = target;
//		this.launcher = launcher;
//		this.location = launcher.getEyeLocation();
//		this.flightStart = System.currentTimeMillis();
//		this.direction = target.getTarget().toVector().subtract(this.location.toVector());
//		
//		this.task = Bukkit.getScheduler().runTaskTimer(Main.i, () -> {
//			
//			boolean isObstructed = false;
//			o: do {
//				
//			//	System.out.println("[b] "+isObstructed);
//				
//				// todo
//				if(!isObstructed)
//					this.direction = target.getTarget().toVector().subtract(this.location.toVector()).normalize();
//				else
//					this.direction.add(new Vector(rand.nextDouble()*2-1, rand.nextDouble()*2-1, rand.nextDouble()*2-1)).normalize();
//				
//				Location l = this.location.clone();
//				double max = Math.ceil(this.mcSpeed);
//				
//				for(int i = 0; i < max; i++) {
//					if(isObstructed = this.isCollision(l.add(this.direction))) {
//				//		System.out.println(isObstructed);
//						continue o;
//					}
//				}
//				
//			} while(isObstructed);
//			
//			for(int i = 0; i < n + 1 && !exploded; i++) {
//				float speed = i == n ? r : PRECISION;
//				if(speed == 0) continue;
//				float nextSpeed = (i+1) < n || (i==n && n>=1) ? PRECISION : r;
//				
//				this.direction.normalize().multiply(speed);
//				
//				this.location.add(this.direction);
//				this.displayParticle();
//			}
//		}, 1, 1);
//		
//		if(this.launcher!=null)
//			this.launcher.sendMessage("§6Missile §c["+this.getID()+"]§6 lancé. Cible à §a"+this.direction.length()+"§6 mètres.");
//		
//		launchedMissiles.add(this);
//	}
//	
//	
//	
//	public void explode() {
//		if(this.exploded || this.task==null || this.location==null) return;
//		this.task.cancel();
//		Location l = this.location;
//		l.getWorld().createExplosion(l.getX(), l.getY(), l.getZ(), this.explosionPower, this.explosionSetFire, this.explosionBreakBlocks);
//		this.exploded = true;
//		
//		if(launchedMissiles.contains(this)) {
//			launchedMissiles.remove(this);
//		}
//		
//		if(this.isLaunched() && this.launcher!=null && this.launcher.isOnline()) {
//			this.launcher.sendMessage("§6Missile §c["+this.getID()+"]§6 a explosé à environ §c"+Math.round(this.location.distance(this.target.getTarget()))
//					+ " mètres§6 de la cible.");
//			this.launcher.sendMessage("§6Temps de vol : §a"+((int) ((System.currentTimeMillis() - this.flightStart) / 1000))+" secondes§6.");
//		}
//	}
//	
//	private void displayParticle() {
//		if(this.isCollision(this.location) || this.isNearEnoughToExplode(this.location))
//			this.explode();
//		else
//			Main.display(this.particle, this.location);
//	}
//	
//	private boolean isNearEnoughToExplode(Location loc) {
//		return loc.distance(this.target.getTarget()) <= 1;
//	}
//	
//	private boolean isCollision(Location loc) {
//		Material type = loc.getBlock().getType();
//		if(type.isSolid() || type==Material.FIRE || type==Material.LAVA) {
//			return true;
//		} else {
//			for(Entity ent : loc.getChunk().getEntities()) {
//				if(ent.getLocation().distance(this.location)<=0.1) {
//					return true;
//				}
//			}
//		}
//		return false;
//	}
//	
//	@Override
//	public BasicMissile clone() {
//		return new BasicMissile(particle, this.explosionPower, weight, rotatingForce, range, speed);
//	}
//	
//	
//	
//	
//	
//	
//	public class Target {
//		
//		private Location loc;
//		private Entity entity;
//		
//		public Target(Location loc) {
//			this.loc = loc;
//		}
//		
//		public Target(Entity entity) {
//			this.entity = entity;
//		}
//		
//		public Location getTarget() {
//			return this.entity == null ? this.loc : this.entity.getLocation();
//		}
//	}
//	
//}

package be.vilevar.missiles.missile.ballistic;

import static java.lang.Math.cos;
import static java.lang.Math.pow;
import static java.lang.Math.sin;

import java.util.ArrayList;
import java.util.Iterator;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.craftbukkit.libs.org.apache.commons.lang3.tuple.Pair;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import be.vilevar.missiles.Main;
import be.vilevar.missiles.WorldManager;
import be.vilevar.missiles.missile.ballistic.explosives.Detonation;
import be.vilevar.missiles.missile.ballistic.explosives.ExplosiveManager;
import be.vilevar.missiles.utils.Vec3d;

public class ReentryVehicle {

	public static final ArrayList<ReentryVehicle> air = new ArrayList<>();
	
	private double surface = 0.981;
	private double cd = 0.176;
	private double mass = 10;
	
	private double theta;
	private double psi;
	private double[][] matrix;
	private Explosive explosive;
	private int yExplosion;
	
	private WorldManager wm = Main.i.getWorldManager();
	private double dt = 0.01;
	
	private int id;
	private Vec3d pos;
	private Vec3d velocity;
	private Location loc;
	private boolean exploded = false;

	private Player launcher;
	
	public ReentryVehicle(double theta, double psi, Explosive explosive, int yExplosion) {
		theta = -theta;
		psi = -psi;
		
		this.theta = theta;
		this.psi = psi;
		
		this.matrix = new double[][] {{cos(psi), -sin(psi), 0}, {sin(psi), cos(psi), 0}, {0, 0, 1}};
		
		this.explosive = explosive;
		
		this.yExplosion = yExplosion;
	}
	
	public double getMass() {
		return mass;
	}
	
	public double getTheta() {
		return theta;
	}
	
	public double getPsi() {
		return psi;
	}
	
	public Vec3d getNewVelocity(Vec3d velocity) {
		return velocity.clone().matrixProduct(this.matrixProduct(this.makeMatrix(velocity), matrix));
	}
	
	public void launch(Player launcher, Vec3d x, Vec3d v) {
		this.launcher = launcher;
		x = x.clone();
		v = v.clone().matrixProduct(this.matrixProduct(this.makeMatrix(v), matrix));
		
		double t = 0;
		
		while(x.getZ() > 500) {
			Vec3d dv = new Vec3d(0, 0, -wm.getGM() / pow(wm.getR() + x.getZ(), 2) * dt);
			
			v.add(dv);
			x.add(v.clone().multiply(dt));
			
			t += dt;
		}
		
		final Vec3d finalX = x;
		final Vec3d finalV = v;
		
		System.out.println("MIRV will enter at "+x+" with speed "+v+" "+t);
		
		new BukkitRunnable() {
			@Override
			public void run() {
				System.out.println("Finish 2");
				ReentryVehicle.this.airReentry(launcher, finalX, finalV);
			}
		}.runTaskLater(Main.i, (int) (20 * t));
	}
	
	private void airReentry(Player launcher, Vec3d x, Vec3d v) {
		air.add(this);
		
		x = x.clone();
		v = v.clone();
		
		System.out.println("AIR "+x);
		System.out.println(v);
		
		ArrayList<Pair<Vec3d, Vec3d>> states = new ArrayList<>();
		Iterator<Pair<Vec3d, Vec3d>> it;
		
		World world = launcher.getWorld();
		double forceCoef = -0.5 * this.surface * this.cd * wm.getAirDensity(world);
		Vec3d wind = wm.getWind(world);

//		Vec3d wind = new Vec3d(32.5, 0, 0); 0/5
//		Vec3d wind = new Vec3d(-32.5, 0, 0); 1/5
//		Vec3d wind = new Vec3d(0, 32.5, 0); 2/5
//		Vec3d wind = new Vec3d(0, -32.5, 0); 1/5
		
//		Vec3d wind = new Vec3d(0, 0, 0);
		
		while(x.getZ() >= this.yExplosion) {
			
			Vec3d dv = new Vec3d(0, 0, -wm.getGM() / pow(wm.getR() + x.getZ(), 2));
			
			if(x.getZ() <= 300) {
				Vec3d force = v.clone().subtract(wind);
				force.multiply(forceCoef * force.length());
				
				dv.add(force.divide(mass));
			}
			dv.multiply(dt);
			
			v.add(dv);
			x.add(v.clone().multiply(dt));
			
			states.add(Pair.of(x.clone(), v.clone()));
		}
		it = states.iterator();
		
		System.out.println("MIRV will explode at "+x+" "+v);
		
		final Vec3d finalX = x;
		
		this.id = new BukkitRunnable() {
			@Override
			public void run() {
				if(ReentryVehicle.this.exploded) {
					this.cancel();
					return;
				}
				if(it.hasNext()) {
					for(double t = 0; t < 0.05 && it.hasNext(); t += dt) {
						Pair<Vec3d, Vec3d> state = it.next();
						Vec3d x = state.getLeft();
						
						if(x.getZ() <= 256) { 
							loc = x.toLocation(world);
							Material block = loc.getBlock().getType();
							if(block.isSolid()) {
								ReentryVehicle.this.explode(launcher, loc);
								this.cancel();
								return;
							}
							Main.display(Particle.FLAME, loc);
						}
						
						pos = x;
						velocity = state.getRight();
					}
				} else {
					ReentryVehicle.this.explode(launcher, finalX.toLocation(world));
					this.cancel();
				}
			}
		}.runTaskTimer(Main.i, 1, 1).getTaskId();
	}
	
	public void intercepted(Player launcher, Location loc) {
		this.explode(launcher, loc, true);
	}

	public void explode(Player launcher, Location loc) {
		this.explode(launcher, loc, false);
	}
	
	private void explode(Player launcher, Location loc, boolean intercepted) {
		ExplosiveManager.addDetonation(new Detonation(this.explosive, loc, this.launcher, intercepted));
		launcher.sendMessage("§6MIRV "+this.getSignature()
				+ "§6 atterrie en §cx=§a" + loc.getBlockX() + " §cy=§a" + loc.getBlockY() + " §cz=§a" + loc.getBlockZ());
		air.remove(this);
		this.exploded = true;
	}
	
	public int getId() {
		return id;
	}
	
	public String getSignature() {
		return "§c["+this.id+"] ";
	}
	
	public Vec3d getPosition() {
		return pos;
	}
	
	public Vec3d getVelocity() {
		return velocity;
	}
	
	public Player getLauncher() {
		return launcher;
	}
	
	public Location getLocation() {
		return loc;
	}
	
	public boolean isExploded() {
		return exploded;
	}
	
	private double[][] makeMatrix(Vec3d vector) {
		Vec3d w = new Vec3d(-vector.getY(), vector.getX(), 0).normalize();
		double x = w.getX(), y = w.getY(), z = w.getZ();
		double ca = cos(theta);
		double pa = 1 - ca;
		double sa = sin(theta);
		
		return new double[][] {
			{x*x*pa + ca, 	x*y*pa - z*sa, 	x*z*pa + y*sa},
			{x*y*pa + z*sa,	y*y*pa + ca,	y*z*pa - x*sa},
			{x*z*pa - y*sa,	y*z*pa + x*sa,	z*z*pa + ca}
		};
	}
	
	private double[][] matrixProduct(double[][] a, double[][] b) {
		double[][] c = {new double[3], new double[3], new double[3]};
		for(int i = 0; i < 3; i++) {
			for(int j = 0; j < 3; j++) {
				for(int k = 0; k < 3; k++) {
					c[i][j] += a[i][k] * b[k][j];
				}
			}
		}
		return c;
	}
}

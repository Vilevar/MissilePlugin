package be.vilevar.missiles.missile.ballistic;

import static java.lang.Math.cos;
import static java.lang.Math.pow;
import static java.lang.Math.sin;

import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import be.vilevar.missiles.Main;
import be.vilevar.missiles.WorldManager;
import be.vilevar.missiles.missile.ballistic.explosives.Detonation;
import be.vilevar.missiles.missile.ballistic.explosives.ExplosiveManager;
import be.vilevar.missiles.utils.ParticleEffect;
import be.vilevar.missiles.utils.Vec3d;

public class ReentryVehicle {

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
		x = x.clone();
		v = v.clone().matrixProduct(this.matrixProduct(this.makeMatrix(v), matrix));
		
//		int count = 0;
//		HashMap<Integer, Pair<Vec3d, Vec3d>> states = new HashMap<>();
		double t = 0;
		
		while(x.getZ() > 300) {
			Vec3d dv = new Vec3d(0, 0, -wm.getGM() / pow(wm.getR() + x.getZ(), 2) * dt);
			
			v.add(dv);
			x.add(v.clone().multiply(dt));
			
			t += dt;
		//	states.put(count++, Pair.of(x, v));
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
		x = x.clone();
		v = v.clone();
		
		System.out.println("AIR "+x);
		System.out.println(v);
		
		int count = 0;
		HashMap<Integer, Vec3d> positions = new HashMap<>();
		
		World world = launcher.getWorld();
		double forceCoef = -0.5 * this.surface * this.cd * wm.getAirDensity(world);
		Vec3d wind = wm.getWind(world);
		
		while(x.getZ() >= this.yExplosion) {
			Vec3d force = v.clone().subtract(wind);
			force.multiply(forceCoef * force.length());
			
			Vec3d dv = new Vec3d(0, 0, -wm.getGM() / pow(wm.getR() + x.getZ(), 2)).add(force.divide(mass)).multiply(dt);
			
			v.add(dv);
			x.add(v.clone().multiply(dt));
			
			positions.put(count++, x.clone());
		}
		
		System.out.println("MIRV will explode at "+x+" "+v);
		
		final Vec3d finalX = x;
		
		new BukkitRunnable() {
			private int i;
			
			@Override
			public void run() {
				if(!positions.isEmpty()) {
					for(double t = 0; t < 0.05 && !positions.isEmpty(); t += dt) {
						Vec3d x = positions.get(i);
						positions.remove(i++);
						
						Location loc = new Location(world, x.getX(), x.getZ(), x.getY());
						Material block = loc.getBlock().getType();
						if(block != Material.AIR && block != Material.VOID_AIR) {
							ReentryVehicle.this.explode(launcher, loc);
							this.cancel();
							positions.clear();
							return;
						}
						Main.display(ParticleEffect.FLAME, loc);
					}
				} else {
					ReentryVehicle.this.explode(launcher, new Location(world, finalX.getX(), finalX.getZ(), finalX.getY()));
					this.cancel();
				}
			}
		}.runTaskTimer(Main.i, 1, 1);
	}
	
	public void explode(Player launcher, Location loc) {
		ExplosiveManager.addDetonation(new Detonation(this.explosive, loc, launcher));
		launcher.sendMessage(loc.toString());
		// world.createExplosion(loc, this.shell.getPower(), this.shell.setFire(), true, this.gunner);
		// this.gunner.sendMessage("§6Obus explosé à §cx=§a"+loc.getBlockX()+" §cy=§a"+loc.getBlockY()+" §cz=§a"+loc.getBlockZ());
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

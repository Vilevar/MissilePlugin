package be.vilevar.missiles.mcelements.abm;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;

import be.vilevar.missiles.Main;
import be.vilevar.missiles.missile.ballistic.ReentryVehicle;
import be.vilevar.missiles.utils.Vec3d;

public class ABM {
	
	private static final double explosiveRange = 4;
	private static final double V = 200;
	private static final double V_SQUARED = V*V;
	
	private double g = Main.i.getWorldManager().getGravityConstant();

	private double dt = 0.005;
	
	private Vec3d lastV;

	private boolean exploded = false;

	public boolean shoot(ABMLauncher launcher, ReentryVehicle target, boolean sendMessage) {
		Vec3d[] states = this.computeTrajectory(launcher.getFiringLocation(), target, true, 2);
		
		if(states == null) {
			return false;
		}
		
		World world = launcher.getLocation().getWorld();

		new BukkitRunnable() {
			int i = 0;
			Location loc;
			
			@Override
			public void run() {
				if (i < states.length) {
					for (double t = 0; t < 0.05 && i < states.length; t += dt) {
						Vec3d state = states[i++];
						
						if(state == null) {
							ABM.this.secondTrajectory(launcher, world, this.loc, target, sendMessage);
							this.cancel();
							return;
						} else {
							loc = state.toLocation(world);
							Material block = loc.getBlock().getType();
							if((loc.getBlockY() <= 256 && block.isSolid()) || target.getLocation().distance(loc) <= explosiveRange) {
								System.out.println("MIRV exploded during flight 1 "+block+" "+block.isSolid());
								ABM.this.explode(launcher, target, loc, sendMessage);
								this.cancel();
								return;
							}
							
							Main.display(Particle.FLAME, loc);
						}
						
					}
				} else {
					ABM.this.secondTrajectory(launcher, world, this.loc, target, sendMessage);
					this.cancel();
				}
			}
		}.runTaskTimer(Main.i, 1, 1);
		
		return true;
	}
	
	public boolean explode(ABMLauncher launcher, ReentryVehicle target, Location loc, boolean sendMessage) {
		System.out.println("MIRV exploded "+target.getLocation().distance(loc)+" "+target.isExploded());
		if (target.getLocation().distance(loc) <= explosiveRange) {
			target.intercepted(target.getLauncher(), target.getLocation());
			
			if(sendMessage) {
				launcher.getDefender().sendMessage(launcher.getSignature() 
						+ "§dMIRV " + target.getSignature()+"§dinterceptée en §cx=§a" + loc.getBlockX() + " §cy=§a" + loc.getBlockY() 
						+ " §cz=§a" + loc.getBlockZ());
			}
			return true;
		}
		
		this.exploded = true;
		Main.display(Particle.EXPLOSION_LARGE, loc);
		
		return false;
	}

	public boolean isExploded() {
		return this.exploded;
	}

	
	private void secondTrajectory(ABMLauncher launcher, World world, Location loc, ReentryVehicle target, boolean sendMessage) {
		Vec3d[] states = this.computeTrajectory(loc, target, false, 1);
		
		if(states == null) {
			this.explode(launcher, target, loc, sendMessage);
			return;
		}

		new BukkitRunnable() {
			int i = 0;
			Location loc;
			
			@Override
			public void run() {
				if (i < states.length) {
					for (double t = 0; t < 0.05 && i < states.length; t += dt) {
						Vec3d state = states[i++];
						
						if(state == null) {
							ABM.this.explode(launcher, target, this.loc, sendMessage);
							this.cancel();
							return;
						} else {
							loc = state.toLocation(world);
							
							Material block = loc.getBlock().getType();
							if((loc.getBlockY() <= 256 && block != Material.AIR && block != Material.VOID_AIR) ||
									target.getLocation().distance(loc) <= explosiveRange) {
								System.out.println("MIRV exploded during flight 2 "+block+" "+block.isSolid());
								ABM.this.explode(launcher, target, loc, sendMessage);
								this.cancel();
								return;
							}
							
							Main.display(Particle.FLAME, loc);
						}
						
					}
				} else {
					ABM.this.explode(launcher, target, this.loc, sendMessage);
					this.cancel();
				}
			}
		}.runTaskTimer(Main.i, 1, 1);
		
	}
	
	
	private Vec3d[] computeTrajectory(Location firingLocation, ReentryVehicle target, boolean testPos, int div) {
		Vec3d relativePos = new Vec3d(target.getLocation().clone().subtract(firingLocation));
		Vec3d u = target.getVelocity();
		
		if(testPos && relativePos.getZ() <= 0) {
			return null;
		}
		
		double A = u.squaredLength() - V_SQUARED;
		double B = relativePos.dot(u);
		double C = relativePos.squaredLength();
		
		double T;
		
		if(A == 0) {
			T = -C / (2 * B);
		} else {
			double D = B*B - A*C;
			
			if(D < 0) {
				return null;
			} else {
				T = -(B + Math.sqrt(D)) / A;
			}
		}
		
		if(T <= 0) {
			return null;
		}
		
		Vec3d v = u.clone().add(relativePos.clone().divide(T));
		
		T /= div;
		
		System.out.println("T = "+T);
		
		System.out.println("V ABM ("+(3 - div)+") = "+v+" "+v.length());
		if(this.lastV != null)
			System.out.println("Déviation "+this.lastV.angleWith(v));
		
		if(testPos && v.getZ() * T - g*T*T <= 0) {
			return null;
		}
		
		Vec3d x = new Vec3d(firingLocation);
		
		Vec3d[] states = new Vec3d[(((int) (T / dt)) + 6)];
		int index = 0;

		for(double t = 0; t <= T; t += dt) {
			x.add(v.clone().multiply(dt));
			x.add(0, 0, -0.5 * g * dt*dt);
			
			v.add(0, 0, -g * dt);
			
			states[index++] = x.clone();
		}
		
		this.lastV = v;
		
		
		return states;
	}
	
}

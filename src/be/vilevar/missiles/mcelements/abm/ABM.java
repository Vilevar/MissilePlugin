package be.vilevar.missiles.mcelements.abm;

import java.util.ArrayList;
import java.util.Iterator;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;

import be.vilevar.missiles.Main;
import be.vilevar.missiles.defense.Defender;
import be.vilevar.missiles.missile.ballistic.ReentryVehicle;
import be.vilevar.missiles.utils.Vec3d;

public class ABM {
	
	private static final double explosiveRange = 5;
	
	private double g = Main.i.getWorldManager().getGravityConstant();

	private double dt = 0.005;

	private boolean exploded = false;

	public void shoot(ABMLauncher launcher, ReentryVehicle target, Vec3d v, double T, boolean sendMessage) {
		World world = launcher.getLocation().getWorld();
		Vec3d x = new Vec3d(launcher.getFiringLocation());
		
//		ArrayList<Vec3d> states = new ArrayList<>();
//		Iterator<Vec3d> it;
		Vec3d[] states = new Vec3d[((int) (T / dt)) + 6];
		System.out.println("States : "+states.length);
		int index = 0;

		for(double t = 0; t <= T; t += dt) {
			x.add(v.clone().multiply(dt));
			x.add(0, 0, -0.5 * g * dt*dt);
			
			v.add(0, 0, -g * dt);
			
			System.out.println(t+" "+index);
			states[index++] = x.clone();
		}
		final int l = index;
		
//		it = states.iterator();

		final Vec3d finalX = x;

		new BukkitRunnable() {
			int i = 0;
			
			@Override
			public void run() {
				if (i < l) {
					for (double t = 0; t < 0.05 && i < l; t += dt) {
						Vec3d state = states[i++];

						Location loc = state.toLocation(world);
						Material block = loc.getBlock().getType();
						if((loc.getBlockY() <= 256 && block != Material.AIR && block != Material.VOID_AIR) ||
								target.getLocation().distance(loc) <= explosiveRange) {
							
							ABM.this.explode(launcher.getDefender(), target, loc, sendMessage);
							this.cancel();
							return;
						}
						
						Main.display(Particle.FLAME, loc);
					}
				} else {
					ABM.this.explode(launcher.getDefender(), target, finalX.toLocation(world), sendMessage);
					this.cancel();
				}
			}
		}.runTaskTimer(Main.i, 1, 1);
	}
	
	public boolean explode(Defender defender, ReentryVehicle target, Location loc, boolean sendMessage) {
		if (target.getLocation().distance(loc) <= explosiveRange) {
			target.intercepted(target.getLauncher(), target.getLocation());
			
			if(sendMessage) {
				defender.sendMessage("§6MIRV interceptée en §cx=§a" + loc.getBlockX() + " §cy=§a" + loc.getBlockY() 
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

}

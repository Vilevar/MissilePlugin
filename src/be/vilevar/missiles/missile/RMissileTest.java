package be.vilevar.missiles.missile;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import be.vilevar.missiles.Main;
import be.vilevar.missiles.utils.ParticleEffect;

public class RMissileTest {

	
	public static void createParticles(Location location, Entity ent, ParticleEffect effect, double m) {
		(new BukkitRunnable() {
			
			Vector direction = location.getDirection().multiply(6);
			Location loc = location;
			
			@Override
			public void run() {
			//	int times = (int) direction.length();
				Vector dir = direction.clone().normalize();
				loc.add(dir);
				Main.display(effect, loc);
				if(loc.distance(ent.getLocation()) < 1) {
					loc.getWorld().createExplosion(loc, 5, false, false);
					this.cancel();
					System.out.println("stop "+m);
					return;
				}
			/*	if(direction.length() > 1) {
					for(int i = 0; i < times; i++) {
						loc.add(dir);
						Main.display(ParticleEffect.HEART, loc);
						if(loc.distance(ent.getLocation()) < 1) {
							System.out.println("Stop");
							ent.remove();
							Bukkit.getScheduler().cancelTask(s);
							s = -1;
							return;
						}
					}
					dir.multiply(direction.length() % 1);
					loc.add(dir);
					Main.display(ParticleEffect.HEART, loc);
					if(loc.distance(ent.getLocation()) < 1) {
						System.out.println("Stop");
						ent.remove();
						Bukkit.getScheduler().cancelTask(s);
						s = -1;
						return;
					}
				} else {
					loc.add(direction);
					Main.display(ParticleEffect.HEART, loc);
					if(loc.distance(ent.getLocation()) < 1) {
						System.out.println("Stop");
						ent.remove();
						Bukkit.getScheduler().cancelTask(s);
						s = -1;
						return;
					}
				}*/
				Vector dPos = ent.getLocation().toVector().subtract(loc.toVector());
				Vector dDirection = dPos.subtract(direction);
			//	if(dDirection.length() > 1)
					dDirection.normalize().multiply(m);
				direction.add(dDirection);
			//	System.out.println(dDirection+" "+direction);
			//	System.out.println(loc.distance(ent.getLocation()));
			}
			
			
		}).runTaskTimer(Main.i, 0, 1);
	}
	
}

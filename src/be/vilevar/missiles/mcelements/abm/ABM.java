package be.vilevar.missiles.mcelements.abm;

import java.util.ArrayList;
import java.util.Iterator;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.craftbukkit.libs.org.apache.commons.lang3.tuple.Pair;
import org.bukkit.scheduler.BukkitRunnable;

import be.vilevar.missiles.Main;
import be.vilevar.missiles.WorldManager;
import be.vilevar.missiles.defense.Defender;
import be.vilevar.missiles.defense.Target;
import be.vilevar.missiles.utils.ParticleEffect;
import be.vilevar.missiles.utils.Vec3d;

public class ABM {

	private WorldManager wm = Main.i.getWorldManager();
	private Target target;
	private ABMLauncher launcher;

	private final double range = 200;
	private final double vmin = 1;
	private double dt = 0.01;

	private double vx,
					vy,
					vz;
	private double t = 0;
	private boolean exploded = false;
	
	// TODO
	public void shoot(Target target, ABMLauncher launcher, boolean sendMessage) {
		this.target = target;
		this.launcher = launcher;

		trajectoryCalculation();

		if(t <= 0) {
			return;
		} else {
			double tmin = Math.sqrt(Math.pow(target.getTarget().getLocation().getX() + target.getTarget().getVelocity().getX() * t, 2)
								+ Math.pow(target.getTarget().getLocation().getY() + target.getTarget().getVelocity().getZ() * t, 2)
								+ Math.pow(target.getTarget().getLocation().getZ() + target.getTarget().getVelocity().getY() * t, 2)) / this.vmin;
			if(tmin - t <= 0) {
				this.launch();
			} else {
				new BukkitRunnable() {
					@Override
					public void run() {
						ABM.this.shoot(target, launcher, sendMessage);
					}
				}.runTaskLaterAsynchronously(Main.i, (long) (tmin - t)*20);
			}
		}
	}

	public void launch() {
		ArrayList<Pair<Vec3d, Vec3d>> states = new ArrayList<>();
		Iterator<Pair<Vec3d, Vec3d>> it;

		World world = launcher.getLocation().getWorld();
		Vec3d x = new Vec3d(launcher.getLocation().getX() + 0.5, launcher.getLocation().getZ() + 0.5, launcher.getLocation().getY() + 1);
		Vec3d v = new Vec3d(vx, vy, vz);
		
		while(Math.sqrt(Math.pow(Math.abs(launcher.getLocation().getX() - x.getX()), 2) 
			+ Math.pow(Math.abs(launcher.getLocation().getZ() - x.getY()), 2) 
			+ Math.pow(Math.abs(launcher.getLocation().getY() - x.getZ()), 2))
			<= range) {
			Vec3d dv = new Vec3d(0, 0, -wm.getGM() / Math.pow(wm.getR() + x.getZ(), 2)).multiply(dt);
			
			v.add(dv);
			x.add(v.clone().multiply(dt));

			states.add(Pair.of(x.clone(), v.clone()));
		}

		it = states.iterator();

		final Vec3d finalX = x;

		new BukkitRunnable() {
			private int i;
			
			@Override
			public void run() {
				if(it.hasNext()) {
					for(double t = 0; t < 0.05 && !it.hasNext(); t += dt) {
						Pair<Vec3d, Vec3d> state = states.get(i);
						Vec3d x = state.getLeft();
						
						Location loc = new Location(world, x.getX(), x.getZ(), x.getY());
						Material block = loc.getBlock().getType();
						if(block != Material.AIR && block != Material.VOID_AIR) {
							ABM.this.explode(launcher.getDefender(), loc);
							this.cancel();
							return;
						}
						if(target.getTarget().getLocation().distance(loc) <= 5) {
							ABM.this.explode(launcher.getDefender(), loc);
						}
						Main.display(ParticleEffect.FLAME, loc);
						
					}
				} else {
					ABM.this.explode(launcher.getDefender(), new Location(world, finalX.getX(), finalX.getZ(), finalX.getY()));
					this.cancel();
				}
			}
		}.runTaskTimer(Main.i, 1, 1);
	}

	public void explode(Defender defender, Location loc) {
		if(target.getTarget().getLocation().distance(loc) <= 5) {
			target.getTarget().explode(loc);
			defender.sendMessage("§6MIRV interceptée en §cx=§a"+loc.getBlockX()+" §cy=§a"+loc.getBlockY()+" §cz=§a"+loc.getBlockZ());
		}
		this.exploded = true;
		Main.display(ParticleEffect.EXPLOSION_LARGE, loc);
	}

	public void trajectoryCalculation() {

		double x = target.getTarget().getLocation().getX() - launcher.getLocation().getX();
		double y = target.getTarget().getLocation().getZ() - launcher.getLocation().getZ();
		double z = target.getTarget().getLocation().getY() - launcher.getLocation().getY();
		double vxm = target.getTarget().getVelocity().getX(),
				vym = target.getTarget().getVelocity().getY(),
				vzm = target.getTarget().getVelocity().getZ();
				
		double delta = 4 * ((x * vxm + y * vym + z * vzm) * (x * vxm + y * vym + z * vzm)) - 4 * (vxm * vxm + vym * vym + vzm * vzm) * (x * x + y * y + z * z - range * range);
		if(delta < 0) {
			return;
		} else {
			this.t = (- (x * vxm + y * vym + z * vzm) + Math.sqrt(delta)) / (vxm * vxm + vym * vym + vzm * vzm);
		}

		this.vx = (x / t) + vxm;
		this.vy = (y / t) + vym;
		this.vz = (z / t) + vzm;	
	}

	public boolean isExploded() {
		return this.exploded;
	}

}

package be.vilevar.missiles.artillery;

import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import be.vilevar.missiles.Main;
import be.vilevar.missiles.WorldManager;
import be.vilevar.missiles.utils.ParticleEffect;
import be.vilevar.missiles.utils.Vec3d;

public class ShellPath extends BukkitRunnable {

	private WorldManager wm = Main.i.getWorldManager();
	private double dt = 0.005;
	private World world;
	private Shell shell;
	private Player gunner;
	
	private int count;
	private HashMap<Integer, Vec3d> locs = new HashMap<>();
	private int i;
	
	public ShellPath(World world, Shell shell, Vec3d x, double theta, double psi, Player gunner) {
		this.world = world;
		this.shell = shell;
		this.gunner = gunner;
		
		Vec3d v = new Vec3d(Math.cos(theta)*Math.cos(psi), Math.cos(theta)*Math.sin(psi), Math.sin(theta)).multiply(shell.getV0());
		
		locs.put(count++, x.clone());
		double forceCoef = -0.5 * shell.getS() * shell.getCd() * wm.getAirDensity(world);
		while(x.getZ() >= 0) {
			Vec3d force = v.clone().subtract(wm.getWind(world));
			force.multiply(forceCoef * force.length());
			
			Vec3d dv = wm.getG().clone().add(force.divide(shell.getMass())).multiply(dt);
			Vec3d averageV = v.clone().add(dv.clone().divide(2));
			v.add(dv);
			x.add(averageV.multiply(dt));
			
			locs.put(count++, x.clone());
		}
	}
	
	@Override
	public void run() {
		if(!locs.isEmpty()) {
			for(double t = 0; t < 0.05 && !locs.isEmpty(); t += dt) {
				Vec3d x = locs.get(i);
				locs.remove(i++);
				Location loc = new Location(world, x.getX(), x.getZ(), x.getY());
				if(loc.getBlock().getType().isSolid() || loc.getBlock().getType() == Material.LAVA) {
					world.createExplosion(loc, this.shell.getPower(), this.shell.setFire(), true, this.gunner);
					this.gunner.sendMessage("§6Obus explosé à §cx=§a"+loc.getBlockX()+" §cy=§a"+loc.getBlockY()+" §cz=§a"+loc.getBlockZ());
					this.cancel();
					locs.clear();
				}
				Main.display(ParticleEffect.FLAME, loc);
			}
		} else {
			this.cancel();
		}
	}
}

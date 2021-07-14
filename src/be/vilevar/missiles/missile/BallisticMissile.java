package be.vilevar.missiles.missile;

import static java.lang.Math.cos;
import static java.lang.Math.log;
import static java.lang.Math.min;
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
import be.vilevar.missiles.missile.ballistic.ReentryVehicle;
import be.vilevar.missiles.missile.ballistic.MissileStage;
import be.vilevar.missiles.utils.ParticleEffect;
import be.vilevar.missiles.utils.Vec3d;

public class BallisticMissile {

	private double mass;
	private MissileStage[] stages;
	private ReentryVehicle[] mirv;
	
	private WorldManager wm = Main.i.getWorldManager();
	
	private double dt = 0.01;
	protected Vec3d defaultEjection = new Vec3d(0, 0, 1);
	private int stage;
	
	public BallisticMissile(MissileStage[] stages, ReentryVehicle[] mirv) {
		this.mass = 550 + mirv.length * mirv[0].getMass();
		this.stages = stages;
		this.mirv = mirv;
	}
	
	public MissileStage getStage() {
		if(this.stages.length <= this.stage) {
			return null;
		} else if(this.stages[this.stage].getFuelMass() == 0) {
			this.stage++;
			return this.getStage();
		}
		return this.stages[this.stage];
	}
	
	public double getTotalMass() {
		double mass = this.mass;
		for(int i = this.stage; i < this.stages.length; i++) {
			mass += this.stages[i].getTotalMass();
		}
		return mass;
	}
	
	
	public void launch(Player launcher, Location loc, double yaw, double pitch) {
		Location test = loc.clone();
		for(int i = test.getBlockY(); i < 255; i++) {
			if(test.add(0, 1, 0).getBlock().getType() != Material.AIR) {
				launcher.sendMessage("§cLe missile n'a pas accès au ciel. "+test+" "+test.getBlock().getType());
				return;
			}
		}
		if(loc.getY() < 30) {
			launcher.sendMessage("§cLe missile a besoin d'être placé plus haut.");
			return;
		}
		if(loc.getY() > 100) {
			launcher.sendMessage("§cIl n'y a pas assez d'oxygène si haut.");
			return;
		}
		
		// Compute asynchronously the path
		new BukkitRunnable() {
			@Override
			public void run() {
				int count = 0;
				HashMap<Integer, Vec3d> positions = new HashMap<>();
				
				Vec3d x = new Vec3d(loc.getX() + 0.5, loc.getZ() + 0.5, loc.getY() + 1);
				Vec3d v = new Vec3d(0, 0, 0);
				
				positions.put(count++, x.clone());
				
				Vec3d ejection = BallisticMissile.this.defaultEjection;
				
				do {
					if(x.getZ() >= 300 && ejection.equals(BallisticMissile.this.defaultEjection)) {
						ejection = new Vec3d(cos(pitch)*cos(yaw), cos(pitch)*sin(yaw), sin(pitch));
					}
					
					Vec3d dv = new Vec3d(0, 0, -wm.getGM() / pow(wm.getR() + x.getZ(), 2) * dt);
					
					MissileStage stage = BallisticMissile.this.getStage();
					if(stage != null) {
						double eject = min(stage.getFuelMass(), stage.getEject() * dt);
						double mass = BallisticMissile.this.getTotalMass();
						dv.add(ejection.clone().multiply(stage.getImpulse() * log(mass / (mass - eject))));
						stage.addFuelMass(-eject);
					}
					
					v.add(dv);
					x.add(v.clone().multiply(dt));
					
					positions.put(count++, x.clone());
				} while(v.getZ() > 0);
				
				final Vec3d finalX = x;
				final Vec3d finalV = v;
				
				System.out.println("launch "+positions.size()+" mirv at "+x+" "+v+" after "+(count * dt));
				
				
				// Run synchronously the path
				new BukkitRunnable() {
					private int i;
					private World world = launcher.getWorld();
					
					@Override
					public void run() {
						if(!positions.isEmpty()) {
							for(double t = 0; t < 0.05 && !positions.isEmpty(); t += dt) {
								Vec3d x = positions.get(i);
								positions.remove(i++);
								if(x.getZ() <= 256) {
									Location loc = new Location(world, x.getX(), x.getZ(), x.getY());
									Material block = loc.getBlock().getType();
									if(block.isSolid() || block == Material.LAVA) {
										for(ReentryVehicle mirv : BallisticMissile.this.mirv) {
											mirv.explode(launcher, loc);
										}
										positions.clear();
										this.cancel();
										return;
									} else {
										Main.display(ParticleEffect.FLAME, loc);
									}
								}
							}
						} else {
							this.cancel();
							
							// Compute MIRV path asynchronously
							new BukkitRunnable() {
								@Override
								public void run() {
									for(ReentryVehicle mirv : BallisticMissile.this.mirv) {
										mirv.launch(launcher, finalX, finalV);
									}
								}
							}.runTaskAsynchronously(Main.i);
						}
					}
				}.runTaskTimer(Main.i, 1, 1);
			}
		}.runTaskAsynchronously(Main.i);
	}
	
}

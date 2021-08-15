package be.vilevar.missiles.missile.ballistic.explosives;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitScheduler;

import be.vilevar.missiles.Main;
import be.vilevar.missiles.mcelements.CustomElementManager;
import be.vilevar.missiles.missile.ballistic.Explosive;
import io.netty.buffer.ByteBuf;

public class NuclearExplosive implements Explosive {

	private final Main main;
	private final double energy0;
	private final double radius;
	private final double height;
	
	private final double Radius;
	private final BukkitScheduler scheduler;
	
	private double energy;
	private boolean isDone = true;
	
	public NuclearExplosive(Main main, double energy, double radius, double height) {
		this.main = main;
		this.energy = this.energy0 = energy;
		this.radius = radius;
		this.height = height;
		
		this.Radius = radius * radius;
		this.scheduler = main.getServer().getScheduler();
	}


	@Override
	public void explode(Location loc, Player damager) {
		this.isDone = false;
		
		// Crater
		List<Block> crater = new ArrayList<>();
		for(double y = 0; y >= -height; y--) {
			for(double x = -radius; x <= radius; x++) {
				for(double z = -radius; z <= radius; z++) {
					if(Math.pow(x / radius, 2) + Math.pow(z / radius, 2) + Math.pow(y / height, 2) <= 1) {
						Location explode = loc.clone().add(x, y, z);
						Material block = explode.getBlock().getType();
						float resistance = block.getBlastResistance();
						if(resistance < 100 || (resistance < 1500 && energy >= resistance)) {
							energy -= resistance;
							crater.add(explode.getBlock());
						}
					}
				}
			}
		}
		
		// High explosion
		List<Block> explosion = new ArrayList<>();
		for(int y = 1; y <= 256 - loc.getBlockY(); y++) {
			for(double x = -radius; x <= radius; x++) {
				for(double z = -radius; z <= radius; z++) {
					if(Math.pow(x, 2) + Math.pow(z, 2) <= Radius) {
						Location explode = loc.clone().add(x, y, z);
						Material block = explode.getBlock().getType();
						float resistance = block.getBlastResistance();
						if(block != Material.AIR && (resistance < 100 || (resistance < 1500 && energy >= resistance))) {
							energy -= resistance;
							explosion.add(explode.getBlock());
						}
					}
				}
			}
		}
		
		System.out.println(energy+" "+crater.size()+" "+explosion.size());
		
		
		// Damages
		double eps = Math.sqrt(energy0);
		scheduler.runTaskLater(main, () -> {
			
			double damageFactor;
			if(this.energy < -3*eps) {
				damageFactor = 0;
			} else if(this.energy > 3*eps) {
				damageFactor = 2;
			} else {
				damageFactor = 1 + Math.tanh(energy / eps);
			}
			System.out.println(damageFactor);
			
			double sRange = damageFactor * energy0 / (4 * Math.PI);
			double range = Math.sqrt(sRange);
			System.out.println(range);
			
			double instantKill = damageFactor * eps / (4 * Math.PI);
			System.out.println(Math.sqrt(instantKill));
			
			if(sRange < Radius) {
				double b = 3 * eps / (radius * Math.sqrt(2 * Math.PI));
				double c = -4.5 / Radius;
				loc.getWorld().getNearbyEntities(loc, radius, radius, radius).forEach(entity -> {
					if(entity instanceof LivingEntity) {
						LivingEntity ent = (LivingEntity) entity;
						double dist = ent.getLocation().distanceSquared(loc);
						double dam = b * Math.exp(c * dist);
						ent.damage(dam, damager);
					}
				});
			} else {
				loc.getWorld().getNearbyEntities(loc, range, range, range).forEach(entity -> {
					if(entity instanceof LivingEntity) {
						LivingEntity ent = (LivingEntity) entity;
						
						double dist = ent.getLocation().distanceSquared(loc);
						if(dist <= instantKill) {
							ent.damage(eps, damager);
						} else if(dist <= sRange) {
							ent.damage(sRange / dist, damager);
						}
					}
				});
				
			}
		}, 10);
		
		
		// Destroy blocks
		scheduler.runTaskLater(main, () -> {
			for(Block block : explosion) {
				block.setType(Material.AIR);
			}
		}, 20);
		scheduler.runTaskLater(main, () -> {
			for(Block block : crater) {
				block.setType(Material.FIRE);
			}
		}, 40);
		
		
		// Residual energy for fire
		if(this.energy > 0) {
			scheduler.runTaskLater(main, () -> {
				double radius = this.radius + Math.pow(Math.log(1 + this.energy) / 2, 2);
				double R = radius * radius;
				Location l = loc.clone().add(0, 1, 0);
				
//				List<Block> fires = new ArrayList<>();
				int count = 0;
				for(double x = -radius; x <= radius; x++) {
					for(double z = -radius; z <= radius; z++) {
						Location fire = l.clone().add(x, 0, z);
						double distSquare = fire.distanceSquared(l);
						if(distSquare <= R && distSquare > this.Radius && fire.getBlock().getType() == Material.AIR) {
//							fires.add(fire.getBlock());
							fire.getBlock().setType(Material.FIRE);
							count++;
						}
					}
				}
				
				System.out.println("Fire radius : "+radius+" -> blocks="+count);
				
//				scheduler.runTaskLater(main, () -> {
//					for(Block fire : fires) {
//						fire.setType(Material.FIRE);
//					}
//				}, 10);
			}, 70);
		}
		
		
		// Burn
		scheduler.runTaskLater(main, () -> {
			double burnRange = 5 * radius;
			double m = -eps / burnRange;
			
			loc.getWorld().getNearbyEntities(loc, burnRange, burnRange, burnRange).forEach(entity -> {
				if(entity instanceof LivingEntity) {
					LivingEntity ent = (LivingEntity) entity;
					
					double dist = ent.getLocation().distance(loc);
					if(dist < burnRange) {
						ent.setFireTicks((int) (m * dist + eps));
					}
				}
			});
		}, 90);
		
		scheduler.runTaskLater(main, () -> {
			this.isDone = true;
		}, 120);
	}

	@Override
	public void explodeByInterception(Location loc, Player damager) {
		loc.getWorld().createExplosion(loc, 100, true, true, damager);
	}
	
	@Override
	public boolean isDone() {
		return isDone;
	}
	
	@Override
	public void saveIn(ByteBuf buffer) {
		buffer.writeInt(1);
		buffer.writeDouble(energy0);
		buffer.writeDouble(radius);
		buffer.writeDouble(height);
	}


	@Override
	public ItemStack toItem() {
		return CustomElementManager.A_BOMB.create();
	}

}

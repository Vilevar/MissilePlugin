package be.vilevar.missiles.missile.ballistic.explosives;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;

import be.vilevar.missiles.Main;
import be.vilevar.missiles.mcelements.CustomElementManager;
import be.vilevar.missiles.missile.ballistic.Explosive;
import io.netty.buffer.ByteBuf;

public class ThermonuclearExplosive implements Explosive {

	private final Main main;
	private final double energy0;
	private final double radius;
	private final double height;
	
	private final double Radius;
	private final double a;
	private final BukkitScheduler scheduler;
	
	private double energy;
	private boolean isDone;
	
	public ThermonuclearExplosive(Main main, double energy, double radius, double height) {
		this.main = main;
		this.energy = this.energy0 = energy;
		this.radius = radius;
		this.height = height;
		
		this.Radius = radius * radius;
		this.a = this.Radius / height;
		this.scheduler = main.getServer().getScheduler();
	}
	
	@Override
	public void explode(Location loc, Player damager) {
		List<Block> explosion = new ArrayList<>();
		// Crater
		for(double y = 0; y >= -height; y--) {
			double radius = Math.sqrt(a * (y + height)) + 1;
			for(double x = -radius; x < radius; x++) {
				for(double z = -radius; z < radius; z++) {
					if(y >= x*x/a + z*z/a - height) {
						Location explode = loc.clone().add(x, y, z);
						Material block = explode.getBlock().getType();
						float resistance = block.getBlastResistance();
						if(resistance < 500 || (resistance < 1500 && energy >= resistance)) {
							energy -= resistance;
							explosion.add(explode.getBlock());
						}
					}
				}
			}
		}
		
		for(double y = 1; y <= 256 - loc.getBlockY(); y++) {
			double radius = Math.sqrt(a * (y + height)) + 1;
			for(double x = -radius; x <= radius; x++) {
				for(double z = -radius; z <= radius; z++) {
					if(y >= x*x/a + z*z/a - height) {
						Location explode = loc.clone().add(x, y, z);
						Material block = explode.getBlock().getType();
						float resistance = block.getBlastResistance();
						if(block != Material.AIR && (resistance < 500 || (resistance < 1500 && energy >= resistance))) {
							energy -= resistance;
							explosion.add(explode.getBlock());
						}
					}
				}
			}
		}
		System.out.println(energy+" "+explosion.size());
		
		
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
		new BukkitRunnable() {
			private Iterator<Block> it = explosion.iterator();
			
			@Override
			public void run() {
				for(int i = 0; i < 100000 && it.hasNext(); i++) {
					it.next().setType(Material.AIR);
				}
				if(!it.hasNext()) {
					this.cancel();
					main.getServer().getScheduler().runTaskLater(main, () -> fireBall(loc), 30);
				}
			}
		}.runTaskTimer(main, 20, 20);
	}
	
	private void fireBall(Location loc) {
		double radius;
		if(this.energy > 0) {
			radius = Math.max(this.radius + Math.pow(Math.log(1 + this.energy) / 2, 2), 1.5 * this.radius);
		} else {
			radius = 1.5 * this.radius;
		}
		
		System.out.println(radius);
		double R = radius * radius;
		
		List<Block> onFire = new ArrayList<>();
		for(double x = -radius; x <= radius; x++) {
			for(double y = -radius; y <= radius; y++) {
				for(double z = -radius; z <= radius; z++) {
					Location fire = loc.clone().add(x, y, z);
					if(fire.getBlock().getType() == Material.AIR && fire.distanceSquared(loc) <= R &&
							fire.clone().add(0, -1, 0).getBlock().getType().isSolid()) {
						onFire.add(fire.getBlock());
					}
				}
			}
		}
		System.out.println("Fire : "+onFire.size());
		
		scheduler.runTaskLater(main, () -> {
			for(Block fire : onFire) {
				fire.setType(Material.FIRE);
			}
		}, 20);
		
		scheduler.runTaskLater(main, () -> {
			double burnRange = 2 * radius;
			double eps = Math.sqrt(this.energy0);
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
		}, 40);
		
		scheduler.runTaskLater(main, () -> {
			this.isDone = true;
		}, 80);
	}
	
	@Override
	public boolean isDone() {
		return isDone;
	}

	@Override
	public void saveIn(ByteBuf buffer) {
		buffer.writeInt(2);
		buffer.writeDouble(energy0);
		buffer.writeDouble(radius);
		buffer.writeDouble(height);
	}
	
	@Override
	public ItemStack toItem() {
		return CustomElementManager.H_BOMB.create();
	}

}

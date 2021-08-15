package be.vilevar.missiles.missile.ballistic.explosives;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import be.vilevar.missiles.Main;
import be.vilevar.missiles.mcelements.CustomElementManager;
import be.vilevar.missiles.mcelements.ElectricBlock;
import be.vilevar.missiles.mcelements.abm.ABMLauncher;
import be.vilevar.missiles.mcelements.launcher.MissileLauncherBlock;
import be.vilevar.missiles.mcelements.radar.Radar;
import be.vilevar.missiles.missile.ballistic.Explosive;
import io.netty.buffer.ByteBuf;

public class EMPExplosive implements Explosive {

	private final double energy;
	
	private final double radiusSquared;
	private final int radius;
	
	public EMPExplosive(double energy) {
		this.energy = energy;
		
		this.radiusSquared = energy / 100;
		this.radius = (int) (Math.sqrt(energy) / 10);
	}
	
	
	@Override
	public void explode(Location loc, Player damager) {
		for(int x = -radius; x <= radius; x++) {
			for(int y = -radius; y <= radius; y++) {
				for(int z = -radius; z <= radius; z++) {
					double distance = x*x + y*y + z*z;
					if(distance <= this.radiusSquared) {
						Block block = loc.clone().add(x, y, z).getBlock();
						
						ElectricBlock electric;
						if(CustomElementManager.MISSILE_LAUNCHER.isParentOf(block)) {
							electric = MissileLauncherBlock.getLauncherAt(loc);
						} else if(CustomElementManager.RADAR.isParentOf(block)) {
							electric = Radar.getRadarAt(loc);
						} else if(CustomElementManager.ABM_LAUNCHER.isParentOf(block)) {
							electric = ABMLauncher.getLauncherAt(loc);
						} else {
							electric = null;
						}
						
						if(electric != null) {
							electric.addTimeOut((long) (distance <= 1 ? this.energy : this.energy / distance));
							Main.display(Particle.EXPLOSION_NORMAL, block.getLocation());
						}
					}
				}
			}
		}
		loc.getWorld().spawnParticle(Particle.FLASH, loc, 1000, 1, 1, 1);
	}
	
	@Override
	public void explodeByInterception(Location loc, Player damager) {
	}

	@Override
	public boolean isDone() {
		return true;
	}

	@Override
	public void saveIn(ByteBuf buffer) {
		buffer.writeInt(3);
		buffer.writeDouble(energy);
	}

	@Override
	public ItemStack toItem() {
		return CustomElementManager.E_BOMB.create();
	}

}

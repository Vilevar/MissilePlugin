package be.vilevar.missiles.missile.ballistic.explosives;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Particle;
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
	
	public EMPExplosive(double energy) {
		this.energy = energy;
	}
	
	
	@Override
	public void explode(Location loc, Player damager) {
		List<ElectricBlock> electrics = new ArrayList<>();
		electrics.addAll(MissileLauncherBlock.launchers);
		electrics.addAll(Radar.radars);
		electrics.addAll(ABMLauncher.launchers);
		
		for(ElectricBlock electric : electrics) {
			double distance = Math.max(electric.getLocation().distanceSquared(loc), 0.1);
			long timeOut = (long) (this.energy / distance);
			electric.addTimeOut(timeOut);
			if(timeOut != 0)
				Main.display(Particle.FLASH, electric.getLocation());
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

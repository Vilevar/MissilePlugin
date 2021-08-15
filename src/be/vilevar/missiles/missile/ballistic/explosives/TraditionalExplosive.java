package be.vilevar.missiles.missile.ballistic.explosives;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import be.vilevar.missiles.Main;
import be.vilevar.missiles.missile.ballistic.Explosive;
import io.netty.buffer.ByteBuf;

public class TraditionalExplosive implements Explosive {

	private final Main main;
	private final float power;
	
	private boolean isDone = true;
	
	public TraditionalExplosive(Main main, float power) {
		this.main = main;
		this.power = power;
	}

	@Override
	public void explode(Location loc, Player damager) {
		this.isDone = false;
		
		loc.getWorld().createExplosion(loc, power, true, true, damager);
		main.getServer().getScheduler().runTaskLater(main, () -> {
			this.isDone = true;
		}, 20);
	}

	@Override
	public boolean isDone() {
		return isDone;
	}

	@Override
	public void saveIn(ByteBuf buffer) {
		buffer.writeInt(0);
		buffer.writeFloat(power);
	}
	
	@Override
	public ItemStack toItem() {
		return new ItemStack(Material.TNT);
	}

}

package be.vilevar.missiles.missile.ballistic;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import be.vilevar.missiles.Main;
import be.vilevar.missiles.missile.ballistic.explosives.EMPExplosive;
import be.vilevar.missiles.missile.ballistic.explosives.NuclearExplosive;
import be.vilevar.missiles.missile.ballistic.explosives.ThermonuclearExplosive;
import be.vilevar.missiles.missile.ballistic.explosives.TraditionalExplosive;
import io.netty.buffer.ByteBuf;

public interface Explosive {

	void explode(Location loc, Player damager);
	default void explodeByInterception(Location loc, Player damager) {
		this.explode(loc, damager);
	}
	
	default void explode(Location loc, Player damager, boolean intercepted) {
		if(intercepted)
			this.explodeByInterception(loc, damager);
		else
			this.explode(loc, damager);
	}
	boolean isDone();
	
	void saveIn(ByteBuf buffer);
	ItemStack toItem();
	
	public static Explosive readFrom(ByteBuf buffer) {
		int id = buffer.readInt();
		switch(id) {
		case 0:
			return new TraditionalExplosive(Main.i, buffer.readFloat());
		case 1:
			return new NuclearExplosive(Main.i, buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
		case 2:
			return new ThermonuclearExplosive(Main.i, buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
		case 3:
			return new EMPExplosive(buffer.readDouble());
		}
		return null;
	}
}

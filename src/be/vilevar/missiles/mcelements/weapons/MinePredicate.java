package be.vilevar.missiles.mcelements.weapons;

import java.util.function.Predicate;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import be.vilevar.missiles.Main;

public class MinePredicate implements Predicate<Entity> {

	private Main main = Main.i;
	private Location loc;
	private Player miner;
	private boolean instant;
	
	public MinePredicate(Location loc, Player miner, boolean instant) {
		this.loc = loc;
		this.miner = miner;
		this.instant = instant;
	}

	public Location getLocation() {
		return loc;
	}
	
	public Player getMiner() {
		return miner;
	}
	
	public boolean isInstant() {
		return instant;
	}
		

	@Override
	public boolean test(Entity entity) {
		if(entity instanceof LivingEntity && !entity.equals(miner)) {
			if(miner != null) {
				if(main.getCapitalism().hasEntry(miner.getName()) && main.getCapitalism().hasEntry(entity.getName())) {
					return false;
				} else if(main.getCommunism().hasEntry(miner.getName()) && main.getCommunism().hasEntry(entity.getName())) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

}

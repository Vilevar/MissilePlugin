package be.vilevar.missiles.missile.ballistic.explosives;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import be.vilevar.missiles.missile.ballistic.Explosive;

public class Detonation {

	private final Explosive explosive;
	private final Location loc;
	private final Player damager;
	private final boolean intercepted;
	
	public Detonation(Explosive explosive, Location loc, Player damager, boolean intercepted) {
		this.explosive = explosive;
		this.loc = loc;
		this.damager = damager;
		this.intercepted = intercepted;
	}

	public Explosive getExplosive() {
		return explosive;
	}

	public Location getLoc() {
		return loc;
	}

	public Player getDamager() {
		return damager;
	}
	
	public boolean isIntercepted() {
		return intercepted;
	}
	
	
}

package be.vilevar.missiles.mcelements.radar;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;

import be.vilevar.missiles.Main;

public class Radar {

	public static final ArrayList<Radar> radars = new ArrayList<>();
	
	private Main main = Main.i;
	
	private final Location loc;
	private final Team team;
	
	private int channel;
	
	public Radar(Location loc, Player p) {
		this.loc = loc;
		
		if(main.getCapitalism().hasEntry(p.getName())) {
			this.team = main.getCapitalism();
		} else if(main.getCommunism().hasEntry(p.getName())) {
			this.team = main.getCommunism();
		} else {
			this.team = null;
		}
	}
	
	
}

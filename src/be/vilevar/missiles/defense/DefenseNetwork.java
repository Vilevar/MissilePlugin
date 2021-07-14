package be.vilevar.missiles.defense;

import java.util.ArrayList;

import org.bukkit.scoreboard.Team;

import be.vilevar.missiles.mcelements.abm.ABM;
import be.vilevar.missiles.mcelements.radar.Radar;

public class DefenseNetwork {

	
	
	private final Team team;
	private final int channel;
	
	private final ArrayList<Radar> radars = new ArrayList<>();
	private final ArrayList<ABM> abms = new ArrayList<>();
	
	public DefenseNetwork(Team team, int channel) {
		this.team = team;
		this.channel = channel;
	}
	
	
}

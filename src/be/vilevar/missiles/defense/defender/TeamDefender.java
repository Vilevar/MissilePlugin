package be.vilevar.missiles.defense.defender;

import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;

import be.vilevar.missiles.Main;
import be.vilevar.missiles.defense.Defender;
import be.vilevar.missiles.defense.DefenseNetwork;

public class TeamDefender implements Defender {

	private Main main = Main.i;
	
	private final Team team;
	private final String displayName;
	
	private final DefenseNetwork[] networks;
	
	private final String horse;
	
	
	public TeamDefender(Team team, int channels, String horse) {
		this.team = team;
		this.displayName = this.team.getColor() + this.team.getDisplayName();
	
		this.networks = new DefenseNetwork[channels];
		for(int i = 0; i < channels; i++) {
			this.networks[i] = new DefenseNetwork(this, i);
		}
		
		this.horse = horse;
	}
	
	public Team getTeam() {
		return team;
	}
	
	public String getDisplayName() {
		return displayName;
	}
	
	@Override
	public DefenseNetwork getNetwork(int channel) {
		return this.networks[channel];
	}
	
	@Override
	public String getHorseTag() {
		return horse;
	}
	
	@Override
	public void sendMessage(String message) {
		for(Player p : main.getServer().getOnlinePlayers()) {
			if(this.team.hasEntry(p.getName())) {
				p.sendMessage(message);
			}
		}
	}

}

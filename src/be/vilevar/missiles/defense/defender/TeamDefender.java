package be.vilevar.missiles.defense.defender;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;

import be.vilevar.missiles.Main;
import be.vilevar.missiles.defense.Defender;
import be.vilevar.missiles.defense.DefenseNetwork;
import be.vilevar.missiles.mcelements.merchant.MissileMerchant;

public class TeamDefender implements Defender {

	private Main main = Main.i;
	
	private final Team team;
	private final String displayName;
	private final String horse;
	
	private final DefenseNetwork[] networks = new DefenseNetwork[10];
	
	private MissileMerchant merchant;
	private Location outpost;
	
	public TeamDefender(Team team, String horse) {
		this.team = team;
		this.displayName = this.team.getColor() + this.team.getDisplayName();
		
		for(int i = 0; i < 10; i++) {
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
	public String getHorseTag() {
		return horse;
	}
	
	public MissileMerchant getMerchant() {
		return merchant;
	}
	
	public void setMerchant(MissileMerchant merchant) {
		this.merchant = merchant;
	}
	
	public Location getOutpost() {
		return outpost;
	}
	
	public void setOutpost(Location outpost) {
		this.outpost = outpost;
	}
	
	@Override
	public DefenseNetwork getNetwork(int channel) {
		return this.networks[channel];
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

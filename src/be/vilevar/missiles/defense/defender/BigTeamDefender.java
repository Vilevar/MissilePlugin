package be.vilevar.missiles.defense.defender;

import org.bukkit.Location;
import org.bukkit.scoreboard.Team;

import be.vilevar.missiles.merchant.WeaponsMerchant;

public class BigTeamDefender extends TeamDefender {

	private WeaponsMerchant merchant;
	private Location outpost;
	
	public BigTeamDefender(Team team, int channels, String horse) {
		super(team, channels, horse);
	}
	
	
	public WeaponsMerchant getMerchant() {
		return merchant;
	}
	
	public void setMerchant(WeaponsMerchant merchant) {
		this.merchant = merchant;
	}
	
	public Location getOutpost() {
		return outpost;
	}
	
	public void setOutpost(Location outpost) {
		this.outpost = outpost;
	}

}

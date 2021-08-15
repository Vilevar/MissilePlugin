package be.vilevar.missiles.game;

import org.bukkit.event.player.PlayerRespawnEvent;

import be.vilevar.missiles.defense.defender.TeamDefender;

public interface Game {

	String prepare();
	void start();
	void stop(TeamDefender winner, boolean message);
	boolean isStarted();
	
	TeamDefender getTeamCapitalism();
	TeamDefender getTeamCommunism();
	
	void handleRespawn(PlayerRespawnEvent e);
	
}

package be.vilevar.missiles.game;

import org.bukkit.event.player.PlayerRespawnEvent;

import be.vilevar.missiles.defense.defender.TeamDefender;

public interface Game {

	GameType getType();
	
	String prepare();
	void start();
	void stop(TeamDefender winner, boolean message);
	boolean isStarted();
	
	TeamDefender getTeamCapitalism();
	TeamDefender getTeamCommunism();
	
	void handleRespawn(PlayerRespawnEvent e);
	
	
	
	public static enum GameType {
		MISSILE(true),
		SIEGE(true),
		SNIPER(false),
		TDM(false);
		
		private boolean strategic;
		
		private GameType(boolean strategic) {
			this.strategic = strategic;
		}
		
		public boolean isStrategic() {
			return strategic;
		}
	}
	
}

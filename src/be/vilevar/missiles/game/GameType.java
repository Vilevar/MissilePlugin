package be.vilevar.missiles.game;

import be.vilevar.missiles.game.missile.MissileGameManager;
import be.vilevar.missiles.game.siege.SiegeGameManager;

public enum GameType {

	MISSILE(new MissileGameManager()),
	SIEGE(new SiegeGameManager()),
	SNIPER(null);
	
	private GameManager gameManager;
	
	private GameType(GameManager gm) {
		this.gameManager = gm;
	}
	
	public GameManager getGameManager() {
		return gameManager;
	}
}

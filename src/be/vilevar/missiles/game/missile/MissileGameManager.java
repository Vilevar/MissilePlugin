package be.vilevar.missiles.game.missile;

import java.util.List;

import org.bukkit.command.CommandSender;

import be.vilevar.missiles.game.Game;
import be.vilevar.missiles.game.GameManager;

public class MissileGameManager implements GameManager {

	@Override
	public Game createGame(CommandSender sender, String[] args) {
		return new MissileGame();
	}

	@Override
	public List<String> completeCommand(String[] args) {
		return null;
	}

	@Override
	public boolean isStrategic() {
		return true;
	}

}

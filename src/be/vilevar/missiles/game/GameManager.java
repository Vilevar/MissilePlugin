package be.vilevar.missiles.game;

import java.util.List;

import org.bukkit.command.CommandSender;

public interface GameManager {
	
	
	Game createGame(CommandSender sender, String[] args);
	List<String> completeCommand(String[] args);

	boolean isStrategic();
}

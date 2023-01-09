package be.vilevar.missiles.game.siege;

import java.util.HashMap;
import java.util.List;

import org.bukkit.command.CommandSender;

import be.vilevar.missiles.game.Game;
import be.vilevar.missiles.game.GameManager;
import be.vilevar.missiles.utils.Vec3d;

public class SiegeGameManager implements GameManager {

	private HashMap<String, SiegeWorld> worlds = new HashMap<>();
	
	public SiegeGameManager() {
		this.registerWorld("siege1", new SiegeWorld("siege1.zip", "SiegeWorld1",
				new Vec3d(-7, 7, 0), 550, new Vec3d(26, -1, 66), new Vec3d(-16, -87, 110)));
	}
	
	public void registerWorld(String name, SiegeWorld world) {
		this.worlds.put(name, world);
	}
	
	@Override
	public Game createGame(CommandSender sender, String[] args) {
		return new SiegeGame(this.worlds.get("siege1"));
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

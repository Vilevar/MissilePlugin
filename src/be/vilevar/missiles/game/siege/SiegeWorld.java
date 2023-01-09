package be.vilevar.missiles.game.siege;

import java.io.File;
import java.io.IOException;

import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.craftbukkit.libs.org.apache.commons.io.FileUtils;

import be.vilevar.missiles.Main;
import be.vilevar.missiles.utils.Unzipper;
import be.vilevar.missiles.utils.Vec3d;

public class SiegeWorld {

	// Minimum 5 minutes of preparation
	private final Main main = Main.i;
	private final String zip;
	private final String worldDefenseName;
	private final String worldAttackName;
//	private Vec3d center;
//	private double borderSize;
	private final Vec3d defenseSpawn;
	private final Vec3d attackSpawn;
	
	private World world1;
	private World world2;
	
	public SiegeWorld(String zip, String worldName, Vec3d center, double borderSize, Vec3d defenseSpawn, Vec3d attackSpawn) {
		this.zip = zip;
		this.worldDefenseName = worldName + "-defense";
		this.worldAttackName = worldName + "-attack";
//		this.center = center;
//		this.borderSize = borderSize;
		this.defenseSpawn = defenseSpawn;
		this.attackSpawn = attackSpawn;
	}
	
	public Vec3d getDefenseSpawn() {
		return defenseSpawn;
	}
	
	public Vec3d getAttackSpawn() {
		return attackSpawn;
	}
	
	
	public void copyWorlds() throws Exception {
		File zip = new File(main.getDataFolder(), this.zip);
		Unzipper.unzip(zip, new File(worldDefenseName+"/"));
		Unzipper.unzip(zip, new File(worldAttackName+"/"));
		
		this.world1 = new WorldCreator(worldDefenseName).createWorld();
		this.world2 = new WorldCreator(worldAttackName).createWorld();
	}
	
	public void removeWorlds() {
		main.getServer().unloadWorld(this.world1, false);
		main.getServer().unloadWorld(this.world2, false);
		
		try {
			FileUtils.deleteDirectory(new File(worldDefenseName+"/"));
			FileUtils.deleteDirectory(new File(worldAttackName+"/"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public World getWorld1() {
		return world1;
	}
	
	public World getWorld2() {
		return world2;
	}
	
}

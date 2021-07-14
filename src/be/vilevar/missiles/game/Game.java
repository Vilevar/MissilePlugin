package be.vilevar.missiles.game;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.block.Chest;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scoreboard.Team;

import be.vilevar.missiles.Main;
import be.vilevar.missiles.mcelements.CustomElementManager;

public class Game {

	private HashMap<Team, Location> banners = new HashMap<>();
	private HashMap<Team, Location> outposts = new HashMap<>();
	private Main main = Main.i;
	private Team communism;
	private Team capitalism;
	private boolean started;
	private boolean stopped;
	private int task;
	private Random random = new Random();

	public Game() {
		this.communism = main.getCommunism();
		this.capitalism = main.getCapitalism();
	}

	public void prepare() {
		Server server = main.getServer();
		server.broadcastMessage("§6Préparation de la partie (§c10 minutes§6) !");

		ItemStack chest = new ItemStack(Material.CHEST, 4);
		this.getFirstPlayer(communism).getInventory().addItem(chest, new ItemStack(Material.RED_BANNER));
		this.getFirstPlayer(capitalism).getInventory().addItem(chest, new ItemStack(Material.BLUE_BANNER));

		BukkitScheduler scheduler = server.getScheduler();

		this.task = scheduler.scheduleSyncRepeatingTask(main, () -> {
			server.broadcastMessage("Cargaison");
//			this.giveDuringPreparation(communism);
//			this.giveDuringPreparation(capitalism);
		}, 1200, 1200);
		scheduler.runTaskLater(main, () -> {
			scheduler.cancelTask(task);
			if (!banners.containsKey(communism)) {
				server.broadcastMessage("§6Fin du temps de §cpréparation§6. Le " + capitalism.getColor()
						+ capitalism.getDisplayName() + "§6 est §avainqueur§6 car drapeau ennemi non posé !");
				this.stop(capitalism, false);
			} else if (!banners.containsKey(capitalism)) {
				server.broadcastMessage("§6Fin du temps de §cpréparation§6. Le " + communism.getColor()
						+ communism.getDisplayName() + "§6 est §avainqueur§6 car drapeau ennemi non posé !");
				this.stop(communism, false);
			} else {
				this.started = true;
				server.broadcastMessage("§6Fin de la préparation ! Que le §ameilleure§c gagne§6 !");
//				this.giveAfterPreparation(communism);
//				this.giveAfterPreparation(capitalism);
				task = scheduler.scheduleSyncRepeatingTask(main, () -> {
					server.broadcastMessage("§6Ravitaillement en cours !!");
//					this.giveDuringGame(communism);
//					this.giveDuringGame(capitalism);
				}, 6000, 6000);
			}
		}, 12100);
	}

	private Player getFirstPlayer(Team team) {
		for (String entry : team.getEntries()) {
			Player p = main.getServer().getPlayer(entry);
			if (p != null)
				return p;
		}
		return null;
	}

	public boolean isStarted() {
		return started;
	}

	public boolean isStopped() {
		return stopped;
	}

	public void setBanner(Team team, Location location) {
		this.banners.put(team, location);
	}

	public Location getBanner(Team team) {
		return this.banners.get(team);
	}

	public void removeBanner(Team team) {
		System.out.println(Arrays.toString(Thread.currentThread().getStackTrace()));
		this.banners.remove(team);
	}

	public Location getOutpost(Team team) {
		return this.outposts.get(team);
	}

	public void setOutpost(Team team, Location loc) {
		this.outposts.put(team, loc);
	}

	public void stop(Team winner, boolean message) {
		if (message)
			main.getServer().broadcastMessage(
					"§6Fin de la partie ! Le §a" + winner.getColor() + winner.getDisplayName() + "§6 a gagné !");
		for (Player p : main.getServer().getOnlinePlayers()) {
			p.getInventory().clear();
			p.setGameMode(GameMode.CREATIVE);
		}
		main.resetGame();
		banners.clear();
		outposts.clear();
		main.getServer().getScheduler().cancelTask(task);
		started = false;
		stopped = true;
	}

}

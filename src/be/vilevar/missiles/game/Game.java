package be.vilevar.missiles.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

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
import org.bukkit.inventory.meta.SkullMeta;
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
		server.getPlayer(this.communism.getEntries().iterator().next()).getInventory().addItem(chest, new ItemStack(Material.RED_BANNER));
		server.getPlayer(this.capitalism.getEntries().iterator().next()).getInventory().addItem(chest, new ItemStack(Material.BLUE_BANNER));
		
		BukkitScheduler scheduler = server.getScheduler();
		
		this.task = scheduler.scheduleSyncRepeatingTask(main, () -> {
			server.broadcastMessage("Cargaison");
			this.giveDuringPreparation(communism);
			this.giveDuringPreparation(capitalism);
		}, 1200, 1200);
		scheduler.runTaskLater(main, () -> {
			scheduler.cancelTask(task);
			if(!banners.containsKey(communism)) {
				server.broadcastMessage(
						"§6Fin du temps de §cpréparation§6. Le "
								+capitalism.getColor()+capitalism.getDisplayName()+"§6 est §avainqueur§6 car drapeau ennemi non posé !");
				this.stop(capitalism, false);
			} else if(!banners.containsKey(capitalism)) {
				server.broadcastMessage(
						"§6Fin du temps de §cpréparation§6. Le "
								+communism.getColor()+communism.getDisplayName()+"§6 est §avainqueur§6 car drapeau ennemi non posé !");
				this.stop(communism, false);
			} else {
				this.started = true;
				server.broadcastMessage("§6Fin de la préparation ! Que le §ameilleure§c gagne§6 !");
				this.giveAfterPreparation(communism);
				this.giveAfterPreparation(capitalism);
				task = scheduler.scheduleSyncRepeatingTask(main, () -> {
					server.broadcastMessage("§6Ravitaillement en cours !!");
					this.giveDuringGame(communism);
					this.giveDuringGame(capitalism);
				}, 6000, 6000);
			}
		}, 1200 * 2 + 10);//12100);
	}
	
	public boolean isStarted() {
		return started;
	}
	
	public void setBanner(Team team, Location location) {
		this.banners.put(team, location);
	}
	
	public Location getBanner(Team team) {
		return this.banners.get(team);
	}
	
	public void removeBanner(Team team) {
		this.banners.remove(team);
	}
	
	public Location getOutpost(Team team) {
		return this.outposts.get(team);
	}
	
	public void setOutpost(Team team, Location loc) {
		this.outposts.put(team, loc);
	}
	
	public void stop(Team winner, boolean message) {
		if(message)
			main.getServer().broadcastMessage("§6Fin de la partie ! Le §a"+winner.getColor()+winner.getDisplayName()+"§6 a gagné !");
		for(Player p : main.getServer().getOnlinePlayers()) {
			p.getInventory().clear();
			p.setGameMode(GameMode.CREATIVE);
		}
		main.resetGame();
		banners.clear();
		outposts.clear();
		main.getServer().getScheduler().cancelTask(task);
		started = false;
	}
	
	
	@SuppressWarnings("deprecation")
	private void giveDuringPreparation(Team team) {
		Location banner = this.banners.get(team);
		if(banner != null) {
			int given = 0;
			int max = 25;
			List<ItemStack> iss = new ArrayList<>();
			
			// Heads
			if(random.nextDouble() < 0.4) {
				for(String player : team.getEntries()) {
					ItemStack is = new ItemStack(Material.PLAYER_HEAD);
					SkullMeta sm = (SkullMeta) is.getItemMeta();
					sm.setOwner(player);
					is.setItemMeta(sm);
					iss.add(is);
				}
				given++;
			}
			// Tool
			if(random.nextDouble() < 0.01) {
				ItemStack is = new ItemStack(random.nextBoolean() ? Material.IRON_AXE : Material.IRON_PICKAXE);
				ItemMeta im = is.getItemMeta();
				im.addEnchant(Enchantment.DIG_SPEED, 9, true);
				is.setItemMeta(im);
				iss.add(is);
				given++;
			}
			// Radar
			if(random.nextDouble() < 0.3) {
				int amount = random.nextInt(3) + 1;
				iss.add(CustomElementManager.MISSILE_RADAR.create(amount));
				given += amount;
			}
			// Missile Launcher
			if(random.nextDouble() < 0.2) {
				iss.add(CustomElementManager.MISSILE_LAUNCHER.create());
				iss.add(new ItemStack(Material.STONE_BUTTON));
				given += 2;
			}
			// Missile Crafter
			if(random.nextDouble() < 0.1) {
				iss.add(CustomElementManager.MISSILE_CRAFT.create());
				given++;
			}
			// Rangefinder
			if(random.nextDouble() < 0.2) {
				int amount = random.nextInt(3) + 1;
				iss.add(CustomElementManager.RANGEFINDER.create(amount));
				given += amount;
			}
			// Compass
			if(random.nextDouble() < 0.15) {
				iss.add(new ItemStack(Material.COMPASS));
				given++;
			}
			// Obsidian
			if(random.nextDouble() < 0.025) {
				int amount = random.nextInt(6) + 5;
				iss.add(new ItemStack(Material.OBSIDIAN, amount));
				given += amount;
			}
			// Magma block
			if(given < max && random.nextDouble() < 0.05) {
				int amount = Math.min(max - given, random.nextInt(11) + 5);
				iss.add(new ItemStack(Material.MAGMA_BLOCK, amount));
				given += amount;
			}
			// Golden apple
			if(given < max && random.nextDouble() < 0.001) {
				iss.add(new ItemStack(random.nextBoolean() ? Material.GOLDEN_APPLE : Material.ENCHANTED_GOLDEN_APPLE));
				given++;
			}
			// Beef
			if(given < max && random.nextDouble() < 0.2) {
				int amount = Math.min(max - given, random.nextInt(6) + 5);
				iss.add(new ItemStack(Material.COOKED_BEEF, amount));
				given += amount;
			}
			int remaining = max - given;
			int ammos = remaining / 3;
			for(int i = 0; i < ammos; i++) {
				iss.add(this.randomAmmo());
			}
			for(int i = 0; i < remaining - ammos; i++) {
				ItemStack missileStuff = random.nextBoolean() ? CustomElementManager.FUEL.create() : new ItemStack(Material.BLAZE_POWDER);
				iss.add(missileStuff);
			}
			
			// Give
			this.give(team, iss);
		}
	}
	
	private void giveAfterPreparation(Team team) {
		List<ItemStack> iss = new ArrayList<>();
		iss.add(CustomElementManager.BALLISTIC_MISSILE.create());
		iss.add(CustomElementManager.BOMB.create(3));
		iss.add(CustomElementManager.SMOKE_BOMB.create());
		iss.add(CustomElementManager.FUEL.create(10));
		iss.add(CustomElementManager.PISTOL.createItem());
		iss.add(CustomElementManager.PISTOL.getAmmunition().create());
		iss.add(new ItemStack(Material.IRON_SWORD));
		iss.add(CustomElementManager.RANGEFINDER.create());
		iss.add(new ItemStack(Material.BLAZE_POWDER, 10));
		iss.add(CustomElementManager.MISSILE_RADAR.create());
		iss.add(CustomElementManager.MISSILE_CRAFT.create());
		iss.add(CustomElementManager.MISSILE_LAUNCHER.create());
		iss.add(new ItemStack(Material.STONE_BUTTON));
		if(random.nextDouble() < 0.1) {
			iss.add(CustomElementManager.SNIPER.createItem());
		}
		if(random.nextDouble() < 0.2) {
			iss.add(CustomElementManager.SHOTGUN.createItem());
		}
		if(random.nextDouble() < 0.3) {
			iss.add(CustomElementManager.MACHINE_GUN.createItem());
		}
		if(random.nextDouble() < 0.01) {
			iss.add(new ItemStack(Material.ENCHANTED_GOLDEN_APPLE));
		}
		if(random.nextDouble() < 0.1) {
			iss.add(new ItemStack(Material.SHIELD));
		}
		if(random.nextDouble() < 0.05) {
			iss.add(new ItemStack(Material.DIAMOND_CHESTPLATE));
		}
		
		
		
		this.give(team, iss);
		
	}
	
	private void giveDuringGame(Team team) {
		List<ItemStack> iss = new ArrayList<>();
		
		// Ammo's
		for(int i = 0; i < random.nextInt(5) + 3; i++)
			iss.add(this.randomAmmo());
		
		// Missile stuff
		int nMissiles = random.nextInt(2);
		int nFuel = random.nextInt(7) + 3;
		int nTnt = random.nextInt(8);
		int nCompass = random.nextInt(4);
		int nBlazePowder = Math.abs(20 - nMissiles - nFuel - nTnt - nCompass);
		iss.add(CustomElementManager.BALLISTIC_MISSILE.create(nMissiles));
		iss.add(new ItemStack(Material.BLAZE_POWDER, nBlazePowder));
		iss.add(new ItemStack(Material.TNT, nTnt));
		iss.add(new ItemStack(Material.COMPASS, nCompass));
		iss.add(CustomElementManager.FUEL.create(nFuel));
		
		// Random stuff
		
		
		
		
		this.give(team, iss);
	}
	
	private ItemStack randomAmmo() {
		double ammoType = random.nextDouble();
		if(ammoType < 0.25)
			return CustomElementManager.SNIPER.getAmmunition().create();
		else if(ammoType < 0.5)
			return CustomElementManager.PISTOL.getAmmunition().create();
		else if(ammoType < 0.75)
			return CustomElementManager.MACHINE_GUN.getAmmunition().create();
		else
			return CustomElementManager.SHOTGUN.getAmmunition().create();
	}
	
	private void give(Team team, List<ItemStack> iss) {
		Location banner = this.getBanner(team);
		int chest = 0;
		Inventory inv = this.getChest(banner, chest);
		for(ItemStack is : iss) {
			while(chest < 4 && (inv == null || inv.firstEmpty() == -1)) {
				inv = this.getChest(banner, ++chest);
			}
			if(chest == 4)
				break;
			inv.addItem(is);
		}
	}
	
	private Inventory getChest(Location base, int nChest) {
		Location loc = base.clone();
		switch (nChest) {
		case 0:
			loc.add(1, 0, 0);
			break;
		case 1:
			loc.add(-1, 0, 0);
			break;
		case 2:
			loc.add(0, 0, 1);
			break;
		default:
			loc.add(0, 0, -1);
		}
		if(loc.getBlock().getType() == Material.CHEST) {
			Chest chest = (Chest) loc.getBlock().getState();
			return chest.getInventory();
		}
		return null;
	}
}

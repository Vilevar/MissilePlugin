package be.vilevar.missiles.game.missile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitScheduler;

import be.vilevar.missiles.Main;
import be.vilevar.missiles.defense.Defender;
import be.vilevar.missiles.defense.defender.BigTeamDefender;
import be.vilevar.missiles.game.Game;
import be.vilevar.missiles.game.GameType;
import be.vilevar.missiles.game.missile.merchant.MissileMerchant;
import be.vilevar.missiles.merchant.WeaponsMerchant;
import be.vilevar.missiles.utils.Vec3d;

public class MissileGame implements Game {
	
	private static final String capitalistHorse = "{Variant:513,Health:30,Attributes:[{Name:\"horse.jump_strength\",Base:1.5f},"
										+ "{Name:\"generic.movement_speed\",Base:0.8f},{Name:\"generic.max_health\",Base:30F}]}",
								communistHorse = "{Variant:768,Health:30,Attributes:[{Name:\"horse.jump_strength\",Base:1.5f},"
										+ "{Name:\"generic.movement_speed\",Base:0.8f},{Name:\"generic.max_health\",Base:30F}]}";
	
	private static final List<Vec3d> spawns = new ArrayList<>(Arrays.asList(
			new Vec3d(-325, 1514, 70),
			new Vec3d(976, 930, 103),
			new Vec3d(-152, 95, 80),
			new Vec3d(1743, 67, 63),
			new Vec3d(761, -243, 71),
			new Vec3d(177, -617, 63),
			new Vec3d(964, -585, 63),
			new Vec3d(1945, -517, 63),
			new Vec3d(2848, -621, 71),
			new Vec3d(1278, -1027, 74)));

	private Main main = Main.i;
	private BigTeamDefender communism;
	private BigTeamDefender capitalism;
	private boolean started;
	private boolean stopped;
	private int task;

	public MissileGame() {
		this.communism = new BigTeamDefender(main.getCommunism(), 10, communistHorse);
		this.capitalism = new BigTeamDefender(main.getCapitalism(), 10, capitalistHorse);
	}
	
	@Override
	public GameType getType() {
		return GameType.MISSILE;
	}
	
	@Override
	public String prepare() {
		Collections.shuffle(spawns);
		int communistIndex = 0;
		int capitalistIndex = 0;
		int size = spawns.size() / 2;
		
		String error = this.prepareTeams(main);
		if(error != null) {
			return error;
		}
		
		ItemStack pickaxe = new ItemStack(Material.DIAMOND_PICKAXE);
		pickaxe.addEnchantment(Enchantment.DIG_SPEED, 4);
		ItemStack beef = new ItemStack(Material.COOKED_BEEF, 10);
		
		for(Player p : main.getServer().getOnlinePlayers()) {
			p.getInventory().clear();
			p.getInventory().addItem(pickaxe, beef);
			p.getEnderChest().clear();
			p.setGameMode(GameMode.SURVIVAL);
			
			Vec3d pos = spawns.get(
					main.getCommunism().hasEntry(p.getName()) ? (communistIndex++) % size : size + ((capitalistIndex++) % size));
			p.teleport(pos.toLocation(p.getWorld()));
		}
		
		return null;
	}

	@Override
	public void start() {
		Server server = main.getServer();
		server.broadcastMessage("§6Préparation de la partie (§c15 minutes§6) !");

		BukkitScheduler scheduler = server.getScheduler();

		this.task = scheduler.scheduleSyncRepeatingTask(main, () -> {
			if(this.communism.getMerchant() != null)
				this.communism.getMerchant().addMoney(20);
			if(this.capitalism.getMerchant() != null)
				this.capitalism.getMerchant().addMoney(20);
		}, 400, 400);
		scheduler.runTaskLater(main, () -> {
			scheduler.cancelTask(task);
			
			if (communism.getMerchant() == null) {
				server.broadcastMessage("§6Fin du temps de §cpréparation§6. Le " + capitalism.getDisplayName() 
						+ "§6 est §avainqueur§6 car cible ennemie non vivante !");
				this.stop(capitalism, false);
			} else if (capitalism.getMerchant() == null) {
				server.broadcastMessage("§6Fin du temps de §cpréparation§6. Le " + communism.getDisplayName()
						+ "§6 est §avainqueur§6 car cible ennemie non vivante !");
				this.stop(communism, false);
			} else {
				this.started = true;
				server.broadcastMessage("§6Fin de la préparation ! Que le §ameilleur§c gagne§6 !");
				this.sendNear(this.capitalism, this.communism.getMerchant().getLocation());
				this.sendNear(this.communism, this.capitalism.getMerchant().getLocation());
				
				// Bonus
				task = scheduler.scheduleSyncRepeatingTask(main, () -> {
					this.communism.getMerchant().addMoney(15);
					this.capitalism.getMerchant().addMoney(15);
				}, 400, 400);
				
				// Normal
				scheduler.runTaskLater(main, () -> {
					scheduler.cancelTask(task);
					
					task = scheduler.scheduleSyncRepeatingTask(main, () -> {
						this.communism.getMerchant().addMoney(20);
						this.capitalism.getMerchant().addMoney(20);
					}, 800, 800);
				}, 6200);
			}
		}, 18100);
	}
	
	public BigTeamDefender getTeamCapitalism() {
		return capitalism;
	}
	
	public BigTeamDefender getTeamCommunism() {
		return communism;
	}
	
	
	@Override
	public WeaponsMerchant createMerchant(BigTeamDefender team, Location loc) {
		return new MissileMerchant(team, loc);
	}


	public boolean isStarted() {
		return started;
	}

	public boolean isStopped() {
		return stopped;
	}


	public void stop(BigTeamDefender winner, boolean message) {
		if (message)
			main.getServer().broadcastMessage("§6Fin de la partie ! Le §a" + winner.getDisplayName() + "§6 a gagné !");
		for (Player p : main.getServer().getOnlinePlayers()) {
			p.getInventory().clear();
			p.setGameMode(GameMode.CREATIVE);
		}
		main.resetGame();
		main.getServer().getScheduler().cancelTask(task);
		started = false;
		stopped = true;
	}
	
	
	private void sendNear(BigTeamDefender defender, Location loc) {
		double angle = Math.random() * 2 * Math.PI;
		double dist = 200 * Math.sqrt(Math.random());
		
		double x = loc.getX() + dist * Math.cos(angle);
		double z = loc.getZ() + dist * Math.sin(angle);
		
		Location def = defender.getMerchant().getLocation();
		double distance = Main.round(Math.sqrt(Math.pow(x - def.getX(), 2) + Math.pow(z - def.getZ(), 2)));
		
		defender.sendMessage("§6Activité ennemie détectée proche de §ax="+Main.round(x)+"§6, §az="+Main.round(z)+"§6 (§c"+distance+"m§6).");
	}

	
	@Override
	public void handleRespawn(PlayerRespawnEvent e) {
		final Player p = e.getPlayer();
		Defender def = this.main.getDefender(p);
		if(def instanceof BigTeamDefender) {
			WeaponsMerchant wm = ((BigTeamDefender) def).getMerchant();
			if(wm != null) {
				MissileMerchant merchant = wm.getAsMissileMerchant();
				if(merchant != null) {
					p.setGameMode(GameMode.SPECTATOR);
					e.setRespawnLocation(merchant.getLocation());
					main.getServer().getScheduler().runTaskLater(main, () -> {
						if(main.hasGame() && p.isOnline()) {
							p.setGameMode(GameMode.SURVIVAL);
						}
					}, 60);
				}
			}
		}		
	}

	@Override
	public void handlePlayerDeath(Player p, List<ItemStack> drops) {
	}

	@Override
	public void handleMerchantDeath(WeaponsMerchant merchant, List<ItemStack> drops) {
		if(merchant.equals(this.capitalism.getMerchant())) {
			this.stop(this.communism, true);
		} else if(merchant.equals(this.communism.getMerchant())) {
			this.stop(this.capitalism, true);
		}
	}
}

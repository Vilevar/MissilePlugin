package be.vilevar.missiles.game.siege;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;

import be.vilevar.missiles.Main;
import be.vilevar.missiles.defense.Defender;
import be.vilevar.missiles.defense.defender.BigTeamDefender;
import be.vilevar.missiles.game.Game;
import be.vilevar.missiles.game.GameType;
import be.vilevar.missiles.game.siege.merchant.SiegeMerchant;
import be.vilevar.missiles.merchant.WeaponsMerchant;

public class SiegeGame implements Game {

	private static final int MAX_DEATHS = 3;
	private static final int MAX_TIME = 1800;
	
	private Main main = Main.i;
	private BigTeamDefender communism;
	private BigTeamDefender capitalism;
	private SiegeWorld world;
	
	// Round data
	private HashMap<UUID, Integer> deaths = new HashMap<>();
	private int time;
	
	private boolean started;
	private BukkitTask task1;
	private BukkitTask task2;
	
	private boolean firstRound;
	
	// Last round
	private boolean defenderWonFirstRound;
	private int lastTime;
	
	// Game data
	private Objective obj;
	private boolean stopped;
	
	public SiegeGame(SiegeWorld world) {
		this.communism = new BigTeamDefender(main.getCommunism(), 0, null);
		this.capitalism = new BigTeamDefender(main.getCapitalism(), 0, null);
		
		this.world = world;
	}
	
	@Override
	public GameType getType() {
		return GameType.SIEGE;
	}

	@Override
	public String prepare() {
		// Setup teams
		String error = this.prepareTeams(main);
		if(error != null) {
			return error;
		}
		
		// Create maps
		try {
			this.world.copyWorlds();
		} catch (Exception e) {
			return e.getMessage();
		}
		
		// Create objective
		this.obj = this.main.getScoreboard().registerNewObjective("MissilePL-GSiege", "dummy",
				"§7------§b Partie de §dSiège§7 ------");
		this.obj.setDisplaySlot(DisplaySlot.SIDEBAR);
		
		this.firstRound = true;
		
		return null;
	}

	@Override
	public void start() {
		System.out.println("Start");
		World world = this.firstRound ? this.world.getWorld1() : this.world.getWorld2();
		
		ItemStack beef = new ItemStack(Material.COOKED_BEEF, 16);
		
		for(Player p : main.getServer().getOnlinePlayers()) {
			p.getInventory().clear();
			p.getInventory().addItem(beef);
			p.getEnderChest().clear();
			
			if(this.isDefender(p)) {
				p.setGameMode(GameMode.SURVIVAL);
				p.teleport(this.world.getDefenseSpawn().toLocation(world));
			} else {
				p.setGameMode(GameMode.ADVENTURE);
				p.teleport(this.world.getAttackSpawn().toLocation(world));
				p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 7200, 2, false, false));
			}
			
			p.setHealth(20);
			p.setFoodLevel(20);
			p.setSaturation(8);
		}
		
		
		
		this.task2 = new BukkitRunnable() {
			@Override
			public void run() {
				WeaponsMerchant merchant = communism.getMerchant();
				if(merchant != null)
					merchant.addMoney(1); // TODO see
				
				merchant = capitalism.getMerchant();
				if(merchant != null)
					merchant.addMoney(1); // TODO see
				
				if(isStarted()) {
					
					// Setup
					WeaponsMerchant defender = firstRound ? communism.getMerchant() : capitalism.getMerchant();
					Villager v = defender.getVillager();
					
					if(started)
						time += 1;
					
					int rLives = getRemainingLive();
					
					// Score board
					resetScoreboard();
					int seconds = time % 60;
					String secs = seconds / 10 == 0 ? "0"+seconds : String.valueOf(seconds);
					obj.getScore("§6Temps de jeu :§a "+(time / 60)+":"+secs).setScore(2);
					obj.getScore("§6Santé (défense) : §a"+v.getHealth()+"§6/§c"+v.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue()).setScore(1);
					obj.getScore("§6Vies (attaque) : §a"+rLives).setScore(0);
					
					// Health
					if(v.isDead()) {
						testStop(TestStopReason.MERCHANT_DEATH);
						return;
					}
					
					if(rLives <= 0) {
						testStop(TestStopReason.ATTACKERS_DEATH);
						return;
					}
					
					// Time
					int remainingTime = MAX_TIME - time;
					if(remainingTime <= 0) {
						testStop(TestStopReason.TIME);
					} else if(remainingTime <= 5) {
						for(Player online : main.getServer().getOnlinePlayers()) {
							online.sendTitle("§c"+remainingTime, null, 0, 15, 0);
						}
					} else if(remainingTime <= 10) {
						for(Player online : main.getServer().getOnlinePlayers()) {
							online.sendTitle("§e"+remainingTime, null, 0, 15, 0);
						}
					}
				}
			}
		}.runTaskTimer(main, 1200, 20);
		
		
		this.task1 = new BukkitRunnable() {
			@Override
			public void run() {
				if((firstRound ? communism : capitalism).getMerchant() == null) {
					main.getServer().broadcastMessage("§5[§2SiegeGame§5] §6La défense n'a pas posé ");
					testStop(TestStopReason.MERCHANT_DEATH);
					return;
				}
				
				for(Player p : main.getServer().getOnlinePlayers()) {
					if(!isDefender(p)) {
						p.setGameMode(GameMode.SURVIVAL);
						p.teleport(behind(p.getLocation()));
					}
				}
				main.getServer().broadcastMessage("§5[§2SiegeGame§5] §6Début de la manche, que le meilleur gagne !");
				started = true;
			}
		}.runTaskLater(main, 7200);
		
		main.getServer().broadcastMessage("§5[§2SiegeGame§5] §6Phase de préparation, §e5 minutes§6.");
	}

	private void testStop(TestStopReason reason) {
		boolean defenderWon = reason != TestStopReason.MERCHANT_DEATH;
		
		if(this.firstRound) {
			
			this.defenderWonFirstRound = defenderWon;
			this.lastTime = this.time;
			this.started = false;
			
			main.getServer().broadcastMessage("§6Le §a"+(defenderWon ? this.communism.getDisplayName() : this.capitalism.getDisplayName())
					+"§6 a gagné §dla première manche §6!");
			
			for(Player p : main.getServer().getOnlinePlayers())
				p.setGameMode(GameMode.SPECTATOR);
			
			this.task1.cancel();
			this.deaths.clear();
			
			main.getServer().getScheduler().runTaskLater(main, () -> {
				this.task2.cancel();
				
				this.time = 0;
				this.firstRound = false;
				
				this.communism.setMerchant(null);
				this.capitalism.setMerchant(null);
				WeaponsMerchant.killMerchants();
				
				this.resetScoreboard();
				
				this.start();
			}, 200);
			
		} else {
			
			main.getServer().broadcastMessage("§6Le §a"+(defenderWon ? this.capitalism.getDisplayName() : this.communism.getDisplayName())
					+"§6 a gagné §dla deuxième manche §6!");
			
			if(this.defenderWonFirstRound != defenderWon) { // The same team won twice
				this.stop(defenderWon ? capitalism : communism, true);
			} else { // The attackers or defenders won only
				if(this.time <= this.lastTime) { // The last time was better -> The winner of the game is the winner of the round
					this.stop(defenderWon ? capitalism : communism, true);
				} else { // The previous time was better -> The winner of the game is the winner of the previous round
					this.stop(defenderWon ? communism : capitalism, true);
				}
			}
		}
	}
	
	@Override
	public void stop(BigTeamDefender winner, boolean message) {
		if (message)
			main.getServer().broadcastMessage("§6Fin de la partie ! Le §a" + winner.getDisplayName() + "§6 a gagné !");
		for (Player p : main.getServer().getOnlinePlayers()) {
			p.getInventory().clear();
			p.setGameMode(GameMode.CREATIVE);
			p.teleport(main.getServer().getWorlds().get(0).getSpawnLocation());
		}
		if(task1 != null)
			task1.cancel();
		if(task2 != null)
			task2.cancel();
		if(obj != null) {
			this.resetScoreboard();
			this.obj.unregister();
		}
		started = false;
		stopped = true;
		main.resetGame();
		this.world.removeWorlds();
	}

	@Override
	public boolean isStarted() {
		return this.started;
	}
	
	@Override
	public boolean isStopped() {
		return this.stopped;
	}

	@Override
	public BigTeamDefender getTeamCapitalism() {
		return this.capitalism;
	}

	@Override
	public BigTeamDefender getTeamCommunism() {
		return this.communism;
	}

	@Override
	public WeaponsMerchant createMerchant(BigTeamDefender team, Location loc) {
		boolean defender = firstRound == team.equals(this.communism);
		if(defender) {
			return new SiegeMerchant(team, loc, defender);
		} else {
			if(this.started) {
				SiegeMerchant merchant = new SiegeMerchant(team, loc, defender);
				merchant.addMoney(40); // TODO see
				return merchant;
			} else {
				return null;
			}
		}
	}

	@Override
	public void handleRespawn(PlayerRespawnEvent e) {
		final Player p = e.getPlayer();
		Defender def = this.main.getDefender(p);
		if(def instanceof BigTeamDefender) {
			WeaponsMerchant merchant = ((BigTeamDefender) def).getMerchant();
			// Respawn point
			if(merchant != null) {
				e.setRespawnLocation(merchant.getLocation());
			} else {
				World world = this.firstRound ? this.world.getWorld1() : this.world.getWorld2();
				e.setRespawnLocation(
						behind(this.isDefender(p) ? this.world.getDefenseSpawn().toLocation(world) : this.world.getAttackSpawn().toLocation(world)));
			}
			// GameMode after respawn
			p.setGameMode(GameMode.SPECTATOR);
			if(this.isStarted() && (this.isDefender(p) || this.deaths.getOrDefault(p.getUniqueId(), 0) < MAX_DEATHS)) {
				main.getServer().getScheduler().runTaskLater(main, () -> {
					if(main.hasGame() && p.isOnline()) {
						p.setGameMode(GameMode.SURVIVAL);
					}
				}, 60);
			}
		}
	}

	@Override
	public void handlePlayerDeath(Player p, List<ItemStack> drops) {
		if(!this.isDefender(p)) {
			int deaths = this.deaths.getOrDefault(p.getUniqueId(), 0);
			this.deaths.put(p.getUniqueId(), deaths + 1);
		}
	}

	@Override
	public void handleMerchantDeath(WeaponsMerchant merchant, List<ItemStack> drops) {
		drops.clear();
		boolean defender = firstRound == merchant.equals(this.communism.getMerchant());
		if(defender) {
			this.testStop(TestStopReason.MERCHANT_DEATH);
		}
	}
	
	public boolean isDefender(Player p) {
		return firstRound == this.communism.getTeam().hasEntry(p.getName());
	}
	
	private int getRemainingLive() {
		int r = 0;
		for(Player online : main.getServer().getOnlinePlayers()) {
			if(!this.isDefender(online)) {
				r += MAX_DEATHS - this.deaths.getOrDefault(online.getUniqueId(), 0);
			}
		}
		return r;
	}
	
	private void resetScoreboard() {
		for(String entry : main.getScoreboard().getEntries()) {
			if(obj.getScore(entry).isScoreSet()) {
				main.getScoreboard().resetScores(entry);
			}
		}
	}
	
	private Location behind(Location test) {
		test.add(0, -2, 0);
		while(test.add(0, -1, 0).getBlock().getType() == Material.AIR);
		test.add(0, 1, 0);
		return test;
	}
	
	
	private static enum TestStopReason {
		TIME, MERCHANT_DEATH, ATTACKERS_DEATH;
	}

}

package be.vilevar.missiles.game;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;

import be.vilevar.missiles.Main;
import be.vilevar.missiles.defense.defender.TeamDefender;
import be.vilevar.missiles.mcelements.merchant.WeaponsMerchant;

public class Game {
	
	private static final String capitalistHorse = "{Variant:513,Health:30,Attributes:[{Name:\"horse.jump_strength\",Base:1.5f},"
										+ "{Name:\"generic.movement_speed\",Base:0.8f},{Name:\"generic.max_health\",Base:30F}]}",
								communistHorse = "{Variant:768,Health:30,Attributes:[{Name:\"horse.jump_strength\",Base:1.5f},"
										+ "{Name:\"generic.movement_speed\",Base:0.8f},{Name:\"generic.max_health\",Base:30F}]}";
	

	private Main main = Main.i;
	private TeamDefender communism;
	private TeamDefender capitalism;
	private boolean started;
	private boolean stopped;
	private int task;

	public Game() {
		this.communism = new TeamDefender(main.getCommunism(), communistHorse);
		this.capitalism = new TeamDefender(main.getCapitalism(), capitalistHorse);
	}

	public void prepare() {
		Server server = main.getServer();
		server.broadcastMessage("§6Préparation de la partie (§c15 minutes§6) !");

		BukkitScheduler scheduler = server.getScheduler();

		this.task = scheduler.scheduleSyncRepeatingTask(main, () -> {
			if(this.communism.getMerchant() != null)
				this.communism.getMerchant().addMoney(20);
			if(this.capitalism.getMerchant() != null)
				this.capitalism.getMerchant().addMoney(20);
		}, 200, 200);
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
				}, 200, 200);
				
				// Normal
				scheduler.runTaskLater(main, () -> {
					scheduler.cancelTask(task);
					
					task = scheduler.scheduleSyncRepeatingTask(main, () -> {
						this.communism.getMerchant().addMoney(20);
						this.capitalism.getMerchant().addMoney(20);
					}, 400, 400);
				}, 6100);
			}
		}, 18100);
	}
	
	public TeamDefender getTeamCapitalism() {
		return capitalism;
	}
	
	public TeamDefender getTeamCommunism() {
		return communism;
	}


	public boolean isStarted() {
		return started;
	}

	public boolean isStopped() {
		return stopped;
	}


	public void stop(TeamDefender winner, boolean message) {
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
		WeaponsMerchant.killMerchants();
	}
	
	
	private void sendNear(TeamDefender defender, Location loc) {
		double angle = Math.random() * 2 * Math.PI;
		double dist = 200 * Math.sqrt(Math.random());
		
		double x = loc.getX() + dist * Math.cos(angle);
		double z = loc.getZ() + dist * Math.sin(angle);
		
		Location def = defender.getMerchant().getLocation();
		double distance = Main.round(Math.sqrt(Math.pow(x - def.getX(), 2) + Math.pow(z - def.getZ(), 2)));
		
		defender.sendMessage("§6Activité ennemie détectée proche de §ax="+Main.round(x)+"§6, §az="+Main.round(z)+"§6 (§c"+distance+"m§6).");
	}

}

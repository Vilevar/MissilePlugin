package be.vilevar.missiles;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.scoreboard.Team.Option;
import org.bukkit.scoreboard.Team.OptionStatus;

import be.vilevar.missiles.commands.BasicCommands;
import be.vilevar.missiles.commands.BigGameCommands;
import be.vilevar.missiles.defense.Defender;
import be.vilevar.missiles.defense.defender.PlayerDefender;
import be.vilevar.missiles.defense.defender.TeamDefender;
import be.vilevar.missiles.game.Game;
import be.vilevar.missiles.game.GameListener;
import be.vilevar.missiles.mcelements.CustomElementManager;
import be.vilevar.missiles.mcelements.abm.ABMLauncher;
import be.vilevar.missiles.mcelements.artillery.Howitzer;
import be.vilevar.missiles.mcelements.crafting.MissileCraftBlock;
import be.vilevar.missiles.mcelements.crafting.RVCraftBlock;
import be.vilevar.missiles.mcelements.launcher.MissileLauncherBlock;
import be.vilevar.missiles.mcelements.radar.Radar;
import be.vilevar.missiles.merchant.WeaponsMerchant;
import be.vilevar.missiles.missile.ballistic.explosives.ExplosiveManager;

public class Main extends JavaPlugin {

	public static Main i;

	
	private HashMap<UUID, PlayerDefender> players = new HashMap<>();

	private CustomElementManager custom;

	private WorldManager wm;

	private Scoreboard scoreboard;
	private Team communism;
	private Team capitalism;

	private Game game;

	@Override
	public void onEnable() {
		i = this;

		this.wm = new WorldManager(this, this.getServer().getWorlds().get(0));

		this.scoreboard = this.getServer().getScoreboardManager().getMainScoreboard();
		this.communism = this.scoreboard.getTeam("Communisme");
		if (this.communism == null)
			this.prepareTeam(this.communism = this.scoreboard.registerNewTeam("Communisme"), ChatColor.RED,
					"Communiste ");
		this.capitalism = this.scoreboard.getTeam("Capitalisme");
		if (this.capitalism == null)
			this.prepareTeam(this.capitalism = this.scoreboard.registerNewTeam("Capitalisme"), ChatColor.BLUE,
					"Capitaliste ");

		PluginManager pm = getServer().getPluginManager();
		
		pm.registerEvents(this.custom = new CustomElementManager(this, pm), this);
		pm.registerEvents(new MainEventListener(), this);
		pm.registerEvents(new GameListener(), this);
		
		BasicCommands basicCommands = new BasicCommands();
		BigGameCommands gameCommands = new BigGameCommands();
		getCommand("game").setExecutor(basicCommands);
		getCommand("merchant").setExecutor(basicCommands);
//		getCommand("discharge").setExecutor(this);
		getCommand("outpost").setExecutor(gameCommands);
		getCommand("setoutpost").setExecutor(gameCommands);
		getCommand("base").setExecutor(gameCommands);

		Radar.createCheckMissileScheduler(this);
		ExplosiveManager.startScheduler(this);

		for (Player p : getServer().getOnlinePlayers()) {
			players.put(p.getUniqueId(), new PlayerDefender(p));
		}
	}

	@Override
	public void onDisable() {
		if (game != null)
			game.stop(null, false);
		else
			this.resetGame();
	}

	
	public CustomElementManager getCustomElementManager() {
		return custom;
	}
	
	

	
	
	public WorldManager getWorldManager() {
		return wm;
	}

	private void prepareTeam(Team team, ChatColor color, String prefix) {
		team.setColor(color);
		team.setPrefix(prefix);
		this.updateTeam(team);
	}
	
	private void updateTeam(Team team) {
		team.setAllowFriendlyFire(false);
		team.setCanSeeFriendlyInvisibles(true);
		team.setOption(Option.NAME_TAG_VISIBILITY, OptionStatus.FOR_OTHER_TEAMS);
		team.setOption(Option.COLLISION_RULE, OptionStatus.FOR_OWN_TEAM);
	}
	
	public void updateTeams() {
		this.updateTeam(communism);
		this.updateTeam(capitalism);
	}

	
	
	public Game getGame() {
		return game;
	}
	
	public boolean hasGame() {
		return this.game != null;
	}

	public Team getCommunism() {
		return communism;
	}

	public Team getCapitalism() {
		return capitalism;
	}
	
	public Scoreboard getScoreboard() {
		return scoreboard;
	}
	
	public String prepareGame(Game game) {
		this.resetGame();
		
		this.game = game;
		
		String error = game.prepare();
		if(error == null) {
			game.start();
		} else {
			game.stop(null, false);
		}
		return error;
	}
	
	
	public void resetGame() {
		this.game = null;
		
		WeaponsMerchant.killMerchants();
		
		ABMLauncher.destroyAll(false);
		Howitzer.destroyAll();
		MissileCraftBlock.destroyAll(false);
		RVCraftBlock.destroyAll(false);
		MissileLauncherBlock.destroyAll(false);
		// TODO Remove everything (merchant, missile crafters, radars, missile launchers, etc.)
	}
	
	public TeamDefender getTeamDefender(Player p) {
		if (this.game != null) {
			if (this.communism.hasEntry(p.getName())) {
				return this.game.getTeamCommunism();
			} else if (this.capitalism.hasEntry(p.getName())) {
				return this.game.getTeamCapitalism();
			}
		}
		return null;
	}

	public Defender getDefender(Player p) {
		TeamDefender td = this.getTeamDefender(p);
		if(td != null)
			return td;
		PlayerDefender def = this.players.get(p.getUniqueId());
		if (def != null) {
			return def;
		} else {
			def = new PlayerDefender(p);
			this.players.put(p.getUniqueId(), def);
			return def;
		}
	}
	
	
	
	
	
	
	
	
	
	public static void display(Particle particle, Location loc) {
		loc.getWorld().spawnParticle(particle, loc, 1, 0, 0, 0, 0, null);
	}
	
	public static void display(Particle particle, Location loc, boolean force) {
		loc.getWorld().spawnParticle(particle, loc, 1, 0, 0, 0, 0, null, force);
	}

	public static int clamp(int min, int max, int value) {
		return Math.max(min, Math.min(max, value));
	}

	public static double clamp(double min, double max, double value) {
		return Math.max(min, Math.min(max, value));
	}

	private static final MathContext mc = new MathContext(5, RoundingMode.HALF_UP);

	public static double round(double a) {
		return new BigDecimal(a, mc).doubleValue();
	}
}

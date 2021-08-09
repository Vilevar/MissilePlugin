package be.vilevar.missiles;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.scoreboard.Team.Option;
import org.bukkit.scoreboard.Team.OptionStatus;

import be.vilevar.missiles.command.CommandsManager;
import be.vilevar.missiles.defense.Defender;
import be.vilevar.missiles.defense.defender.PlayerDefender;
import be.vilevar.missiles.event.EventsManager;
import be.vilevar.missiles.game.Game;
import be.vilevar.missiles.mcelements.CustomElementManager;
import be.vilevar.missiles.mcelements.merchant.WeaponsMerchant;
import be.vilevar.missiles.mcelements.radar.Radar;
import be.vilevar.missiles.missile.ballistic.explosives.ExplosiveManager;
import be.vilevar.missiles.utils.ParticleEffect;

public class Main extends JavaPlugin {

	public static void display(ParticleEffect particle, Location loc) {
		particle.display(0, 0, 0, 0, 1, loc, loc.getWorld().getPlayers());
	}

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

		new EventsManager(this); 
		new CommandsManager(this);
		
		Radar.createCheckMissileScheduler(this);
		ExplosiveManager.startScheduler(this);
		
		for(Player p : getServer().getOnlinePlayers()) {
			players.put(p.getUniqueId(), new PlayerDefender(p));
		}
	}

	@Override
	public void onDisable() {
		if(game != null)
			game.stop(null, false);
		WeaponsMerchant.killMerchants();
	}
	
	public WorldManager getWorldManager() {
		return wm;
	}

	private void prepareTeam(Team team, ChatColor color, String prefix) {
		team.setAllowFriendlyFire(false);
		team.setCanSeeFriendlyInvisibles(true);
		team.setColor(color);
		team.setOption(Option.NAME_TAG_VISIBILITY, OptionStatus.FOR_OWN_TEAM);
		team.setPrefix(prefix);
	}
	
	public Game getGame() {
		return game;
	}

	public boolean hasGame() {
		return this.game != null;
	}

	public void newGame(Game game) {
		this.game = game;
		this.game.prepare();
	}
	
	public Team getCommunism() {
		return communism;
	}
	
	public Team getCapitalism() {
		return capitalism;
	}
	
	public void resetGame() {
		this.game = null;
	}
	
	public Defender getDefender(Player p) {
		if(this.game != null) {
			if(this.communism.hasEntry(p.getName())) {
				return this.game.getTeamCommunism();
			} else if(this.capitalism.hasEntry(p.getName())) {
				return this.game.getTeamCapitalism();
			}
		}
		PlayerDefender def = this.players.get(p.getUniqueId());
		if(def != null) {
			return def;
		} else {
			def = new PlayerDefender(p);
			this.players.put(p.getUniqueId(), def);
			return def;
		}
	}

	public CustomElementManager getCustom() {
		return this.custom;
	}

	public void setCustom(CustomElementManager custom) {
		this.custom = custom;
	}

	public HashMap<UUID, PlayerDefender> getPlayers() {
		return this.players;
	}

	
//	public static Vector rotate(Vector v, Location loc) {
//		double yaw = toRadians(loc.getYaw());
//		double pitch = toRadians(loc.getPitch());
//		v = rotateAboutX(v, pitch);
//		v = rotateAboutY(v, -yaw);
//		// v = rotateAboutZ(v, pitch);
//		return v;
//	}
//
//	public static Vector rotateAboutX(Vector v, double a) {
//		double y = cos(a) * v.getY() - sin(a) * v.getZ();
//		double z = sin(a) * v.getY() + cos(a) * v.getZ();
//		return v.setY(y).setZ(z);
//	}
//
//	public static Vector rotateAboutY(Vector v, double b) {
//		double x = cos(b) * v.getX() + sin(b) * v.getZ();
//		double z = -sin(b) * v.getX() + cos(b) * v.getZ();
//		return v.setX(x).setZ(z);
//	}
//
//	public static Vector rotateAboutZ(Vector v, double c) {
//		double x = cos(c) * v.getX() - sin(c) * v.getY();
//		double y = sin(c) * v.getX() + cos(c) * v.getY();
//		return v.setX(x).setY(y);
//	}

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

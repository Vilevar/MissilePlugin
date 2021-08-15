package be.vilevar.missiles;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.BlockState;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.scoreboard.Team.Option;
import org.bukkit.scoreboard.Team.OptionStatus;

import be.vilevar.missiles.defense.Defender;
import be.vilevar.missiles.defense.defender.PlayerDefender;
import be.vilevar.missiles.defense.defender.TeamDefender;
import be.vilevar.missiles.game.DefaultGame;
import be.vilevar.missiles.game.Game;
import be.vilevar.missiles.game.GameListener;
import be.vilevar.missiles.mcelements.CustomElementManager;
import be.vilevar.missiles.mcelements.merchant.WeaponsMerchant;
import be.vilevar.missiles.mcelements.radar.Radar;
import be.vilevar.missiles.mcelements.weapons.Weapon;
import be.vilevar.missiles.missile.ballistic.explosives.ExplosiveManager;

public class Main extends JavaPlugin implements Listener {

	public static void display(Particle particle, Location loc) {
		loc.getWorld().spawnParticle(particle, loc, 1, 0, 0, 0, 0, null);
	}
	
	public static void display(Particle particle, Location loc, boolean force) {
		loc.getWorld().spawnParticle(particle, loc, 1, 0, 0, 0, 0, null, force);
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

		PluginManager pm = getServer().getPluginManager();
		
		pm.registerEvents(this, this);
		pm.registerEvents(this.custom = new CustomElementManager(this, pm), this);
		pm.registerEvents(new GameListener(), this);
		
		getCommand("missile").setExecutor(this);
		getCommand("discharge").setExecutor(this);
		getCommand("outpost").setExecutor(this);
		getCommand("setoutpost").setExecutor(this);
		getCommand("base").setExecutor(this);
		getCommand("merchant").setExecutor(this);

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
		WeaponsMerchant.killMerchants();
	}

	
	
	
	@EventHandler
	public void onload(ChunkLoadEvent e) {
		Chunk c = e.getChunk();
		
		for(BlockState state : c.getTileEntities()) {
//			state.getBlock().setType(Material.AIR);
			System.out.println(state.getBlock().getType());
		}
		
		for(Entity ent : c.getEntities()) {
			if(ent.getType() != EntityType.PLAYER) {
//				ent.remove();
				System.out.println(ent.getType());
			}
		}
	}
	
	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		if(!players.containsKey(e.getPlayer().getUniqueId()))
			players.put(e.getPlayer().getUniqueId(), new PlayerDefender(e.getPlayer()));
	}
	
	@EventHandler
	public void onClick(PlayerInteractEvent e) {
		if (e.getItem() != null && e.getItem().getType() == Material.BOOK && e.getPlayer().getGameMode() == GameMode.CREATIVE) {
			e.getPlayer().getInventory().addItem(
					CustomElementManager.SNIPER.createItem(), CustomElementManager.SNIPER.getAmmunition().create(),
					CustomElementManager.PISTOL.createItem(), CustomElementManager.PISTOL.getAmmunition().create(),
					CustomElementManager.MACHINE_GUN.createItem(), CustomElementManager.MACHINE_GUN.getAmmunition().create(),
					CustomElementManager.SHOTGUN.createItem(), CustomElementManager.SHOTGUN.getAmmunition().create(),
					CustomElementManager.BOMB.create(), CustomElementManager.SMOKE_BOMB.create(),
					CustomElementManager.SRBM.create(), CustomElementManager.MRBM.create(), CustomElementManager.ICBM.create(),
					CustomElementManager.RANGEFINDER.create(),
					CustomElementManager.WEATHER_FORECASTER.create(),
					CustomElementManager.SMALL_SHELL.create(), CustomElementManager.BIG_SHELL.create());
//					CustomElementManager.REMOTE_CONTROL.create());
			if(e.getPlayer().isSneaking())
				e.getPlayer().getWorld().createExplosion(e.getPlayer().getLocation(), 5.f);
		}
		if (e.getItem() != null && e.getItem().getType() == Material.STICK && e.getPlayer().getGameMode() == GameMode.CREATIVE && e.getAction() == Action.LEFT_CLICK_AIR) {
			List<EntityType> ents = new ArrayList<>();
			e.getPlayer().sendMessage(e.getPlayer().getWorld().getEntities().size()+"");
			for(Entity ent : e.getPlayer().getWorld().getEntities()) {
				if(!ents.contains(ent.getType())) {
					ents.add(ent.getType());
					e.getPlayer().sendMessage(ent.getType()+" "+ent.getLocation());
				}
				if(ent.getType() != EntityType.PLAYER)
					ent.remove();
			}
			List<Material> mat = new ArrayList<>();
			for(Chunk c : e.getPlayer().getWorld().getLoadedChunks()) {
				for(BlockState bs : c.getTileEntities()) {
					if(!mat.contains(bs.getType())) {
						mat.add(bs.getType());
						e.getPlayer().sendMessage(bs.getType()+" "+bs.getLocation());
					}
					bs.getBlock().setType(Material.AIR);
				}
			}
		}
		if(e.getItem() != null && e.getItem().getType() == Material.BLAZE_ROD && e.getAction() == Action.RIGHT_CLICK_BLOCK) {
			
			System.out.println(Particle.FLAME.getDataType().getSimpleName());
			e.getClickedBlock().getWorld().spawnParticle(Particle.FLAME, e.getClickedBlock().getLocation().add(0, 2, 0),
					1, 0, 0, 0, 0);
			
		}
	}

	@EventHandler
	public void onDeath(PlayerDeathEvent e) {
		Player p = e.getEntity();
		if (p.getKiller() != null) {
			try {
				e.setDeathMessage(e.getDeathMessage() + " (" + p.getLocation().distance(p.getKiller().getLocation()) + "m)");
			} catch (Exception exp) {
			}
		}
	}
	
	@EventHandler
	public void onSpawn(CreatureSpawnEvent e) {
//		if(e.getSpawnReason() == SpawnReason.NATURAL || e.getSpawnReason() == SpawnReason.SPAWNER) {
		getServer().broadcastMessage(e.getEntity()+" spawned !!!! "+e.getSpawnReason());
//		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onBlockPhysics(BlockFadeEvent e) {
		if (e.getBlock().getType() == Material.ICE || e.getBlock().getType() == Material.SNOW) {
			e.setCancelled(true);
		}
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (sender instanceof Player) {
			Player p = (Player) sender;
			if (command.getName().equals("discharge")) {
				ItemStack is = p.getInventory().getItemInMainHand();
				Weapon w = this.custom.getWeapons().getWeapon(is);
				if (w == null) {
					p.sendMessage("§6Mettez une §carme à feu§6 dans votre §amain principale§c.");
				} else {
					ItemMeta im = is.getItemMeta();
					if (im instanceof Damageable) {
						Damageable dam = (Damageable) im;
						dam.setDamage(is.getType().getMaxDurability());
						is.setItemMeta(im);
						p.getInventory().setItemInMainHand(is);
						p.sendMessage("§6Arme déchargée.");
					} else {
						p.sendMessage("§6Cette arme a munitions infinies.");
					}
				}
				return true;
			} else if (command.getName().equals("base")) {
				if(game != null) {
					TeamDefender team = communism.hasEntry(p.getName()) ? game.getTeamCommunism()
							: capitalism.hasEntry(p.getName()) ? game.getTeamCapitalism() : null;
					if(team != null) {
						if(team.getMerchant() != null) {
							p.teleport(team.getMerchant().getLocation());
						} else {
							Location loc = p.getLocation();
							for(int i = -1; i <= 1; i++) {
								for(int j = -1; j <= 1; j++) {
									loc.clone().add(i, -1, j).getBlock().setType(Material.BEDROCK);
								}
							}
							team.setMerchant(new WeaponsMerchant(team,
									loc.getBlock().getLocation().add(0.5, 0, 0.5).setDirection(loc.getDirection())));
						}
					} else {
						p.sendMessage("§cVous n'êtes pas membre d'une équipe.");
					}
				} else {
					p.sendMessage("§cPas de partie en cours.");
				}
				return true;
			} else if (command.getName().equals("outpost")) {
				if(game != null) {
					TeamDefender team = communism.hasEntry(p.getName()) ? game.getTeamCommunism()
							: capitalism.hasEntry(p.getName()) ? game.getTeamCapitalism() : null;
					if(team != null && team.getOutpost() != null) {
						p.teleport(team.getOutpost());
					} else {
						p.sendMessage("§cVous n'êtes pas membre d'une équipe ayant un avant-poste.");
					}
				} else {
					p.sendMessage("§cPas de partie en cours.");
				}
				return true;
			} else if (command.getName().equals("setoutpost")) {
				if(game != null) {
					TeamDefender team = communism.hasEntry(p.getName()) ? game.getTeamCommunism()
							: capitalism.hasEntry(p.getName()) ? game.getTeamCapitalism() : null;
					if(team != null) {
						team.setOutpost(p.getLocation());
						p.sendMessage("§6Avant-poste §aplacé§6 à votre position.");
					} else {
						p.sendMessage("§cVous n'êtes pas membre d'une équipe.");
					}
				} else {
					p.sendMessage("§cPas de partie en cours.");
				}
				return true;
			} else if (command.getName().equals("merchant") && this.game == null) {
				new WeaponsMerchant(this.getDefender(p), p.getLocation());
			}
		} else {
			if (command.getName().equals("missile")) {
				
				if(this.game != null) {
					sender.sendMessage("§cIl y a déjà une partie commencée.");
					return true;
				}
				
				WeaponsMerchant.killMerchants();
				
				this.game = new DefaultGame();
				
				
				
				String error = this.game.prepare();
				if(error == null) {
					this.game.start();
				} else {
					sender.sendMessage(error);
				}
				
				return true;
			}
		}
		return true;
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
		if (this.game != null) {
			if (this.communism.hasEntry(p.getName())) {
				return this.game.getTeamCommunism();
			} else if (this.capitalism.hasEntry(p.getName())) {
				return this.game.getTeamCapitalism();
			}
		}
		PlayerDefender def = this.players.get(p.getUniqueId());
		if (def != null) {
			return def;
		} else {
			def = new PlayerDefender(p);
			this.players.put(p.getUniqueId(), def);
			return def;
		}
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

package be.vilevar.missiles;

import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.toRadians;

import java.util.Collection;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.PortalCreateEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.scoreboard.Team.Option;
import org.bukkit.scoreboard.Team.OptionStatus;
import org.bukkit.util.Vector;

import be.vilevar.missiles.game.Game;
import be.vilevar.missiles.game.GameListener;
import be.vilevar.missiles.mcelements.CustomElementManager;
import be.vilevar.missiles.mcelements.crafting.MissileCraftBlock;
import be.vilevar.missiles.mcelements.launcher.MissileLauncherBlock;
import be.vilevar.missiles.mcelements.radar.MissileRadarBlock;
import be.vilevar.missiles.mcelements.weapons.Weapon;
import be.vilevar.missiles.utils.ParticleEffect;

public class Main extends JavaPlugin implements Listener {

	public static void display(ParticleEffect particle, Location loc) {
		particle.display(0, 0, 0, 0, 1, loc, loc.getWorld().getPlayers());
	}

	public static Main i;

	private CustomElementManager custom;

	private Scoreboard scoreboard;
	private Team communism;
	private Team capitalism;
	
	private Game game;

	private HashMap<UUID, Horse> horses = new HashMap<>();

	@Override
	public void onEnable() {
		i = this;

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
		getCommand("horse").setExecutor(this);
		getCommand("ladder").setExecutor(this);
		getCommand("chest").setExecutor(this);
		getCommand("outpost").setExecutor(this);
		getCommand("setoutpost").setExecutor(this);
		getCommand("base").setExecutor(this);
	}

	@Override
	public void onDisable() {
		if(game != null)
			game.stop(null, false);
	}
	
	@EventHandler
	public void onClick(PlayerInteractEvent e) {
		if (e.getItem() != null && CustomElementManager.FUEL.isParentOf(e.getItem())) {
			e.setCancelled(true);
		}
		if (e.getItem() != null && e.getItem().getType() == Material.BOOK && e.getPlayer().getGameMode() == GameMode.CREATIVE) {
			e.getPlayer().getInventory().addItem(CustomElementManager.SNIPER.createItem(),
					CustomElementManager.SNIPER.getAmmunition().create(), CustomElementManager.PISTOL.createItem(),
					CustomElementManager.PISTOL.getAmmunition().create(), CustomElementManager.MACHINE_GUN.createItem(),
					CustomElementManager.MACHINE_GUN.getAmmunition().create(),
					CustomElementManager.SHOTGUN.createItem(), CustomElementManager.SHOTGUN.getAmmunition().create(),
					CustomElementManager.BOMB.create(), CustomElementManager.SMOKE_BOMB.create(),
					CustomElementManager.BALLISTIC_MISSILE.create(), CustomElementManager.FUEL.create(),
					CustomElementManager.RANGEFINDER.create());
			if(e.getPlayer().isSneaking())
				e.getPlayer().getWorld().createExplosion(e.getPlayer().getLocation(), 5.f);
		}
	}

	@EventHandler
	public void onPlace(BlockPlaceEvent e) {
		Block block = e.getBlock();
		if (CustomElementManager.MISSILE_RADAR.isParentOf(block)) {
			MissileRadarBlock.radars.add(new MissileRadarBlock(block.getLocation()));
		}
		if (CustomElementManager.MISSILE_LAUNCHER.isParentOf(block)) {
			MissileLauncherBlock.launchers.add(new MissileLauncherBlock(block.getLocation()));
		}
		if (CustomElementManager.MISSILE_CRAFT.isParentOf(block)) {
			MissileCraftBlock.crafts.add(new MissileCraftBlock(block.getLocation()));
		}
	}

	@EventHandler
	public void onBreak(BlockBreakEvent e) {
		this.blockBreak(e.getBlock());
	}

	@EventHandler
	public void onExplosionByEntity(EntityExplodeEvent e) {
		e.blockList().forEach(block -> this.blockBreak(block));
	}
	
	@EventHandler
	public void onExplosionByBlock(BlockExplodeEvent e) {
		e.blockList().forEach(block -> this.blockBreak(block));
	}

	private void blockBreak(Block block) {
		if (CustomElementManager.MISSILE_RADAR.isParentOf(block)) {
			MissileRadarBlock.checkDestroy(block.getLocation());
		}
		if (CustomElementManager.MISSILE_LAUNCHER.isParentOf(block)) {
			MissileLauncherBlock.checkDestroy(block.getLocation());
		}
		if (CustomElementManager.MISSILE_CRAFT.isParentOf(block)) {
			MissileCraftBlock.checkDestroy(block.getLocation());
		}
	}

	@EventHandler
	public void onPortal(PortalCreateEvent e) {
		e.setCancelled(true);
	}

	@EventHandler
	public void onEntitySpawn(EntitySpawnEvent e) {
		if (e.getEntityType() == EntityType.SLIME)
			e.setCancelled(true);
	}

	@EventHandler
	public void onDeath(PlayerDeathEvent e) {
		Player p = e.getEntity();
		if (p.getKiller() != null) {
			try {
				e.setDeathMessage(
						e.getDeathMessage() + " (" + p.getLocation().distance(p.getKiller().getLocation()) + "m)");
			} catch (Exception exp) {
			}
		}
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
						p.sendMessage("§6Arme déchargée.");
					} else {
						p.sendMessage("§6Cette arme a munitions infinies.");
					}
				}
				return true;
			} else if (command.getName().equals("horse")) {
				Horse horse = this.horses.get(p.getUniqueId());
				if (horse != null && !horse.isDead()) {
					horse.teleport(p.getLocation());
				} else {
					horse = p.getWorld().spawn(p.getLocation(), Horse.class);
					if (this.capitalism.hasEntry(p.getName())) {
						horse.setColor(Horse.Color.CREAMY);
						horse.setStyle(Horse.Style.WHITE_DOTS);
					} else if (this.communism.hasEntry(p.getName())) {
						horse.setColor(Horse.Color.DARK_BROWN);
						horse.setStyle(Horse.Style.BLACK_DOTS);
					} else {
						horse.setColor(Horse.Color.BLACK);
						horse.setStyle(Horse.Style.NONE);
					}
					horse.setJumpStrength(1.5);
					horse.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(0.75);
					horse.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(30);
					horse.setHealth(30);
					horse.setTamed(true);
					horse.getInventory().setSaddle(new ItemStack(Material.SADDLE));
					this.horses.put(p.getUniqueId(), horse);
				}
				return true;
			} else if (command.getName().equals("ladder")) {
				p.getInventory().addItem(new ItemStack(Material.LADDER, 64));
				return true;
			} else if (command.getName().equals("chest")) {
				p.getInventory().addItem(new ItemStack(Material.CHEST));
				return true;
			} else if (command.getName().equals("base")) {
				if(game != null) {
					Team team = communism.hasEntry(p.getName()) ? communism : capitalism.hasEntry(p.getName()) ? capitalism : null;
					Location banner;
					if(team != null && (banner = game.getBanner(team)) != null) {
						p.teleport(banner);
					} else {
						p.sendMessage("§cVous n'êtes pas membre d'une équipe ayant placé sa bannière.");
					}
				} else {
					p.sendMessage("§cPas de partie en cours.");
				}
				return true;
			} else if (command.getName().equals("outpost")) {
				if(game != null) {
					Team team = communism.hasEntry(p.getName()) ? communism : capitalism.hasEntry(p.getName()) ? capitalism : null;
					Location outpost;
					if(team != null && (outpost = game.getOutpost(team)) != null) {
						p.teleport(outpost);
					} else {
						p.sendMessage("§cVous n'êtes pas membre d'une équipe ayant un avant-poste.");
					}
				} else {
					p.sendMessage("§cPas de partie en cours.");
				}
				return true;
			} else if (command.getName().equals("setoutpost")) {
				if(game != null) {
					Team team = communism.hasEntry(p.getName()) ? communism : capitalism.hasEntry(p.getName()) ? capitalism : null;
					if(team != null) {
						game.setOutpost(team, p.getLocation());
						p.sendMessage("§6Avant-poste §aplacé§6 à votre position.");
					} else {
						p.sendMessage("§cVous n'êtes pas membre d'une équipe.");
					}
				} else {
					p.sendMessage("§cPas de partie en cours.");
				}
				return true;
			}
		} else {
			if (command.getName().equals("missile")) {
				
				if(this.game != null) {
					sender.sendMessage("§cPartie déjà commencée.");
					return true;
				}
				
				Collection<? extends Player> online = getServer().getOnlinePlayers();
				if(online.size() < 2) {
					sender.sendMessage("§cPas assez de joueurs.");
					return true;
				}
				
				ItemStack pickaxe = new ItemStack(Material.DIAMOND_PICKAXE);
				pickaxe.addEnchantment(Enchantment.DIG_SPEED, 4);
				ItemStack helmet = new ItemStack(Material.IRON_HELMET);
				ItemStack chestplate = new ItemStack(Material.IRON_CHESTPLATE);
				ItemStack leggings = new ItemStack(Material.IRON_LEGGINGS);
				ItemStack boots = new ItemStack(Material.IRON_BOOTS);
				ItemStack beef = new ItemStack(Material.COOKED_BEEF, 64);
				ItemStack obsidian = new ItemStack(Material.OBSIDIAN, 16);
				
				for(Player p : online) {
					if(!this.communism.hasEntry(p.getName()) && !this.capitalism.hasEntry(p.getName())) {
						if(this.capitalism.getSize() < this.communism.getSize()) {
							this.capitalism.addEntry(p.getName());
						} else {
							this.communism.addEntry(p.getName());
						}
					}
					p.getInventory().clear();
					p.getInventory().addItem(pickaxe, helmet, chestplate, leggings, boots, beef, obsidian);
					p.getEnderChest().clear();
					p.setGameMode(GameMode.SURVIVAL);
				}
				
				if(this.capitalism.getSize() == 0 || this.communism.getSize() == 0) {
					sender.sendMessage("§cLes équipes ne sont pas bien réparties.");
					return true;
				}
				
				this.game = new Game();
				this.game.prepare();
				
				return true;
			}
		}
		return true;
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
	
	public Team getCommunism() {
		return communism;
	}
	
	public Team getCapitalism() {
		return capitalism;
	}
	
	public void resetGame() {
		this.game = null;
	}

	public static Vector rotate(Vector v, Location loc) {
		double yaw = toRadians(loc.getYaw());
		double pitch = toRadians(loc.getPitch());
		v = rotateAboutX(v, pitch);
		v = rotateAboutY(v, -yaw);
		// v = rotateAboutZ(v, pitch);
		return v;
	}

	public static Vector rotateAboutX(Vector v, double a) {
		double y = cos(a) * v.getY() - sin(a) * v.getZ();
		double z = sin(a) * v.getY() + cos(a) * v.getZ();
		return v.setY(y).setZ(z);
	}

	public static Vector rotateAboutY(Vector v, double b) {
		double x = cos(b) * v.getX() + sin(b) * v.getZ();
		double z = -sin(b) * v.getX() + cos(b) * v.getZ();
		return v.setX(x).setZ(z);
	}

	public static Vector rotateAboutZ(Vector v, double c) {
		double x = cos(c) * v.getX() - sin(c) * v.getY();
		double y = sin(c) * v.getX() + cos(c) * v.getY();
		return v.setX(x).setY(y);
	}

}

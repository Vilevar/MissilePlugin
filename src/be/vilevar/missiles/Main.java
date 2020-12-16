package be.vilevar.missiles;

import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.toRadians;

import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.world.PortalCreateEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.scoreboard.Team.Option;
import org.bukkit.scoreboard.Team.OptionStatus;
import org.bukkit.util.Vector;

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
	private int t;
	
	private CustomElementManager custom;
	
	private Scoreboard scoreboard;
	private Team communist;
	private Team capitalist;
	
	@Override
	public void onEnable() {
		i = this;
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(this, this);
		pm.registerEvents(this.custom = new CustomElementManager(this, pm), this);
		getCommand("missile").setExecutor(this);
		getCommand("discharge").setExecutor(this);
		
		this.scoreboard = this.getServer().getScoreboardManager().getMainScoreboard();
		this.communist = this.scoreboard.getTeam("Communisme");
		if(this.communist == null)
			this.prepareTeam(this.communist = this.scoreboard.registerNewTeam("Communisme"), ChatColor.RED, "Communiste ");
		this.capitalist = this.scoreboard.getTeam("Capitalisme");
		if(this.capitalist == null)
			this.prepareTeam(this.capitalist = this.scoreboard.registerNewTeam("Capitalisme"), ChatColor.BLUE, "Capitaliste ");
	}
	
	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		
	}
	
	@EventHandler
	public void onClick(PlayerInteractEvent e) {
		if(e.getItem() != null && CustomElementManager.FUEL.isParentOf(e.getItem())) {
			e.setCancelled(true);
		}
		if(e.getItem() != null && e.getItem().getType() == Material.BOOK) {
			e.getPlayer().getInventory().addItem(
					CustomElementManager.SNIPER.createItem(), CustomElementManager.SNIPER.getAmmunition().create(),
					CustomElementManager.PISTOL.createItem(), CustomElementManager.PISTOL.getAmmunition().create(),
					CustomElementManager.MACHINE_GUN.createItem(), CustomElementManager.MACHINE_GUN.getAmmunition().create(),
					CustomElementManager.SHOTGUN.createItem(), CustomElementManager.SHOTGUN.getAmmunition().create(),
					CustomElementManager.BOMB.create(), CustomElementManager.SMOKE_BOMB.create(),
					CustomElementManager.BALLISTIC_MISSILE.create(), CustomElementManager.FUEL.create(), CustomElementManager.RANGEFINDER.create());
		}
	}
	
	@EventHandler
	public void onPlace(BlockPlaceEvent e) {
		if(e.getBlock().getType() == CustomElementManager.MISSILE_RADAR) {
			MissileRadarBlock.radars.add(new MissileRadarBlock(e.getBlock().getLocation()));
		}
		if(e.getBlock().getType() == CustomElementManager.MISSILE_LAUNCHER) {
			MissileLauncherBlock.launchers.add(new MissileLauncherBlock(e.getBlock().getLocation()));
		}
		if(e.getBlock().getType() == CustomElementManager.MISSILE_CRAFT) {
			MissileCraftBlock.crafts.add(new MissileCraftBlock(e.getBlock().getLocation()));
		}
	}
	
	@EventHandler
	public void onBreak(BlockBreakEvent e) {
		this.blockBreak(e.getBlock());
	}
	
	@EventHandler
	public void onBlockExplode(BlockExplodeEvent e) {
		this.blockBreak(e.getBlock());
	}
	
	private void blockBreak(Block block) {
		if(block.getType()==CustomElementManager.MISSILE_RADAR) {
			MissileRadarBlock.checkDestroy(block.getLocation());
		}
		if(block.getType()==CustomElementManager.MISSILE_LAUNCHER) {
			Location l = block.getLocation();
			MissileLauncherBlock launcher = null;
			for(MissileLauncherBlock mlb : MissileLauncherBlock.launchers) {
				if(mlb.getLocation().equals(l)) {
					launcher = mlb;
					break;
				}
			}
			if(launcher!=null) {
				Bukkit.broadcastMessage(
						"§6Un lanceur de missiles de "+launcher.getOwner().getName()+" a été détruit en ("+l.getBlockX()+"; "+l.getBlockZ()+") !");
			} else {
				Bukkit.broadcastMessage("§6Un lanceur de missiles a été détruit en ("+l.getBlockX()+"; "+l.getBlockZ()+") !");
			}
			MissileLauncherBlock.checkDestroy(block.getLocation());
		}
		if(block.getType()==CustomElementManager.MISSILE_CRAFT) {
			MissileCraftBlock.checkDestroy(block.getLocation());
		}
	}
	
	@EventHandler
	public void onPortal(PortalCreateEvent e) {
		e.setCancelled(true);
	}
	
	@EventHandler
	public void onEntitySpawn(EntitySpawnEvent e) {
		if(e.getEntityType()==EntityType.SLIME)
			e.setCancelled(true);
	}
	
	@EventHandler
	public void onDeath(PlayerDeathEvent e) {
		if(e.getEntity().getKiller() != null) {
			System.out.println(e.getDeathMessage());
		}
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(!(sender instanceof Player) && command.getName().equals("missile")) {
			
			if(t!=0) {
				getServer().getScheduler().cancelTask(t);
				for(Player p : Bukkit.getOnlinePlayers()) {
					p.getInventory().clear();
				}
				return true;
			}
			getServer().broadcastMessage("§aDébut de partie");
			ItemStack pickaxe = new ItemStack(Material.DIAMOND_PICKAXE, 1, (short) 999);
			pickaxe.addEnchantment(Enchantment.DIG_SPEED, 4);
			for(Player p : Bukkit.getOnlinePlayers()) {
				p.getInventory().clear();
				p.setGameMode(GameMode.SURVIVAL);
				ItemStack h = new ItemStack(Material.PLAYER_HEAD);
				SkullMeta sm = (SkullMeta) h.getItemMeta();
				sm.setOwner(p.getName());
				h.setItemMeta(sm);
				p.getInventory().addItem(new ItemStack(Material.IRON_HELMET), new ItemStack(Material.IRON_CHESTPLATE),
						new ItemStack(Material.IRON_LEGGINGS), new ItemStack(Material.IRON_BOOTS), new ItemStack(Material.DIAMOND_SWORD),
						pickaxe, new ItemStack(CustomElementManager.MISSILE_RADAR, 2), new ItemStack(CustomElementManager.MISSILE_LAUNCHER),
						new ItemStack(CustomElementManager.MISSILE_CRAFT), CustomElementManager.RANGEFINDER.create(), h,
						new ItemStack(Material.STONE_BUTTON), new ItemStack(Material.OBSIDIAN, 32), new ItemStack(Material.COOKED_BEEF, 64));
				p.updateInventory();
			}
			t = getServer().getScheduler().scheduleSyncRepeatingTask(this, () -> {
				for(Player p : Bukkit.getOnlinePlayers()) {
					if(p.getGameMode()!=GameMode.SURVIVAL) continue;
					p.sendMessage("§6Nouvelle cargaison");
					Random random = new Random();
					int nMissiles = random.nextInt(2);
					int nFuel = random.nextInt(7) + 3;
					int nTnt = random.nextInt(8);
					int nCompass = random.nextInt(4);
					int nBlazePowder = Math.abs(20 - nMissiles - nFuel - nTnt - nCompass);
					p.getInventory().addItem(CustomElementManager.BALLISTIC_MISSILE.create(nMissiles),
							new ItemStack(Material.BLAZE_POWDER, nBlazePowder), new ItemStack(Material.TNT, nTnt),
							new ItemStack(Material.COMPASS, nCompass), CustomElementManager.FUEL.create(nFuel));
				}
			}, 0, 1200);
			return true;
		/*} else if(command.getName().equals("missile_world")) {
			String name = "missile_world";
			World world = getServer().getWorld(name);
			if(world == null && !(sender instanceof Player))
				MissileWorldGenerator.createWorld((CraftServer) getServer(), name);
			else if(world != null && sender instanceof Player)
				((Player) sender).teleport(world.getSpawnLocation());
				
			return true;*/
		} else if(command.getName().equals("discharge") && sender instanceof Player) {
			Player p = (Player) sender;
			ItemStack is = p.getInventory().getItemInMainHand();
			Weapon w = this.custom.getWeapons().getWeapon(is);
			if(w == null) {
				p.sendMessage("§6Mettez une §carme à feu§6 dans votre §amain principale§c.");
			} else {
				ItemMeta im = is.getItemMeta();
				if(im instanceof Damageable) {
					Damageable dam = (Damageable) im;
					dam.setDamage(is.getType().getMaxDurability());
					p.sendMessage("§6Arme déchargée.");
				} else {
					p.sendMessage("§6Cette arme a munitions infinies.");
				}
			}
			return true;
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
	
	
	public static Vector rotate(Vector v, Location loc) {
		double yaw = toRadians(loc.getYaw());
		double pitch = toRadians(loc.getPitch());
		v = rotateAboutX(v, pitch);
		v = rotateAboutY(v, -yaw);
	//	v = rotateAboutZ(v, pitch);
		return v;
	}
	
	public static Vector rotateAboutX(Vector v, double a) {
		double y = cos(a)*v.getY() - sin(a)*v.getZ();
		double z = sin(a)*v.getY() + cos(a)*v.getZ();
		return v.setY(y).setZ(z);
	}
	
	public static Vector rotateAboutY(Vector v, double b) {
		double x = cos(b)*v.getX() + sin(b)*v.getZ();
		double z = -sin(b)*v.getX() + cos(b)*v.getZ();
		return v.setX(x).setZ(z);
	}
	
	public static Vector rotateAboutZ(Vector v, double c) {
		double x = cos(c)*v.getX() - sin(c)*v.getY();
		double y = sin(c)*v.getX() + cos(c)*v.getY();
		return v.setX(x).setY(y);
	}
	
}

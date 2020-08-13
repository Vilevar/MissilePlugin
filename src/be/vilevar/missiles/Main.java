package be.vilevar.missiles;

import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.toRadians;

import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftLivingEntity;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import be.vilevar.missiles.mcelements.CustomElementManager;
import be.vilevar.missiles.mcelements.MissileCraftBlock;
import be.vilevar.missiles.mcelements.MissileLauncherBlock;
import be.vilevar.missiles.mcelements.MissileRadarBlock;
import be.vilevar.missiles.missile.RBalisticMissile;
import be.vilevar.missiles.utils.MegaExplosion;
import be.vilevar.missiles.utils.ParticleEffect;

public class Main extends JavaPlugin implements Listener {

	public static void display(ParticleEffect particle, Location loc) {
		particle.display(0, 0, 0, 0, 1, loc, loc.getWorld().getPlayers());
	}
	
	public static Main i;
	private int t;
	
//	private static final UUID LAURENT = UUID.fromString("d03d7128-f318-4c68-ad0a-cd0511fa7d6d");
//	private static final UUID VILEVAR = UUID.fromString("b821a472-2b9d-40f3-a5df-d28b50cae96f");
	
	@Override
	public void onEnable() {
		i = this;
		getServer().getPluginManager().registerEvents(this, this);
		getServer().getPluginManager().registerEvents(new CustomElementManager(), this);
		getCommand("missile").setExecutor(this);
	//	PluginCommand cmd = getCommand("offrir_gateau_a_laurent");
	//	cmd.setExecutor(this);
	//	cmd.setPermissionMessage(SpigotConfig.unknownCommandMessage);
		
		MegaExplosion.registerMegaExplosion();
		CustomElementManager.createMissileTrackerBlockScheduler();
	}
	
	@EventHandler
	public void onClick(PlayerInteractEvent e) {
		if(e.getItem()!=null && e.getItem().getType()==CustomElementManager.FUEL) {
			e.setCancelled(true);
		}
		if(e.getItem()!=null && e.getItem().getType()==Material.BOOK && e.getAction()==Action.RIGHT_CLICK_BLOCK) {
			RMissileTest.createParticles(e.getPlayer().getEyeLocation(), e.getPlayer(), ParticleEffect.FLAME, 0.5);
			d: for(Entity ent : e.getPlayer().getNearbyEntities(100, 50, 100)) {
				if(ent instanceof Minecart) {
					ent.setInvulnerable(true);
					RMissileTest.createParticles(e.getPlayer().getEyeLocation(), ent, ParticleEffect.HEART, 0.1);
				//	RMissileTest.createParticles(e.getPlayer().getEyeLocation(), ent, ParticleEffect.HEART, 1);
				//	RMissileTest.createParticles(e.getPlayer().getEyeLocation(), ent, ParticleEffect.BARRIER, 0.5);
				//	RMissileTest.createParticles(e.getPlayer().getEyeLocation(), ent, ParticleEffect.FIREWORKS_SPARK, 50);
					break d;
				}
			}
		//	Explosions1.createExplosion(e.getClickedBlock().getLocation(), 10, true);
		}
		if(e.getAction()==Action.LEFT_CLICK_BLOCK && e.getClickedBlock()!=null && e.getClickedBlock().getType()==Material.GRASS_BLOCK
				&& e.getItem()!=null && e.getItem().getType()==CustomElementManager.LASER_POINTER) {
			Location t = CustomElementManager.getLaserPointerData(e.getItem()).getTarget().add(.5, 1, .5);
			e.setCancelled(true);
			Location l = e.getClickedBlock().getLocation().add(.5, 1, .5);
			new RBalisticMissile(ParticleEffect.FLAME, 0, 1, 25, 1000000, l).launch(t, e.getPlayer());
			/*
			BasicMissile bm = new BasicMissile(ParticleEffect.FLAME, 2, 1, 100, 1000, 20);
			Entity ent = null;
			for(Entity et : e.getPlayer().getWorld().getEntities()) {
				if(et instanceof Minecart) {
					ent = et;
				}
			}
			if(ent != null)
				bm.launch(bm.new Target(ent), e.getPlayer());
			else
				e.getPlayer().sendMessage("§cNo minecart");*/
		}
	}
	
	@EventHandler
	public void onPlace(BlockPlaceEvent e) {
		if(e.getBlock().getType()==CustomElementManager.MISSILE_RADAR) {
			MissileRadarBlock.radars.add(new MissileRadarBlock(e.getBlock().getLocation()));
		}
		if(e.getBlock().getType()==CustomElementManager.MISSILE_LAUNCHER) {
			MissileLauncherBlock.launchers.add(new MissileLauncherBlock(e.getBlock().getLocation()));
		}
		if(e.getBlock().getType()==CustomElementManager.MISSILE_CRAFT) {
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
						"§6Un lanceur de missiles de "+launcher.getOwner().getName()+" vient de se casser en ("+l.getBlockX()+"; "+l.getBlockZ()+") !");
			} else {
				Bukkit.broadcastMessage("§6Un lanceur de missiles vient de se casser en ("+l.getBlockX()+"; "+l.getBlockZ()+") !");
			}
			MissileLauncherBlock.checkDestroy(block.getLocation());
		}
		if(block.getType()==CustomElementManager.MISSILE_CRAFT) {
			MissileCraftBlock.checkDestroy(block.getLocation());
		}
	}
	
	@EventHandler
	public void onEntitySpawn(EntitySpawnEvent e) {
		if(e.getEntityType()==EntityType.SLIME || e.getEntityType() == EntityType.PHANTOM)
			e.setCancelled(true);
	}
	
	@EventHandler
	public void onProjectileHit(ProjectileHitEvent e) {
	/*	if(e.getEntity() instanceof Arrow && e.getHitEntity() == null) {
			Arrow arrow = (Arrow) e.getEntity();
			Vector direction = arrow.getVelocity().multiply(5);
			double energy = .5 * Math.pow(direction.length(), 2);
			direction.normalize();
			Location hit = e.getEntity().getLocation().subtract(direction);
			Location center = hit.clone();
			while(energy > 0) {
				center.add(direction);
				int radius = (int) Math.ceil(Math.sqrt(energy)/2);
				for(int x = -radius; x <= radius; x++) {
					for(int y = -radius; y <= radius; y++) {
						Vector dPos = rotate(new Vector(x, y, 1), hit);
						if(Math.ceil(dPos.length()) <= radius) {
							center.add(dPos);
							Material blockType = center.getBlock().getType();
							if(blockType != Material.AIR) {
								float resistance = blockType.getBlastResistance();
								if(resistance < (energy + 1)) {
									Vector dHit = hit.toVector().subtract(center.toVector()).normalize().multiply(energy / 2);
									FallingBlock falling = center.getWorld().spawnFallingBlock(center, center.getBlock().getBlockData());
									falling.setVelocity(dHit);
									center.getBlock().setType(Material.AIR);
								}
								energy -= (resistance / 4d) + (Math.random() * resistance / 2d);
							} else {
								energy -= Math.random()*0.01;
							}
							center.subtract(dPos);
						}
					}
				}
			}
			arrow.remove();
		}*/
		if(e.getEntity() instanceof Arrow && e.getHitEntity() != null && e.getHitEntity() instanceof LivingEntity &&
				e.getEntity().getShooter() instanceof Player) {
			CraftLivingEntity target = (CraftLivingEntity) e.getHitEntity();
			Arrow arw = (Arrow) e.getEntity();
			System.out.println("hit : "+e.getEntity().getLocation());
			if(arw.getLocation().getY() > target.getHandle().getHeadY()) {
				((Player) e.getEntity().getShooter()).sendMessage("Headshot");
				arw.setDamage(arw.getDamage() + 4);
			}
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
						new ItemStack(CustomElementManager.MISSILE_CRAFT), new ItemStack(CustomElementManager.LASER_POINTER), h,
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
					p.getInventory().addItem(new ItemStack(CustomElementManager.BALISTIC_MISSILE, nMissiles),
							new ItemStack(Material.BLAZE_POWDER, nBlazePowder), new ItemStack(Material.TNT, nTnt),
							new ItemStack(Material.COMPASS, nCompass), new ItemStack(CustomElementManager.FUEL, nFuel));
				}
			}, 0, 1200);
	/*	} else if(command.getName().equals("offrir_gateau_a_laurent") && sender instanceof Player) {
			Player p = (Player) sender;
			if(p.getUniqueId().equals(LAURENT)) {
				p.sendMessage(SpigotConfig.unknownCommandMessage);
				return false;
			}
			Player laurent = getServer().getPlayer(LAURENT);
			if(laurent == null || !laurent.isOnline()) {
				p.sendMessage("§cLaurent n'est pas présent.");
				return true;
			} else {
				p.getInventory().addItem(new ItemStack(CustomElementManager.FUEL, 64));
				laurent.getInventory().addItem(new ItemStack(Material.CAKE));
				laurent.sendMessage("§6"+p.getDisplayName()+"§a t'a offert un gâteau pour ton anniversaire !");
				p.sendMessage("§6Tu as offert ton gâteau à §aLaurent§6, c'est que tu es prêt à §cfaire feu§6.");
				Player vilevar = getServer().getPlayer(VILEVAR);
				if(vilevar != null && vilevar.isOnline()) {
					vilevar.sendMessage("§a"+p.getDisplayName()+"§6 est prêt à §cfaire feu§6.");
				}
				return true;
			}
		} else if(command.getName().equals("missile_world")) {
			String name = "missile_world";
			World world = getServer().getWorld(name);
			if(world == null && !(sender instanceof Player))
				MissileWorldGenerator.createWorld((CraftServer) getServer(), name);
			else if(world != null && sender instanceof Player)
				((Player) sender).teleport(world.getSpawnLocation());
				
			return true;*/
		}
		return true;
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

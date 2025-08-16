package be.vilevar.missiles.mcelements.weapons;

import static be.vilevar.missiles.mcelements.CustomElementManager.BOMB;
import static be.vilevar.missiles.mcelements.CustomElementManager.CLAYMORE;
import static be.vilevar.missiles.mcelements.CustomElementManager.MACHINE_GUN;
import static be.vilevar.missiles.mcelements.CustomElementManager.MINE;
import static be.vilevar.missiles.mcelements.CustomElementManager.PISTOL;
import static be.vilevar.missiles.mcelements.CustomElementManager.PLIERS;
import static be.vilevar.missiles.mcelements.CustomElementManager.SHOTGUN;
import static be.vilevar.missiles.mcelements.CustomElementManager.SMOKE_BOMB;
import static be.vilevar.missiles.mcelements.CustomElementManager.SNIPER;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
import java.util.UUID;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.util.Vector;

import be.vilevar.missiles.Main;
import be.vilevar.missiles.missile.ballistic.explosives.TraditionalExplosive;

public class WeaponListener implements Listener {

	private Main main = Main.i;
	private HashMap<UUID, Long> recover = new HashMap<>();
	private ArrayList<UUID> aiming = new ArrayList<>();
	private HashMap<UUID, Integer> discharge = new HashMap<>();
	
	private ArrayList<UUID> headshotArrows = new ArrayList<>();
	
	private ArrayList<MinePredicate> mines = new ArrayList<>();
	private Random random = new Random();
	
	
	public WeaponListener() {
		main.getServer().getScheduler().runTaskTimer(main, () -> {
			Iterator<MinePredicate> mines = this.mines.iterator();
			while(mines.hasNext()) {
				MinePredicate mine = mines.next();
				Location loc = mine.getLocation();
				Player miner = mine.getMiner();
				if(!MINE.isParentOf(loc.getBlock()) && !CLAYMORE.isParentOf(loc.getBlock())) {
					mines.remove();
				} else {
					if(!loc.getWorld().getNearbyEntities(loc, 1, 1, 1, mine).isEmpty()) {
						mines.remove();
						loc.getBlock().setType(Material.AIR);
						if(mine.isInstant()) {
							new TraditionalExplosive(main, 5.0f, false).explode(mine.getLocation(), mine.getMiner());
						} else {
							TNTPrimed tnt = (TNTPrimed) loc.getWorld().spawnEntity(loc, EntityType.PRIMED_TNT);
							tnt.setFuseTicks(5);
							tnt.setYield(5.0f);
							tnt.setIsIncendiary(false);
							tnt.setVelocity(new Vector(0, 0.4, 0));
							tnt.setSource(miner);
						}
					}
				}
			}
		}, 20, 1);
	}
	
	
	
	@EventHandler
	public void onPlaceMine(BlockPlaceEvent e) {
		if(MINE.isParentOf(e.getBlock()) || CLAYMORE.isParentOf(e.getBlock())) {
			this.mines.add(new MinePredicate(e.getBlock().getLocation(), e.getPlayer(), CLAYMORE.isParentOf(e.getBlock())));
		}
	}
	
	
	@EventHandler
	public void onActivateMine(PlayerInteractEvent e) {
		if((e.getAction() == Action.PHYSICAL && MINE.isParentOf(e.getClickedBlock())) || 
				(e.getAction() == Action.RIGHT_CLICK_BLOCK && CLAYMORE.isParentOf(e.getClickedBlock()))) {
			Iterator<MinePredicate> mines = this.mines.iterator();
			while(mines.hasNext()) {
				MinePredicate mine = mines.next();
				if(mine.getLocation().equals(e.getClickedBlock().getLocation())) {
					if(mine.test(e.getPlayer())) {
						new TraditionalExplosive(main, 5.0f, false).explode(mine.getLocation(), mine.getMiner());
						mines.remove();
					}
					return;
				}
			}
		}
		if(e.getAction() == Action.RIGHT_CLICK_BLOCK && PLIERS.isParentOf(e.getItem())) {
			e.setCancelled(true);
			Block block = e.getClickedBlock();
			if(MINE.isParentOf(block) || CLAYMORE.isParentOf(block)) {
				Iterator<MinePredicate> mines = this.mines.iterator();
				while(mines.hasNext()) {
					// Look for the mine
					MinePredicate mine = mines.next();
					if(mine.getLocation().equals(e.getClickedBlock().getLocation()) && mine.test(e.getPlayer())) { // Only for enemies
						
						ItemStack is = e.getItem();
						Damageable im = (Damageable) is.getItemMeta();
						int newDamage = im.getDamage() + 35;
						// Explode because of lack of luck
						if(this.random.nextDouble() < 0.33) {
							new TraditionalExplosive(main, 3.0f, false).explode(mine.getLocation(), mine.getMiner());
						} else {
							
							mine.getLocation().getWorld().dropItem(mine.getLocation(), BOMB.create());
							
							if(newDamage >= is.getType().getMaxDurability()) {
								e.getPlayer().getInventory().setItem(e.getHand(), null);
							} else {
								im.setDamage(newDamage);
								is.setItemMeta(im);
							}
						}
						
						block.setType(Material.AIR);
						mines.remove();
						return;
					}
				}
				
			}
		}
	}
	
	@EventHandler
	public void onBreakMine(BlockBreakEvent e) {
		Block block = e.getBlock();
		if(MINE.isParentOf(block) || CLAYMORE.isParentOf(block)) {
			Iterator<MinePredicate> mines = this.mines.iterator();
			while(mines.hasNext()) {
				MinePredicate mine = mines.next();
				if(mine.getLocation().equals(block.getLocation())) {
					e.setCancelled(true);
					if(mine.test(e.getPlayer())) {
						new TraditionalExplosive(main, 5.0f, false).explode(mine.getLocation(), mine.getMiner());
						mines.remove();
					}
					e.getBlock().setType(Material.AIR);
					return;
				}
			}
		}
	}
	
	
	@EventHandler
	public void onShoot(PlayerInteractEvent e) {
		if(main.hasGame() && !main.getGame().isStarted()) {
			return;
		}
		Player p = e.getPlayer();
		ItemStack is = e.getItem();
		Weapon w = this.getWeapon(is);
		if(w != null && e.getHand() == EquipmentSlot.HAND) {
			e.setCancelled(true);
			if(e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
				// Test recover
				if(recover.containsKey(p.getUniqueId())) {
					long time = recover.get(p.getUniqueId());
					if(time <= System.currentTimeMillis()) {
						recover.remove(p.getUniqueId());
					} else {
						return;
					}
				}
				// Test ammunitions
				ItemMeta im = is.getItemMeta();
				long recover = w.getRecover();
				if(p.getGameMode() != GameMode.CREATIVE && im instanceof Damageable) {
					Damageable dam = (Damageable) im;
					int newDamage = dam.getDamage() + w.getPrice();
					// Not enough ammo's
					if(newDamage > is.getType().getMaxDurability()) {
						// Charge
						ItemStack newStock = p.getInventory().getItemInOffHand();
						if(newStock != null && w.getAmmunition().isParentOf(newStock)) {
							newStock.setAmount(newStock.getAmount() - 1);
							dam.setDamage(0);
							is.setItemMeta(im);
							this.recover.put(p.getUniqueId(), System.currentTimeMillis() + w.getRechargeTime());
						} else {
							p.sendMessage("§cRechargez§6 votre arme via votre §amain secondaire§6.");
						}
						return;
					} else {
						// Take ammo
						dam.setDamage(newDamage);
						is.setItemMeta(im);
						// Check if need charge directly
						if(newDamage + w.getPrice() > is.getType().getMaxDurability()) {
							// Check if can charge directly
							ItemStack newStock = p.getInventory().getItemInOffHand();
							if(newStock != null && w.getAmmunition().isParentOf(newStock)) {
								newStock.setAmount(newStock.getAmount() - 1);
								dam.setDamage(0);
								is.setItemMeta(im);
								recover = w.getRechargeTime();
							}
						}
					}
				}
				boolean isAiming = this.aiming.contains(p.getUniqueId());
				// Test Headshot
				boolean isHeadshot = false;
				for(Player target : Main.i.getServer().getOnlinePlayers()) {
					if(!p.getUniqueId().equals(target.getUniqueId())) {
						Vector dist = target.getEyeLocation().subtract(p.getEyeLocation()).toVector();
						Vector dir = p.getLocation().getDirection().normalize();
						double y = this.shootY(dist.clone().setY(0).length(), dir.clone().setY(0).length(), dir.getY());
						Vector hit = dir.clone().setY(0).normalize().multiply(dist.clone().setY(0).length()).setY(y);
						System.out.println(hit.distance(dist));
						if(hit.distance(dist) <= 0.25 && (isAiming ? w.getAimingSpread() : w.getSpread())*dist.length() <= 5) {
							isHeadshot = true;
							break;
						}
						
					}
				}
				System.out.println(isHeadshot);
				// Shoot
				Arrow[] arrows = w.shoot(p, isAiming);
				if(isHeadshot) {
					for(Arrow arw : arrows) {
						this.headshotArrows.add(arw.getUniqueId());
					}
				}
				this.recover.put(p.getUniqueId(), System.currentTimeMillis() + recover);
			// Aim
			} else {
				if(p.hasPotionEffect(PotionEffectType.SLOW)) {
					p.removePotionEffect(PotionEffectType.SLOW);
					if(aiming.contains(p.getUniqueId()))
						aiming.remove(p.getUniqueId());
				} else {
					p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 999999, w.getAiming(), true, false, false));
					aiming.add(p.getUniqueId());
				}
			}
		}
	}
	
	@EventHandler
	public void onSwitchItem(PlayerItemHeldEvent e) {
		Player p = e.getPlayer();
		if(e.getNewSlot() != e.getPreviousSlot() && aiming.contains(p.getUniqueId()) && p.hasPotionEffect(PotionEffectType.SLOW)) {
			p.removePotionEffect(PotionEffectType.SLOW);
			aiming.remove(p.getUniqueId());
		}
	}
	
	@EventHandler
	public void onDamage(PlayerItemDamageEvent e) {
		if(this.getWeapon(e.getItem()) != null)
			e.setCancelled(true);
	}
	
	@EventHandler
	public void onLaunch(ProjectileLaunchEvent e) {
		ProjectileSource shooter = e.getEntity().getShooter();
		if(shooter != null && shooter instanceof Player) {
			Player p = (Player) shooter;
			if(e.getEntityType() == EntityType.ARROW) {
				e.setCancelled(true);
			} else if(e.getEntityType() == EntityType.SNOWBALL) {
				ItemStack main = p.getInventory().getItemInMainHand();
				if(BOMB.isParentOf(main) || (main.getType() != Material.SNOWBALL && BOMB.isParentOf(p.getInventory().getItemInOffHand()))) {
					e.getEntity().setMetadata("bomb-type", new FixedMetadataValue(this.main, 0));
				} else if(SMOKE_BOMB.isParentOf(main)
						|| (main.getType() != Material.SNOWBALL && SMOKE_BOMB.isParentOf(p.getInventory().getItemInOffHand()))) {
					e.getEntity().setMetadata("bomb-type", new FixedMetadataValue(this.main, 1));
				}
			}
		}
	}
	
	@EventHandler
	public void onHit(ProjectileHitEvent e) {
		Projectile proj = e.getEntity();
		ProjectileSource shooter = proj.getShooter();
		if(shooter != null && shooter instanceof Player) {
			Player p = (Player) shooter;
			if(proj.getType() == EntityType.SNOWBALL && proj.hasMetadata("bomb-type")) {
				int bombType = proj.getMetadata("bomb-type").get(0).asInt();
				if(bombType == 0) {
					TNTPrimed tnt = (TNTPrimed) proj.getWorld().spawnEntity(proj.getLocation(), EntityType.PRIMED_TNT);
					tnt.setFuseTicks(30);
					tnt.setYield(3.0f);
					tnt.setIsIncendiary(false);
					tnt.setSource(p);
				} else if(bombType == 1) {
					final int task = this.createSmoke(proj.getLocation(), 5., 100);
					main.getServer().getScheduler().runTaskLater(main, () -> main.getServer().getScheduler().cancelTask(task), 20*10);
				}
			} else if(proj instanceof Arrow) {
				if(e.getHitBlock() != null && e.getHitBlock().getType().toString().contains("GLASS")) {
					e.getHitBlock().setType(Material.AIR);
				} else if(this.headshotArrows.contains(proj.getUniqueId())) {
					if(e.getHitEntity() != null && e.getHitEntity().getType() == EntityType.PLAYER) {
						Arrow arrow = (Arrow) proj;
						arrow.setDamage(arrow.getDamage() * 1.5);
						p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
					}
					this.headshotArrows.remove(proj.getUniqueId());
//				} else if(e.getHitEntity() != null && e.getHitEntity().getType() == EntityType.PLAYER) {
//					Player hit = (Player) e.getHitEntity();
//					if(proj.getLocation().distance(hit.getEyeLocation()) <= 0.25) {
//						Arrow arrow = (Arrow) proj;
//						arrow.setDamage(arrow.getDamage() * 1.5);
//						p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
//					}
				}
			}
		}
	}
	
	
	@EventHandler
	public void onDischarge(PlayerToggleSneakEvent e) {
		Player p = e.getPlayer();
		ItemStack is = p.getInventory().getItemInMainHand();
		Weapon weapon = this.getWeapon(is);
		if(p.isSneaking() && weapon != null) {
			UUID id = p.getUniqueId();
			BukkitScheduler scheduler = this.main.getServer().getScheduler();
			int nSneaks = this.discharge.getOrDefault(id, 0);
			
			if(nSneaks == 0) {
				this.discharge.put(id, 1);
				scheduler.runTaskLater(this.main, () -> {
					if(this.discharge.getOrDefault(id, 0) == 1) {
						this.discharge.remove(id);
					}
				}, 10);
			} else if(nSneaks == 1) {
				this.discharge.put(id, 2);
				scheduler.runTaskLater(this.main, () -> {
					if(this.discharge.getOrDefault(id, 0) == 2) {
						this.discharge.remove(id);
					}
				}, 10);
			} else if(nSneaks == 2) {
				this.discharge.remove(id);
				// Discharge
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
		}
	}
	
	
	
	public Weapon getWeapon(ItemStack is) {
		if(is == null) {
			return null;
		}
		if(SNIPER.getItem().isParentOf(is))
			return SNIPER;
		else if(PISTOL.getItem().isParentOf(is))
			return PISTOL;
		else if(MACHINE_GUN.getItem().isParentOf(is))
			return MACHINE_GUN;
		else if(SHOTGUN.getItem().isParentOf(is))
			return SHOTGUN;
		else
			return null;
	}
	
	private int createSmoke(Location loc, double radius, int n) {
		return main.getServer().getScheduler().scheduleSyncRepeatingTask(main, () -> {
			for(int i = 0; i < n; i++) {
				double r = radius * Math.sqrt(Math.random());
				double theta = 2 * Math.PI * Math.random();
				double phi = 2 * Math.PI * Math.random();
				Vector add = new Vector(r*Math.sin(theta)*Math.cos(phi), r*Math.cos(theta), r*Math.sin(theta)*Math.sin(phi));
				Main.display(Particle.SMOKE_LARGE, loc.clone().add(add));
			}
			loc.getWorld().spawnParticle(Particle.SMOKE_LARGE, loc, 1, radius, radius, radius, 0, null, true);
			for(Entity ent : loc.getWorld().getNearbyEntities(loc, radius, radius, radius, e -> e instanceof LivingEntity)) {
				((LivingEntity) ent).addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 100, 255));
			}
		}, 1, 1);
	}
	
	
	private double g = -0.05;
	private double k = 0.99;
	private double v0 = 20;
	
	private double shootY(double x, double vxMultiplier, double vyMultiplier) {
		double vx = v0 * vxMultiplier;
		double vy = v0 * vyMultiplier;
		return x / vx * (vy + g/(k-1)) - g/(k-1)*Math.log(1 + (k-1)*x/vx)/Math.log(k);
	}
}

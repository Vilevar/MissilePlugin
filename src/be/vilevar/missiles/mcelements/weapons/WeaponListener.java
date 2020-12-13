package be.vilevar.missiles.mcelements.weapons;

import static be.vilevar.missiles.mcelements.CustomElementManager.BOMB;
import static be.vilevar.missiles.mcelements.CustomElementManager.MACHINE_GUN;
import static be.vilevar.missiles.mcelements.CustomElementManager.PISTOL;
import static be.vilevar.missiles.mcelements.CustomElementManager.SHOTGUN;
import static be.vilevar.missiles.mcelements.CustomElementManager.SMOKE_BOMB;
import static be.vilevar.missiles.mcelements.CustomElementManager.SNIPER;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.Vector;

import be.vilevar.missiles.Main;
import be.vilevar.missiles.utils.ParticleEffect;

public class WeaponListener implements Listener {

	private HashMap<UUID, Long> recover = new HashMap<>();
	private ArrayList<UUID> aiming = new ArrayList<>();
	
	@EventHandler
	public void onShoot(PlayerInteractEvent e) {
		Player p = e.getPlayer();
		ItemStack is = e.getItem();
		Weapon w = this.getWeapon(is);
		if(w != null) {
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
				// Shoot
				w.shoot(p, this.aiming.contains(p.getUniqueId()));
				this.recover.put(p.getUniqueId(), System.currentTimeMillis() + recover);
			} else {
				// Aim
				if(!p.isSneaking()) {
					if(p.hasPotionEffect(PotionEffectType.SLOW)) {
						p.removePotionEffect(PotionEffectType.SLOW);
						if(aiming.contains(p.getUniqueId()))
							aiming.remove(p.getUniqueId());
					} else {
						p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 999999, w.getAiming(), true, false, false));
						aiming.add(p.getUniqueId());
					}
				} else if(p.getGameMode() != GameMode.CREATIVE) {
					// Discharge
					ItemMeta im = is.getItemMeta();
					if(im instanceof Damageable) {
						((Damageable) im).setDamage(is.getType().getMaxDurability());
						is.setItemMeta(im);
					}
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
	public void onLaunch(ProjectileLaunchEvent e) {
		ProjectileSource shooter = e.getEntity().getShooter();
		if(shooter != null && shooter instanceof Player) {
			Player p = (Player) shooter;
			if(e.getEntityType() == EntityType.ARROW) {
				e.setCancelled(true);
			} else if(e.getEntityType() == EntityType.SNOWBALL) {
				ItemStack main = p.getInventory().getItemInMainHand();
				if(BOMB.isParentOf(main)  || (main.getType() != Material.SNOWBALL && BOMB.isParentOf(p.getInventory().getItemInOffHand()))) {
					e.getEntity().setMetadata("bomb-type", new FixedMetadataValue(Main.i, 0));
				} else if(SMOKE_BOMB.isParentOf(main)
						|| (main.getType() != Material.SNOWBALL && SMOKE_BOMB.isParentOf(p.getInventory().getItemInOffHand()))) {
					e.getEntity().setMetadata("bomb-type", new FixedMetadataValue(Main.i, 1));
				}
			}
		}
	}
	
	@EventHandler
	public void onHit(ProjectileHitEvent e) {
		Projectile ball = e.getEntity();
		ProjectileSource shooter = ball.getShooter();
		if(shooter != null && shooter instanceof Player && ball.getType() == EntityType.SNOWBALL && ball.hasMetadata("bomb-type")) {
			int bombType = ball.getMetadata("bomb-type").get(0).asInt();
			if(bombType == 0) {
				TNTPrimed tnt = (TNTPrimed) ball.getWorld().spawnEntity(ball.getLocation(), EntityType.PRIMED_TNT);
				tnt.setFuseTicks(20);
				tnt.setYield(5.0f);
				tnt.setIsIncendiary(true);
				tnt.setSource((Player) shooter);
			} else if(bombType == 1) {
				final int task = this.createSmoke(ball.getLocation(), 5., 100);
				Bukkit.getScheduler().runTaskLater(Main.i, () -> Bukkit.getScheduler().cancelTask(task), 20*10);
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
		return Bukkit.getScheduler().scheduleSyncRepeatingTask(Main.i, () -> {
			for(int i = 0; i < n; i++) {
				double r = radius * Math.sqrt(Math.random());
				double theta = 2 * Math.PI * Math.random();
				double phi = 2 * Math.PI * Math.random();
				Vector add = new Vector(r*Math.sin(theta)*Math.cos(phi), r*Math.cos(theta), r*Math.sin(theta)*Math.sin(phi));
				Main.display(ParticleEffect.SMOKE_LARGE, loc.clone().add(add));
			}
			for(Entity ent : loc.getWorld().getNearbyEntities(loc, radius, radius, radius, (e) -> e instanceof LivingEntity)) {
				((LivingEntity) ent).addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 100, 255));
			}
		}, 1, 1);
	}
}

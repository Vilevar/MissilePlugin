package be.vilevar.missiles.mcelements;

import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import be.vilevar.missiles.Main;

public class EnchantmentListener implements Listener {

	private PotionEffect blindness = new PotionEffect(PotionEffectType.BLINDNESS, 60, 2, true, false);
	private PotionEffect miningFatigue = new PotionEffect(PotionEffectType.SLOW_DIGGING, 150, 2, true, false);
	private PotionEffect slowness = new PotionEffect(PotionEffectType.SLOW, 150, 2, true, false);
	
	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void onDamage(EntityDamageByEntityEvent e) {
		if(e.getDamager() instanceof Projectile && e.getEntity() instanceof Player && e.getDamage() > 10) {
			Player p = (Player) e.getEntity();
			double reduction = 0;
			if(this.isBulletProof(p.getInventory().getHelmet())) {
				reduction += 0.1;
			}
			if(this.isBulletProof(p.getInventory().getChestplate())) {
				reduction += 0.13;
			}
			if(this.isBulletProof(p.getInventory().getLeggings())) {
				reduction += 0.08;
			}
			if(p.getInventory().getBoots() != null) {
				reduction += 0.04;
			}
			if(e.getDamage() > 20) {
				p.addPotionEffect(blindness);
				p.addPotionEffect(miningFatigue);
				p.addPotionEffect(slowness);
			}
			e.setDamage(Math.max((1 - reduction) * e.getDamage(), 0));
		}
	}
	
	private boolean isBulletProof(ItemStack is) {
		if(is != null && is.hasItemMeta()) {
			ItemMeta im = is.getItemMeta();
			return im.hasLore() && im.getLore().contains("§5Bulletproof");
		}
		return false;
	}
	
	@EventHandler
	public void onChat(AsyncPlayerChatEvent e) {
		if(e.getMessage().startsWith("%bow ")) {
			String[] args = e.getMessage().split(" ");
			Player p = e.getPlayer();
			if(args.length >= 2) {
				for(Player target : Main.i.getServer().getOnlinePlayers()) {
					if(target.getName().equals(args[1])) {
						Vector dist = target.getEyeLocation().subtract(p.getEyeLocation()).toVector();
						Vector dir = p.getLocation().getDirection().normalize();
						double y = this.shootY(dist.clone().setY(0).length(), dir.clone().setY(0).length(), dir.getY());
						Vector hit = dir.clone().setY(0).normalize().multiply(dist.clone().setY(0).length()).setY(y);
						p.sendMessage(""+hit.distance(dist));
					}
				}
			}
		}
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

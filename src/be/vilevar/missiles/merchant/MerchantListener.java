package be.vilevar.missiles.merchant;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;

public class MerchantListener implements Listener {
	
	@EventHandler
	public void onOpenMerchant(PlayerInteractEntityEvent e) {
		if(e.getRightClicked().getType() == EntityType.VILLAGER) {
			WeaponsMerchant merchant = WeaponsMerchant.getMerchant((Villager) e.getRightClicked());
			if(merchant != null) {
				Player p = e.getPlayer();
				
				merchant.testLocation();
				merchant.open(p);
				
				e.setCancelled(true);
			}
		}
	}
	
	@EventHandler
	public void onDamage(EntityDamageByEntityEvent e) {
		if(e.getEntity().getType() == EntityType.VILLAGER) {
			WeaponsMerchant merchant = WeaponsMerchant.getMerchant((Villager) e.getEntity());
			merchant.testLocation();
			if(merchant != null) {
				Entity damager = e.getDamager();
				if(damager.getType() == EntityType.PLAYER && !merchant.canBeHurtBy((Player) damager)) {
					e.setCancelled(true);
				} else if(damager instanceof Projectile) {
					Projectile proj = (Projectile) damager;
					if(proj.getShooter() != null && proj.getShooter() instanceof Player 
							&& !merchant.canBeHurtBy((Player) proj.getShooter())) {
						e.setCancelled(true);
					}
				}
			}
		}
	}

}

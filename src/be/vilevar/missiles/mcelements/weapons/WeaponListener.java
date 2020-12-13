package be.vilevar.missiles.mcelements.weapons;

import static be.vilevar.missiles.mcelements.CustomElementManager.MACHINE_GUN;
import static be.vilevar.missiles.mcelements.CustomElementManager.PISTOL;
import static be.vilevar.missiles.mcelements.CustomElementManager.SHOTGUN;
import static be.vilevar.missiles.mcelements.CustomElementManager.SNIPER;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class WeaponListener implements Listener {

	@EventHandler
	public void onShoot(PlayerInteractEvent e) {
		Player p = e.getPlayer();
		ItemStack is = e.getItem();
		Weapon w = this.getWeapon(is);
		if(w != null) {
			e.setCancelled(true);
			if(e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
				// Shoot
				ItemMeta im = is.getItemMeta();
				if(p.getGameMode() == GameMode.CREATIVE || !(im instanceof Damageable)) {
					w.shoot(p);
				} else {
					Damageable dam = (Damageable) im;
					int newDamage = dam.getDamage() + w.getPrice();
					if(newDamage <= is.getType().getMaxDurability()) {
						dam.setDamage(newDamage);
						is.setItemMeta(im);
						w.shoot(p);
					} else {
						e.setUseItemInHand(Result.DENY);
					}
				}
			} else {
				// Aim
				if(p.hasPotionEffect(PotionEffectType.SLOW))
					p.removePotionEffect(PotionEffectType.SLOW);
				else
					p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 999999, w.getAiming(), true, false, false));
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
}

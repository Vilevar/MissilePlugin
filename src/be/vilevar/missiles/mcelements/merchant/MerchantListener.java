package be.vilevar.missiles.mcelements.merchant;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantInventory;

import be.vilevar.missiles.Main;
import be.vilevar.missiles.mcelements.merchant.WeaponsMerchant.WeaponsMerchantStage;

public class MerchantListener implements Listener {

	private HashMap<UUID, MerchantView> openMerchant = new HashMap<>();
	
	@EventHandler
	public void onOpenMerchant(PlayerInteractEntityEvent e) {
		if(e.getRightClicked().getType() == EntityType.VILLAGER) {
			WeaponsMerchant merchant = WeaponsMerchant.getMerchant((Villager) e.getRightClicked());
			if(merchant != null) {
				Player p = e.getPlayer();
				
				merchant.testLocation();
				if(merchant.open(p)) {
					this.openMerchant.put(p.getUniqueId(), new MerchantView(merchant, merchant.getOpenStage()));
				}
				
				e.setCancelled(true);
			}
		}
	}
	
	@EventHandler
	public void onInventoryClose(InventoryCloseEvent e) {
		UUID id = e.getPlayer().getUniqueId();
		if(openMerchant.containsKey(id)) {
			MerchantView view = openMerchant.get(id);
			WeaponsMerchant merchant = view.getMerchant();
			if(merchant.getOpenStage() == view.getStage()) {
				if(merchant.getOpenStage() == WeaponsMerchantStage.HOME) {
					merchant.close();
					openMerchant.remove(id);
				} else {
					merchant.close();
					Main.i.getServer().getScheduler().runTaskLater(Main.i, () -> {
						if(merchant.open((Player) e.getPlayer())) {
							view.setStage(merchant.getOpenStage());
						} else {
							openMerchant.remove(id);
						}
					}, 1);
				}
			}
		}
	}
	
	@EventHandler
	public void onClick(InventoryClickEvent e) {
		HumanEntity p = e.getWhoClicked();
		if(openMerchant.containsKey(p.getUniqueId())) {
			
			MerchantView view = openMerchant.get(p.getUniqueId());
			WeaponsMerchant merchant = view.getMerchant();
			
			if(view.getStage() == WeaponsMerchantStage.HOME) {
				
				switch(e.getRawSlot()) {
				case 2:
					merchant.openUtilitaries(p);
					view.setStage(merchant.getOpenStage());
					break;
				case 3:
					merchant.openResearch(p);
					view.setStage(merchant.getOpenStage());
					break;
				case 4:
					merchant.openDevelopment(p);
					view.setStage(merchant.getOpenStage());
					break;
				case 6:
					break; // Attack item
				case 7:
					break; // Health item
				case 8:
					int availableMoney = Math.min(merchant.getMoney(), 64);
					ItemStack is = new ItemStack(Material.EMERALD, availableMoney);
					p.getInventory().addItem(is);
					merchant.addMoney(-availableMoney);
					e.getClickedInventory().setItem(8, merchant.updateMoneyItem());
					break;
				}
				
				e.setCancelled(true);
			} else if(view.getStage() == WeaponsMerchantStage.RESEARCH && e.getRawSlot() == 2 && e.getCurrentItem() != null &&
					e.getCurrentItem().getType() == Material.PAPER && e.getClickedInventory().getType() == InventoryType.MERCHANT) {
				MerchantInventory inv = (MerchantInventory) e.getClickedInventory();
				merchant.research(merchant.getResearchRecipeAdvancement(inv.getSelectedRecipeIndex()));
			}
		}
	}
	
	@EventHandler
	public void onDamage(EntityDamageByEntityEvent e) {
		if(e.getEntity().getType() == EntityType.VILLAGER) {
			WeaponsMerchant merchant = WeaponsMerchant.getMerchant((Villager) e.getEntity());
			if(merchant != null) {
				Entity damager = e.getDamager();
				if(damager.getType() == EntityType.PLAYER && !merchant.canBeHurtBy((Player) damager)) {
					e.setCancelled(true);
				} else if(damager instanceof Projectile) {
					Projectile proj = (Projectile) damager;
					if(proj.getShooter() != null && proj.getShooter() instanceof Player && !merchant.canBeHurtBy((Player) proj.getShooter())) {
						e.setCancelled(true);
					}
				}
				merchant.updateHealthItem();
				merchant.updateAttackItem();
			}
		}
	}
	
	
}

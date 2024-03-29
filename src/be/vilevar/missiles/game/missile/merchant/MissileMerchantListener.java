package be.vilevar.missiles.game.missile.merchant;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantInventory;

import be.vilevar.missiles.Main;
import be.vilevar.missiles.game.missile.merchant.MissileMerchant.WeaponsMerchantStage;
import be.vilevar.missiles.merchant.WeaponsMerchant;

public class MissileMerchantListener implements Listener {

	private HashMap<UUID, MissileMerchantView> openMerchant = new HashMap<>();
	private Main main = Main.i;

	public void registerOpenMerchant(Player p, MissileMerchant merchant) {
		this.openMerchant.put(p.getUniqueId(), new MissileMerchantView(merchant, merchant.getOpenStage()));
	}
	
	@EventHandler
	public void onInventoryClose(InventoryCloseEvent e) {
		UUID id = e.getPlayer().getUniqueId();
		if(openMerchant.containsKey(id)) {
			MissileMerchantView view = openMerchant.get(id);
			MissileMerchant merchant = view.getMerchant();
			if(merchant.getOpenStage() == view.getStage()) {
				if(merchant.getOpenStage() == WeaponsMerchantStage.HOME) {
					merchant.close();
					openMerchant.remove(id);
				} else {
					merchant.close();
					this.main.getServer().getScheduler().runTaskLater(this.main, () -> {
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
			
			MissileMerchantView view = openMerchant.get(p.getUniqueId());
			
			MissileMerchant merchant = view.getMerchant().getAsMissileMerchant();
			if(merchant == null)
				return;
			
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
					int availableMoney;
					ItemStack is;
					if(e.getAction() == InventoryAction.PICKUP_HALF) {
						availableMoney = Math.min(merchant.getMoney() / 9, 64);
						is = new ItemStack(Material.EMERALD_BLOCK, availableMoney);
						availableMoney *= 9;
					} else {
						availableMoney = Math.min(merchant.getMoney(), 64);
						is = new ItemStack(Material.EMERALD, availableMoney);
					}
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
				MissileMerchant mm = merchant.getAsMissileMerchant();
				if(mm != null) {
					mm.updateHealthItem();
					mm.updateAttackItem();
				}
			}
		}
	}
	
	
}

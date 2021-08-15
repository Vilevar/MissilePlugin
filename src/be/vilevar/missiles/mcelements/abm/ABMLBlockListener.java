package be.vilevar.missiles.mcelements.abm;

import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.craftbukkit.libs.org.apache.commons.lang3.tuple.Pair;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import be.vilevar.missiles.Main;
import be.vilevar.missiles.mcelements.CustomElementManager;

public class ABMLBlockListener implements Listener {

	private HashMap<UUID, Pair<Inventory, ABMLauncher>> launcherInventories = new HashMap<>();
	private String launcherInventoryName = "§eABM Launcher";
	private ItemStack unusedSlot;

	public ABMLBlockListener(ItemStack unusedSlot) {
		this.unusedSlot = unusedSlot;
	}

	public HashMap<UUID, Pair<Inventory, ABMLauncher>> getLauncherInventories() {
		return launcherInventories;
	}

	public void openLauncherInventory(ABMLauncher launcher, Player p) {
		Inventory inv = Bukkit.createInventory(null, 18, this.launcherInventoryName);
		
		for(int i = 0; i < 18; i++) {
			inv.setItem(i, this.unusedSlot);
		}
		
		this.updateLauncherInventoryChannel(launcher, inv);
		this.updateLauncherInventoryMessageSend(launcher, inv);
		this.updateLauncherInventoryMessageInter(launcher, inv);
		this.updateLauncherInventoryMissiles(launcher, inv);
		
		this.launcherInventories.put(p.getUniqueId(), Pair.of(inv, launcher));
		p.openInventory(inv);
		launcher.setOpen(p);
	}

	@EventHandler
	public void onInventoryClick(InventoryClickEvent e) {
		if (e.getWhoClicked() instanceof Player) {
			Player p = (Player) e.getWhoClicked();

			if (this.launcherInventories.containsKey(p.getUniqueId())) {
				
				Pair<Inventory, ABMLauncher> pair = this.launcherInventories.get(p.getUniqueId());
				Inventory inv = pair.getLeft();
				ABMLauncher launcher = pair.getRight();
				ItemStack is = e.getCurrentItem();
				int slot = e.getSlot();
				
				e.setCancelled(true);
				if (is == null || launcher.getTimeOut() > 0)
					return;
				
				if(inv.equals(e.getClickedInventory())) {
					if(slot == 2) {
						launcher.setChannel(Main.clamp(0, 9, launcher.getChannel() + (e.getAction() == InventoryAction.PICKUP_ALL ? 1 : -1)));
						this.updateLauncherInventoryChannel(launcher, inv);
					} else if(slot == 3) {
						launcher.setMessageSend(!launcher.isMessageSend());
						this.updateLauncherInventoryMessageSend(launcher, inv);
					} else if(slot == 4) {
						launcher.setMessageInter(!launcher.isMessageInter());
						this.updateLauncherInventoryMessageInter(launcher, inv);
					} else if(slot % 9 == 6 || slot % 9 == 7) {
						int missileId = 4 * ((slot - 6) % 9) + 2 * ((slot - 6) / 9);
						launcher.setABM(missileId + is.getAmount() - 1, null);
						
						ItemStack give = is.clone();
						give.setAmount(1);
						p.getInventory().addItem(give);
						
						this.updateLauncherInventoryMissiles(launcher, inv);
					}
					return;
				} else if(CustomElementManager.ABM.isParentOf(is)) {
					for(int i = 0; i < 8; i++) {
						if(launcher.getABM(i) == null) {
							is.setAmount(is.getAmount() - 1);
							p.getInventory().setItem(slot, is);
							p.updateInventory();
							
							launcher.setABM(i, new ABM());
							this.updateLauncherInventoryMissiles(launcher, inv);
							return;
						}
					}
				}
			}
		}
	}

	private void updateLauncherInventoryChannel(ABMLauncher launcher, Inventory inv) {
		ItemStack is = new ItemStack(Material.ENDER_PEARL);
		ItemMeta im = is.getItemMeta();
		im.setDisplayName("§6Canal n°§c" + launcher.getChannel()+" §5("+launcher.getId()+")");
		im.setCustomModelData(launcher.getChannel() + 1);
		
		int offTime = launcher.getTimeOut();
		if(offTime != 0) {
			im.setLore(Arrays.asList("§cDéfense ABM §4neutralisée§c pendant §4"+offTime+"ms"));
		}
		
		is.setItemMeta(im);
		
		inv.setItem(2, is);
	}
	
	
	private void updateLauncherInventoryMessageSend(ABMLauncher launcher, Inventory inv) {
		ItemStack is = new ItemStack(launcher.isMessageSend() ? Material.LIME_WOOL : Material.RED_WOOL);
		ItemMeta im = is.getItemMeta();
		im.setDisplayName(launcher.isMessageSend() ? "§aEnvoie un message d'envoi" : "§cN'envoie pas de message d'envoi");
		is.setItemMeta(im);
		
		inv.setItem(3, is);
	}
	
	private void updateLauncherInventoryMessageInter(ABMLauncher launcher, Inventory inv) {
		ItemStack is = new ItemStack(launcher.isMessageInter() ? Material.LIME_WOOL : Material.RED_WOOL);
		ItemMeta im = is.getItemMeta();
		im.setDisplayName(launcher.isMessageInter() ? "§aEnvoie un message d'interception" : "§cN'envoie pas de message d'interception");
		is.setItemMeta(im);
		
		inv.setItem(4, is);
	}
	
	private void updateLauncherInventoryMissiles(ABMLauncher launcher, Inventory inv) {
		for(int i = 0; i < 4; i++) {
			int slot = 9 * (i % 2) + i / 2 + 6;
			
			int amount = 0;
			for(int j = 0; j < 2; j++) {
				if(launcher.getABM(2*i + j) != null) {
					amount++;
				}
			}
			
			if(amount == 0) {
				inv.setItem(slot, null);
			} else {
				inv.setItem(slot, CustomElementManager.ABM.create(amount));
			}
		}
	}
	
}

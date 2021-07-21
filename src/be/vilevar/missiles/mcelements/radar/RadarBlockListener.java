package be.vilevar.missiles.mcelements.radar;

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

public class RadarBlockListener implements Listener {

	
	private HashMap<UUID, Pair<Inventory, Radar>> radarInventories = new HashMap<>();
	private String radarInventoryName = "§eRadar";
	private ItemStack unusedSlot;

	public RadarBlockListener(ItemStack unusedSlot) {
		this.unusedSlot = unusedSlot;
	}

	public HashMap<UUID, Pair<Inventory, Radar>> getRadarInventories() {
		return radarInventories;
	}

	public void openRadarInventory(Radar radar, Player p) {
		Inventory inv = Bukkit.createInventory(null, 9, this.radarInventoryName);
		
		inv.setItem(0, this.unusedSlot);
		inv.setItem(1, this.unusedSlot);
		inv.setItem(3, this.unusedSlot);
		inv.setItem(7, this.unusedSlot);
		inv.setItem(8, this.unusedSlot);
		
		this.updateRadarInventoryChannel(radar, inv);
		this.updateRadarInventorySound(radar, inv);
		this.updateRadarInventoryMessage(radar, inv);
		this.updateRadarInventorySend(radar, inv);
		
		this.radarInventories.put(p.getUniqueId(), Pair.of(inv, radar));
		p.openInventory(inv);
		radar.setOpen(p);
	}

	@EventHandler
	public void onInventoryClick(InventoryClickEvent e) {
		if (e.getWhoClicked() instanceof Player) {
			Player p = (Player) e.getWhoClicked();

			if (this.radarInventories.containsKey(p.getUniqueId())) {
				
				Pair<Inventory, Radar> pair = this.radarInventories.get(p.getUniqueId());
				Inventory inv = pair.getLeft();
				Radar radar = pair.getRight();
				ItemStack is = e.getCurrentItem();
				int slot = e.getSlot();
				
				e.setCancelled(true);
				if (is == null || !inv.equals(e.getClickedInventory()))
					return;
				
				if(slot == 2) {
					radar.setChannel(Main.clamp(0, 9, radar.getChannel() + (e.getAction() == InventoryAction.PICKUP_ALL ? 1 : -1)));
					this.updateRadarInventoryChannel(radar, inv);
				} else if(slot == 4) {
					radar.setSound(!radar.isSound());
					this.updateRadarInventorySound(radar, inv);
				} else if(slot == 5) {
					radar.setMessage(!radar.isMessage());
					this.updateRadarInventoryMessage(radar, inv);
				} else if(slot == 6) {
					radar.setSend(!radar.isSend());
					this.updateRadarInventorySend(radar, inv);
				}				
			}
		}
	}

	private void updateRadarInventoryChannel(Radar radar, Inventory inv) {
		ItemStack is = new ItemStack(Material.ENDER_PEARL);
		ItemMeta im = is.getItemMeta();
		im.setDisplayName("§6Canal n°§c" + radar.getChannel());
		im.setCustomModelData(radar.getChannel() + 1);
		is.setItemMeta(im);
		
		inv.setItem(2, is);
	}
	
	private void updateRadarInventorySound(Radar radar, Inventory inv) {
		ItemStack is = new ItemStack(radar.isSound() ? Material.LIME_WOOL : Material.RED_WOOL);
		ItemMeta im = is.getItemMeta();
		im.setDisplayName(radar.isSound() ? "§aÉmet une alerte sonore" : "§cN'émet pas d'alerte sonore");
		is.setItemMeta(im);
		
		inv.setItem(4, is);
	}
	
	private void updateRadarInventoryMessage(Radar radar, Inventory inv) {
		ItemStack is = new ItemStack(radar.isMessage() ? Material.LIME_WOOL : Material.RED_WOOL);
		ItemMeta im = is.getItemMeta();
		im.setDisplayName(radar.isMessage() ? "§aEnvoie un message" : "§cN'envoie pas de message");
		is.setItemMeta(im);
		
		inv.setItem(5, is);
	}
	
	private void updateRadarInventorySend(Radar radar, Inventory inv) {
		ItemStack is = new ItemStack(radar.isSend() ? Material.LIME_WOOL : Material.RED_WOOL);
		ItemMeta im = is.getItemMeta();
		im.setDisplayName(radar.isSend() ? "§aTransmet à la défense" : "§cNe transmet pas à la défense");
		is.setItemMeta(im);
		
		inv.setItem(6, is);
	}
}

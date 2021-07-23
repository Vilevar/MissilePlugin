package be.vilevar.missiles.mcelements.launcher;

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
import be.vilevar.missiles.mcelements.data.BallisticMissileData;

public class LauncherBlockListener implements Listener {

	private HashMap<UUID, Pair<Inventory, MissileLauncherBlock>> launcherInventories = new HashMap<>();
	private String launcherInventoryName = "§bOptions de lancement";
	private String noMissile = "§cIl faut un missile capable d'être tiré.", fired = "§6Missile tiré avec succès.";
	private ItemStack unusedSlot;
	private ItemStack cantFireItem, fireItem;
	
	public LauncherBlockListener(ItemStack unusedSlot) {
		this.unusedSlot = unusedSlot;
		
		this.cantFireItem = new ItemStack(Material.RED_WOOL);
		ItemMeta im = this.cantFireItem.getItemMeta();
		im.setDisplayName("§cImpossible de tirer");
		im.setLore(Arrays.asList("§6Vous devez placer un missile (prêt)", "§6pour pouvoir en tirer en.",
				"§cEt il faut que la partie soit commencée (s'il y en a)."));
		this.cantFireItem.setItemMeta(im);
		
		this.fireItem = new ItemStack(Material.LIME_WOOL);
		im = this.fireItem.getItemMeta();
		im.setDisplayName("§aTir prêt");
		im.setLore(Arrays.asList("§6Il suffit de faire un clic gauche et le missile est parti."));
		this.fireItem.setItemMeta(im);
	}
	
	public HashMap<UUID, Pair<Inventory, MissileLauncherBlock>> getLauncherInventories() {
		return launcherInventories;
	}
	
	
	public void openLauncherInventory(MissileLauncherBlock launcher, Player p) {
		Inventory inv = Bukkit.createInventory(null, 18, this.launcherInventoryName);
		inv.setItem(2, this.unusedSlot);
		inv.setItem(6, this.unusedSlot);
		this.updateLauncherInventory(launcher, inv);
		
		launcherInventories.put(p.getUniqueId(), Pair.of(inv, launcher));
		p.openInventory(inv);
		launcher.setOpen(p);
	}
	
	
	@EventHandler
	public void onInventoryClick(InventoryClickEvent e) {
		if(e.getWhoClicked() instanceof Player) {
			Player p = (Player) e.getWhoClicked();
			
			if(this.launcherInventories.containsKey(p.getUniqueId())) {
				Pair<Inventory, MissileLauncherBlock> pair = this.launcherInventories.get(p.getUniqueId());
				
				Inventory inv = pair.getKey();
				MissileLauncherBlock launcher = pair.getRight();
				ItemStack is = e.getCurrentItem();
				int slot = e.getSlot();
				
				e.setCancelled(true);
				if(is == null)
					return;
				
				if(inv.equals(e.getClickedInventory())) {
					if(slot == 0 && launcher.getMissileData() != null) {
						p.getInventory().addItem(launcher.getMissileData().toItemStack());
						launcher.setMissileData(null);
						this.updateLauncherInventory(launcher, inv);
					} else if(slot == 1) {
						if(launcher.canLaunchMissile()) {
							if(launcher.launchMissile()) {
								p.sendMessage(this.fired);
							}
							this.updateLauncherInventory(launcher, inv);
						} else {
							p.sendMessage(this.noMissile);
						}
					} else if(slot == 3) {
						this.addYaw(launcher, e.getAction() == InventoryAction.PICKUP_ALL ? 100 : -100);
						this.updateLauncherInventory(launcher, inv);
					} else if(slot == 4) {
						this.addYaw(launcher, e.getAction() == InventoryAction.PICKUP_ALL ? 10 : -10);
						this.updateLauncherInventory(launcher, inv);
					} else if(slot == 5) {
						this.addYaw(launcher, e.getAction() == InventoryAction.PICKUP_ALL ? 1 : -1);
						this.updateLauncherInventory(launcher, inv);
					} else if(slot == 7) {
						this.addPitch(launcher, e.getAction() == InventoryAction.PICKUP_ALL ? 10 : -10);
						this.updateLauncherInventory(launcher, inv);
					} else if(slot == 8) {
						this.addPitch(launcher, e.getAction() == InventoryAction.PICKUP_ALL ? 1 : -1);
						this.updateLauncherInventory(launcher, inv);
					}
					return;
				} else if(launcher.getMissileData() == null) {
					BallisticMissileData missile = BallisticMissileData.getBallisticMissileData(is);
					if(missile != null && missile.isReady()) {
						is.setAmount(is.getAmount() - 1);
						p.getInventory().setItem(slot, is);
						p.updateInventory();
						
						launcher.setMissileData(missile);
						this.updateLauncherInventory(launcher, inv);
					}
					return;
				}
			}
		}
	}
	
	
	
	private void updateLauncherInventory(MissileLauncherBlock launcher, Inventory inv) {
		if(!launcher.canLaunchMissile()) {
			inv.setItem(0, null);
			inv.setItem(1, this.cantFireItem);
		} else {
			inv.setItem(0, launcher.getMissileData().toItemStack());
			inv.setItem(1, this.fireItem);
		}
		
		// Yaw
		ItemStack yawH = new ItemStack(Material.ENDER_PEARL);
		ItemMeta im = yawH.getItemMeta();
		int hundred = launcher.getYaw() / 100;
		im.setDisplayName("§6" + (hundred * 100) + "° Yaw");
		im.setCustomModelData(hundred + 1);
		yawH.setItemMeta(im);
		
		ItemStack yawD = new ItemStack(Material.ENDER_PEARL);
		im = yawD.getItemMeta();
		int dozen = (launcher.getYaw() % 100) / 10;
		im.setDisplayName("§6" + (dozen * 10) + "° Yaw");
		im.setCustomModelData(dozen + 1);
		yawD.setItemMeta(im);
		
		ItemStack yawU = new ItemStack(Material.ENDER_PEARL);
		im = yawU.getItemMeta();
		int unity = launcher.getYaw() % 10;
		im.setDisplayName("§6" + unity + "° Yaw");
		im.setCustomModelData(unity + 1);
		yawU.setItemMeta(im);
		
		inv.setItem(3, yawH);
		inv.setItem(4, yawD);
		inv.setItem(5, yawU);
		
		// Pitch
		ItemStack pitchD = new ItemStack(Material.ENDER_PEARL);
		im = pitchD.getItemMeta();
		dozen = launcher.getPitch() / 10;
		im.setDisplayName("§6" + (dozen * 10) + "° Pitch");
		im.setCustomModelData(dozen + 1);
		pitchD.setItemMeta(im);
		
		ItemStack pitchU = new ItemStack(Material.ENDER_PEARL);
		im = pitchU.getItemMeta();
		unity = launcher.getPitch() % 10;
		im.setDisplayName("§6" + unity + "° Pitch");
		im.setCustomModelData(unity + 1);
		pitchU.setItemMeta(im);
		
		inv.setItem(7, pitchD);
		inv.setItem(8, pitchU);
	}
	
	
	private void addPitch(MissileLauncherBlock launcher, int add) {
		int pitch = Main.clamp(25, 75, launcher.getPitch() + add);
		launcher.setPitch(pitch);
	}
	
	private void addYaw(MissileLauncherBlock launcher, int add) {
		int yaw = Main.clamp(0, 359, launcher.getYaw() + add);
		launcher.setYaw(yaw);
	}
	
}

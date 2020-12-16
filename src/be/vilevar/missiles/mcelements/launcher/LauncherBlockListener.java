package be.vilevar.missiles.mcelements.launcher;

import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.libs.org.apache.commons.lang3.tuple.Pair;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import be.vilevar.missiles.Main;
import be.vilevar.missiles.mcelements.CustomElementManager;
import be.vilevar.missiles.mcelements.data.BallisticMissileData;
import be.vilevar.missiles.mcelements.data.RangefinderData;

public class LauncherBlockListener implements Listener {

	private HashMap<UUID, Pair<Inventory, MissileLauncherBlock>> launcherInventories = new HashMap<>();
	private String launcherInventoryName = "§bOptions de lancement";
	private ItemStack unusedSlot;
	private ItemStack launcherItem0, launcherItem10, launcherItem11, launcherItem2, launcherItem3;
	
	public LauncherBlockListener(ItemStack unusedSlot) {
		this.unusedSlot = unusedSlot;
		
		this.launcherItem0 = new ItemStack(Material.ZOMBIE_HEAD);
		ItemMeta im = this.launcherItem0.getItemMeta();
		im.setDisplayName("§a<- Proprétaire du lanceur");
		im.setLore(Arrays.asList("§6Le joueur dont il y a la tête", "§6recevra des informations quant au tir."));
		this.launcherItem0.setItemMeta(im);
		
		this.launcherItem10 = new ItemStack(Material.RED_WOOL);
		im = this.launcherItem10.getItemMeta();
		im.setDisplayName("§aLancement non confirmé");
		im.setLore(Arrays.asList("§6Cliquez dessus pour confirmer le tir", "§4Il sera alors impossible", "§4de changer le missile ou la cible"));
		this.launcherItem10.setItemMeta(im);
		
		this.launcherItem11 = new ItemStack(Material.LIME_WOOL);
		im = this.launcherItem11.getItemMeta();
		im.setDisplayName("§aLancement confirmé");
		im.setLore(Arrays.asList("§6Il suffit d'une impulsion redstone pour tirer", "§4Il est désormais impossible",
				"§4de changer le missile ou la cible"));
		this.launcherItem11.setItemMeta(im);
		
		this.launcherItem2 = new ItemStack(Material.ARROW);
		im = this.launcherItem2.getItemMeta();
		im.setDisplayName("§d<- Missile à lancer");
		this.launcherItem2.setItemMeta(im);
		
		this.launcherItem3 = new ItemStack(Material.BOW);
		im = this.launcherItem3.getItemMeta();
		im.setDisplayName("§dPointeur Laser avec la cible ->");
		this.launcherItem3.setItemMeta(im);
	}
	
	public HashMap<UUID, Pair<Inventory, MissileLauncherBlock>> getLauncherInventories() {
		return launcherInventories;
	}
	
	
	public void openLauncherInventory(MissileLauncherBlock launcher, Player p) {
		Inventory inv = Bukkit.createInventory(null, 9, this.launcherInventoryName);
		inv.setItem(0, launcher.getOwnerHead());
		inv.setItem(1, launcherItem0);
		inv.setItem(2, unusedSlot);
		inv.setItem(3, this.createLauncherConfirmedItem(launcher));
		inv.setItem(4, launcher.getMissileItem());
		inv.setItem(5, launcherItem2);
		inv.setItem(6, unusedSlot);
		inv.setItem(7, launcherItem3);
		inv.setItem(8, launcher.getLaserPointerItem());
		launcherInventories.put(p.getUniqueId(), Pair.of(inv, launcher));
		p.openInventory(inv);
		launcher.setOpen(true);
	}
	
	
	@SuppressWarnings("deprecation")
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
				if(is==null) return;
				if(inv.equals(e.getClickedInventory())) {
					boolean a, b = false;
					if( ((a = slot==0) || ( ( (b = slot==4) || slot==8) && !launcher.isLaunchingConfirmed() ) )  && p.getInventory().firstEmpty()!=-1) {
						p.getInventory().addItem(is);
						inv.setItem(e.getSlot(), null);
						if(a) {
							launcher.setOwner(null);
						} else if(b) {
							launcher.setMissileData(null);
						} else {
							launcher.setLaserPointer(null);
						}
						return;
					}
					if(slot==3 && launcher.confirmLaunching(p)) {
						inv.setItem(slot, launcherItem11);
						return;
					}
				} else {
					if(is.getType()==Material.PLAYER_HEAD && inv.getItem(0)==null) {
						ItemStack i = is.clone();
						is.setAmount(is.getAmount()-1);
						p.getInventory().setItem(slot, is.getAmount() > 0 ? is : null);
						p.updateInventory();
						i.setAmount(1);
						inv.setItem(0, i);
						SkullMeta sm = (SkullMeta) i.getItemMeta();
						launcher.setOwner(Bukkit.getPlayer(sm.getOwner()));
						return;
					}
					if(CustomElementManager.BALLISTIC_MISSILE.isParentOf(is) && inv.getItem(4)==null) {
						ItemStack i = is.clone();
						is.setAmount(is.getAmount()-1);
						p.getInventory().setItem(slot, is.getAmount() > 0 ? is : null);
						p.updateInventory();
						i.setAmount(1);
						inv.setItem(4, i);
						launcher.setMissileData(BallisticMissileData.getBallisticMissileData(i));
						return;
					}
					if(CustomElementManager.RANGEFINDER.isParentOf(is) && inv.getItem(8)==null) {
						ItemStack i = is.clone();
						is.setAmount(is.getAmount()-1);
						p.getInventory().setItem(slot, is.getAmount() > 0 ? is : null);
						p.updateInventory();
						i.setAmount(1);
						inv.setItem(8, i);
						launcher.setLaserPointer(RangefinderData.getRangefinderData(i));
						return;
					}
				}
				return;
			}
		}
	}
	
	private ItemStack createLauncherConfirmedItem(MissileLauncherBlock launcher) {
		return launcher.isLaunchingConfirmed() ? launcherItem11 : launcherItem10;
	}
	
	
	
	@EventHandler
	public void onRedstone(BlockRedstoneEvent e) {
		for(int x = -1; x < 2; x++) {
			for(int y = -1; y < 2; y++) {
				for(int z = -1; z < 2; z++) {
					final Block b = e.getBlock().getLocation().add(x, y, z).getBlock();
					if(b.getType()==CustomElementManager.MISSILE_LAUNCHER && !b.isBlockPowered()) {
						MissileLauncherBlock launcher = MissileLauncherBlock.getLauncherAt(b.getLocation());
						if(launcher!=null && launcher.isLaunchingConfirmed()) {
							Bukkit.getScheduler().runTaskLater(Main.i, () -> {
								if(b.isBlockPowered()) {
									launcher.launchMissile();
								}
							}, 1);
						}
					}
				}
			}
		}
	}
}

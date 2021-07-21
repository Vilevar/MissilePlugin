package be.vilevar.missiles.mcelements.crafting;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.craftbukkit.libs.org.apache.commons.lang3.tuple.Pair;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import be.vilevar.missiles.mcelements.CustomElementManager;
import be.vilevar.missiles.mcelements.data.BallisticMissileData;
import be.vilevar.missiles.mcelements.data.MIRVData;

public class MissileCraftingTableListener implements Listener {

	private HashMap<UUID, Pair<Inventory, MissileCraftBlock>> craftInventories = new HashMap<>();
	private String craftInventoryName = "§eConstruction de missile";
	private ItemStack unusedSlot;
	private ItemStack craftItemBarrier;

	public MissileCraftingTableListener(ItemStack unusedSlot) {
		this.unusedSlot = unusedSlot;

		this.craftItemBarrier = new ItemStack(Material.BARRIER);
		ItemMeta im = this.craftItemBarrier.getItemMeta();
		im.setDisplayName("§4Indisponible");
		this.craftItemBarrier.setItemMeta(im);
	}

	public HashMap<UUID, Pair<Inventory, MissileCraftBlock>> getCraftInventories() {
		return craftInventories;
	}

	public void openCraftTable(MissileCraftBlock craft, Player p) {
		Inventory inv = Bukkit.createInventory(null, 45, this.craftInventoryName);
		for(int i = 0; i < 45; i++) {
			inv.setItem(i, unusedSlot);
		}
		inv.setItem(19, null);
		
		this.updateCraftInventory(craft, inv);
		
		this.craftInventories.put(p.getUniqueId(), Pair.of(inv, craft));
		p.openInventory(inv);
		craft.setOpen(p);
	}

	@EventHandler
	public void onInventoryClick(InventoryClickEvent e) {
		if (e.getWhoClicked() instanceof Player) {
			Player p = (Player) e.getWhoClicked();

			if (this.craftInventories.containsKey(p.getUniqueId())) {
				
				Pair<Inventory, MissileCraftBlock> pair = this.craftInventories.get(p.getUniqueId());
				Inventory inv = pair.getLeft();
				MissileCraftBlock craft = pair.getRight();
				BallisticMissileData missile = craft.getMissile();
				ItemStack is = e.getCurrentItem();
				int slot = e.getSlot();
				
				e.setCancelled(true);
				if (is == null)
					return;
				
				if (inv.equals(e.getClickedInventory())) {
					if(missile == null) {
						return;
					} else if(slot == 19) {
						p.getInventory().addItem(missile.toItemStack());
						craft.setMissile(null);
						this.updateCraftInventory(craft, inv);
					} else if(!is.equals(craftItemBarrier) && !is.equals(unusedSlot)) {
						p.getInventory().addItem(is);
						switch(slot) {
						case 3:
							missile.setWarhead(null);
							break;
						case 12:
							missile.setEject(2, 0);
							break;
						case 13:
							missile.setImpulse(2, 0);
							missile.setNFuel(2, 0);
							break;
						case 30:
							missile.setEject(1, 0);
							break;
						case 31:
							missile.setImpulse(1, 0);
							missile.setNFuel(1, 0);
							break;
						case 39:
							missile.setEject(0, 0);
							break;
						case 40:
							missile.setImpulse(0, 0);
							missile.setNFuel(0, 0);
							break;
						}
						this.updateCraftInventory(craft, inv);
					}
					return;
				} else {
					if(missile == null) {
						missile = BallisticMissileData.getBallisticMissileData(is);
						if(missile != null) {
							is.setAmount(is.getAmount() - 1);
							p.getInventory().setItem(slot, is);
							p.updateInventory();
							
							craft.setMissile(missile);
							this.updateCraftInventory(craft, inv);
						}
						return;
					} else {
						if(missile.getWarhead() == null) {
							MIRVData warhead = MIRVData.getMIRVData(is);
							if(warhead != null && warhead.isReadyForMissile()) {
								is.setAmount(is.getAmount() - 1);
								p.getInventory().setItem(slot, is);
								p.updateInventory();
								
								missile.setWarhead(warhead);
								this.updateCraftInventory(craft, inv);
								return;
							}
						}
						
						int impulse = this.getImpulseFrom(is);
						if(impulse != 0) {
							for(int i = 0; i < missile.getStages(); i++) {
								if(missile.getImpulse(i) == 0 || missile.getImpulse(i) == impulse) {
									int neededFuel = missile.getMaxFuel(i) - missile.getNFuel(i);
									if(neededFuel > 0) {
										int added = Math.min(is.getAmount(), neededFuel);
										
										is.setAmount(is.getAmount() - added);
										p.getInventory().setItem(slot, is);
										p.updateInventory();
										
										missile.setImpulse(i, impulse);
										missile.setNFuel(i, missile.getNFuel(i) + added);
										this.updateCraftInventory(craft, inv);
										return;
									}
								}
							}
							return;
						}
						
						int eject = this.getEjectFrom(is);
						if(eject != 0) {
							for(int i = 0; i < missile.getStages(); i++) {
								if(missile.getEject(i) == 0) {
									
									is.setAmount(is.getAmount() - 1);
									p.getInventory().setItem(slot, is);
									p.updateInventory();
									
									missile.setEject(i, eject);
									this.updateCraftInventory(craft, inv);
									return;
								}
							}
							return;
						}
					}
				}
			}
		}
	}

	private void updateCraftInventory(MissileCraftBlock craft, Inventory inv) {
		if(craft.getMissile() == null) {
			inv.setItem(3, craftItemBarrier);
			inv.setItem(12, craftItemBarrier);
			inv.setItem(13, craftItemBarrier);
			inv.setItem(19, null);
			inv.setItem(30, craftItemBarrier);
			inv.setItem(31, craftItemBarrier);
			inv.setItem(39, craftItemBarrier);
			inv.setItem(40, craftItemBarrier);
		} else {
			BallisticMissileData missile = craft.getMissile();
			inv.setItem(3, missile.getWarhead() == null ? null : missile.getWarhead().toItem());
			inv.setItem(19, missile.toItemStack());
			inv.setItem(39, this.convertEjectToItem(missile.getEject(0)));
			inv.setItem(40, this.convertImpulseToItem(missile.getImpulse(0), missile.getNFuel(0)));
			if(missile.getStages() > 1) {
				inv.setItem(30, this.convertEjectToItem(missile.getEject(1)));
				inv.setItem(31, this.convertImpulseToItem(missile.getImpulse(1), missile.getNFuel(1)));
				if(missile.getStages() == 3) {
					inv.setItem(12, this.convertEjectToItem(missile.getEject(2)));
					inv.setItem(13, this.convertImpulseToItem(missile.getImpulse(2), missile.getNFuel(2)));
				}
			}
		}
	}
	
	private ItemStack convertImpulseToItem(int impulse, int count) {
		if(count == 0) {
			return null;
		}
		switch(impulse) {
		case 210:
			return CustomElementManager.FUEL_1.create(count);
		case 215:
			return CustomElementManager.FUEL_2.create(count);
		case 220:
			return CustomElementManager.FUEL_3.create(count);
		default:
			return null;
		}
	}
	
	private ItemStack convertEjectToItem(int eject) {
		switch(eject) {
		case 450:
			return CustomElementManager.ENGINE_1.create();
		case 425:
			return CustomElementManager.ENGINE_2.create();
		case 400:
			return CustomElementManager.ENGINE_3.create();
		default:
			return null;
		}
	}
	
	private int getImpulseFrom(ItemStack is) {
		if(CustomElementManager.FUEL_1.isParentOf(is)) {
			return 210;
		} else if(CustomElementManager.FUEL_2.isParentOf(is)) {
			return 215;
		} else if(CustomElementManager.FUEL_3.isParentOf(is)) {
			return 220;
		}
		return 0;
	}
	
	private int getEjectFrom(ItemStack is) {
		if(CustomElementManager.ENGINE_1.isParentOf(is)) {
			return 450;
		} else if(CustomElementManager.ENGINE_2.isParentOf(is)) {
			return 425;
		} else if(CustomElementManager.ENGINE_3.isParentOf(is)) {
			return 400;
		}
		return 0;
	}
	
}

package be.vilevar.missiles.mcelements.crafting;

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
import be.vilevar.missiles.mcelements.data.MIRVData;
import be.vilevar.missiles.missile.ballistic.Explosive;
import be.vilevar.missiles.missile.ballistic.explosives.EMPExplosive;
import be.vilevar.missiles.missile.ballistic.explosives.NuclearExplosive;
import be.vilevar.missiles.missile.ballistic.explosives.ThermonuclearExplosive;
import be.vilevar.missiles.missile.ballistic.explosives.TraditionalExplosive;

public class RVCraftingTableListener implements Listener {

	private HashMap<UUID, Pair<Inventory, RVCraftBlock>> craftInventories = new HashMap<>();
	private String craftInventoryName = "§eConstruction de mirv";
	private ItemStack unusedSlot;
	private ItemStack craftItemBarrier;

	public RVCraftingTableListener(ItemStack unusedSlot) {
		this.unusedSlot = unusedSlot;

		this.craftItemBarrier = new ItemStack(Material.BARRIER);
		ItemMeta im = this.craftItemBarrier.getItemMeta();
		im.setDisplayName("§4Indisponible");
		this.craftItemBarrier.setItemMeta(im);
	}

	public HashMap<UUID, Pair<Inventory, RVCraftBlock>> getCraftInventories() {
		return craftInventories;
	}

	public void openCraftTable(RVCraftBlock craft, Player p) {
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
				
				Pair<Inventory, RVCraftBlock> pair = this.craftInventories.get(p.getUniqueId());
				Inventory inv = pair.getLeft();
				RVCraftBlock craft = pair.getRight();
				MIRVData mirv = craft.getMIRV();
				ItemStack is = e.getCurrentItem();
				int slot = e.getSlot();
				
				e.setCancelled(true);
				if (is == null)
					return;
				
				if (inv.equals(e.getClickedInventory())) {
					if(mirv == null) {
						return;
					} else if(slot == 19) {
						p.getInventory().addItem(mirv.toItem());
						craft.setMIRV(null);
						this.updateCraftInventory(craft, inv);
						return;
					} else if(mirv.getMIRVs() == 1) {
						if(slot == 21) {
							p.getInventory().addItem(is);
							mirv.setExplosive(0, null);
							this.updateCraftInventory(craft, inv);
						} else if(slot == 23) {
							this.addAltitudeExplosion(mirv, e.getAction() == InventoryAction.PICKUP_ALL ? 100 : -100);
							this.updateCraftInventory(craft, inv);
						} else if(slot == 24) {
							this.addAltitudeExplosion(mirv, e.getAction() == InventoryAction.PICKUP_ALL ? 10 : -10);
							this.updateCraftInventory(craft, inv);
						} else if(slot == 25) {
							this.addAltitudeExplosion(mirv, e.getAction() == InventoryAction.PICKUP_ALL ? 1 : -1);
							this.updateCraftInventory(craft, inv);
						}
						return;
					} else {
						if(slot % 9 == 3) {
							int rvID = slot / 9;
							
							MIRVData rv = new MIRVData(1, new int[] {mirv.getAltitude(rvID)}, new int[1], new int[1], 
									new Explosive[] {mirv.getExplosive(rvID)});
							p.getInventory().addItem(rv.toItem());
							
							mirv.setExplosive(rvID, null);
							mirv.setAltitude(rvID, 0);
							this.updateCraftInventory(craft, inv);
						} else if(slot % 9 == 4) {
							this.inverseYaw(mirv, slot / 9);
							this.updateCraftInventory(craft, inv);
						} else if(slot % 9 == 5) {
							this.addYaw(mirv, slot / 9, e.getAction() == InventoryAction.PICKUP_ALL ? 1 : -1);
							this.updateCraftInventory(craft, inv);
						} else if(slot % 9 == 7) {
							this.inversePitch(mirv, slot / 9);
							this.updateCraftInventory(craft, inv);
						} else if(slot % 9 == 8) {
							this.addPitch(mirv, slot / 9, e.getAction() == InventoryAction.PICKUP_ALL ? 1 : -1);
							this.updateCraftInventory(craft, inv);
						}
						return;
					}
				} else {
					if(mirv == null) {
						mirv = MIRVData.getMIRVData(is);
						if(mirv != null) {
							ItemStack iss = is.clone();
							is.setAmount(is.getAmount() - 1);
							p.getInventory().setItem(slot, is);
							p.updateInventory();
							
							iss.setAmount(1);
							inv.setItem(19, iss);
							craft.setMIRV(mirv);
							this.updateCraftInventory(craft, inv);
						}
						return;
					} else if(mirv.getMIRVs() == 1) {
						if(mirv.getExplosive(0) == null) {
							Explosive exp = this.getExplosiveFrom(is);
							if(exp != null) {
								is.setAmount(is.getAmount() - 1);
								p.getInventory().setItem(slot, is);
								p.updateInventory();
								
								mirv.setExplosive(0, exp);
								this.updateCraftInventory(craft, inv);
							}
						}
						return;
					} else {
						MIRVData data = MIRVData.getMIRVData(is);
						if(data != null && data.getMIRVs() == 1 && data.getExplosive(0) != null) {
							for(int i = 0; i < mirv.getMIRVs(); i++) {
								if(mirv.getExplosive(i) == null) {
									is.setAmount(is.getAmount() - 1);
									p.getInventory().setItem(slot, is);
									p.updateInventory();
									
									mirv.setExplosive(i, data.getExplosive(0));
									mirv.setAltitude(i, data.getAltitude(0));
									this.updateCraftInventory(craft, inv);
									return;
								}
							}
						}
					}
				}
			}
		}
	}

	private void updateCraftInventory(RVCraftBlock craft, Inventory inv) {
		MIRVData mirv = craft.getMIRV();
		if(mirv == null) {
			for(int i = 0; i < 5; i++) {
				int a = 9*i;
				inv.setItem(a + 3, this.craftItemBarrier);
				
				for(int j = 4; j < 9; j++) {
					inv.setItem(a + j, this.unusedSlot);
				}
			}
			
			inv.setItem(19, null);
		} else if(mirv.getMIRVs() == 1) {
			// Background
			for(int i = 4; i < 9; i++) {
				for(int j = 0; j < 2; j++) {
					int raw = 9*j;
					inv.setItem(raw + i, this.unusedSlot);
					inv.setItem(raw + i + 27, this.unusedSlot);
				}
			}
			inv.setItem(19, CustomElementManager.REENTRY_VEHICLE.create());
			inv.setItem(22, this.unusedSlot);
			inv.setItem(26, this.unusedSlot);
			// Items
			inv.setItem(21, mirv.getExplosive(0) == null ? null : mirv.getExplosive(0).toItem());
			
			ItemStack yH = new ItemStack(Material.ENDER_PEARL);
			ItemMeta im = yH.getItemMeta();
			int hundred = mirv.getAltitude(0) / 100;
			im.setDisplayName("§6" + (hundred * 100) + " altitude d'explosion");
			im.setCustomModelData(hundred + 1);
			yH.setItemMeta(im);
			
			ItemStack yD = new ItemStack(Material.ENDER_PEARL);
			im = yD.getItemMeta();
			int dozen = (mirv.getAltitude(0) % 100) / 10;
			im.setDisplayName("§6" + (dozen * 10) + " altitude d'explosion");
			im.setCustomModelData(dozen + 1);
			yD.setItemMeta(im);
			
			ItemStack yU = new ItemStack(Material.ENDER_PEARL);
			im = yU.getItemMeta();
			int unity = mirv.getAltitude(0) % 10;
			im.setDisplayName("§6" + unity + " altitude d'explosion");
			im.setCustomModelData(unity + 1);
			yU.setItemMeta(im);
			
			inv.setItem(23, yH);
			inv.setItem(24, yD);
			inv.setItem(25, yU);
		} else {
			// Background
			for(int i = 0; i < 5; i++) {
				inv.setItem(9*i + 6, this.unusedSlot);
			}
			inv.setItem(19, CustomElementManager.MIRV.create());
			// Items
			for(int i = 0; i < 5; i++) {
				inv.setItem(9*i + 3, mirv.getExplosive(i) == null ? null : mirv.getExplosive(i).toItem());
				
				ItemStack yawS = new ItemStack(Material.ENDER_PEARL);
				ItemMeta im = yawS.getItemMeta();
				boolean minus = Math.signum(mirv.getYaw(i)) == -1;
				im.setDisplayName("§6"+(minus ? "-" : "+"));
				im.setCustomModelData(minus ? 11 : 12);
				yawS.setItemMeta(im);
				
				ItemStack yawU = new ItemStack(Material.ENDER_PEARL);
				im = yawU.getItemMeta();
				int yaw = Math.abs(mirv.getYaw(i));
				im.setDisplayName("§6"+yaw+"° Yaw");
				im.setCustomModelData(yaw + 1);
				yawU.setItemMeta(im);
				
				ItemStack pitchS = new ItemStack(Material.ENDER_PEARL);
				im = pitchS.getItemMeta();
				minus = Math.signum(mirv.getPitch(i)) == -1;
				im.setDisplayName("§6"+(minus ? "-" : "+"));
				im.setCustomModelData(minus ? 11 : 12);
				pitchS.setItemMeta(im);
				
				ItemStack pitchU = new ItemStack(Material.ENDER_PEARL);
				im = pitchU.getItemMeta();
				int pitch = Math.abs(mirv.getPitch(i));
				im.setDisplayName("§6"+pitch+"° Pitch");
				im.setCustomModelData(pitch + 1);
				pitchU.setItemMeta(im);
				
				inv.setItem(9*i + 4, yawS);
				inv.setItem(9*i + 5, yawU);
				inv.setItem(9*i + 7, pitchS);
				inv.setItem(9*i + 8, pitchU);
			}
		}
	}

	
	
	private void addAltitudeExplosion(MIRVData mirv, int add) {
		int altitude = Main.clamp(0, 256, mirv.getAltitude(0) + add);
		mirv.setAltitude(0, altitude);
	}
	
	private void addPitch(MIRVData mirv, int rv, int add) {
		int pitch = Main.clamp(-9, 9, mirv.getPitch(rv) + add);
		mirv.setPitch(rv, pitch);
	}
	
	private void inversePitch(MIRVData mirv, int rv) {
		mirv.setPitch(rv, -mirv.getPitch(rv));
	}
	
	private void addYaw(MIRVData mirv, int rv, int add) {
		int yaw = Main.clamp(-9, 9, mirv.getYaw(rv) + add);
		mirv.setYaw(rv, yaw);
	}
	
	private void inverseYaw(MIRVData mirv, int rv) {
		mirv.setYaw(rv, -mirv.getYaw(rv));
	}
	
	private Explosive getExplosiveFrom(ItemStack is) {
		if(is.getType() == Material.TNT) {
			return new TraditionalExplosive(Main.i, 200);
		} else if(CustomElementManager.A_BOMB.isParentOf(is)) {
			return new NuclearExplosive(Main.i, 2000000, 50, 20);
		} else if(CustomElementManager.H_BOMB.isParentOf(is)) {
			return new ThermonuclearExplosive(Main.i, 8000000, 75, 40);
		} else if(CustomElementManager.E_BOMB.isParentOf(is)) {
			return new EMPExplosive(250_000_000);
		}
		return null;
	}
	
}

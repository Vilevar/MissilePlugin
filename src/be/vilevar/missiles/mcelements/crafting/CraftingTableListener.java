package be.vilevar.missiles.mcelements.crafting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
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

import be.vilevar.missiles.mcelements.CustomElementManager;
import be.vilevar.missiles.mcelements.data.BallisticMissileData;

public class CraftingTableListener implements Listener {

	private HashMap<UUID, Pair<Inventory, MissileCraftBlock>> craftInventories = new HashMap<>();
	private String craftInventoryName = "§eConstruction de missile";
	private ItemStack unusedSlot;
	private ItemStack craftItemBarrier, craftItem0;

	public CraftingTableListener(ItemStack unusedSlot) {
		this.unusedSlot = unusedSlot;

		this.craftItemBarrier = new ItemStack(Material.BARRIER);
		ItemMeta im = this.craftItemBarrier.getItemMeta();
		im.setDisplayName("§4Indisponible");
		this.craftItemBarrier.setItemMeta(im);

		this.craftItem0 = new ItemStack(Material.ARROW);
		im = this.craftItem0.getItemMeta();
		im.setDisplayName("§d<- Missile");
		this.craftItem0.setItemMeta(im);
	}

	public HashMap<UUID, Pair<Inventory, MissileCraftBlock>> getCraftInventories() {
		return craftInventories;
	}

	public void openCraftTable(MissileCraftBlock craft, Player p) {
		Inventory inv = Bukkit.createInventory(null, 45, this.craftInventoryName);
		for(int i = 9; i < 18; i++) {
			inv.setItem(i, unusedSlot);
			inv.setItem(i+18, unusedSlot);
		}
		for(int i = 0; i < 5; i++) {
			int j = i*9;
			inv.setItem(j + 3, unusedSlot);
			inv.setItem(j + 6, unusedSlot);
			inv.setItem(j + 7, unusedSlot);
			inv.setItem(j + 8, unusedSlot);
		}
		inv.setItem(38, unusedSlot);
		this.setDefaultCraftInventory(inv);
		
		this.craftInventories.put(p.getUniqueId(), Pair.of(inv, craft));
		p.openInventory(inv);
		craft.setOpen(true);
	}

	@EventHandler
	public void onInventoryClick(InventoryClickEvent e) {
		if (e.getWhoClicked() instanceof Player) {
			Player p = (Player) e.getWhoClicked();

			if (this.craftInventories.containsKey(p.getUniqueId())) {
				Pair<Inventory, MissileCraftBlock> pair = this.craftInventories.get(p.getUniqueId());
				Inventory inv = pair.getLeft();
				MissileCraftBlock craft = pair.getRight();
				ItemStack is = e.getCurrentItem();
				int slot = e.getSlot();
				e.setCancelled(true);
				if (is == null)
					return;
				if (inv.equals(e.getClickedInventory())) {
					if (craft.getOriginalMissile() != null) {
						if (slot == 0) {
							if (p.getInventory().firstEmpty() != -1) {
								BallisticMissileData o = craft.getOriginalMissile();
								if (craft.getExplosionPower() < o.getExplosionPower() || craft.getRange() < o.getRange()
										|| craft.getRotatingForce() < o.getRotatingForce()
										|| craft.getSpeed() < o.getSpeed())
									return;
								p.getInventory().addItem(o.toItemStack());
								p.updateInventory();
								inv.setItem(0, null);
								craft.setMissile(null);
								this.adaptCraftInventory(inv, craft);
							}
							return;
						} else if (slot == 2) {
							// Flight Height
							double f = craft.getFlightHeight();
							if (e.getAction() == InventoryAction.PICKUP_ALL && f < 350) {
								craft.setFlightHeight(f + 1);
							} else if (e.getAction() == InventoryAction.PICKUP_HALF && f > 75) {
								craft.setFlightHeight(f - 1);
							}
							this.adaptCraftInventory(inv, craft);
							return;
						} else if (slot == 20) {
							// Detector Distance
							double d = craft.getDetectorDistance();
							if (e.getAction() == InventoryAction.PICKUP_ALL && d < 10) {
								craft.setDetectorDistance(d + 1);
							} else if (e.getAction() == InventoryAction.PICKUP_HALF && d > 0) {
								craft.setDetectorDistance(d - 1);
							}
							this.adaptCraftInventory(inv, craft);
							return;
						} else if (slot == 5) {
							// Speed
							ItemStack a = inv.getItem(4);
							if (a == null || a.getType() == Material.AIR)
								a = CustomElementManager.FUEL.create(0);
							if (e.getAction() == InventoryAction.PICKUP_ALL) {
								if (a.getAmount() > 0) {
									a.setAmount(a.getAmount() - 1);
									inv.setItem(4, a);
									craft.addSpeedFuel(-1);
									craft.setSpeed(craft.getSpeed() + 10);
									this.adaptCraftInventory(inv, craft);
								}
							} else if (e.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
								if (a.getAmount() > 0) {
									inv.setItem(4, null);
									craft.addSpeedFuel(-a.getAmount());
									craft.setSpeed(craft.getSpeed() + 10 * a.getAmount());
									this.adaptCraftInventory(inv, craft);
								}
							} else if (e.getAction() == InventoryAction.PICKUP_HALF) {
								if ((craft.getSpeed() - 10) >= Math.max(BallisticMissileData.defaultSpeed,
										craft.getResultMissile().getMinSpeed())
										&& a.getAmount() < a.getType().getMaxStackSize()) {
									a.setAmount(a.getAmount() + 1);
									inv.setItem(4, a);
									craft.addSpeedFuel(1);
									craft.setSpeed(craft.getSpeed() - 10);
									this.adaptCraftInventory(inv, craft);
								}
							} else if (e.getAction() == InventoryAction.CLONE_STACK) {
								int amount = a.getAmount();
								int nb = Math.min(a.getType().getMaxStackSize() - amount,
										(int) ((craft.getSpeed() - Math.max(BallisticMissileData.defaultSpeed,
												craft.getResultMissile().getMinSpeed())) / 10));
								a.setAmount(amount + nb);
								inv.setItem(4, a);
								craft.addSpeedFuel(nb);
								craft.setSpeed(craft.getSpeed() - 10 * nb);
								this.adaptCraftInventory(inv, craft);
							}
							return;
						} else if (slot == 37) {
							// Range
							ItemStack a = inv.getItem(36);
							if (a == null || a.getType() == Material.AIR)
								a = CustomElementManager.FUEL.create(0);
							if (e.getAction() == InventoryAction.PICKUP_ALL) {
								if (a.getAmount() > 0) {
									a.setAmount(a.getAmount() - 1);
									inv.setItem(36, a);
									craft.addRangeFuel(-1);
									craft.setRange(craft.getRange() + 100);
									this.adaptCraftInventory(inv, craft);
								}
							} else if (e.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
								if (a.getAmount() > 0) {
									inv.setItem(36, null);
									craft.addRangeFuel(-a.getAmount());
									craft.setRange(craft.getRange() + 100 * a.getAmount());
									this.adaptCraftInventory(inv, craft);
								}
							} else if (e.getAction() == InventoryAction.PICKUP_HALF) {
								if ((craft.getRange() - 100) >= Math.max(BallisticMissileData.defaultRange,
										craft.getResultMissile().getMinRange())
										&& a.getAmount() < a.getType().getMaxStackSize()) {
									a.setAmount(a.getAmount() + 1);
									inv.setItem(36, a);
									craft.addRangeFuel(1);
									craft.setRange(craft.getRange() - 100);
									this.adaptCraftInventory(inv, craft);
								}
							} else if (e.getAction() == InventoryAction.CLONE_STACK) {
								int amount = a.getAmount();
								int nb = Math.min(a.getType().getMaxStackSize() - amount,
										(int) ((craft.getRange() - Math.max(BallisticMissileData.defaultRange,
												craft.getResultMissile().getMinRange())) / 100));
								a.setAmount(amount + nb);
								inv.setItem(36, a);
								craft.addRangeFuel(nb);
								craft.setRange(craft.getRange() - 100 * nb);
								this.adaptCraftInventory(inv, craft);
							}
							return;
						} else if (slot == 41) {
							// Rotating Force
							ItemStack a = inv.getItem(40);
							if (a == null || a.getType() == Material.AIR)
								a = new ItemStack(Material.BLAZE_POWDER, 0);
							if (e.getAction() == InventoryAction.PICKUP_ALL) {
								if (a.getAmount() > 0 && (craft.getRotatingForce() + 50) <= craft.getResultMissile()
										.getMaxRotatingForce()) {
									a.setAmount(a.getAmount() - 1);
									inv.setItem(40, a);
									craft.addBlazePowder(-1);
									craft.setRotatingForce(craft.getRotatingForce() + 50);
									this.adaptCraftInventory(inv, craft);
								}
							} else if (e.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
								int nb = Math.min(a.getAmount(), (int) ((craft.getResultMissile().getMaxRotatingForce()
										- craft.getRotatingForce()) / 50));
								a.setAmount(a.getAmount() - nb);
								inv.setItem(40, a);
								craft.addBlazePowder(-nb);
								craft.setRotatingForce(craft.getRotatingForce() + 50 * nb);
								this.adaptCraftInventory(inv, craft);
							} else if (e.getAction() == InventoryAction.PICKUP_HALF) {
								if ((craft.getRotatingForce() - 50) >= Math.max(
										BallisticMissileData.defaultRotatingForce,
										craft.getResultMissile().getMinRotatingForce())
										&& a.getAmount() < a.getType().getMaxStackSize()) {
									a.setAmount(a.getAmount() + 1);
									inv.setItem(40, a);
									craft.addBlazePowder(1);
									craft.setRotatingForce(craft.getRotatingForce() - 50);
									this.adaptCraftInventory(inv, craft);
								}
							} else if (e.getAction() == InventoryAction.CLONE_STACK) {
								int amount = a.getAmount();
								int nb = Math.min(a.getType().getMaxStackSize() - amount,
										(int) ((craft.getRotatingForce()
												- Math.max(BallisticMissileData.defaultRotatingForce,
														craft.getResultMissile().getMinRotatingForce()))
												/ 50));
								a.setAmount(amount + nb);
								inv.setItem(40, a);
								craft.addBlazePowder(nb);
								craft.setRotatingForce(craft.getRotatingForce() - 50 * nb);
								this.adaptCraftInventory(inv, craft);
							}
							return;
						} else if (slot == 23) {
							// Explosion Power
							ItemStack a = inv.getItem(22);
							if (a == null || a.getType() == Material.AIR)
								a = new ItemStack(Material.TNT, 0);
							if (e.getAction() == InventoryAction.PICKUP_ALL) {
								if (a.getAmount() > 0) {
									a.setAmount(a.getAmount() - 1);
									inv.setItem(22, a);
									craft.addTNT(-1);
									craft.setExplosionPower(craft.getExplosionPower() + 10);
									this.adaptCraftInventory(inv, craft);
								}
							} else if (e.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
								if (a.getAmount() > 0) {
									inv.setItem(22, null);
									craft.addTNT(-a.getAmount());
									craft.setExplosionPower(craft.getExplosionPower() + 10 * a.getAmount());
									this.adaptCraftInventory(inv, craft);
								}
							} else if (e.getAction() == InventoryAction.PICKUP_HALF) {
								if ((craft.getExplosionPower() - 10) >= BallisticMissileData.defaultExplosionPower
										&& a.getAmount() < a.getType().getMaxStackSize()) {
									a.setAmount(a.getAmount() + 1);
									inv.setItem(22, a);
									craft.addTNT(1);
									craft.setExplosionPower(craft.getExplosionPower() - 10);
									this.adaptCraftInventory(inv, craft);
								}
							} else if (e.getAction() == InventoryAction.CLONE_STACK) {
								int amount = a.getAmount();
								int nb = Math.min(a.getType().getMaxStackSize() - amount,
										(int) ((craft.getExplosionPower() - BallisticMissileData.defaultExplosionPower)
												/ 10));
								a.setAmount(amount + nb);
								inv.setItem(22, a);
								craft.addTNT(nb);
								craft.setExplosionPower(craft.getExplosionPower() - 10 * nb);
								this.adaptCraftInventory(inv, craft);
							}
							return;
						} else if (slot == 25) {
							if (is.getItemMeta().getLore().size() == 10) {
								inv.setItem(0, null);
								inv.setItem(26, craft.getResultMissile().toItemStack());
								craft.resetOriginalMissile();
								this.adaptCraftInventory(inv, craft);
							}
							return;
						}
					}
					if (slot == 0 || slot == 1 || slot == 2 || slot == 5 || slot == 20 || slot == 37 || slot == 41
							|| slot == 23 || slot == 25)
						return;
					if (!is.equals(unusedSlot) && !is.equals(craftItemBarrier) && p.getInventory().firstEmpty() != -1) {
						if (inv.getItem(0) == null) {
							p.getInventory().addItem(is);
							p.updateInventory();
							inv.setItem(slot, this.craftItemBarrier);
							switch (slot) {
							case 22:
								craft.addTNT(-is.getAmount());
								break;
							case 40:
								craft.addBlazePowder(-is.getAmount());
								break;
							case 4:
								craft.addSpeedFuel(-is.getAmount());
								break;
							case 36:
								craft.addRangeFuel(-is.getAmount());
								break;
							case 26:
								craft.resetResultMissile();
								this.adaptCraftInventory(inv, craft);
								break;
							}
							return;
						} else if (craft.getOriginalMissile() != null) {
							boolean move = e.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY;
							int max = 0;
							switch (slot) {
							case 22:
								max = craft.getCanTakeTNT();
								if (max > 0)
									craft.addTNT(move ? -max : -1);
								break;
							case 40:
								max = craft.getCanTakeBlazePowder();
								if (max > 0)
									craft.addBlazePowder(move ? -max : -1);
								break;
							case 4:
								max = craft.getCanTakeSpeedFuel();
								if (max > 0)
									craft.addSpeedFuel(move ? -max : -1);
								break;
							case 36:
								max = craft.getCanTakeRangeFuel();
								if (max > 0)
									craft.addRangeFuel(move ? -max : -1);
								break;
							}
							if (max <= 0)
								return;
							int nb = move ? max : 1;
							ItemStack i = is.clone();
							is.setAmount(is.getAmount() - nb);
							inv.setItem(slot, is);
							i.setAmount(nb);
							p.getInventory().addItem(i);
							p.updateInventory();
							return;
						}
					}
				} else {
					if (CustomElementManager.BALLISTIC_MISSILE.isParentOf(is) && inv.getItem(0) == null
							&& craft.getResultMissile() == null) {
						ItemStack i = is.clone();
						is.setAmount(is.getAmount() - 1);
						p.getInventory().setItem(slot, is.getAmount() > 0 ? is : null);
						p.updateInventory();
						i.setAmount(1);
						inv.setItem(0, i);
						craft.setMissile(BallisticMissileData.getBallisticMissileData(i));
						this.adaptCraftInventory(inv, craft);
						return;
					} else if (is.getType() == Material.TNT && inv.getItem(0) != null) {
						if (e.getAction() == InventoryAction.PICKUP_ALL) {
							ItemStack a = inv.getItem(22);
							if (a == null || a.getType() == Material.AIR)
								a = new ItemStack(Material.TNT, 0);
							if (a.getAmount() >= a.getType().getMaxStackSize())
								return;
							ItemStack i = is.clone();
							is.setAmount(is.getAmount() - 1);
							p.getInventory().setItem(slot, is.getAmount() > 0 ? is : null);
							p.updateInventory();
							i.setAmount(1 + a.getAmount());
							inv.setItem(22, i);
							craft.addTNT(1);
						} else if (e.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
							ItemStack a = inv.getItem(22);
							if (a == null || a.getType() == Material.AIR)
								a = new ItemStack(Material.TNT, 0);
							int amount = is.getAmount();
							int max = is.getType().getMaxStackSize() - a.getAmount();
							int nb = Math.min(amount, max);
							ItemStack i = is.clone();
							i.setAmount(amount - nb);
							p.getInventory().setItem(slot, i.getAmount() > 0 ? i : null);
							p.updateInventory();
							is.setAmount(a.getAmount() + nb);
							inv.setItem(22, is);
							craft.addTNT(nb);
						}
						return;
					} else if (is.getType() == Material.BLAZE_POWDER && inv.getItem(0) != null) {
						if (e.getAction() == InventoryAction.PICKUP_ALL) {
							ItemStack a = inv.getItem(40);
							if (a == null || a.getType() == Material.AIR)
								a = new ItemStack(Material.BLAZE_POWDER, 0);
							if (a.getAmount() >= a.getType().getMaxStackSize()
									|| a.getAmount() >= (int) ((craft.getResultMissile().getMaxRotatingForce()
											- craft.getRotatingForce()) / 50))
								return;
							ItemStack i = is.clone();
							is.setAmount(is.getAmount() - 1);
							p.getInventory().setItem(slot, is.getAmount() > 0 ? is : null);
							p.updateInventory();
							i.setAmount(1 + a.getAmount());
							inv.setItem(40, i);
							craft.addBlazePowder(1);
						} else if (e.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
							ItemStack a = inv.getItem(40);
							if (a == null || a.getType() == Material.AIR)
								a = new ItemStack(Material.BLAZE_POWDER, 0);
							int amount = is.getAmount();
							int max = Math.min(is.getType().getMaxStackSize() - a.getAmount(),
									(int) ((craft.getResultMissile().getMaxRotatingForce() - craft.getRotatingForce())
											/ 50) - a.getAmount());
							int nb = Math.min(amount, max);
							ItemStack i = is.clone();
							i.setAmount(amount - nb);
							p.getInventory().setItem(slot, i.getAmount() > 0 ? i : null);
							p.updateInventory();
							is.setAmount(a.getAmount() + nb);
							inv.setItem(40, is);
							craft.addBlazePowder(nb);
						}
						return;
					} else if (CustomElementManager.FUEL.isParentOf(is) && inv.getItem(0) != null) {
						// Speed
						if (e.getAction() == InventoryAction.PICKUP_ALL) {
							ItemStack a = inv.getItem(4);
							if (a == null || a.getType() == Material.AIR)
								a = CustomElementManager.FUEL.create(0);
							if (a.getAmount() >= a.getType().getMaxStackSize())
								return;
							ItemStack i = is.clone();
							is.setAmount(is.getAmount() - 1);
							p.getInventory().setItem(slot, is.getAmount() > 0 ? is : null);
							p.updateInventory();
							i.setAmount(1 + a.getAmount());
							inv.setItem(4, i);
							craft.addSpeedFuel(1);
						} else if (e.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
							ItemStack a = inv.getItem(4);
							if (a == null || a.getType() == Material.AIR)
								a = CustomElementManager.FUEL.create(0);
							int amount = is.getAmount();
							int max = is.getType().getMaxStackSize() - a.getAmount();
							int nb = Math.min(amount, max);
							ItemStack i = is.clone();
							i.setAmount(amount - nb);
							p.getInventory().setItem(slot, i.getAmount() > 0 ? i : null);
							p.updateInventory();
							is.setAmount(a.getAmount() + nb);
							inv.setItem(4, is);
							craft.addSpeedFuel(nb);
						}
						// Range
						if (e.getAction() == InventoryAction.PICKUP_HALF) {
							ItemStack a = inv.getItem(36);
							if (a == null || a.getType() == Material.AIR)
								a = CustomElementManager.FUEL.create(0);
							if (a.getAmount() >= a.getType().getMaxStackSize())
								return;
							ItemStack i = is.clone();
							is.setAmount(is.getAmount() - 1);
							p.getInventory().setItem(slot, is.getAmount() > 0 ? is : null);
							p.updateInventory();
							i.setAmount(1 + a.getAmount());
							inv.setItem(36, i);
							craft.addRangeFuel(1);
						} else if (e.getAction() == InventoryAction.CLONE_STACK) {
							ItemStack a = inv.getItem(36);
							if (a == null || a.getType() == Material.AIR)
								a = CustomElementManager.FUEL.create(0);
							int amount = is.getAmount();
							int max = is.getType().getMaxStackSize() - a.getAmount();
							int nb = Math.min(amount, max);
							ItemStack i = is.clone();
							i.setAmount(amount - nb);
							p.getInventory().setItem(slot, i.getAmount() > 0 ? i : null);
							p.updateInventory();
							is.setAmount(a.getAmount() + nb);
							inv.setItem(36, is);
							craft.addRangeFuel(nb);
						}
						return;
					}
				}
			}
		}
	}

	private void setDefaultCraftInventory(Inventory inv) {
		inv.setItem(4, craftItemBarrier);
		inv.setItem(22, craftItemBarrier);
		inv.setItem(40, craftItemBarrier);
		inv.setItem(36, craftItemBarrier);
		inv.setItem(26, craftItemBarrier);
		inv.setItem(18, craftItemBarrier); // TODO Exception
		// Items
		inv.setItem(1, craftItem0);
		inv.setItem(2, getCraftItemFlightHeight(null));
		inv.setItem(5, getCraftItemSpeed(null));
		inv.setItem(19, getCraftItemLaserPointer(null));
		inv.setItem(20, getCraftItemDetectorDistance(null));
		inv.setItem(23, getCraftItemExplosionPower(null));
		inv.setItem(25, getCraftItemResult(null));
		inv.setItem(37, getCraftItemRange(null));
		inv.setItem(41, getCraftItemRotatingForce(null));
	}

	private void adaptCraftInventory(Inventory inv, MissileCraftBlock craft) {
		if (inv.getItem(0) != null) {
			// Barriers
			int[] s = { 4, 40, 36, 26, 22 };
			for (int i : s) {
				if (craftItemBarrier.equals(inv.getItem(i)))
					inv.setItem(i, null);
			}
		} else {
			int[] s = { 4, 40, 36, 26, 22 };
			for (int i : s) {
				ItemStack is = inv.getItem(i);
				if (is == null || is.getType() == Material.AIR)
					inv.setItem(i, craftItemBarrier);
			}
		}
		inv.setItem(2, this.getCraftItemFlightHeight(craft));
		inv.setItem(5, this.getCraftItemSpeed(craft));
		inv.setItem(19, this.getCraftItemLaserPointer(craft));
		inv.setItem(20, this.getCraftItemDetectorDistance(craft));
		inv.setItem(23, this.getCraftItemExplosionPower(craft));
		inv.setItem(25, this.getCraftItemResult(craft));
		inv.setItem(37, this.getCraftItemRange(craft));
		inv.setItem(41, this.getCraftItemRotatingForce(craft));
	}

	private ItemStack getCraftItemFlightHeight(MissileCraftBlock craft) {
		ItemStack is = new ItemStack(Material.LADDER);
		ItemMeta im = is.getItemMeta();
		im.setDisplayName(craft == null || craft.getResultMissile() == null ? "§aHauteur de vol"
				: "§aHauteur de vol (" + craft.getFlightHeight() + ")");
		im.setLore(Arrays.asList("§3Clic gauche pour augmenter", "§3Clic droit pour diminuer"));
		is.setItemMeta(im);
		return is;
	}

	private ItemStack getCraftItemDetectorDistance(MissileCraftBlock craft) {
		ItemStack is = new ItemStack(Material.ENDER_EYE);
		ItemMeta im = is.getItemMeta();
		im.setDisplayName(craft == null || craft.getResultMissile() == null ? "§aDistance du détecteur"
				: "§aDistance du détecteur (" + craft.getDetectorDistance() + ")");
		im.setLore(Arrays.asList("§3Clic gauche pour augmenter", "§3Clic droit pour diminuer"));
		is.setItemMeta(im);
		return is;
	}

	private ItemStack getCraftItemSpeed(MissileCraftBlock craft) {
		ItemStack is = new ItemStack(Material.COOKED_RABBIT);
		ItemMeta im = is.getItemMeta();
		im.setDisplayName(craft == null || craft.getResultMissile() == null ? "§aVitesse"
				: "§aVitesse (" + craft.getSpeed() + ")");
		im.setLore(Arrays.asList("§7S'il y n'y a pas d'item à côté,", "§7clic droit (+dupliquer)",
				"§7pour diminuer la vitesse (-10m/s).", "§9S'il y a du carburant à côté,", "§9(+shift) clic gauche",
				"§9pour augmenter la vitesse (+10m/s)."));
		is.setItemMeta(im);
		return is;
	}

	private ItemStack getCraftItemLaserPointer(MissileCraftBlock craft) {
		return craftItemBarrier; // TODO
	}

	private ItemStack getCraftItemExplosionPower(MissileCraftBlock craft) {
		ItemStack is = new ItemStack(Material.FIRE_CHARGE);
		ItemMeta im = is.getItemMeta();
		im.setDisplayName(craft == null || craft.getResultMissile() == null ? "§aCharge explosive"
				: "§aCharge explosive (" + craft.getExplosionPower() + ")");
		im.setLore(Arrays.asList("§7S'il n'y a pas d'item à côté,", "§7clic droit (+dupliquer)",
				"§7pour réduire la charge (-10).", "§9S'il y a de la TNT à coté, ", "§9(+shift) clic gauche",
				"§9pour augmenter la charge."));
		is.setItemMeta(im);
		return is;
	}

	private ItemStack getCraftItemResult(MissileCraftBlock craft) {
		ItemStack is = new ItemStack(Material.CRAFTING_TABLE);
		ItemMeta im = is.getItemMeta();
		im.setDisplayName("§bRésultat");
		if (craft != null && craft.getResultMissile() != null) {
			List<String> lore = new ArrayList<>();
			double radius = craft.getRadius();
			BallisticMissileData r = craft.getResultMissile();
			lore.add("§aPuissance d'explosion : §7" + craft.getExplosionPower());
			lore.add("§aForce de rotation : §7" + craft.getRotatingForce());
			lore.add("§aPortée : §7" + craft.getRange());
			lore.add("§aVitesse : §7" + craft.getSpeed());
			lore.add("§aHauteur de vol : §7" + craft.getFlightHeight());
			lore.add("§aDistance du détecteur : §7" + craft.getDetectorDistance());
			lore.add("§dPoids : §b" + craft.getWeight());
			lore.add("§dRayon : §b" + Math.round(radius));
			lore.add("§dPortée minimale : §b" + Math.round(r.getMinRange()));
			lore.add("§cHauteur max. de lancement : §e" + Math.round(craft.getFlightHeight() - (radius + 2)));
			// Errors
			if (craft.getRange() <= r.getMinRange()) {
				lore.add("§4/!\\ Irrespect §6portée > " + Math.round(r.getMinRange()));
			}
			if (craft.getSpeed() < r.getMinSpeed()) {
				lore.add("§4/!\\ Irrespect §6vitesse ≥ " + Math.round(r.getMinSpeed()));
			}
			if (craft.getRotatingForce() < r.getMinRotatingForce()
					|| craft.getRotatingForce() > r.getMaxRotatingForce()) {
				lore.add("§4/!\\ Irrespect §6" + Math.round(r.getMinRotatingForce()) + " ≤ force ≤ "
						+ Math.round(r.getMaxRotatingForce()));
			}
			im.setLore(lore);
		}
		is.setItemMeta(im);
		return is;
	}

	private ItemStack getCraftItemRange(MissileCraftBlock craft) {
		ItemStack is = new ItemStack(Material.COMPASS);
		ItemMeta im = is.getItemMeta();
		im.setDisplayName(
				craft == null || craft.getResultMissile() == null ? "§aPortée" : "§aPortée (" + craft.getRange() + ")");
		im.setLore(Arrays.asList("§7S'il y n'y a pas d'item à côté,", "§7clic droit (+dupliquer)",
				"§7pour diminuer la portée (-100m).", "§9S'il y a du carburant à côté,", "§9(+shift) clic gauche",
				"§9pour augmenter la portée (+100m)."));
		is.setItemMeta(im);
		return is;
	}

	private ItemStack getCraftItemRotatingForce(MissileCraftBlock craft) {
		ItemStack is = new ItemStack(Material.RABBIT_STEW);
		ItemMeta im = is.getItemMeta();
		im.setDisplayName(craft == null || craft.getResultMissile() == null ? "§aForce de rotation"
				: "§aForce de rotation (" + craft.getRotatingForce() + ")");
		im.setLore(Arrays.asList("§7S'il y n'y a pas d'item à côté,", "§7clic droit (+dupliquer)",
				"§7pour diminuer la force (-50N).", "§9S'il y a de la blaze powder à côté,", "§9(+shift) clic gauche",
				"§9pour augmenter la portée (+50N)."));
		is.setItemMeta(im);
		return is;
	}
}

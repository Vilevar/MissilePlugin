package be.vilevar.missiles.mcelements;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.libs.org.apache.commons.lang3.tuple.Pair;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.util.Vector;

import be.vilevar.missiles.Main;
import be.vilevar.missiles.mcelements.persistantdata.LaserPointerPersistantDataType;
import be.vilevar.missiles.missile.BalisticMissile;
import be.vilevar.missiles.utils.ParticleEffect;

public class CustomElementManager implements Listener {

	public static final Material LASER_POINTER = Material.GLOWSTONE_DUST,
								FUEL = Material.GLASS_BOTTLE,
								BALISTIC_MISSILE = Material.SUGAR;
	public static final Material MISSILE_RADAR = Material.RED_NETHER_BRICKS,
								MISSILE_LAUNCHER = Material.NETHER_QUARTZ_ORE,
								MISSILE_CRAFT = Material.NETHERRACK;
	
	public static final double laserPointerDefaultRange = 100;
	public static final float balisticMissileDefaultExplosionPower = 10;
	public static final double balisticMissileDefaultWeight = 1;
	public static final double balisticMissileDefaultRotatingForce = 100;
	public static final double balisticMissileDefaultRange = 300;
	public static final float balisticMissileDefaultSpeed = 20;
	public static final double balisticMissileDefaultFlightHeight = 200;
	public static final double balisticMissileDefaultDetectorDistance = 0;
	
	
	private ItemStack unusedSlot;
	
	private HashMap<UUID, Pair<Inventory, MissileRadarBlock>> radarInventories = new HashMap<>();
	private String radarInventoryName = "§dParamètres du radar",
			radarRangeStrings[]={"§cS'il n'y a pas d'item à côté,",
					"§cclic droit (+ dupliquer) ici pour diminuer la portée (-50) (min = "+MissileRadarBlock.MIN_RANGE+").",
					"§aS'il y a une boussole à côté,",
					"§a(+ shift) clic gauche ici pour augmenter la portée (+50) (max = "+MissileRadarBlock.MAX_RANGE+")."};
	private ItemStack radarItem0;
	
	private HashMap<UUID, Pair<Inventory, MissileLauncherBlock>> launcherInventories = new HashMap<>();
	private String launcherInventoryName = "§bOptions de lancement";
	private ItemStack launcherItem0, launcherItem10, launcherItem11, launcherItem2, launcherItem3;
	
	private HashMap<UUID, Pair<Inventory, MissileCraftBlock>> craftInventories = new HashMap<>();
	private String craftInventoryName = "§eConstruction de missile";
	private ItemStack craftItemBarrier, craftItem0;
	
	
	public CustomElementManager() {
		this.unusedSlot = new ItemStack(Material.WHITE_STAINED_GLASS_PANE);
		ItemMeta im = this.unusedSlot.getItemMeta();
		im.setDisplayName("§cSlot inutilisé");
		this.unusedSlot.setItemMeta(im);
		
		this.radarItem0 = new ItemStack(Material.PLAYER_HEAD);
		im = this.radarItem0.getItemMeta();
		im.setDisplayName("§a<- Proprétaire du radar");
		im.setLore(Arrays.asList("§6Le joueur dont il y a la tête recevra", "§6des messages lors de repérages de missile"));
		this.radarItem0.setItemMeta(im);
		
		this.launcherItem0 = new ItemStack(Material.ZOMBIE_HEAD);
		im = this.launcherItem0.getItemMeta();
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
		
		this.craftItemBarrier = new ItemStack(Material.BARRIER);
		im = this.craftItemBarrier.getItemMeta();
		im.setDisplayName("§4Indisponible");
		this.craftItemBarrier.setItemMeta(im);
		
		this.craftItem0 = new ItemStack(Material.ARROW);
		im = this.craftItem0.getItemMeta();
		im.setDisplayName("§d<- Missile");
		this.craftItem0.setItemMeta(im);
	}
	
	
	
	@EventHandler
	public void onInteract(PlayerInteractEvent e) {
		if(e.getAction()==Action.RIGHT_CLICK_BLOCK && !e.getPlayer().isSneaking()) {
			if(e.getClickedBlock().getType()==MISSILE_RADAR) {
				MissileRadarBlock radar = MissileRadarBlock.getRadarAt(e.getClickedBlock().getLocation());
				if(radar!=null && !radar.isOpen()) {
					Inventory inv = Bukkit.createInventory(null, 9, this.radarInventoryName);
					inv.setItem(0, radar.getOwnerHead());
					inv.setItem(1, radarItem0);
					for(int i = 2; i < 7; i++) {
						inv.setItem(i, unusedSlot);
					}
					inv.setItem(7, this.createRadarRangeItem(radar));
					radarInventories.put(e.getPlayer().getUniqueId(), Pair.of(inv, radar));
					e.getPlayer().openInventory(inv);
					radar.setOpen(true);
					e.setCancelled(true);
				}
				return;
			}
			if(e.getClickedBlock().getType()==MISSILE_LAUNCHER) {
				MissileLauncherBlock launcher = MissileLauncherBlock.getLauncherAt(e.getClickedBlock().getLocation());
				if(launcher!=null && !launcher.isOpen()) {
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
					launcherInventories.put(e.getPlayer().getUniqueId(), Pair.of(inv, launcher));
					e.getPlayer().openInventory(inv);
					launcher.setOpen(true);
					e.setCancelled(true);
				}
				return;
			}else
			if(e.getClickedBlock().getType()==MISSILE_CRAFT) {
				MissileCraftBlock craft = MissileCraftBlock.getCraftAt(e.getClickedBlock().getLocation());
				if(craft!=null && !craft.isOpen()) {
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
					
					craftInventories.put(e.getPlayer().getUniqueId(), Pair.of(inv, craft));
					e.getPlayer().openInventory(inv);
					craft.setOpen(true);
					e.setCancelled(true);
				}
			}else
			if(e.getClickedBlock().getType()==Material.CHEST) {
				System.out.println(e.getPlayer()+" interact with a chest");
			}
		}
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler
	public void onInventoryClick(InventoryClickEvent e) {
		if(e.getWhoClicked() instanceof Player) {
			Player p = (Player) e.getWhoClicked();
			
			// RADAR
			if(this.radarInventories.containsKey(p.getUniqueId())) {
				Pair<Inventory, MissileRadarBlock> pair = this.radarInventories.get(p.getUniqueId());
				Inventory inv = pair.getLeft();
				MissileRadarBlock radar = pair.getRight();
				ItemStack is = e.getCurrentItem();
				e.setCancelled(true);
				if(inv.equals(e.getClickedInventory())) {
					if(e.getSlot()==0 && is!=null && p.getInventory().firstEmpty()!=-1) {
						inv.setItem(0, null);
						p.getInventory().addItem(is);
						p.updateInventory();
						radar.setOwner(null);
						return;
					}
					if(e.getSlot()==8 && is!=null && p.getInventory().firstEmpty()!=-1) {
						if(e.getAction()==InventoryAction.MOVE_TO_OTHER_INVENTORY || is.getAmount()==1) {
							p.getInventory().addItem(is);
							inv.setItem(8, null);
							p.updateInventory();
							return;
						} else {
							is.setAmount(is.getAmount()-1);
							inv.setItem(8, is);
							is.setAmount(1);
							p.getInventory().addItem(is);
							p.updateInventory();
							return;
						}
					}
					if(e.getSlot()==7) {
						if(e.getAction()==InventoryAction.MOVE_TO_OTHER_INVENTORY && inv.getItem(8)!=null) {
							is = inv.getItem(8);
							int amount = is.getAmount();
							int max = (MissileRadarBlock.MAX_RANGE - radar.getRange()) / 50;
							int nb = Math.min(amount, max);
							is.setAmount(amount - nb);
							inv.setItem(8, is.getAmount()==0 ? null : is);
							radar.setRange(radar.getRange() + (nb * 50));
							inv.setItem(7, this.createRadarRangeItem(radar));
							return;
						} else if(e.getAction()==InventoryAction.PICKUP_ALL && inv.getItem(8)!=null && radar.getRange() < MissileRadarBlock.MAX_RANGE) {
							is = inv.getItem(8);
							is.setAmount(is.getAmount()-1);
							inv.setItem(8, is);
							radar.setRange(radar.getRange()+50);
							inv.setItem(7, this.createRadarRangeItem(radar));
							return;
						} else if(e.getAction()==InventoryAction.CLONE_STACK) {
							is = inv.getItem(8);
							if(is==null) is = new ItemStack(Material.COMPASS, 0);
							int amount = is.getType().getMaxStackSize() - is.getAmount();
							int range = (radar.getRange() - MissileRadarBlock.MIN_RANGE) / 50;
							int nb = Math.min(amount, range);
							is.setAmount(is.getAmount() + nb);
							inv.setItem(8, is);
							radar.setRange(radar.getRange() - (nb * 50));
							inv.setItem(7, this.createRadarRangeItem(radar));
							return;
						} else if(e.getAction()==InventoryAction.PICKUP_HALF && radar.getRange() > MissileRadarBlock.MIN_RANGE) {
							is = inv.getItem(8);
							if(is==null) is = new ItemStack(Material.COMPASS, 0);
							is.setAmount(is.getAmount()+1);
							inv.setItem(8, is);
							radar.setRange(radar.getRange()-50);
							inv.setItem(7, this.createRadarRangeItem(radar));
							return;
						}
					}
				} else if(is!=null) {
					if(is.getType()==Material.PLAYER_HEAD && inv.getItem(0)==null) {
						ItemStack i = is.clone();
						is.setAmount(is.getAmount()-1);
						p.getInventory().setItem(e.getSlot(), is.getAmount() > 0 ? is : null);
						p.updateInventory();
						i.setAmount(1);
						inv.setItem(0, i);
						SkullMeta sm = (SkullMeta) i.getItemMeta();
						radar.setOwner(Bukkit.getPlayer(sm.getOwner()));
						return;
					}
					if(is.getType()==Material.COMPASS) {
						ItemStack a = inv.getItem(8);
						if(a==null) a = new ItemStack(Material.COMPASS, 0);
						if(e.getAction()==InventoryAction.MOVE_TO_OTHER_INVENTORY) {
							int amount = is.getAmount();
							int already = a.getAmount();
							int max = (MissileRadarBlock.MAX_RANGE - radar.getRange()) / 50;
							int nb = Math.min(amount, max-already);
							is.setAmount(amount - nb);
							p.getInventory().setItem(e.getSlot(), is.getAmount() == 0 ? null : is);
							p.updateInventory();
							is.setAmount(nb + already);
							inv.setItem(8, is);
							return;
						} else
						if((radar.getRange() + ((a.getAmount() + 1) * 50)) < MissileRadarBlock.MAX_RANGE) {
							a.setAmount(a.getAmount()+1);
							inv.setItem(8, a);
							is.setAmount(is.getAmount() - 1);
							p.getInventory().setItem(e.getSlot(), is.getAmount() == 0 ? null : is);
							p.updateInventory();
							return;
						}
					}
				}
				return;
			}
			
			// LAUNCHER
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
					if(is.getType()==BALISTIC_MISSILE && inv.getItem(4)==null) {
						ItemStack i = is.clone();
						is.setAmount(is.getAmount()-1);
						p.getInventory().setItem(slot, is.getAmount() > 0 ? is : null);
						p.updateInventory();
						i.setAmount(1);
						inv.setItem(4, i);
						launcher.setMissileData(getBalisticMissileData(i));
						return;
					}
					if(is.getType()==LASER_POINTER && inv.getItem(8)==null) {
						ItemStack i = is.clone();
						is.setAmount(is.getAmount()-1);
						p.getInventory().setItem(slot, is.getAmount() > 0 ? is : null);
						p.updateInventory();
						i.setAmount(1);
						inv.setItem(8, i);
						launcher.setLaserPointer(getLaserPointerData(i));
						return;
					}
				}
				return;
			}
			
			// CRAFT
			if(this.craftInventories.containsKey(p.getUniqueId())) {
				Pair<Inventory, MissileCraftBlock> pair = this.craftInventories.get(p.getUniqueId());
				Inventory inv = pair.getLeft();
				MissileCraftBlock craft = pair.getRight();
				ItemStack is = e.getCurrentItem();
				int slot = e.getSlot();
				e.setCancelled(true);
				if(is==null) return;
				if(inv.equals(e.getClickedInventory())) {
					if(craft.getOriginalMissile()!=null) {
						if(slot==0) {
							if(p.getInventory().firstEmpty()!=-1) {
								BalisticMissileData o = craft.getOriginalMissile();
								if(craft.getExplosionPower() < o.getExplosionPower() || craft.getRange() < o.getRange() ||
										craft.getRotatingForce() < o .getRotatingForce() || craft.getSpeed() < o.getSpeed()) return;
								p.getInventory().addItem(o.toItemStack());
								p.updateInventory();
								inv.setItem(0, null);
								craft.setMissile(null);
								this.adaptCraftInventory(inv, craft);
							}
							return;
						}else
						if(slot==2) {
							// Flight Height
							double f = craft.getFlightHeight();
							if(e.getAction()==InventoryAction.PICKUP_ALL && f < 350) {
								craft.setFlightHeight(f + 1);
							} else if(e.getAction()==InventoryAction.PICKUP_HALF && f > 75) {
								craft.setFlightHeight(f - 1);
							}
							this.adaptCraftInventory(inv, craft);
							return;
						}else
						if(slot==20) {
							// Detector Distance
							double d = craft.getDetectorDistance();
							if(e.getAction()==InventoryAction.PICKUP_ALL && d < 10) {
								craft.setDetectorDistance(d + 1);
							} else if(e.getAction()==InventoryAction.PICKUP_HALF && d > 0) {
								craft.setDetectorDistance(d - 1);
							}
							this.adaptCraftInventory(inv, craft);
							return;
						}else
						if(slot==5) {
							// Speed
							ItemStack a = inv.getItem(4);
							if(a==null || a.getType()==Material.AIR) a = new ItemStack(FUEL, 0);
							if(e.getAction()==InventoryAction.PICKUP_ALL) {
								if(a.getAmount() > 0) {
									a.setAmount(a.getAmount() - 1);
									inv.setItem(4, a);
									craft.addSpeedFuel(-1);
									craft.setSpeed(craft.getSpeed() + 10);
									this.adaptCraftInventory(inv, craft);
								}
							} else
							if(e.getAction()==InventoryAction.MOVE_TO_OTHER_INVENTORY) {
								if(a.getAmount() > 0) {
									inv.setItem(4, null);
									craft.addSpeedFuel(-a.getAmount());
									craft.setSpeed(craft.getSpeed() + 10*a.getAmount());
									this.adaptCraftInventory(inv, craft);
								}
							} else
							if(e.getAction()==InventoryAction.PICKUP_HALF) {
								if((craft.getSpeed()-10) >= Math.max(balisticMissileDefaultSpeed, craft.getResultMissile().getMinSpeed())
										&& a.getAmount() < a.getType().getMaxStackSize()) {
									a.setAmount(a.getAmount()+1);
									inv.setItem(4, a);
									craft.addSpeedFuel(1);
									craft.setSpeed(craft.getSpeed() - 10);
									this.adaptCraftInventory(inv, craft);
								}
							} else
							if(e.getAction()==InventoryAction.CLONE_STACK) {
								int amount = a.getAmount();
								int nb = Math.min(a.getType().getMaxStackSize() - amount, 
										(int) ((craft.getSpeed()-Math.max(balisticMissileDefaultSpeed, craft.getResultMissile().getMinSpeed()))/10));
								a.setAmount(amount + nb);
								inv.setItem(4, a);
								craft.addSpeedFuel(nb);
								craft.setSpeed(craft.getSpeed() - 10*nb);
								this.adaptCraftInventory(inv, craft);
							}
							return;
						}else
						if(slot==37) {
							// Range
							ItemStack a = inv.getItem(36);
							if(a==null || a.getType()==Material.AIR) a = new ItemStack(FUEL, 0);
							if(e.getAction()==InventoryAction.PICKUP_ALL) {
								if(a.getAmount() > 0) {
									a.setAmount(a.getAmount() - 1);
									inv.setItem(36, a);
									craft.addRangeFuel(-1);
									craft.setRange(craft.getRange() + 100);
									this.adaptCraftInventory(inv, craft);
								}
							} else
							if(e.getAction()==InventoryAction.MOVE_TO_OTHER_INVENTORY) {
								if(a.getAmount() > 0) {
									inv.setItem(36, null);
									craft.addRangeFuel(-a.getAmount());
									craft.setRange(craft.getRange() + 100*a.getAmount());
									this.adaptCraftInventory(inv, craft);
								}
							} else
							if(e.getAction()==InventoryAction.PICKUP_HALF) {
								if((craft.getRange()-100) >= Math.max(balisticMissileDefaultRange, craft.getResultMissile().getMinRange())
										&& a.getAmount() < a.getType().getMaxStackSize()) {
									a.setAmount(a.getAmount()+1);
									inv.setItem(36, a);
									craft.addRangeFuel(1);
									craft.setRange(craft.getRange() - 100);
									this.adaptCraftInventory(inv, craft);
								}
							} else
							if(e.getAction()==InventoryAction.CLONE_STACK) {
								int amount = a.getAmount();
								int nb = Math.min(a.getType().getMaxStackSize() - amount, 
										(int) ((craft.getRange()-Math.max(balisticMissileDefaultRange, craft.getResultMissile().getMinRange()))/100));
								a.setAmount(amount + nb);
								inv.setItem(36, a);
								craft.addRangeFuel(nb);
								craft.setRange(craft.getRange() - 100*nb);
								this.adaptCraftInventory(inv, craft);
							}
							return;
						}else
						if(slot==41) {
							// Rotating Force
							ItemStack a = inv.getItem(40);
							if(a==null || a.getType()==Material.AIR) a = new ItemStack(Material.BLAZE_POWDER, 0);
							if(e.getAction()==InventoryAction.PICKUP_ALL) {
								if(a.getAmount() > 0 && (craft.getRotatingForce() + 50) <= craft.getResultMissile().getMaxRotatingForce()) {
									a.setAmount(a.getAmount() - 1);
									inv.setItem(40, a);
									craft.addBlazePowder(-1);
									craft.setRotatingForce(craft.getRotatingForce() + 50);
									this.adaptCraftInventory(inv, craft);
								}
							} else
							if(e.getAction()==InventoryAction.MOVE_TO_OTHER_INVENTORY) {
								int nb = Math.min(a.getAmount(),
										(int) ((craft.getResultMissile().getMaxRotatingForce() - craft.getRotatingForce()) / 50));
								a.setAmount(a.getAmount() - nb);
								inv.setItem(40, a);
								craft.addBlazePowder(-nb);
								craft.setRotatingForce(craft.getRotatingForce() + 50*nb);
								this.adaptCraftInventory(inv, craft);
							} else
							if(e.getAction()==InventoryAction.PICKUP_HALF) {
								if((craft.getRotatingForce()-50) >= 
										Math.max(balisticMissileDefaultRotatingForce, craft.getResultMissile().getMinRotatingForce())
										&& a.getAmount() < a.getType().getMaxStackSize()) {
									a.setAmount(a.getAmount()+1);
									inv.setItem(40, a);
									craft.addBlazePowder(1);
									craft.setRotatingForce(craft.getRotatingForce() - 50);
									this.adaptCraftInventory(inv, craft);
								}
							} else
							if(e.getAction()==InventoryAction.CLONE_STACK) {
								int amount = a.getAmount();
								int nb = Math.min(a.getType().getMaxStackSize() - amount, 
										(int) ((craft.getRotatingForce() - Math.max(balisticMissileDefaultRotatingForce,
												craft.getResultMissile().getMinRotatingForce()))/50));
								a.setAmount(amount + nb);
								inv.setItem(40, a);
								craft.addBlazePowder(nb);
								craft.setRotatingForce(craft.getRotatingForce() - 50*nb);
								this.adaptCraftInventory(inv, craft);
							}
							return;
						}else
						if(slot==23) {
							// Explosion Power
							ItemStack a = inv.getItem(22);
							if(a==null || a.getType()==Material.AIR) a = new ItemStack(Material.TNT, 0);
							if(e.getAction()==InventoryAction.PICKUP_ALL) {
								if(a.getAmount() > 0) {
									a.setAmount(a.getAmount() - 1);
									inv.setItem(22, a);
									craft.addTNT(-1);
									craft.setExplosionPower(craft.getExplosionPower() + 10);
									this.adaptCraftInventory(inv, craft);
								}
							} else
							if(e.getAction()==InventoryAction.MOVE_TO_OTHER_INVENTORY) {
								if(a.getAmount() > 0) {
									inv.setItem(22, null);
									craft.addTNT(-a.getAmount());
									craft.setExplosionPower(craft.getExplosionPower() + 10*a.getAmount());
									this.adaptCraftInventory(inv, craft);
								}
							} else
							if(e.getAction()==InventoryAction.PICKUP_HALF) {
								if((craft.getExplosionPower()-10) >= balisticMissileDefaultExplosionPower 
										&& a.getAmount() < a.getType().getMaxStackSize()) {
									a.setAmount(a.getAmount()+1);
									inv.setItem(22, a);
									craft.addTNT(1);
									craft.setExplosionPower(craft.getExplosionPower() - 10);
									this.adaptCraftInventory(inv, craft);
								}
							} else
							if(e.getAction()==InventoryAction.CLONE_STACK) {
								int amount = a.getAmount();
								int nb = Math.min(a.getType().getMaxStackSize() - amount,
										(int) ((craft.getExplosionPower()-balisticMissileDefaultExplosionPower)/10));
								a.setAmount(amount + nb);
								inv.setItem(22, a);
								craft.addTNT(nb);
								craft.setExplosionPower(craft.getExplosionPower() - 10*nb);
								this.adaptCraftInventory(inv, craft);
							}
							return;
						}else
						if(slot==25) {
							if(is.getItemMeta().getLore().size()==10) {
								inv.setItem(0, null);
								inv.setItem(26, craft.getResultMissile().toItemStack());
								craft.resetOriginalMissile();
								this.adaptCraftInventory(inv, craft);
							}
							return;
						}
					}
					if(slot==0 || slot==1 || slot==2 || slot==5 || slot==20 || slot==37 || slot==41 || slot==23 || slot==25) return;
					if(!is.equals(unusedSlot) && !is.equals(craftItemBarrier) && p.getInventory().firstEmpty()!=-1) {
						if(inv.getItem(0)==null) {
							p.getInventory().addItem(is);
							p.updateInventory();
							inv.setItem(slot, this.craftItemBarrier);
							switch(slot) {
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
						} else if(craft.getOriginalMissile()!=null) {
							boolean move = e.getAction()==InventoryAction.MOVE_TO_OTHER_INVENTORY;
							int max = 0;
							switch(slot) {
							case 22:
								max = craft.getCanTakeTNT();
								if(max > 0)
									craft.addTNT(move ? -max : -1);
								break;
							case 40:
								max = craft.getCanTakeBlazePowder();
								if(max > 0)
									craft.addBlazePowder(move ? -max : -1);
								break;
							case 4:
								max = craft.getCanTakeSpeedFuel();
								if(max > 0)
									craft.addSpeedFuel(move ? -max : -1);
								break;
							case 36:
								max = craft.getCanTakeRangeFuel();
								if(max > 0)
									craft.addRangeFuel(move ? -max : -1);
								break;
							}
							if(max <= 0) return;
							int nb = move ? max : 1;
							ItemStack i = is.clone();
							is.setAmount(is.getAmount()-nb);
							inv.setItem(slot, is);
							i.setAmount(nb);
							p.getInventory().addItem(i);
							p.updateInventory();
							return;
						}
					}
				} else {
					if(is.getType()==BALISTIC_MISSILE && inv.getItem(0)==null && craft.getResultMissile()==null) {
						ItemStack i = is.clone();
						is.setAmount(is.getAmount()-1);
						p.getInventory().setItem(slot, is.getAmount() > 0 ? is : null);
						p.updateInventory();
						i.setAmount(1);
						inv.setItem(0, i);
						craft.setMissile(getBalisticMissileData(i));
						this.adaptCraftInventory(inv, craft);
						return;
					}else
					if(is.getType()==Material.TNT && inv.getItem(0)!=null) {
						if(e.getAction()==InventoryAction.PICKUP_ALL) {
							ItemStack a = inv.getItem(22);
							if(a==null || a.getType()==Material.AIR) a = new ItemStack(Material.TNT, 0);
							if(a.getAmount()>=a.getType().getMaxStackSize()) return;
							ItemStack i = is.clone();
							is.setAmount(is.getAmount()-1);
							p.getInventory().setItem(slot, is.getAmount() > 0 ? is : null);
							p.updateInventory();
							i.setAmount(1+a.getAmount());
							inv.setItem(22, i);
							craft.addTNT(1);
						} else if(e.getAction()==InventoryAction.MOVE_TO_OTHER_INVENTORY) {
							ItemStack a = inv.getItem(22);
							if(a==null || a.getType()==Material.AIR) a = new ItemStack(Material.TNT, 0);
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
					}else
					if(is.getType()==Material.BLAZE_POWDER && inv.getItem(0)!=null) {
						if(e.getAction()==InventoryAction.PICKUP_ALL) {
							ItemStack a = inv.getItem(40);
							if(a==null || a.getType()==Material.AIR) a = new ItemStack(Material.BLAZE_POWDER, 0);
							if(a.getAmount()>=a.getType().getMaxStackSize() ||
									a.getAmount() >= (int) ((craft.getResultMissile().getMaxRotatingForce()-craft.getRotatingForce())/50)) return;
							ItemStack i = is.clone();
							is.setAmount(is.getAmount()-1);
							p.getInventory().setItem(slot, is.getAmount() > 0 ? is : null);
							p.updateInventory();
							i.setAmount(1+a.getAmount());
							inv.setItem(40, i);
							craft.addBlazePowder(1);
						} else if(e.getAction()==InventoryAction.MOVE_TO_OTHER_INVENTORY) {
							ItemStack a = inv.getItem(40);
							if(a==null || a.getType()==Material.AIR) a = new ItemStack(Material.BLAZE_POWDER, 0);
							int amount = is.getAmount();
							int max = Math.min(is.getType().getMaxStackSize() - a.getAmount(),
									(int) ((craft.getResultMissile().getMaxRotatingForce()-craft.getRotatingForce())/50) - a.getAmount());
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
					}else
					if(is.getType()==FUEL && inv.getItem(0)!=null) {
						// Speed
						if(e.getAction()==InventoryAction.PICKUP_ALL) {
							ItemStack a = inv.getItem(4);
							if(a==null || a.getType()==Material.AIR) a = new ItemStack(FUEL, 0);
							if(a.getAmount()>=a.getType().getMaxStackSize()) return;
							ItemStack i = is.clone();
							is.setAmount(is.getAmount()-1);
							p.getInventory().setItem(slot, is.getAmount() > 0 ? is : null);
							p.updateInventory();
							i.setAmount(1+a.getAmount());
							inv.setItem(4, i);
							craft.addSpeedFuel(1);
						} else if(e.getAction()==InventoryAction.MOVE_TO_OTHER_INVENTORY) {
							ItemStack a = inv.getItem(4);
							if(a==null || a.getType()==Material.AIR) a = new ItemStack(FUEL, 0);
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
						if(e.getAction()==InventoryAction.PICKUP_HALF) {
							ItemStack a = inv.getItem(36);
							if(a==null || a.getType()==Material.AIR) a = new ItemStack(FUEL, 0);
							if(a.getAmount()>=a.getType().getMaxStackSize()) return;
							ItemStack i = is.clone();
							is.setAmount(is.getAmount()-1);
							p.getInventory().setItem(slot, is.getAmount() > 0 ? is : null);
							p.updateInventory();
							i.setAmount(1+a.getAmount());
							inv.setItem(36, i);
							craft.addRangeFuel(1);
						} else if(e.getAction()==InventoryAction.CLONE_STACK) {
							ItemStack a = inv.getItem(36);
							if(a==null || a.getType()==Material.AIR) a = new ItemStack(FUEL, 0);
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
	
	@EventHandler
	public void onCloseInventory(InventoryCloseEvent e) {
		if(e.getPlayer() instanceof Player) {
			Player p = (Player) e.getPlayer();
			if(this.radarInventories.containsKey(p.getUniqueId())) {
				Pair<Inventory, MissileRadarBlock> pair = this.radarInventories.get(p.getUniqueId());
				MissileRadarBlock radar = pair.getRight();
				ItemStack compass = pair.getLeft().getItem(8);
				if(compass!=null) radar.getLocation().getWorld().dropItem(radar.getLocation(), compass);
				radar.setOpen(false);
				this.radarInventories.remove(p.getUniqueId());
				return;
			}
			if(this.launcherInventories.containsKey(p.getUniqueId())) {
				Pair<Inventory, MissileLauncherBlock> pair = this.launcherInventories.get(p.getUniqueId());
				MissileLauncherBlock launcher = pair.getRight();
				launcher.setOpen(false);
				this.launcherInventories.remove(p.getUniqueId());
				return;
			}
			if(this.craftInventories.containsKey(p.getUniqueId())) {
				Pair<Inventory, MissileCraftBlock> pair = this.craftInventories.get(p.getUniqueId());
				MissileCraftBlock craft = pair.getRight();
				craft.setOpen(false);
				craft.destroy(false);
				this.craftInventories.remove(p.getUniqueId());
				return;
			}
		}
	}
	
	
	
	
	@EventHandler
	public void onLaserPointer(PlayerInteractEvent e) {
		ItemStack is = e.getItem();
		Player p = e.getPlayer();
		if(is!=null && is.getType()==LASER_POINTER && is.equals(p.getInventory().getItemInMainHand()) && p.isSneaking()) {
			if(e.getAction()==Action.RIGHT_CLICK_BLOCK) {
				int amount = is.getAmount();
				LaserPointerData lp = getLaserPointerData(is);
				lp.setTarget(e.getClickedBlock().getLocation());
				is = lp.toItemStack();
				is.setAmount(amount);
				p.getInventory().setItemInMainHand(is);
				p.updateInventory();
				e.setCancelled(true);
				return;
			} else if(e.getAction()==Action.RIGHT_CLICK_AIR) {
				int amount = is.getAmount();
				LaserPointerData lp = getLaserPointerData(is);
				Location l = p.getEyeLocation();
				Vector v = l.getDirection().normalize();
				a: for(int i = 0; i < lp.getRange(); i++) {
					l.add(v);
					if(l.getBlock().getType().isSolid()) break a;
				}
				lp.setTarget(l);
				is = lp.toItemStack();
				is.setAmount(amount);
				p.getInventory().setItemInMainHand(is);
				p.updateInventory();
				e.setCancelled(true);
				return;
			}
		}
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
	
	@EventHandler
	public void onCommand(AsyncPlayerChatEvent e) {
		Player p = e.getPlayer();
		String msg = e.getMessage();
		String[] args = msg.split(" ");
		ItemStack is = p.getInventory().getItemInMainHand();
		if(args[0].equalsIgnoreCase("%lp")) {
			e.setCancelled(true);
			if(is!=null && is.getType()==LASER_POINTER) {
				if(args.length < 4) {
					p.sendMessage("§c%lp <x> <y> <z>");
					return;
				}
				try {
					double x = Double.parseDouble(args[1]);
					double y = Double.parseDouble(args[2]);
					double z = Double.parseDouble(args[3]);
					int amount = is.getAmount();
					LaserPointerData lp = getLaserPointerData(is);
					lp.setTarget(new Location(p.getWorld(), x, y, z));
					is = lp.toItemStack();
					is.setAmount(amount);
					p.getInventory().setItemInMainHand(is);
					p.updateInventory();
				} catch (Exception ex) {
					p.sendMessage("§cLes coordonnées sont incorrectes.");
				}
			} else {
				p.sendMessage("§cVous n'avez pas de pointeur laser en main.");
			}
			return;
		}
		if(args[0].equalsIgnoreCase("%msg") && p.getGameMode()==GameMode.SURVIVAL) {
			e.setCancelled(true);
			if(args.length<3) {
				p.sendMessage("§c%msg <player> [msg]");
				return;
			}
			Player s = Bukkit.getPlayer(args[1]);
			if(s==null) {
				p.sendMessage("§cJoueur introuvable.");
				return;
			}
			StringBuilder m = new StringBuilder();
			for(int i = 2; i < args.length; i++) {
				if(i!=2) m.append(' ');
				m.append(args[i]);
			}
			s.sendMessage("§6["+p.getName()+" -> vous] §r"+m.toString());
			p.sendMessage("§6[Vous -> "+s.getName()+"] §r"+m.toString());
			return;
		}
		if(args[0].equalsIgnoreCase("%dist")) {
			e.setCancelled(true);
			if(args.length < 3) {
				p.sendMessage("§c%dist <x> <z>");
				return;
			}
			try {
				double x = Double.parseDouble(args[1]);
				double z = Double.parseDouble(args[2]);
				p.sendMessage("§a"+p.getLocation().toVector().setY(0).subtract(new Vector(x, 0, z)).length()+" mètres.");
			} catch (Exception ex) {
				p.sendMessage("§cLes coordonnées sont incorrectes.");
			}
		}
		if(args[0].equalsIgnoreCase("%help")) {
			e.setCancelled(true);
			p.sendMessage("§6%msg <to> [message] §c(uniquement en survie)");
			p.sendMessage("§6%lp <x> <y> <z> §a: configurer le pointer laser");
			p.sendMessage("§6%dist <x> <z>");
		}
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	private ItemStack createRadarRangeItem(MissileRadarBlock radar) {
		ItemStack is = new ItemStack(Material.BOW);
		ItemMeta im = is.getItemMeta();
		im.setDisplayName("§6Portée : §a"+radar.getRange());
		im.setLore(Arrays.asList(radarRangeStrings));
		is.setItemMeta(im);
		return is;
	}
	
	private ItemStack createLauncherConfirmedItem(MissileLauncherBlock launcher) {
		return launcher.isLaunchingConfirmed() ? launcherItem11 : launcherItem10;
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
		if(inv.getItem(0)!=null) {
			// Barriers
			int[] s = {4, 40, 36, 26, 22};
			for(int i : s) {
				if(craftItemBarrier.equals(inv.getItem(i)))
					inv.setItem(i, null);
			}
		} else {
			int[] s = {4, 40, 36, 26, 22};
			for(int i : s) {
				ItemStack is = inv.getItem(i);
				if(is==null || is.getType()==Material.AIR)
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
		im.setDisplayName(craft==null || craft.getResultMissile()==null ? "§aHauteur de vol" : "§aHauteur de vol ("+craft.getFlightHeight()+")");
		im.setLore(Arrays.asList("§3Clic gauche pour augmenter", "§3Clic droit pour diminuer"));
		is.setItemMeta(im);
		return is;
	}
	
	private ItemStack getCraftItemDetectorDistance(MissileCraftBlock craft) {
		ItemStack is = new ItemStack(Material.ENDER_EYE);
		ItemMeta im = is.getItemMeta();
		im.setDisplayName(craft==null||craft.getResultMissile()==null ? "§aDistance du détecteur" : "§aDistance du détecteur ("+craft.getDetectorDistance()+")");
		im.setLore(Arrays.asList("§3Clic gauche pour augmenter", "§3Clic droit pour diminuer"));
		is.setItemMeta(im);
		return is;
	}
	
	private ItemStack getCraftItemSpeed(MissileCraftBlock craft) {
		ItemStack is = new ItemStack(Material.COOKED_RABBIT);
		ItemMeta im = is.getItemMeta();
		im.setDisplayName(craft==null || craft.getResultMissile()==null ? "§aVitesse" : "§aVitesse ("+craft.getSpeed()+")");
		im.setLore(Arrays.asList("§7S'il y n'y a pas d'item à côté,", "§7clic droit (+dupliquer)", "§7pour diminuer la vitesse (-10m/s).",
				"§9S'il y a du carburant à côté,", "§9(+shift) clic gauche", "§9pour augmenter la vitesse (+10m/s)."));
		is.setItemMeta(im);
		return is;
	}
	
	private ItemStack getCraftItemLaserPointer(MissileCraftBlock craft) {
		return craftItemBarrier; //TODO
	}
	
	private ItemStack getCraftItemExplosionPower(MissileCraftBlock craft) {
		ItemStack is = new ItemStack(Material.FIRE_CHARGE);
		ItemMeta im = is.getItemMeta();
		im.setDisplayName(craft==null || craft.getResultMissile()==null ? "§aCharge explosive" : "§aCharge explosive ("+craft.getExplosionPower()+")");
		im.setLore(Arrays.asList("§7S'il n'y a pas d'item à côté,", "§7clic droit (+dupliquer)", "§7pour réduire la charge (-10).",
				"§9S'il y a de la TNT à coté, ", "§9(+shift) clic gauche", "§9pour augmenter la charge."));
		is.setItemMeta(im);
		return is;
	}
	
	private ItemStack getCraftItemResult(MissileCraftBlock craft) {
		ItemStack is = new ItemStack(Material.CRAFTING_TABLE);
		ItemMeta im = is.getItemMeta();
		im.setDisplayName("§bRésultat");
		if(craft!=null && craft.getResultMissile()!=null) {
			List<String> lore = new ArrayList<>();
			double radius = craft.getRadius();
			BalisticMissileData r = craft.getResultMissile();
			lore.add("§aPuissance d'explosion : §7"+craft.getExplosionPower());
			lore.add("§aForce de rotation : §7"+craft.getRotatingForce());
			lore.add("§aPortée : §7"+craft.getRange());
			lore.add("§aVitesse : §7"+craft.getSpeed());
			lore.add("§aHauteur de vol : §7"+craft.getFlightHeight());
			lore.add("§aDistance du détecteur : §7"+craft.getDetectorDistance());
			lore.add("§dPoids : §b"+craft.getWeight());
			lore.add("§dRayon : §b"+Math.round(radius));
			lore.add("§dPortée minimale : §b"+Math.round(r.getMinRange()));
			lore.add("§cHauteur max. de lancement : §e"+Math.round(craft.getFlightHeight() - (radius + 2)));
			// Errors
			if(craft.getRange() <= r.getMinRange()) {
				lore.add("§4/!\\ Irrespect §6portée > "+Math.round(r.getMinRange()));
			}
			if(craft.getSpeed() < r.getMinSpeed()) {
				lore.add("§4/!\\ Irrespect §6vitesse ≥ "+Math.round(r.getMinSpeed()));
			}
			if(craft.getRotatingForce() < r.getMinRotatingForce() || craft.getRotatingForce() > r.getMaxRotatingForce()) {
				lore.add("§4/!\\ Irrespect §6"+Math.round(r.getMinRotatingForce())+" ≤ force ≤ "+Math.round(r.getMaxRotatingForce()));
			}
			im.setLore(lore);
		}
		is.setItemMeta(im);
		return is;
	}
	
	private ItemStack getCraftItemRange(MissileCraftBlock craft) {
		ItemStack is = new ItemStack(Material.COMPASS);
		ItemMeta im = is.getItemMeta();
		im.setDisplayName(craft==null || craft.getResultMissile()==null ? "§aPortée" : "§aPortée ("+craft.getRange()+")");
		im.setLore(Arrays.asList("§7S'il y n'y a pas d'item à côté,", "§7clic droit (+dupliquer)", "§7pour diminuer la portée (-100m).",
				"§9S'il y a du carburant à côté,", "§9(+shift) clic gauche", "§9pour augmenter la portée (+100m)."));
		is.setItemMeta(im);
		return is;
	}
	
	private ItemStack getCraftItemRotatingForce(MissileCraftBlock craft) {
		ItemStack is = new ItemStack(Material.RABBIT_STEW);
		ItemMeta im = is.getItemMeta();
		im.setDisplayName(craft==null || craft.getResultMissile()==null ? "§aForce de rotation" : "§aForce de rotation ("+craft.getRotatingForce()+")");
		im.setLore(Arrays.asList("§7S'il y n'y a pas d'item à côté,", "§7clic droit (+dupliquer)", "§7pour diminuer la force (-50N).",
				"§9S'il y a de la blaze powder à côté,", "§9(+shift) clic gauche", "§9pour augmenter la portée (+50N)."));
		is.setItemMeta(im);
		return is;
	}
	
	
	
	
	
	
	
	
	
	public static void createMissileTrackerBlockScheduler() {
		Bukkit.getScheduler().scheduleSyncRepeatingTask(Main.i, () -> {
			for(MissileRadarBlock radar : MissileRadarBlock.radars) {
				radar.checkMissiles();
			}
		}, 20, 20);
	}
	
	public static LaserPointerData getLaserPointerData(ItemStack is) {
		if(is!=null && is.getType()==LASER_POINTER) {
			ItemMeta im = is.getItemMeta();
			return im.getPersistentDataContainer().getOrDefault(LaserPointerPersistantDataType.LASER_POINTER_KEY,
					LaserPointerPersistantDataType.LASER_POINTER, new LaserPointerData(laserPointerDefaultRange, null));
		}
		return null;
	}
	
	public static BalisticMissileData getBalisticMissileData(ItemStack is) {
		if(is!=null && is.getType()==BALISTIC_MISSILE) {
			ItemMeta im = is.getItemMeta();
			if(im.hasLore() && im.getLore().size()==7) {
				try {
					List<String> lore = im.getLore();
					float explosionPower = Float.parseFloat(lore.get(0));
					double weight = Double.parseDouble(lore.get(1));
					double rotatingForce = Double.parseDouble(lore.get(2));
					double range = Double.parseDouble(lore.get(3));
					float speed = Float.parseFloat(lore.get(4));
					double flightHeight = Double.parseDouble(lore.get(5));
					double detectDist = Double.parseDouble(lore.get(6));
					return new BalisticMissileData(explosionPower, weight, rotatingForce, range, speed, flightHeight, detectDist);
				} catch (Exception e) {}
			}
			return new BalisticMissileData(balisticMissileDefaultExplosionPower, balisticMissileDefaultWeight, balisticMissileDefaultRotatingForce,
					balisticMissileDefaultRange, balisticMissileDefaultSpeed, balisticMissileDefaultFlightHeight, balisticMissileDefaultDetectorDistance);
		}
		return null;
	}
	
	
	
	
	
	public static class LaserPointerData {
		
		private double range;
		private Location target;
		
		public LaserPointerData(double range, Location target) {
			this.range = range;
			this.target = target;
		}
		
		public double getRange() {
			return range;
		}

		public void setRange(double range) {
			this.range = range;
		}

		public Location getTarget() {
			return target;
		}

		public void setTarget(Location target) {
			this.target = target;
		}
		
		public ItemStack toItemStack() {
			ItemStack is = new ItemStack(LASER_POINTER);
			ItemMeta im = is.getItemMeta();
			im.getPersistentDataContainer().set(LaserPointerPersistantDataType.LASER_POINTER_KEY, LaserPointerPersistantDataType.LASER_POINTER, this);
			is.setItemMeta(im);
			return is;
		}
	}
	
	
	
	
	public static class BalisticMissileData implements Cloneable {
		
		private float explosionPower;
		private double weight;
		private double rotatingForce;
		private double range;
		private float speed;
		private double flightHeight;
		private double detectDist;
		
		private double minRotatingForce, maxRotatingForce;
		private double minRange;
		private double minSpeed;
		
		public BalisticMissileData(float explosionPower, double weight, double rotatingForce, double range, float speed, double flightHeight, double detectDist) {
			this.explosionPower = explosionPower;
			this.weight = weight;
			this.rotatingForce = rotatingForce;
			this.range = range;
			this.speed = speed;
			this.flightHeight = flightHeight;
			this.detectDist = detectDist;
			this.setMinRotatingForce();
			this.setMaxRotatingForce();
			this.setMinRange();
			this.setMinSpeed();
		}

		public float getExplosionPower() {
			return explosionPower;
		}

		public void setExplosionPower(float explosionPower) {
			this.explosionPower = explosionPower;
		}

		public double getWeight() {
			return weight;
		}

		public void setWeight(double weight) {
			this.weight = weight;
			this.setMinRotatingForce();
			this.setMaxRotatingForce();
			this.setMinRange();
			this.setMinSpeed();
		}
		
		public double getRotatingForce() {
			return rotatingForce;
		}

		public void setRotatingForce(double rotatingForce) {
			this.rotatingForce = rotatingForce;
			this.setMinRange();
			this.setMinSpeed();
		}

		public double getRange() {
			return range;
		}

		public void setRange(double range) {
			this.range = range;
		}

		public float getSpeed() {
			return speed;
		}

		public void setSpeed(float speed) {
			this.speed = speed;
			this.setMinRotatingForce();
			this.setMaxRotatingForce();
			this.setMinRange();
		}

		public double getFlightHeight() {
			return flightHeight;
		}

		public void setFlightHeight(double flightHeight) {
			this.flightHeight = flightHeight;
			this.setMinRotatingForce();
		}
		
		public double getDetectorDistance() {
			return this.detectDist;
		}
		
		public void setDetectorDistance(double distance) {
			this.detectDist = Math.min(10, Math.max(0, distance));
		}
		

		
		
		public double getMinRotatingForce() {
			return minRotatingForce;
		}

		public double getMaxRotatingForce() {
			return maxRotatingForce;
		}

		public double getMinRange() {
			return minRange;
		}

		public double getMinSpeed() {
			return minSpeed;
		}

		
		
		private void setMinRotatingForce() {
			this.minRotatingForce = (this.weight * this.speed * this.speed) / (flightHeight - 4);
		}

		private void setMaxRotatingForce() {
			this.maxRotatingForce = this.weight * this.speed * this.speed;
		}

		private void setMinRange() {
			double a = 1.1 * this.weight * this.speed * this.speed;
			this.minRange = (a / this.rotatingForce) + (a / (this.rotatingForce + (BalisticMissile.GRAVITY * this.weight)));
		}

		private void setMinSpeed() {
			this.minSpeed = Math.sqrt(this.rotatingForce / this.weight);
		}
		
		
		public BalisticMissile toBalisticMissile(Location loc) {
			return new BalisticMissile(ParticleEffect.FLAME, explosionPower, weight, rotatingForce, range, speed, flightHeight, detectDist, loc);
		}
		
		public ItemStack toItemStack() {
			ItemStack is = new ItemStack(BALISTIC_MISSILE);
			ItemMeta im = is.getItemMeta();
			ArrayList<String> lores = new ArrayList<>();
			lores.add(Float.toString(explosionPower));
			lores.add(Double.toString(weight));
			lores.add(Double.toString(rotatingForce));
			lores.add(Double.toString(range));
			lores.add(Float.toString(speed));
			lores.add(Double.toString(flightHeight));
			lores.add(Double.toString(detectDist));
			im.setLore(lores);
			is.setItemMeta(im);
			return is;
		}
		
		public BalisticMissileData clone() {
			return new BalisticMissileData(explosionPower, weight, rotatingForce, range, speed, flightHeight, this.detectDist);
		}
	}
}

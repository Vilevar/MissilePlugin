package be.vilevar.missiles.mcelements.artillery;

import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.craftbukkit.libs.org.apache.commons.lang3.tuple.Pair;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import be.vilevar.missiles.Main;
import be.vilevar.missiles.WorldManager;
import be.vilevar.missiles.artillery.Shell;
import be.vilevar.missiles.mcelements.CustomElementManager;
import be.vilevar.missiles.mcelements.data.RangefinderData;

public class HowitzerBlockListener implements Listener {

	private HashMap<UUID, Pair<Inventory, Howitzer>> howitzerInventories = new HashMap<>();
	private String howitzerInventoryName = "§3Obusier 155mm";
	private ItemStack unusedSlot;
	private ItemStack fire, cantFire;
	
	private WorldManager wm = Main.i.getWorldManager();
	
	public HowitzerBlockListener(ItemStack unusedSlot) {
		this.unusedSlot = unusedSlot;
		
		this.fire = new ItemStack(Material.GREEN_WOOL);
		ItemMeta im = this.fire.getItemMeta();
		im.setDisplayName("§cFeu !");
		this.fire.setItemMeta(im);
		
		this.cantFire = new ItemStack(Material.RED_WOOL);
		im = this.cantFire.getItemMeta();
		im.setDisplayName("§6Vous ne pouvez ouvrir le feu");
		im.setLore(Arrays.asList("§cIl faut que l'obusier soit chargé", "§cEt que la partie soit commencée (s'il y en a)."));
		this.cantFire.setItemMeta(im);
	}
	
	public HashMap<UUID, Pair<Inventory, Howitzer>> getHowitzerInventories() {
		return howitzerInventories;
	}
	
	public void onClick(Howitzer howitzer, PlayerInteractEvent e) {
//		Block block = e.getClickedBlock();
//		if(CustomElementManager.REMOTE_CONTROL.isParentOf(e.getItem())) {
//			ItemStack is = e.getItem();
//			ItemMeta im = is.getItemMeta();
//			Location loc = block.getLocation();
//			im.setLore(Arrays.asList(loc.getX()+" "+loc.getY()+" "+loc.getZ()));
//			is.setItemMeta(im);
//			Player p = e.getPlayer();
//			p.getInventory().setItem(e.getHand(), is);
//			p.updateInventory();
//			e.setCancelled(true); } else
		if(CustomElementManager.RANGEFINDER.isParentOf(e.getItem())) {
			RangefinderData lp = RangefinderData.getRangefinderData(e.getItem());
			if(lp != null && lp.getTarget() != null) {
				howitzer.adaptForTarget(e.getPlayer(), lp.getTarget());
			}
		} else if(!howitzer.isOpen()) {
			this.openHowitzer(howitzer, e.getPlayer());
			e.setCancelled(true);
		}
	}
	
	public void openHowitzer(Howitzer howitzer, Player p) {
		Inventory inv = Bukkit.createInventory(null, 45, this.howitzerInventoryName);
		for(int i = 0; i < 9; i++) {
			inv.setItem(i, unusedSlot);
			inv.setItem(i+36, unusedSlot);
		}
		for(int i = 1; i < 4; i++) {
			int j = i*9;
			inv.setItem(j, unusedSlot);
			inv.setItem(j + 4, unusedSlot);
			inv.setItem(j + 6, unusedSlot);
			inv.setItem(j + 8, unusedSlot);
		}
		for(int i = 19; i < 22; i++) {
			inv.setItem(i, unusedSlot);
		}
		inv.setItem(14, unusedSlot);
		
		inv.setItem(0, howitzer.getShell() == null ? null : howitzer.getShell().getItemStack().create());
		this.setFireItem(inv, howitzer);
		this.setPitchItems(inv, howitzer);
		this.setYawItems(inv, howitzer);
		
		this.howitzerInventories.put(p.getUniqueId(), Pair.of(inv, howitzer));
		p.openInventory(inv);
		howitzer.setOpen(p);
	}
	
	
	
	@EventHandler
	public void onInventoryClick(InventoryClickEvent e) {
		if(e.getWhoClicked() instanceof Player) {
			Player p = (Player) e.getWhoClicked();
			
			if(this.howitzerInventories.containsKey(p.getUniqueId())) {
				Pair<Inventory, Howitzer> pair = this.howitzerInventories.get(p.getUniqueId());
				Inventory inv = pair.getLeft();
				Howitzer howitzer = pair.getRight();
				ItemStack is = e.getCurrentItem();
				int slot = e.getSlot();
				e.setCancelled(true);
				if (is == null)
					return;
				if(inv.equals(e.getClickedInventory())) {
					// Firing system
					if(slot == 0) {
						if(inv.getItem(0) != null && inv.getItem(0).getType() != Material.AIR && howitzer.getShell() != null) {
							p.getInventory().addItem(inv.getItem(0));
							inv.setItem(0, null);
							p.updateInventory();
							howitzer.setShell(null);
							this.setFireItem(inv, howitzer);
						}
					} else if(slot == 5) {
						if(is.equals(fire) && howitzer.canFire()) {
							howitzer.fire(p);
							inv.setItem(0, null);
						}
						this.setFireItem(inv, howitzer);
					}
					// Yaw
					else if(slot == 10) {
						howitzer.setYawDegrees(-howitzer.getYawDegrees());
						this.setYawItems(inv, howitzer);
					} else if(slot == 11) {
						howitzer.addYawDegrees(e.getAction() == InventoryAction.PICKUP_ALL ? 10 : -10);
						this.setYawItems(inv, howitzer);
					} else if(slot == 12) {
						howitzer.addYawDegrees(e.getAction() == InventoryAction.PICKUP_ALL ? 1 : -1);
						this.setYawItems(inv, howitzer);
					} else if(slot == 28) {
						howitzer.setYawMillirad(-howitzer.getYawMillirad());
						this.setYawItems(inv, howitzer);
					} else if(slot == 29) {
						howitzer.addYawMillirad(e.getAction() == InventoryAction.PICKUP_ALL ? 10 : -10);
						this.setYawItems(inv, howitzer);
					} else if(slot == 30) {
						howitzer.addYawMillirad(e.getAction() == InventoryAction.PICKUP_ALL ? 1 : -1);
						this.setYawItems(inv, howitzer);
					}
					// Pitch
					else if(slot == 23) {
						howitzer.addPitchDegrees(e.getAction() == InventoryAction.PICKUP_ALL ? 10 : -10);
						this.setPitchItems(inv, howitzer);
					} else if(slot == 32) {
						howitzer.addPitchDegrees(e.getAction() == InventoryAction.PICKUP_ALL ? 1 : -1);
						this.setPitchItems(inv, howitzer);
					} else if(slot == 16) {
						howitzer.setPitchMillirad(-howitzer.getPitchMillirad());
						this.setPitchItems(inv, howitzer);
					} else if(slot == 25) {
						howitzer.addPitchMillirad(e.getAction() == InventoryAction.PICKUP_ALL ? 10 : -10);
						this.setPitchItems(inv, howitzer);
					} else if(slot == 34) {
						howitzer.addPitchMillirad(e.getAction() == InventoryAction.PICKUP_ALL ? 1 : -1);
						this.setPitchItems(inv, howitzer);
					}
				} else {
					Shell shell = Shell.fromItem(is);
					if(shell != null && (inv.getItem(0) == null || inv.getItem(0).getType() == Material.AIR) && howitzer.getShell() == null) {
						ItemStack shellIS = is.clone();
						is.setAmount(is.getAmount() - 1);
						p.getInventory().setItem(slot, is);
						p.updateInventory();
						shellIS.setAmount(1);
						inv.setItem(0, shellIS);
						howitzer.setShell(shell);
						this.setFireItem(inv, howitzer);
					}
				}
				
			}
		}
	}
	
	@EventHandler
	public void onForecastWeatherAndControlHowitzer(PlayerInteractEvent e) {
		if(e.getItem() != null && CustomElementManager.WEATHER_FORECASTER.isParentOf(e.getItem()) && e.getAction() == Action.RIGHT_CLICK_AIR) {
			Player p = e.getPlayer();
			if(CustomElementManager.WEATHER_FORECASTER.isParentOf(e.getItem())) {
				p.sendMessage("§6Vent: §a"+Main.round(wm.checkWindSpeed(p.getWorld()))+"§cm/s§6, §a"+Main.round(wm.checkWindAngle())+"§c°");
//			} else if(CustomElementManager.REMOTE_CONTROL.isParentOf(e.getItem())) {
//				ItemStack is = e.getItem();
//				ItemMeta im = is.getItemMeta();
//				if(im.hasLore() && im.getLore().size() == 1) {
//					String location = im.getLore().get(0);
//					String[] coordinates = location.split(" ");
//					Location loc = new Location(p.getWorld(), Double.parseDouble(coordinates[0]), Double.parseDouble(coordinates[1]),
//							Double.parseDouble(coordinates[2]));
//					if(CustomElementManager.HOWITZER.isParentOf(loc.getBlock())) {
//						Howitzer howitzer = Howitzer.getHowitzerAt(loc);
//						if(howitzer != null && !howitzer.isOpen()) {
//							this.openHowitzer(howitzer, e.getPlayer());
//							e.setCancelled(true);
//						}
//					}
//				}
			}
		}
	}
	
	
	private void setFireItem(Inventory inv, Howitzer howitzer) {
		inv.setItem(5, !howitzer.canFire() ? this.cantFire : this.fire);
	}
	
	private void setPitchItems(Inventory inv, Howitzer howitzer) {
		ItemStack degD = new ItemStack(Material.ENDER_PEARL);
		ItemMeta im = degD.getItemMeta();
		im.setDisplayName("§6"+((howitzer.getPitchDegrees() / 10) * 10)+"° Pitch");
		im.setCustomModelData((howitzer.getPitchDegrees() / 10) + 1);
		degD.setItemMeta(im);
		
		ItemStack degU = new ItemStack(Material.ENDER_PEARL);
		im = degU.getItemMeta();
		im.setDisplayName("§6"+(howitzer.getPitchDegrees() % 10)+"° Pitch");
		im.setCustomModelData((howitzer.getPitchDegrees() % 10) + 1);
		degU.setItemMeta(im);
		
		ItemStack mrS = new ItemStack(Material.ENDER_PEARL);
		im = mrS.getItemMeta();
		im.setDisplayName("§6"+(Math.signum(howitzer.getPitchMillirad()) == -1 ? "-" : "+"));
		im.setCustomModelData(Math.signum(howitzer.getPitchMillirad()) == -1 ? 11 : 12);
		mrS.setItemMeta(im);
		
		ItemStack mrD = new ItemStack(Material.ENDER_PEARL);
		im = mrD.getItemMeta();
		im.setDisplayName("§6"+(Math.abs(howitzer.getPitchMillirad() / 10) * 10)+"mr Pitch");
		im.setCustomModelData(Math.abs((howitzer.getPitchMillirad() / 10)) + 1);
		mrD.setItemMeta(im);
		
		ItemStack mrU = new ItemStack(Material.ENDER_PEARL);
		im = mrU.getItemMeta();
		im.setDisplayName("§6"+Math.abs(howitzer.getPitchMillirad() % 10)+"mr Pitch");
		im.setCustomModelData(Math.abs(howitzer.getPitchMillirad() % 10) + 1);
		mrU.setItemMeta(im);
		
		inv.setItem(23, degD);
		inv.setItem(32, degU);
		inv.setItem(16, mrS);
		inv.setItem(25, mrD);
		inv.setItem(34, mrU);
	}
	
	private void setYawItems(Inventory inv, Howitzer howitzer) {
		ItemStack degS = new ItemStack(Material.ENDER_PEARL);
		ItemMeta im = degS.getItemMeta();
		im.setDisplayName("§6"+(Math.signum(howitzer.getYawDegrees()) == -1 ? "-" : "+"));
		im.setCustomModelData(Math.signum(howitzer.getYawDegrees()) == -1 ? 11 : 12);
		degS.setItemMeta(im);
		
		ItemStack degD = new ItemStack(Material.ENDER_PEARL);
		im = degD.getItemMeta();
		im.setDisplayName("§6"+(Math.abs(howitzer.getYawDegrees() / 10) * 10)+"° Yaw");
		im.setCustomModelData(Math.abs(howitzer.getYawDegrees() / 10) + 1);
		degD.setItemMeta(im);
		
		ItemStack degU = new ItemStack(Material.ENDER_PEARL);
		im = degU.getItemMeta();
		im.setDisplayName("§6"+Math.abs(howitzer.getYawDegrees() % 10)+"° Yaw");
		im.setCustomModelData(Math.abs(howitzer.getYawDegrees() % 10) + 1);
		degU.setItemMeta(im);
		
		ItemStack mrS = new ItemStack(Material.ENDER_PEARL);
		im = mrS.getItemMeta();
		im.setDisplayName("§6"+(Math.signum(howitzer.getYawMillirad()) == -1 ? "-" : "+"));
		im.setCustomModelData(Math.signum(howitzer.getYawMillirad()) == -1 ? 11 : 12);
		mrS.setItemMeta(im);
		
		ItemStack mrD = new ItemStack(Material.ENDER_PEARL);
		im = mrD.getItemMeta();
		im.setDisplayName("§6"+(Math.abs(howitzer.getYawMillirad() / 10) * 10)+"mr Yaw");
		im.setCustomModelData(Math.abs(howitzer.getYawMillirad() / 10) + 1);
		mrD.setItemMeta(im);
		
		ItemStack mrU = new ItemStack(Material.ENDER_PEARL);
		im = mrU.getItemMeta();
		im.setDisplayName("§6"+Math.abs(howitzer.getYawMillirad() % 10)+"mr Yaw");
		im.setCustomModelData(Math.abs(howitzer.getYawMillirad() % 10) + 1);
		mrU.setItemMeta(im);
		
		inv.setItem(10, degS);
		inv.setItem(11, degD);
		inv.setItem(12, degU);
		inv.setItem(28, mrS);
		inv.setItem(29, mrD);
		inv.setItem(30, mrU);
	}
	
}

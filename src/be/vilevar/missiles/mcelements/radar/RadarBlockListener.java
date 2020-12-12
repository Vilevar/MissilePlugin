package be.vilevar.missiles.mcelements.radar;

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
import org.bukkit.inventory.meta.SkullMeta;

import be.vilevar.missiles.Main;

public class RadarBlockListener implements Listener {

	private HashMap<UUID, Pair<Inventory, MissileRadarBlock>> radarInventories = new HashMap<>();
	private String	radarInventoryName = "§dParamètres du radar",
					radarRangeStrings[]={"§cS'il n'y a pas d'item à côté,",
							"§cclic droit (+ dupliquer) ici pour diminuer la portée (-50) (min = "+MissileRadarBlock.MIN_RANGE+").",
							"§aS'il y a une boussole à côté,",
							"§a(+ shift) clic gauche ici pour augmenter la portée (+50) (max = "+MissileRadarBlock.MAX_RANGE+")."};
	private ItemStack unusedSlot;
	private ItemStack radarItem0;
	
	
	public RadarBlockListener(ItemStack unusedSlot) {
		this.unusedSlot = unusedSlot;
		
		this.radarItem0 = new ItemStack(Material.PLAYER_HEAD);
		ItemMeta im = this.radarItem0.getItemMeta();
		im.setDisplayName("§a<- Proprétaire du radar");
		im.setLore(Arrays.asList("§6Le joueur dont il y a la tête recevra", "§6des messages lors de repérages de missile"));
		this.radarItem0.setItemMeta(im);
		
		createMissileTrackerBlockScheduler();
	}
	
	public HashMap<UUID, Pair<Inventory, MissileRadarBlock>> getRadarInventories() {
		return radarInventories;
	}
	
	
	public void openRadarInventory(MissileRadarBlock radar, Player p) {
		Inventory inv = Bukkit.createInventory(null, 9, this.radarInventoryName);
		inv.setItem(0, radar.getOwnerHead());
		inv.setItem(1, radarItem0);
		for(int i = 2; i < 7; i++) {
			inv.setItem(i, unusedSlot);
		}
		inv.setItem(7, this.createRadarRangeItem(radar));
		radarInventories.put(p.getUniqueId(), Pair.of(inv, radar));
		p.openInventory(inv);
		radar.setOpen(true);
	}
	
	
	@SuppressWarnings("deprecation")
	@EventHandler
	public void onInventoryClick(InventoryClickEvent e) {
		if(e.getWhoClicked() instanceof Player) {
			Player p = (Player) e.getWhoClicked();
			
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
		
	
	
	
	
	
	public static void createMissileTrackerBlockScheduler() {
		Bukkit.getScheduler().scheduleSyncRepeatingTask(Main.i, () -> {
			for(MissileRadarBlock radar : MissileRadarBlock.radars) {
				radar.checkMissiles();
			}
		}, 60, 20);
	}
}

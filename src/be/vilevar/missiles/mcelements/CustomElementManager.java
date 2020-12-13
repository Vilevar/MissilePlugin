package be.vilevar.missiles.mcelements;

import org.bukkit.Material;
import org.bukkit.craftbukkit.libs.org.apache.commons.lang3.tuple.Pair;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.PluginManager;

import be.vilevar.missiles.Main;
import be.vilevar.missiles.mcelements.crafting.CraftingTableListener;
import be.vilevar.missiles.mcelements.crafting.MissileCraftBlock;
import be.vilevar.missiles.mcelements.launcher.LauncherBlockListener;
import be.vilevar.missiles.mcelements.launcher.MissileLauncherBlock;
import be.vilevar.missiles.mcelements.radar.MissileRadarBlock;
import be.vilevar.missiles.mcelements.radar.RadarBlockListener;
import be.vilevar.missiles.mcelements.weapons.Weapon;
import be.vilevar.missiles.mcelements.weapons.WeaponListener;

public class CustomElementManager implements Listener {

	public static final CustomItem 	LASER_POINTER = new CustomItem(Material.GLOWSTONE_DUST, 1, "Laser pointer"),
									FUEL = new CustomItem(Material.GLOWSTONE_DUST, 2, "Fuel"),
									BALLISTIC_MISSILE = new CustomItem(Material.GLOWSTONE_DUST, 3, "Ballistic missile");
	
	public static final Weapon	SNIPER = new Weapon(new CustomItem(Material.IRON_HOE, 1, "Barrett .50"), 500, 50, 1, 0.f, 20.f, 20., 5, 2, 3.f, 1.f),
								PISTOL = new Weapon(new CustomItem(Material.IRON_HOE, 2, "KB-485"), 2, 25, 1, 3.f, 2.f, 2.5, 0, 0, 0.f, 0.f),
								MACHINE_GUN = new Weapon(new CustomItem(Material.IRON_HOE, 3, "Vityaz-SN"), 3, 5, 1, 8.f, 4.f, 1., 1, 0, 1.f, 0.f),
								SHOTGUN = new Weapon(new CustomItem(Material.IRON_HOE, 4, "CZ-569"), 1, 35, 10, 20.f, 1.f, 17., 0, 1, 2.f, 0.5f);
	
	public static final Material	MISSILE_RADAR = Material.RED_NETHER_BRICKS,
									MISSILE_LAUNCHER = Material.NETHER_QUARTZ_ORE,
									MISSILE_CRAFT = Material.NETHERRACK;
	
	private ItemStack unusedSlot;
	
	private RadarBlockListener radar;
	private LauncherBlockListener launcher;
	private CraftingTableListener craft;
	
	
	public CustomElementManager(Main pl, PluginManager pm) {
		this.unusedSlot = new ItemStack(Material.WHITE_STAINED_GLASS_PANE);
		ItemMeta im = this.unusedSlot.getItemMeta();
		im.setDisplayName("§cSlot inutilisé");
		this.unusedSlot.setItemMeta(im);
		
		this.radar = new RadarBlockListener(this.unusedSlot);
		pm.registerEvents(this.radar, pl);
		
		this.launcher = new LauncherBlockListener(this.unusedSlot);
		pm.registerEvents(this.launcher, pl);
		
		this.craft = new CraftingTableListener(this.unusedSlot);
		pm.registerEvents(this.craft, pl);
		
		pm.registerEvents(new LaserPointerListener(), pl);
		pm.registerEvents(new WeaponListener(), pl);
	}
	
	
	
	@EventHandler
	public void onInteract(PlayerInteractEvent e) {
		if(e.getAction() == Action.RIGHT_CLICK_BLOCK && !e.getPlayer().isSneaking()) {
			if(e.getClickedBlock().getType() == MISSILE_RADAR) {
				MissileRadarBlock radar = MissileRadarBlock.getRadarAt(e.getClickedBlock().getLocation());
				if(radar!=null && !radar.isOpen()) {
					this.radar.openRadarInventory(radar, e.getPlayer());
					e.setCancelled(true);
				}
				return;
			}else
			if(e.getClickedBlock().getType() == MISSILE_LAUNCHER) {
				MissileLauncherBlock launcher = MissileLauncherBlock.getLauncherAt(e.getClickedBlock().getLocation());
				if(launcher!=null && !launcher.isOpen()) {
					this.launcher.openLauncherInventory(launcher, e.getPlayer());
					e.setCancelled(true);
				}
				return;
			}else
			if(e.getClickedBlock().getType() == MISSILE_CRAFT) {
				MissileCraftBlock craft = MissileCraftBlock.getCraftAt(e.getClickedBlock().getLocation());
				if(craft != null && !craft.isOpen()) {
					this.craft.openCraftTable(craft, e.getPlayer());
					e.setCancelled(true);
				}
			}else
			if(e.getClickedBlock().getType()==Material.CHEST) {
				System.out.println(e.getPlayer()+" interact with a chest");
			}
		}
	}
	
	@EventHandler
	public void onCloseInventory(InventoryCloseEvent e) {
		if(e.getPlayer() instanceof Player) {
			Player p = (Player) e.getPlayer();
			if(this.radar.getRadarInventories().containsKey(p.getUniqueId())) {
				Pair<Inventory, MissileRadarBlock> pair = this.radar.getRadarInventories().get(p.getUniqueId());
				MissileRadarBlock radar = pair.getRight();
				ItemStack compass = pair.getLeft().getItem(8);
				if(compass != null) radar.getLocation().getWorld().dropItem(radar.getLocation(), compass);
				radar.setOpen(false);
				this.radar.getRadarInventories().remove(p.getUniqueId());
				return;
			}else
			if(this.launcher.getLauncherInventories().containsKey(p.getUniqueId())) {
				Pair<Inventory, MissileLauncherBlock> pair = this.launcher.getLauncherInventories().get(p.getUniqueId());
				MissileLauncherBlock launcher = pair.getRight();
				launcher.setOpen(false);
				this.launcher.getLauncherInventories().remove(p.getUniqueId());
				return;
			}else
			if(this.craft.getCraftInventories().containsKey(p.getUniqueId())) {
				Pair<Inventory, MissileCraftBlock> pair = this.craft.getCraftInventories().get(p.getUniqueId());
				MissileCraftBlock craft = pair.getRight();
				craft.setOpen(false);
				craft.destroy(false);
				this.craft.getCraftInventories().remove(p.getUniqueId());
				return;
			}
		}
	}
	
	
}

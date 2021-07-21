package be.vilevar.missiles.mcelements;

import java.util.ArrayList;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.libs.org.apache.commons.lang3.tuple.Pair;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.PluginManager;

import be.vilevar.missiles.Main;
import be.vilevar.missiles.mcelements.abm.ABMLBlockListener;
import be.vilevar.missiles.mcelements.abm.ABMLauncher;
import be.vilevar.missiles.mcelements.artillery.Howitzer;
import be.vilevar.missiles.mcelements.artillery.HowitzerBlockListener;
import be.vilevar.missiles.mcelements.crafting.MissileCraftBlock;
import be.vilevar.missiles.mcelements.crafting.MissileCraftingTableListener;
import be.vilevar.missiles.mcelements.crafting.RVCraftBlock;
import be.vilevar.missiles.mcelements.crafting.RVCraftingTableListener;
import be.vilevar.missiles.mcelements.launcher.LauncherBlockListener;
import be.vilevar.missiles.mcelements.launcher.MissileLauncherBlock;
import be.vilevar.missiles.mcelements.merchant.MerchantListener;
import be.vilevar.missiles.mcelements.radar.Radar;
import be.vilevar.missiles.mcelements.radar.RadarBlockListener;
import be.vilevar.missiles.mcelements.weapons.Weapon;
import be.vilevar.missiles.mcelements.weapons.WeaponListener;

public class CustomElementManager implements Listener {

	public static final CustomItem 	RANGEFINDER = new CustomItem(Material.GLOWSTONE_DUST, 1, "Rangefinder");
	
	public static final CustomItem	ENGINE_1 = new CustomItem(Material.GLOWSTONE_DUST, 10, "Engine 1"),
									ENGINE_2 = new CustomItem(Material.GLOWSTONE_DUST, 11, "Engine 2"),
									ENGINE_3 = new CustomItem(Material.GLOWSTONE_DUST, 12, "Engine 3");
	
	public static final CustomItem	FUEL_1 = new CustomItem(Material.GLOWSTONE_DUST, 15, "Fuel 1"),
									FUEL_2 = new CustomItem(Material.GLOWSTONE_DUST, 16, "Fuel 2"),
									FUEL_3 = new CustomItem(Material.GLOWSTONE_DUST, 17, "Fuel 3");
	
	public static final CustomItem	SRBM = new CustomItem(Material.GLOWSTONE_DUST, 20, "SRBM"),
									MRBM = new CustomItem(Material.GLOWSTONE_DUST, 21, "MRBM"),
									ICBM = new CustomItem(Material.GLOWSTONE_DUST, 22, "ICBM"),
									ABM = new CustomItem(Material.GLOWSTONE_DUST, 23, "ABM");
	
	public static final CustomItem	REENTRY_VEHICLE = new CustomItem(Material.GLOWSTONE_DUST, 25, "Reentry Vehicle"),
									A_BOMB = new CustomItem(Material.GLOWSTONE_DUST, 26, "A-Bomb"),
									H_BOMB = new CustomItem(Material.GLOWSTONE_DUST, 27, "H-Bomb"),
									MIRV = new CustomItem(Material.GLOWSTONE_DUST, 28, "MIRV");
	
	public static final Weapon	SNIPER = new Weapon(
										new CustomItem(Material.IRON_HOE, 1, "Barrett .50"), new CustomItem(Material.GLOWSTONE_DUST, 4, "Barrett Ammo"),
										0.03, 4., -2.9,
										500, 50, 4000, 4500,
										1, 100.f, 0.f, 20.f,
										1.5, 0, 5, 2,
										5.f, 1.f,
										Sound.ENTITY_BLAZE_DEATH, 2.f, 1.f),
								PISTOL = new Weapon(
										new CustomItem(Material.IRON_HOE, 2, "KB-485"), new CustomItem(Material.GLOWSTONE_DUST, 5, "Pistol Ammo"),
										0., 1., 1.,
										2, 25, 500, 500,
										1, 3.f, .5f, 2.f,
										2.5, 0, 0, 0,
										0.f, 0.f,
										Sound.ENTITY_ENDER_DRAGON_HURT, 0.5f, 1.f),
								MACHINE_GUN = new Weapon(
										new CustomItem(Material.IRON_HOE, 3, "Vityaz-SN"), new CustomItem(Material.GLOWSTONE_DUST, 6, "Vityaz-SN Ammo"),
										0.01, 2., -2.,
										3, 5, 0, 4000,
										1, 8.f, 5.f, 4.f,
										2.5, 0, 0, 0,
										0.f, 0.25f,
										Sound.ENTITY_EVOKER_FANGS_ATTACK, 1.f, 1.f),
								SHOTGUN = new Weapon(
										new CustomItem(Material.IRON_HOE, 4, "CZ-569"), new CustomItem(Material.GLOWSTONE_DUST, 7, "CZ-569 Ammo"),
										0.02, 3.5, -0.5,
										1, 35, 1000, 5000,
										10, 20.f, 15.f, 1.f,
										15., 50, 1, 0,
										3.f, 0.5f,
										Sound.ENTITY_GHAST_DEATH, 1.f, 1.f);
	
	public static final CustomBlock	MINE = new CustomBlock(Material.CRIMSON_PRESSURE_PLATE, "Mine");
	public static final CustomBlock	CLAYMORE = new CustomBlock(Material.STONE_BUTTON, "Claymore");
	
	public static final CustomItem	BOMB = new CustomItem(Material.SNOWBALL, 1, "Bomb"),
									SMOKE_BOMB = new CustomItem(Material.SNOWBALL, 2, "Smoke Bomb");
	
	public static final CustomBlock	MISSILE_LAUNCHER = new CustomBlock(Material.NETHER_QUARTZ_ORE, "Missile Launcher"),
									MISSILE_CRAFT = new CustomBlock(Material.NETHERRACK, "Missile Crafter"),
									RV_CRAFT = new CustomBlock(Material.NETHER_GOLD_ORE, "Reentry Vehicle Crafter"),
									RADAR = new CustomBlock(Material.RED_NETHER_BRICKS, "Radar"),
									ABM_LAUNCHER = new CustomBlock(Material.BLACKSTONE, "ABM Launcher");
	
	public static final CustomBlock HOWITZER = new CustomBlock(Material.NETHER_BRICK_STAIRS, "Howitzer");
	
	public static final CustomItem 	SMALL_SHELL = new CustomItem(Material.GLOWSTONE_DUST, 8, "Small Shell"),
									BIG_SHELL = new CustomItem(Material.GLOWSTONE_DUST, 9, "Big Shell");
//									REMOTE_CONTROL = new CustomItem(Material.IRON_HOE, 5, "Howitzer Remote Control");
	
	public static final CustomItem 	WEATHER_FORECASTER = new CustomItem(Material.IRON_HOE, 6, "Weather Forecaster");
	
	private Main main;
	
	private ItemStack unusedSlot;
	
	private LauncherBlockListener launcher;
	private MissileCraftingTableListener craft;
	private RVCraftingTableListener mirv;
	private HowitzerBlockListener howitzer;
	private RadarBlockListener radar;
	private ABMLBlockListener abm;
	
	private WeaponListener weapons;
	
	private ArrayList<UUID> ids = new ArrayList<>();
	
	
	public CustomElementManager(Main pl, PluginManager pm) {
		this.main = pl;
		
		this.unusedSlot = new ItemStack(Material.WHITE_STAINED_GLASS_PANE);
		ItemMeta im = this.unusedSlot.getItemMeta();
		im.setDisplayName("§cSlot inutilisé");
		this.unusedSlot.setItemMeta(im);
		
		this.launcher = new LauncherBlockListener(this.unusedSlot);
		pm.registerEvents(this.launcher, pl);
		
		this.craft = new MissileCraftingTableListener(this.unusedSlot);
		pm.registerEvents(this.craft, pl);
		
		this.mirv = new RVCraftingTableListener(this.unusedSlot);
		pm.registerEvents(this.mirv, pl);
		
		this.howitzer = new HowitzerBlockListener(this.unusedSlot);
		pm.registerEvents(this.howitzer, pl);
		
		this.radar = new RadarBlockListener(this.unusedSlot);
		pm.registerEvents(this.radar, pl);
		
		this.abm = new ABMLBlockListener(this.unusedSlot);
		pm.registerEvents(this.abm, pl);
		
		pm.registerEvents(new RangefinderListener(), pl);
		
		this.weapons = new WeaponListener();
		pm.registerEvents(this.weapons, pl);
		
		pm.registerEvents(new MerchantListener(), pl);
	}
	
	
	@EventHandler
	public void onPlace(BlockPlaceEvent e) {
		Block block = e.getBlock();
		if (CustomElementManager.MISSILE_LAUNCHER.isParentOf(block)) {
			MissileLauncherBlock.launchers.add(new MissileLauncherBlock(block.getLocation()));
		} else
		if (CustomElementManager.MISSILE_CRAFT.isParentOf(block)) {
			MissileCraftBlock.crafts.add(new MissileCraftBlock(block.getLocation()));
		} else
		if(CustomElementManager.RV_CRAFT.isParentOf(block)) {
			RVCraftBlock.crafts.add(new RVCraftBlock(block.getLocation()));
		} else
		if(CustomElementManager.HOWITZER.isParentOf(block)) {
			Howitzer.howitzers.add(new Howitzer(block));
		} else
		if(CustomElementManager.RADAR.isParentOf(block)) {
			Radar.radars.add(new Radar(block.getLocation(), this.main.getDefender(e.getPlayer())));
		} else
		if(CustomElementManager.ABM_LAUNCHER.isParentOf(block)) {
			ABMLauncher.launchers.add(new ABMLauncher(block.getLocation(), this.main.getDefender(e.getPlayer())));
		}
	}

	@EventHandler
	public void onBreak(BlockBreakEvent e) {
		this.blockBreak(e.getBlock());
	}

	@EventHandler
	public void onExplosionByEntity(EntityExplodeEvent e) {
		e.blockList().forEach(block -> this.blockBreak(block));
	}
	
	@EventHandler
	public void onExplosionByBlock(BlockExplodeEvent e) {
		e.blockList().forEach(block -> this.blockBreak(block));
	}

	private void blockBreak(Block block) {
		if (MISSILE_LAUNCHER.isParentOf(block)) {
			MissileLauncherBlock.checkDestroy(block.getLocation());
		} else
		if (MISSILE_CRAFT.isParentOf(block)) {
			MissileCraftBlock.checkDestroy(block.getLocation());
		} else
		if(RV_CRAFT.isParentOf(block)) {
			RVCraftBlock.checkDestroy(block.getLocation());
		} else
		if(HOWITZER.isParentOf(block)) {
			Howitzer.checkDestroy(block.getLocation());
		} else
		if(RADAR.isParentOf(block)) {
			Radar.checkDestroy(block.getLocation());
		} else
		if(ABM_LAUNCHER.isParentOf(block)) {
			ABMLauncher.checkDestroy(block.getLocation());
		}
	}
	
	
	@EventHandler
	public void onInteract(PlayerInteractEvent e) {
		Block block = e.getClickedBlock();
		if(e.getAction() == Action.RIGHT_CLICK_BLOCK && !e.getPlayer().isSneaking() && !ids.contains(e.getPlayer().getUniqueId())) {
			ids.add(e.getPlayer().getUniqueId());
			main.getServer().getScheduler().runTaskLater(main, () -> ids.remove(e.getPlayer().getUniqueId()), 1);
			if(MISSILE_LAUNCHER.isParentOf(block)) {
				MissileLauncherBlock launcher = MissileLauncherBlock.getLauncherAt(block.getLocation());
				if(launcher!=null && !launcher.isOpen()) {
					this.launcher.openLauncherInventory(launcher, e.getPlayer());
					e.setCancelled(true);
				}
				return;
			} else
			if(MISSILE_CRAFT.isParentOf(block)) {
				MissileCraftBlock craft = MissileCraftBlock.getCraftAt(block.getLocation());
				if(craft != null && !craft.isOpen()) {
					this.craft.openCraftTable(craft, e.getPlayer());
					e.setCancelled(true);
				}
			} else 
			if(RV_CRAFT.isParentOf(block)) {
				RVCraftBlock craft = RVCraftBlock.getCraftAt(block.getLocation());
				if(craft != null && !craft.isOpen()) {
					this.mirv.openCraftTable(craft, e.getPlayer());
					e.setCancelled(true);
				}
			} else
			if(HOWITZER.isParentOf(block)) {
				Howitzer howitzer = Howitzer.getHowitzerAt(block.getLocation());
				if(howitzer != null && !howitzer.isOpen()) {
					this.howitzer.onClick(howitzer, e);
				}
			} else
			if(RADAR.isParentOf(block)) {
				Radar radar = Radar.getRadarAt(block.getLocation());
				if(radar != null && !radar.isOpen()) {
					this.radar.openRadarInventory(radar, e.getPlayer());
				}
			} else
			if(ABM_LAUNCHER.isParentOf(block)) {
				ABMLauncher launcher = ABMLauncher.getLauncherAt(block.getLocation());
				if(launcher != null && launcher.isOpen()) {
					this.abm.openLauncherInventory(launcher, e.getPlayer());
				}
			}
		}
	}
	
	@EventHandler
	public void onCloseInventory(InventoryCloseEvent e) {
		if(e.getPlayer() instanceof Player) {
			Player p = (Player) e.getPlayer();
			UUID id = p.getUniqueId();
			if(this.launcher.getLauncherInventories().containsKey(id)) {
				Pair<Inventory, MissileLauncherBlock> pair = this.launcher.getLauncherInventories().get(id);
				MissileLauncherBlock launcher = pair.getRight();
				launcher.setOpen(null);
				this.launcher.getLauncherInventories().remove(id);
				return;
			} else
			if(this.craft.getCraftInventories().containsKey(id)) {
				Pair<Inventory, MissileCraftBlock> pair = this.craft.getCraftInventories().get(p.getUniqueId());
				MissileCraftBlock craft = pair.getRight();
				craft.setOpen(null);
				this.craft.getCraftInventories().remove(id);
				return;
			} else
			if(this.mirv.getCraftInventories().containsKey(id)) {
				Pair<Inventory, RVCraftBlock> pair = this.mirv.getCraftInventories().get(id);
				RVCraftBlock craft = pair.getRight();
				craft.setOpen(null);
				this.mirv.getCraftInventories().remove(id);
				return;
			} else
			if(this.howitzer.getHowitzerInventories().containsKey(id)) {
				Pair<Inventory, Howitzer> pair = this.howitzer.getHowitzerInventories().get(id);
				Howitzer howitzer = pair.getRight();
				howitzer.setOpen(null);
				this.howitzer.getHowitzerInventories().remove(id);
				return;
			} else
			if(this.radar.getRadarInventories().containsKey(id)) {
				Pair<Inventory, Radar> pair = this.radar.getRadarInventories().get(id);
				Radar radar = pair.getRight();
				radar.setOpen(null);
				this.radar.getRadarInventories().remove(id);
				return;
			} else
			if(this.abm.getLauncherInventories().containsKey(id)) {
				Pair<Inventory, ABMLauncher> pair = this.abm.getLauncherInventories().get(id);
				ABMLauncher launcher = pair.getRight();
				launcher.setOpen(null);
				this.abm.getLauncherInventories().remove(id);
				return;
			}
		}
	}
	
	public WeaponListener getWeapons() {
		return weapons;
	}
}

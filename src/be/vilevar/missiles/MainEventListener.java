package be.vilevar.missiles;

import java.util.Random;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import be.vilevar.missiles.mcelements.CustomElementManager;

public class MainEventListener implements Listener {
	
	
	
	@EventHandler
	public void onDeath(PlayerDeathEvent e) {
		Player p = e.getEntity();
		if (p.getKiller() != null) {
			try {
				e.setDeathMessage(e.getDeathMessage() + " (" + p.getLocation().distance(p.getKiller().getLocation()) + "m)");
			} catch (Exception exp) {
			}
		}
	}
	

	@EventHandler(priority = EventPriority.MONITOR)
	public void onBlockPhysics(BlockFadeEvent e) {
		if (e.getBlock().getType() == Material.ICE || e.getBlock().getType() == Material.SNOW) {
			e.setCancelled(true);
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onBlockBreakIce(BlockBreakEvent e) {
		if (e.getBlock().getType() == Material.ICE || e.getBlock().getType() == Material.BLUE_ICE || e.getBlock().getType() == Material.FROSTED_ICE) {
			e.getBlock().setType(Material.AIR);
		}
	}
	
	
	
	// Cheat trick
	
	@EventHandler
	public void onClick(PlayerInteractEvent e) {
		Player p = e.getPlayer();
		if(p.getGameMode() != GameMode.CREATIVE)
			return;
		// Getting the plug-in items
		if (e.getMaterial() == Material.BOOK) {
			// Part 1
			if(e.getAction() == Action.RIGHT_CLICK_AIR) {
				p.getInventory().addItem(
						CustomElementManager.A_BOMB.create(), CustomElementManager.ABM.create(), CustomElementManager.ABM_LAUNCHER.create(),
						CustomElementManager.SHELL.create(), CustomElementManager.BOMB.create(), CustomElementManager.CLAYMORE.create(),
						CustomElementManager.E_BOMB.create(), CustomElementManager.ENGINE_1.create(), CustomElementManager.ENGINE_2.create(),
						CustomElementManager.ENGINE_3.create(), CustomElementManager.FUEL_1.create(), CustomElementManager.FUEL_2.create(),
						CustomElementManager.FUEL_3.create(), CustomElementManager.H_BOMB.create(), CustomElementManager.HOWITZER.create(),
						CustomElementManager.ICBM.create(), CustomElementManager.MACHINE_GUN.createItem(),
						CustomElementManager.MACHINE_GUN.getAmmunition().create(), CustomElementManager.MINE.create(),
						CustomElementManager.MIRV.create(), CustomElementManager.MISSILE_CRAFT.create(),
						CustomElementManager.MISSILE_LAUNCHER.create(), CustomElementManager.MRBM.create());
	//					CustomElementManager.REMOTE_CONTROL.create());
			// Part 2
			} else {
				p.getInventory().addItem(
						CustomElementManager.PISTOL.createItem(), CustomElementManager.PISTOL.getAmmunition().create(),
						CustomElementManager.RADAR.create(), CustomElementManager.RANGEFINDER.create(), 
						CustomElementManager.REENTRY_VEHICLE.create(), CustomElementManager.RV_CRAFT.create(),
						CustomElementManager.SHOTGUN.createItem(), CustomElementManager.SHOTGUN.getAmmunition().create(),
						CustomElementManager.EXPLOSIVE_SHELL.create(), CustomElementManager.PERFORATING_SHELL.create(),
						CustomElementManager.INCENDIARY_SHELL.create(), CustomElementManager.SMOKE_BOMB.create(),
						CustomElementManager.SNIPER.createItem(), CustomElementManager.SNIPER.getAmmunition().create(),
						CustomElementManager.SRBM.create(), CustomElementManager.WEATHER_FORECASTER.create(),
						CustomElementManager.PLIERS.create());
			}
		}
		
		
		// Removing entities and tile entities
//		if (e.getMaterial() == Material.STICK && e.getAction() == Action.LEFT_CLICK_AIR) {
//			Set<EntityType> ents = new HashSet<>();
//			List<Entity> entities = p.getWorld().getEntities();
//			p.sendMessage("N entities = "+entities.size());
//			for(Entity ent : e.getPlayer().getWorld().getEntities()) {
//				if(ents.add(ent.getType())) {
//					p.sendMessage(ent.getType()+" "+ent.getLocation());
//				}
//				if(ent.getType() != EntityType.PLAYER)
//					ent.remove();
//			}
//			
//			p.sendMessage("Tiles :");
//			Set<Material> mat = new HashSet<>();
//			for(Chunk c : p.getWorld().getLoadedChunks()) {
//				for(BlockState bs : c.getTileEntities()) {
//					if(mat.add(bs.getType())) {
//						p.sendMessage(bs.getType()+" "+bs.getLocation());
//					}
//					bs.getBlock().setType(Material.AIR);
//				}
//			}
//			
//			p.sendMessage("Custom blocks :");
//			for(Chunk c : p.getWorld().getLoadedChunks()) {
//				for(int x = 0; x < 16; x++) {
//					for(int y = 0; y < 256; y++) {
//						for(int z = 0; z < 16; z++) {
//							Block block = c.getBlock(x, y, z);
//							if(CustomElementManager.isCustomBlock(block)) {
//								Location loc = block.getLocation();
//								p.sendMessage("§6Custom §e"+block.getType()+"§6 at : §c"+loc.getBlockX()+"/"+loc.getBlockY()+"/"+loc.getBlockZ());
//							}
//						}
//					}
//				}
//			}
//		}
	}

	
	public static void trackSoundOrigin(Location soundOrigin, double volume, Player origin) {
		if(volume < 0.5) {
			return;
		}
		Main main = Main.i;
		Random random = new Random();
		for(Player p : main.getServer().getOnlinePlayers()) {
			if(!p.getUniqueId().equals(origin.getUniqueId())) {
				double distance = soundOrigin.distance(p.getLocation());
				main.getServer().getScheduler().runTaskLater(main, () -> {
					double angle = random.nextDouble() * 2 * Math.PI;
					double error = distance * distance / (volume * 500.);
					if(error <= 100) {
						Location target = soundOrigin.clone().add(error * Math.cos(angle), error * Math.sin(angle), 0);
						p.setCompassTarget(target);
						if(p.getInventory().getItemInMainHand().getType() == Material.COMPASS) {
							p.sendMessage("§9[Contre-batterie]§6 Un son de tir a été détecté à approximativement §c" 
									+ target.distance(p.getLocation())+"m§6.");
						}
					}
					
				}, (int) (distance / 100 * 20));
			}
		}
	}
	
	
	// Debug
	
	// TODO Add
//	@EventHandler
//	public void onload(ChunkLoadEvent e) {
//		Chunk c = e.getChunk();
//		
//		for(BlockState state : c.getTileEntities()) {
////			state.getBlock().setType(Material.AIR);
//			System.out.println(state.getBlock().getType());
//		}
//		
//		for(Entity ent : c.getEntities()) {
//			if(ent.getType() != EntityType.PLAYER) {
////				ent.remove();
//				System.out.println(ent.getType());
//			}
//		}
//	}
}

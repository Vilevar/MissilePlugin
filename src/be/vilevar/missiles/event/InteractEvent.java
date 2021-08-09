package be.vilevar.missiles.event;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Chunk;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import be.vilevar.missiles.Main;
import be.vilevar.missiles.mcelements.CustomElementManager;

public class InteractEvent implements Listener {

    private Main main;

    public InteractEvent(Main main) {
        this.main = main;
    }

    @EventHandler
	public void onClick(PlayerInteractEvent e) {
		if (e.getItem() != null && e.getItem().getType() == Material.BOOK && e.getPlayer().getGameMode() == GameMode.CREATIVE) {
			e.getPlayer().getInventory().addItem(
					CustomElementManager.SNIPER.createItem(), CustomElementManager.SNIPER.getAmmunition().create(),
					CustomElementManager.PISTOL.createItem(), CustomElementManager.PISTOL.getAmmunition().create(),
					CustomElementManager.MACHINE_GUN.createItem(), CustomElementManager.MACHINE_GUN.getAmmunition().create(),
					CustomElementManager.SHOTGUN.createItem(), CustomElementManager.SHOTGUN.getAmmunition().create(),
					CustomElementManager.BOMB.create(), CustomElementManager.SMOKE_BOMB.create(),
					CustomElementManager.SRBM.create(), CustomElementManager.MRBM.create(), CustomElementManager.ICBM.create(),
					CustomElementManager.RANGEFINDER.create(),
					CustomElementManager.WEATHER_FORECASTER.create(),
					CustomElementManager.SMALL_SHELL.create(), CustomElementManager.BIG_SHELL.create());
//					CustomElementManager.REMOTE_CONTROL.create());
			if(e.getPlayer().isSneaking())
				e.getPlayer().getWorld().createExplosion(e.getPlayer().getLocation(), 5.f);
		}

		/* stick */
		if (e.getItem() != null && e.getItem().getType() == Material.STICK && e.getPlayer().getGameMode() == GameMode.CREATIVE && e.getAction() == Action.LEFT_CLICK_AIR) {
			List<EntityType> ents = new ArrayList<>();
			e.getPlayer().sendMessage(e.getPlayer().getWorld().getEntities().size()+"");
			for(Entity ent : e.getPlayer().getWorld().getEntities()) {
				if(!ents.contains(ent.getType())) {
					ents.add(ent.getType());
					e.getPlayer().sendMessage(ent.getType()+" "+ent.getLocation());
				}
				if(ent.getType() != EntityType.PLAYER)
					ent.remove();
			}
			List<Material> mat = new ArrayList<>();
			for(Chunk c : e.getPlayer().getWorld().getLoadedChunks()) {
				for(BlockState bs : c.getTileEntities()) {
					if(!mat.contains(bs.getType())) {
						mat.add(bs.getType());
						e.getPlayer().sendMessage(bs.getType()+" "+bs.getLocation());
					}
					bs.getBlock().setType(Material.AIR);
				}
			}
		}

	}
	
	@EventHandler
	public void onSpawn(CreatureSpawnEvent e) {
//		if(e.getSpawnReason() == SpawnReason.NATURAL || e.getSpawnReason() == SpawnReason.SPAWNER) {
		main.getServer().broadcastMessage(e.getEntity()+" spawned !!!! "+e.getSpawnReason());
//		}
	}
    
}

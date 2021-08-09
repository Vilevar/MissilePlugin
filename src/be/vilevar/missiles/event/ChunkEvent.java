package be.vilevar.missiles.event;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;

public class ChunkEvent implements Listener {

    @EventHandler
	public void onload(ChunkLoadEvent e) {
		Chunk c = e.getChunk();
		
		for(BlockState state : c.getTileEntities()) {
			state.getBlock().setType(Material.AIR);
		}
		
		for(Entity ent : c.getEntities()) {
			if(ent.getType() != EntityType.PLAYER) {
				ent.remove();
			}
		}
	}
    
}

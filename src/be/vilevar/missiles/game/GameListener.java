package be.vilevar.missiles.game;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import be.vilevar.missiles.Main;
import be.vilevar.missiles.merchant.WeaponsMerchant;

public class GameListener implements Listener {

	private Main main = Main.i;
	
	@EventHandler
	public void onNoDamagePreparation(EntityDamageEvent e) {
		Game game = main.getGame();
		if(game != null && !game.isStarted()) {
			e.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onDeath(EntityDeathEvent e) {
		Game game = main.getGame();
		if(game != null) {
			if(e.getEntityType() == EntityType.VILLAGER) {
				WeaponsMerchant merchant = WeaponsMerchant.getMerchant((Villager) e.getEntity());
				if(merchant != null) {
					game.handleMerchantDeath(merchant, e.getDrops());
				}
			} else if(e.getEntityType() == EntityType.PLAYER) {
				game.handlePlayerDeath((Player) e.getEntity(), e.getDrops());
			}
		}
	}
	
	@EventHandler
	public void onRespawn(PlayerRespawnEvent e) {
		Game game = main.getGame();
		if(game != null) {
			game.handleRespawn(e);
		}
	}
	
//	// Events for SiegeGame only TODO See if necessary
//	@EventHandler
//	public void onTeleport(PlayerTeleportEvent e) {
//		Game game = main.getGame();
//		if(game != null && game instanceof SiegeGame) {
//			SiegeGame sgame = (SiegeGame) game;
//			
//		}
//	}
}

package be.vilevar.missiles.game;

import org.bukkit.GameMode;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import be.vilevar.missiles.Main;
import be.vilevar.missiles.defense.Defender;
import be.vilevar.missiles.defense.defender.TeamDefender;
import be.vilevar.missiles.mcelements.merchant.WeaponsMerchant;

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
		if(game != null && e.getEntityType() == EntityType.VILLAGER) {
			WeaponsMerchant merchant = WeaponsMerchant.getMerchant((Villager) e.getEntity());
			if(merchant != null) {
				if(merchant.equals(game.getTeamCapitalism().getMerchant())) {
					game.stop(game.getTeamCommunism(), true);
				} else if(merchant.equals(game.getTeamCommunism().getMerchant())) {
					game.stop(game.getTeamCapitalism(), true);
				}
			}
		}
	}
	
	@EventHandler
	public void onRespawn(PlayerRespawnEvent e) {
		final Game game = main.getGame();
		if(game != null) {
			final Player p = e.getPlayer();
			Defender def = this.main.getDefender(p);
			if(def instanceof TeamDefender) {
				WeaponsMerchant merchant = ((TeamDefender) def).getMerchant();
				if(merchant != null) {
					p.setGameMode(GameMode.SPECTATOR);
					e.setRespawnLocation(merchant.getLocation());
					main.getServer().getScheduler().runTaskLater(main, () -> {
						if(main.getGame() != null && p.isOnline()) {
							p.setGameMode(GameMode.SURVIVAL);
						}
					}, 60);
				}
			}
		}
	}
}

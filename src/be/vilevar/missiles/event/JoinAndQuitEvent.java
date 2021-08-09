package be.vilevar.missiles.event;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import be.vilevar.missiles.Main;
import be.vilevar.missiles.defense.defender.PlayerDefender;

public class JoinAndQuitEvent implements Listener {

    private Main main;

    public JoinAndQuitEvent(Main main) {
        this.main = main;
    }

    @EventHandler
	public void onJoin(PlayerJoinEvent e) {
		if(!main.getPlayers().containsKey(e.getPlayer().getUniqueId()))
			main.getPlayers().put(e.getPlayer().getUniqueId(), new PlayerDefender(e.getPlayer()));
	}

    @EventHandler
	public void onDeath(PlayerDeathEvent e) {
		Player p = e.getEntity();
		if (p.getKiller() != null) {
			try {
				e.setDeathMessage(
						e.getDeathMessage() + " (" + p.getLocation().distance(p.getKiller().getLocation()) + "m)");
			} catch (Exception exp) {
			}
		}
	}
    
}

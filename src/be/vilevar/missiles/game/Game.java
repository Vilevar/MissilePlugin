package be.vilevar.missiles.game;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;

import be.vilevar.missiles.Main;
import be.vilevar.missiles.defense.defender.BigTeamDefender;
import be.vilevar.missiles.merchant.WeaponsMerchant;

public interface Game {

	GameType getType();
	
	String prepare();
	void start();
	void stop(BigTeamDefender winner, boolean message);
	boolean isStarted();
	boolean isStopped();
	
	BigTeamDefender getTeamCapitalism();
	BigTeamDefender getTeamCommunism();
	
	WeaponsMerchant createMerchant(BigTeamDefender team, Location loc);
	
	void handleRespawn(PlayerRespawnEvent e);
	void handlePlayerDeath(Player p, List<ItemStack> drops);
	void handleMerchantDeath(WeaponsMerchant merchant, List<ItemStack> drops);
	
	
	
	/**
	 * It is a function to help the development of the of {@code Game#prepare()}, it is not to use outside.
	 * @param main
	 * @return an error message to send to the command executor if necessary
	 */
	default String prepareTeams(Main main) {
		Collection<? extends Player> online = main.getServer().getOnlinePlayers();
		
		if(online.size() < 2) {
			return "§cPas assez de joueurs.";
		} else {
			ArrayList<? extends Player> test = new ArrayList<>(online);
			Collections.shuffle(test);
			online = test;
		}
		
		for(Player p : online) {
			if(!main.getCommunism().hasEntry(p.getName()) && !main.getCapitalism().hasEntry(p.getName())) {
				if(main.getCapitalism().getSize() < main.getCommunism().getSize()) {
					main.getCapitalism().addEntry(p.getName());
				} else {
					main.getCommunism().addEntry(p.getName());
				}
			}
		}
		
		if(main.getCapitalism().getSize() == 0 || main.getCommunism().getSize() == 0) {
			return "§cLes équipes ne sont pas bien réparties.";
		}
		main.updateTeams();
		return null;
	}
	
}

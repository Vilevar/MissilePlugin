package be.vilevar.missiles.command;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import be.vilevar.missiles.Main;
import be.vilevar.missiles.defense.defender.TeamDefender;
import be.vilevar.missiles.mcelements.merchant.WeaponsMerchant;

public class CommandBase implements CommandExecutor {

    private Main main;

    public CommandBase(Main main) {
        this.main =main;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(command.getName().equalsIgnoreCase("base")) {

            if(sender instanceof Player) {
                Player p = (Player) sender;
                
                if(main.hasGame()) {
					TeamDefender team = main.getCommunism().hasEntry(p.getName()) ? main.getGame().getTeamCommunism()
							:main.getCapitalism().hasEntry(p.getName()) ? main.getGame().getTeamCapitalism() : null;
					if(team != null) {
						if(team.getMerchant() != null) {
							p.teleport(team.getMerchant().getLocation());
						} else {
							Location loc = p.getLocation();
							for(int i = -1; i <= 1; i++) {
								for(int j = -1; j <= 1; j++) {
									loc.clone().add(i, -1, j).getBlock().setType(Material.BEDROCK);
								}
							}
							team.setMerchant(new WeaponsMerchant(team, loc.getBlock().getLocation().add(0.5, 0, 0.5).setDirection(p.getLocation().getDirection())));
						}
					} else {
						p.sendMessage("§cVous n'êtes pas membre d'une équipe.");
					}
				} else {
					p.sendMessage("§cPas de partie en cours.");
				}

            } else {
                sender.sendMessage("Erreur : Seul un joueur peut effectuer cette commande.");
            }

        }
        return true;
    }
    
}

package be.vilevar.missiles.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import be.vilevar.missiles.Main;
import be.vilevar.missiles.defense.defender.TeamDefender;

public class CommandOutpost implements CommandExecutor {

    private Main main;

    public CommandOutpost(Main main) {
        this.main = main;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(sender instanceof Player) {
            Player p = (Player) sender;

			if(main.hasGame()) {
				TeamDefender team = main.getCommunism().hasEntry(p.getName()) ? main.getGame().getTeamCommunism()
							:main.getCapitalism().hasEntry(p.getName()) ? main.getGame().getTeamCapitalism() : null;
				if(team != null) {

					if(command.getName().equalsIgnoreCase("outpost")) {
						if(team.getOutpost() != null) {
							p.teleport(team.getOutpost());
						} else {
							p.sendMessage("§cVous n'êtes pas membre d'une équipe ayant un avant-poste.");
						}
						return true;
					} else if(command.getName().equalsIgnoreCase("setoutpost")) {
						team.setOutpost(p.getLocation());
						p.sendMessage("§6Avant-poste §aplacé§6 à votre position.");
						return true;
					}

				} else {
					p.sendMessage("§cVous n'êtes pas membre d'une équipe.");
				}
			} else {
				p.sendMessage("§cPas de partie en cours.");
			}

        } else {
            sender.sendMessage("Erreur : Seul un joueur peut effectuer cette commande.");
            return true;
        }
        return true;
    }
    
}

package be.vilevar.missiles.commands;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import be.vilevar.missiles.Main;
import be.vilevar.missiles.defense.defender.TeamDefender;
import be.vilevar.missiles.mcelements.merchant.MissileMerchant;

public class BigGameCommands implements CommandExecutor {

	private Main main = Main.i;

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (sender instanceof Player) {
			Player p = (Player) sender;
			if (main.getGame() == null) {
				p.sendMessage("§cPas de partie en cours.");
				return true;
			} else if(!main.getGame().getType().isStrategic()) {
				p.sendMessage("§cLa partie en cours ne permet pas ces commandes.");
				return true;
			}
			if (command.getName().equals("base")) {
				TeamDefender team = main.getTeamDefender(p);
				if (team != null) {
					if (team.getMerchant() != null) {
						p.teleport(team.getMerchant().getLocation());
					} else {
						Location loc = p.getLocation();
						for (int i = -1; i <= 1; i++) {
							for (int j = -1; j <= 1; j++) {
								loc.clone().add(i, -1, j).getBlock().setType(Material.BEDROCK);
							}
						}
						team.setMerchant(new MissileMerchant(team, loc.getBlock().getLocation().add(0.5, 0, 0.5).setDirection(loc.getDirection())));
					}
				} else {
					p.sendMessage("§cVous n'êtes pas membre d'une équipe.");
				}
				return true;
			} else if (command.getName().equals("outpost")) {
				TeamDefender team = main.getTeamDefender(p);
				if (team != null && team.getOutpost() != null) {
					p.teleport(team.getOutpost());
				} else {
					p.sendMessage("§cVous n'êtes pas membre d'une équipe ayant un avant-poste.");
				}
				return true;
			} else if (command.getName().equals("setoutpost")) {
				TeamDefender team = main.getTeamDefender(p);
				if (team != null) {
					team.setOutpost(p.getLocation());
					p.sendMessage("§6Avant-poste §aplacé§6 à votre position.");
				} else {
					p.sendMessage("§cVous n'êtes pas membre d'une équipe.");
				}
				return true;
			}
		}
		return false;
	}

}

package be.vilevar.missiles.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import be.vilevar.missiles.Main;
import be.vilevar.missiles.mcelements.merchant.MissileMerchant;
import be.vilevar.missiles.mcelements.merchant.WeaponsMerchant;

public class BasicCommands implements CommandExecutor {

	private Main main = Main.i;
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (sender instanceof Player) {
			Player p = (Player) sender;
			if (command.getName().equals("merchant") && this.main.getGame() == null) {
				new MissileMerchant(this.main.getDefender(p), p.getLocation());
			}
		} else {
			if (command.getName().equals("missile")) {
				
				if(this.main.getGame() != null) {
					sender.sendMessage("§cIl y a déjà une partie commencée.");
					return true;
				}
				
				WeaponsMerchant.killMerchants();
				
//				this.game = new DefaultGame(); TODO
				
				
				
				String error = this.main.getGame().prepare();
				if(error == null) {
					this.main.getGame().start();
				} else {
					this.main.getGame().stop(null, false);
					sender.sendMessage(error);
				}
				
				return true;
			}
		}
		return true;
	}

}

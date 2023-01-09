package be.vilevar.missiles.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import be.vilevar.missiles.Main;
import be.vilevar.missiles.game.Game;
import be.vilevar.missiles.game.GameType;
import be.vilevar.missiles.game.missile.merchant.MissileMerchant;

public class BasicCommands implements CommandExecutor, TabCompleter {

	private Main main = Main.i;

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (sender instanceof Player) {
			Player p = (Player) sender;
			if (command.getName().equals("merchant") && !this.main.hasGame()) {
				new MissileMerchant(this.main.getDefender(p), p.getLocation());
//				new BasicMerchant(this.main.getDefender(p), p.getLocation());
			}
		}
		if (command.getName().equals("game")) {
			if (args.length == 0) {
				sender.sendMessage("§cVeuillez spécifier le type de partie.");
			} else if (this.main.hasGame()) {
				sender.sendMessage("§cUne partie est déjà en cours");
			} else {
				try {
					Game game = GameType.valueOf(args[0].toUpperCase()).getGameManager().createGame(sender,
							Arrays.copyOfRange(args, 1, args.length));
					if (game != null) {
						String error = this.main.prepareGame(game);
						if (error != null) {
							sender.sendMessage(error);
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
					sender.sendMessage("§cIl n'existe pas de type de partie nommé §4" + args[0] + "§c.");
				}
			}
			return true;
		}
		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		if (command.getName().equals("game")) {
			if (args.length == 0) {
				return new ArrayList<>(Arrays.asList(GameType.values())).stream().map(gt -> gt.toString().toLowerCase())
						.toList();
			} else if (args.length == 1) {
				return new ArrayList<>(Arrays.asList(GameType.values())).stream().map(gt -> gt.toString().toLowerCase())
						.filter(s -> s.startsWith(args[0])).toList();
			} else if (args.length > 1) {
				try {
					return GameType.valueOf(args[0].toUpperCase()).getGameManager()
							.completeCommand(Arrays.copyOfRange(args, 1, args.length));
				} catch (Exception e) {
				}
			}
		}
		return null;
	}
}

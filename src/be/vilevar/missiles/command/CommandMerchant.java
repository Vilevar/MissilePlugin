package be.vilevar.missiles.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import be.vilevar.missiles.Main;
import be.vilevar.missiles.mcelements.merchant.WeaponsMerchant;

public class CommandMerchant implements CommandExecutor{

    private Main main;

    public CommandMerchant(Main main) {
        this.main = main;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(command.getName().equalsIgnoreCase("merchant")) {
            if(sender instanceof Player) {
                Player p = (Player) sender;

                if (!main.hasGame()) {
                    new WeaponsMerchant(main.getDefender(p), p.getLocation());
                } else {
                    p.sendMessage("§cUne partie est déja en cours.");
                }

            } else {
                sender.sendMessage("Erreur : Seul un joueur peut effectuer cette commande.");
            }
        }
        return true;
    }
    
}

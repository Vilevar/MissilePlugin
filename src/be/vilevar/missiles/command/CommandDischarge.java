package be.vilevar.missiles.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import be.vilevar.missiles.Main;
import be.vilevar.missiles.mcelements.weapons.Weapon;

public class CommandDischarge implements CommandExecutor {

    private Main main;

    public CommandDischarge(Main main) {
        this.main = main;
    }

	@Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(command.getName().equalsIgnoreCase("discharge")) {

        	if(sender instanceof Player) {
            	Player p = (Player) sender;
				
				ItemStack is = p.getInventory().getItemInMainHand();
				Weapon w = main.getCustom().getWeapons().getWeapon(is);
				if (w == null) {
					p.sendMessage("§6Mettez une §carme à feu§6 dans votre §amain principale§c.");
				} else {
					ItemMeta im = is.getItemMeta();
					if (im instanceof Damageable) {
						Damageable dam = (Damageable) im;
						dam.setDamage(is.getType().getMaxDurability());
						is.setItemMeta(im);
						p.getInventory().setItemInMainHand(is);
						p.sendMessage("§6Arme déchargée.");
					} else {
						p.sendMessage("§6Cette arme a munitions infinies.");
					}
				}
				return true;
			} else {
            	sender.sendMessage("Erreur : Seul un joueur peut effectuer cette commande.");
            	return true;
        	} 

        }
        return true;
    }

}

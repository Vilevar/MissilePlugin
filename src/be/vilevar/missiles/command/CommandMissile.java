package be.vilevar.missiles.command;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import be.vilevar.missiles.Main;
import be.vilevar.missiles.mcelements.merchant.WeaponsMerchant;

public class CommandMissile implements CommandExecutor {

    private Main main;

    public CommandMissile(Main main) {
        this.main = main;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("missile")) {
			if(!(sender instanceof Player)) {
                if(!main.hasGame()) {
                
                    Collection<? extends Player> online = main.getServer().getOnlinePlayers();
                    if(online.size() < 2) {
                        sender.sendMessage("§cPas assez de joueurs.");
                        return true;
                    } else {
                        ArrayList<? extends Player> test = new ArrayList<>(online);
                        Collections.shuffle(test);
                        online = test;
                    }
                    
                    ItemStack pickaxe = new ItemStack(Material.DIAMOND_PICKAXE);
                    pickaxe.addEnchantment(Enchantment.DIG_SPEED, 4);
                    ItemStack beef = new ItemStack(Material.COOKED_BEEF, 10);
                    
                    for(Player p : online) {
                        if(!main.getCommunism().hasEntry(p.getName()) && !main.getCapitalism().hasEntry(p.getName())) {
                            if(main.getCapitalism().getSize() < main.getCommunism().getSize()) {
                                main.getCapitalism().addEntry(p.getName());
                            } else {
                                main.getCommunism().addEntry(p.getName());
                            }
                        }
                        
                        p.getInventory().clear();
                        p.getInventory().addItem(pickaxe, beef);
                        p.getEnderChest().clear();
                        p.setGameMode(GameMode.SURVIVAL);
                    }
                    
                    if(main.getCapitalism().getSize() == 0 || main.getCommunism().getSize() == 0) {
                        sender.sendMessage("§cLes équipes ne sont pas bien réparties.");
                        return true;
                    }
                    
                    WeaponsMerchant.killMerchants();
                    
                    return true;
                } else {
                    sender.sendMessage("§cPartie déjà commencée.");
                    return true;
                }
            } else {
                sender.sendMessage("§cSeule la console a le droit de lancer une partie.");
                return true;
            }
            
        }
        return true;
    }
    
}

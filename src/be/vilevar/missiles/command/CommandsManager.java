package be.vilevar.missiles.command;

import org.bukkit.command.CommandExecutor;

import be.vilevar.missiles.Main;

public class CommandsManager {

    public CommandsManager(Main main) {
        main.getCommand("missile").setExecutor(new CommandMissile(main));
		main.getCommand("discharge").setExecutor(new CommandDischarge(main));
		main.getCommand("base").setExecutor(new CommandBase(main));
		main.getCommand("merchant").setExecutor(new CommandMerchant(main));
        CommandExecutor outpost = new CommandOutpost(main);
		main.getCommand("outpost").setExecutor(outpost);
		main.getCommand("setoutpost").setExecutor(outpost);
    }
    
}

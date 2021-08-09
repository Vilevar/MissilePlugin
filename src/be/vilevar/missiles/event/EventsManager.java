package be.vilevar.missiles.event;

import org.bukkit.plugin.PluginManager;

import be.vilevar.missiles.Main;
import be.vilevar.missiles.game.GameListener;
import be.vilevar.missiles.mcelements.CustomElementManager;

public class EventsManager {

    public EventsManager(Main main) {
        PluginManager pm = main.getServer().getPluginManager();
        
        CustomElementManager custom = new CustomElementManager(main, pm);
        main.setCustom(custom);
		pm.registerEvents(custom, main);

		pm.registerEvents(new GameListener(), main);

        pm.registerEvents(new ChunkEvent(), main);
        pm.registerEvents(new JoinAndQuitEvent(main), main);
        pm.registerEvents(new BlockEvent(), main);
        pm.registerEvents(new InteractEvent(main), main);
    }
    
}

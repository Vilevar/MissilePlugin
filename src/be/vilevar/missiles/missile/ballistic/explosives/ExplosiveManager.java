package be.vilevar.missiles.missile.ballistic.explosives;

import java.util.ArrayList;

import be.vilevar.missiles.Main;

public class ExplosiveManager {

	private static final ArrayList<Detonation> waiting = new ArrayList<>();
	private static Detonation current;
	
	public static void addDetonation(Detonation explosion) {
		waiting.add(explosion);
	}
	
	public static void startScheduler(Main main) {
		main.getServer().getScheduler().scheduleSyncRepeatingTask(main, ExplosiveManager::schedule, 20, 10);
	}
	
	private static void schedule() {
		if(current != null) {
			if(current.getExplosive().isDone()) {
				current = null;
				schedule();
			}
		} else if(!waiting.isEmpty()) {
			current = waiting.get(0);
			waiting.remove(0);
			current.getExplosive().explode(current.getLoc(), current.getDamager());
		}
	}
}

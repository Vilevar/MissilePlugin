package be.vilevar.missiles.defense.defender;

import org.bukkit.entity.Player;

import be.vilevar.missiles.defense.Defender;
import be.vilevar.missiles.defense.DefenseNetwork;

public class PlayerDefender implements Defender {

	private static final String horse = "{Variant:260,Health:30,Attributes:[{Name:\"horse.jump_strength\",Base:1.5f},"
			+ "{Name:\"generic.movement_speed\",Base:0.8f},{Name:\"generic.max_health\",Base:30F}]}";
	
	private final Player p;
	private final DefenseNetwork[] networks = new DefenseNetwork[10];
	
	public PlayerDefender(Player player) {
		this.p = player;
		for(int i = 0; i < 10; i++) {
			this.networks[i] = new DefenseNetwork(this, i);
		}
	}
	
	@Override
	public DefenseNetwork getNetwork(int channel) {
		return this.networks[channel];
	}

	@Override
	public void sendMessage(String message) {
		if(p.isOnline())
			p.sendMessage(message);
	}

	@Override
	public String getHorseTag() {
		return horse;
	}
}

package be.vilevar.missiles.defense;

import java.util.ArrayList;
import java.util.HashMap;

import be.vilevar.missiles.mcelements.abm.ABMLauncher;
import be.vilevar.missiles.mcelements.radar.Radar;
import be.vilevar.missiles.missile.ballistic.ReentryVehicle;

public class DefenseNetwork {

	
	
	private final Defender owner;
	private final int channel;
	
	private final ArrayList<Radar> radars = new ArrayList<>();
	private final ArrayList<ABMLauncher> abms = new ArrayList<>();
	
	private final HashMap<ReentryVehicle, Target> targets = new HashMap<>();
	
	public DefenseNetwork(Defender owner, int channel) {
		this.owner = owner;
		this.channel = channel;
	}
	
	public Defender getOwner() {
		return owner;
	}
	
	public int getChannel() {
		return channel;
	}
	
	
	public void addRadar(Radar radar) {
		this.radars.add(radar);
	}
	
	public void delRadar(Radar radar) {
		this.radars.remove(radar);
	}
	
	public int addABMLauncher(ABMLauncher launcher) {
		if(this.abms.add(launcher)) {
			return this.abms.size();
		}
		return 0;
	}
	
	public void delABMLauncher(ABMLauncher launcher) {
		this.abms.remove(launcher);
	}
	
	public Target getTarget(ReentryVehicle vehicle) {
		if(targets.containsKey(vehicle)) {
			return targets.get(vehicle);
		} else {
			Target target = new Target(vehicle);
			targets.put(vehicle, target);
			return target;
		}
	}
	
	public void notifyDefense(Target target) {
		for(ABMLauncher launcher : this.abms) {
			if(launcher.tryToShoot(target)) {
				return;
			}
		}
	}
}

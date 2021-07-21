package be.vilevar.missiles.defense;

import be.vilevar.missiles.missile.ballistic.ReentryVehicle;

public class Target {

	private final ReentryVehicle rv;
	private boolean message;
	private boolean abm;
	
	public Target(ReentryVehicle rv) {
		this.rv = rv;
	}
	
	public ReentryVehicle getTarget() {
		return rv;
	}
	
	public boolean isMessageSent() {
		return message;
	}
	
	public void messageSent() {
		this.message = true;
	}
	
	public boolean isABMLaunched() {
		return abm;
	}
	
	public void ABMLaunched() {
		this.abm = true;
	}
	
}

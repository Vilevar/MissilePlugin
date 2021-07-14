package be.vilevar.missiles.missile.ballistic;

public class MissileStage {

	private double mass;
	private double fuelMass;
	private double impulse;
	private double eject;
	
	public MissileStage(double mass, double fuelMass, double impulse, double eject) {
		this.mass = mass;
		this.fuelMass = fuelMass;
		this.impulse = impulse;
		this.eject = eject;
	}
	
	public double getMass() {
		return mass;
	}
	
	public double getFuelMass() {
		return fuelMass;
	}
	
	public void setFuelMass(double fuelMass) {
		this.fuelMass = fuelMass;
	}
	
	public void addFuelMass(double fuelMass) {
		this.fuelMass += fuelMass;
	}
	
	public double getTotalMass() {
		return mass + fuelMass;
	}
	
	public double getImpulse() {
		return impulse;
	}
	
	public double getEject() {
		return eject;
	}
	
	public static MissileStage createStage(int stage, double impulse, double eject) {
		switch(stage) {
		case 1:
			return new MissileStage(400, 1800, impulse, eject);
		case 2:
			return new MissileStage(100, 200, impulse, eject);
		case 3:
			return new MissileStage(50, 100, impulse, eject);
		}
		return null;
	}
}

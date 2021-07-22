package be.vilevar.missiles.defense;

public interface Defender {

	DefenseNetwork getNetwork(int channel);
	
	void sendMessage(String message);
	
	String getHorseTag();
}

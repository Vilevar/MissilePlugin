package be.vilevar.missiles.mcelements.abm;

import java.util.ArrayList;
import java.util.Iterator;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import be.vilevar.missiles.defense.Defender;
import be.vilevar.missiles.defense.DefenseNetwork;
import be.vilevar.missiles.defense.Target;
import be.vilevar.missiles.mcelements.CustomElementManager;

public class ABMLauncher {
	
	public static final ArrayList<ABMLauncher> launchers = new ArrayList<>();

	private final Location loc;
	private final Defender def;
	
	private int channel = -1;
	private DefenseNetwork network;
	private int id;
	
	private ABM abms[] = new ABM[8];

	private boolean messageSend;
	private boolean messageInter;
	
	private Player open;
	
	public ABMLauncher(Location loc, Defender def) {
		this.loc = loc;
		this.def = def;
		this.setChannel(0);
	}
	
	
	public void tryToShoot(Target target) {
		for(int i = 0; i < abms.length; i++) {
			ABM abm = abms[i];
			if(abm != null) {
				abm.shoot(target, this, this.messageInter);
				abms[i] = null;
				if(this.messageSend) {
					this.def.sendMessage("§5[§a"+channel+":"+this.id+
							"§5] §6Missile d'interception envoyé sur le véhicule §c["+target.getTarget().getId()+"]§6.");
				}
				
				if(this.isOpen())
					this.open.closeInventory();
				
				return;
			}
		}
		
		if(this.messageSend) {
			this.def.sendMessage("§5[§a"+channel+":"+this.id+
					"§5] §cPlus assez de missiles§6 que pour intercepter le véhicule §c["+target.getTarget().getId()+"]§6.");
		}
	}
	
	public ABM getABM(int id) {
		return abms[id];
	}
	
	public void setABM(int id, ABM abm) {
		this.abms[id] = abm;
	}

	public int countABM() {
		int n = 0;
		for(int i = 0; i < abms.length; i++) {
			if(abms[i] != null)
				n += 1;
		}
		return n;
	}
	
	public int getChannel() {
		return channel;
	}
	
	public int getId() {
		return id;
	}

	public void setChannel(int channel) {
		if(this.channel == channel)
			return;
		
		if(this.network != null)
			this.network.delABMLauncher(this);
		
		this.channel = channel;
		this.network = this.def.getNetwork(channel);
		this.id = this.network.addABMLauncher(this);
	}
	
	
	public boolean isMessageSend() {
		return messageSend;
	}
	
	public void setMessageSend(boolean messageSend) {
		this.messageSend = messageSend;
	}
	
	public boolean isMessageInter() {
		return messageInter;
	}
	
	public void setMessageInter(boolean messageInter) {
		this.messageInter = messageInter;
	}
	
	
	
	public boolean isOpen() {
		return open != null;
	}
	
	public void setOpen(Player open) {
		this.open = open;
	}
	
	private void drop() {
		int amount = 0;
		for(int i = 0; i < 8; i++) {
			if(this.getABM(i) != null) {
				amount++;
			}
		}
		
		if(amount != 0) {
			loc.getWorld().dropItem(loc, CustomElementManager.ABM.create(amount));
		}
	}
	
	
	public static void checkDestroy(Location loc) {
		Iterator<ABMLauncher> it = launchers.iterator();
		while(it.hasNext()) {
			ABMLauncher launcher = it.next();
			if(launcher.loc.equals(loc)) {
				it.remove();
				if(launcher.open != null)
					launcher.open.closeInventory();
				launcher.drop();
				return;
			}
		}
	}
	
	public static ABMLauncher getLauncherAt(Location loc) {
		for(ABMLauncher launcher : launchers) {
			if(launcher.loc.equals(loc)) {
				return launcher;
			}
		}
		return null;
	}

	public Location getLocation() {
		return this.loc;
	}

	public Defender getDefender() {
		return this.def;
	}
}

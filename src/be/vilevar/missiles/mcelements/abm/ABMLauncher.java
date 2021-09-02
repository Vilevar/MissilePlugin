package be.vilevar.missiles.mcelements.abm;

import java.util.ArrayList;
import java.util.Iterator;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import be.vilevar.missiles.defense.Defender;
import be.vilevar.missiles.defense.DefenseNetwork;
import be.vilevar.missiles.defense.Target;
import be.vilevar.missiles.mcelements.CustomElementManager;
import be.vilevar.missiles.mcelements.ElectricBlock;

public class ABMLauncher implements ElectricBlock {
	
	public static final ArrayList<ABMLauncher> launchers = new ArrayList<>();

	private final Location loc;
	private final Location firingLoc;
	private final Defender def;
	
	private int channel = -1;
	private DefenseNetwork network;
	private int id;
	
	private ABM abms[] = new ABM[8];

	private boolean messageSend;
	private boolean messageInter;
	
	private long offTime;
	
	private Player open;
	
	public ABMLauncher(Location loc, Defender def) {
		this.loc = loc;
		this.firingLoc = loc.getBlock().getLocation().add(0.5, 1, 0.5);
		this.def = def;
		this.setChannel(0);
	}
	
	
	public boolean tryToShoot(Target target) {
		if(this.getTimeOut() > 0)
			return false;
		
		for(int i = abms.length; i > 0; i--) {
			int j = i - 1;
			ABM abm = abms[j];
			if(abm != null) {
				if(abm.shoot(this, target.getTarget(), this.messageInter)) {
					target.ABMLaunched();
					abms[j] = null;
					if(this.messageSend) {
						this.def.sendMessage(this.getSignature() + "§6Missile d'interception envoyé sur le véhicule §c["
									+ target.getTarget().getId() + "]§6.");
					}
					
					if(this.isOpen())
						this.open.closeInventory();
					
					return true;
				} else {
					return false;
				}
			}
		}
		
		if(this.messageSend) {
			this.def.sendMessage(this.getSignature()+"§4Plus assez de missiles§6 que pour intercepter le véhicule §c[" +
							target.getTarget().getId() + "]§6.");
		}
		
		return false;
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
	
	public Location getFiringLocation() {
		return firingLoc;
	}
	
	public int getChannel() {
		return channel;
	}
	
	public int getId() {
		return id;
	}
	
	public String getSignature() {
		return "§5[§a" + this.channel + ":" + this.id + "§5] ";
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
	
	@Override
	public long getTimeOut() {
		long time = this.offTime - System.currentTimeMillis();
		if(time <= 0) {
			return 0;
		}
		return time;
	}
	
	@Override
	public void addTimeOut(long time) {
		this.offTime = System.currentTimeMillis() + this.getTimeOut() + time; 
	}
	
	
	
	public boolean isOpen() {
		return open != null;
	}
	
	public void setOpen(Player open) {
		this.open = open;
	}
	
	private boolean drop() {
		if(this.getTimeOut() > 0) {
			return false;
		}
		
		int amount = this.countABM();
		
		if(amount != 0) {
			loc.getWorld().dropItem(loc, CustomElementManager.ABM.create(amount));
		}
		
		return true;
	}
	
	
	public static boolean checkDestroy(Location loc) {
		System.out.println("Check Destroy ABMLauncher "+loc);
		Iterator<ABMLauncher> it = launchers.iterator();
		while(it.hasNext()) {
			ABMLauncher launcher = it.next();
			if(launcher.loc.equals(loc)) {
				it.remove();
				if(launcher.open != null)
					launcher.open.closeInventory();
				launcher.network.delABMLauncher(launcher);
				return launcher.drop();
			}
		}
		return true;
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

package be.vilevar.missiles.mcelements.radar;

import java.util.ArrayList;
import java.util.Iterator;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import be.vilevar.missiles.Main;
import be.vilevar.missiles.defense.Defender;
import be.vilevar.missiles.defense.DefenseNetwork;
import be.vilevar.missiles.defense.Target;
import be.vilevar.missiles.missile.ballistic.ReentryVehicle;

public class Radar {

	public static final ArrayList<Radar> radars = new ArrayList<>();
	private static final int area = 7;
	private static final double range = 500;
	private static final double sRange = range * range;
	
	private Main main = Main.i;
	
	private final Location loc;
	private final Defender def;
	
	private int channel = -1;
	private DefenseNetwork network;
	
	private boolean sound;
	private boolean message;
	private boolean send;
	
	private boolean isSounding;
	
	private Player open;
	
	private boolean isEffective;
	private int test;
	
	public Radar(Location loc, Defender def) {
		this.loc = loc;
		this.def = def;
		this.setChannel(0);
		
	}
	
	
	public boolean isEffective() {
		for(int i = -area; i <= area; i++) {
			for(int j = -area; j <= area; j++) {
				Location block = this.loc.clone().add(i, j, i == 0 & j == 0 ? 1 : 0);
				if(block.getBlock().getLightFromSky() != 15) {
					return this.isEffective = false;
				}
			}
		}
		return this.isEffective = true;
	}
	
	public void checkMissiles() {
		if(((test++) % 60 == 0 && !this.isEffective()) || !this.isEffective) {
			return;
		}
		
		for(ReentryVehicle rv : ReentryVehicle.air) {
			Location loc = rv.getLocation();
			if(loc != null && loc.distanceSquared(this.loc) <= sRange) {
				Target target = this.network.getTarget(rv);
				
				if(this.sound  && !this.isSounding) {
					this.loc.getWorld().playSound(this.loc, Sound.ENTITY_MAGMA_CUBE_JUMP, channel, area);
					this.isSounding = true;
					main.getServer().getScheduler().runTaskLaterAsynchronously(main, () -> this.isSounding = false, 330);
				}
				if(this.message && !target.isMessageSent()) {
					this.def.sendMessage("§5[§a"+channel+"§5] §6Missile §c["+rv.getId()+"]§6 repéré en §e("+loc.getBlockX()+", "+loc.getBlockY()+
							", "+loc.getBlockZ()+")§6 avec une vitesse de §e"+Math.round(rv.getVelocity().length())+"m/s§6.");
					target.messageSent();
				}
				if(this.send && !target.isABMLaunched()) {
					this.network.notifyDefense(target);
					target.ABMLaunched();
				}
			}
		}
	}
	

	public int getChannel() {
		return channel;
	}

	public void setChannel(int channel) {
		if(this.channel == channel)
			return;
		
		if(this.network != null)
			this.network.delRadar(this);
		
		this.channel = channel;
		this.network = this.def.getNetwork(channel);
		this.network.addRadar(this);
	}

	public boolean isSound() {
		return sound;
	}

	public void setSound(boolean sound) {
		this.sound = sound;
	}

	public boolean isMessage() {
		return message;
	}

	public void setMessage(boolean message) {
		this.message = message;
	}

	public boolean isSend() {
		return send;
	}

	public void setSend(boolean send) {
		this.send = send;
	}

	public Location getLocation() {
		return loc;
	}

	public Defender getDefender() {
		return def;
	}
	
	public boolean isOpen() {
		return open != null;
	}
	
	public void setOpen(Player open) {
		this.open = open;
	}
	
	
	
	public static void checkDestroy(Location loc) {
		Iterator<Radar> it = radars.iterator();
		while(it.hasNext()) {
			Radar radar = it.next();
			if(radar.getLocation().equals(loc)) {
				it.remove();
				if(radar.open != null)
					radar.open.closeInventory();
				return;
			}
		}
	}
	
	public static Radar getRadarAt(Location loc) {
		for(Radar radar : radars) {
			if(radar.getLocation().equals(loc)) {
				return radar;
			}
		}
		return null;
	}
	
	
	public static void createCheckMissileScheduler(Main main) {
		main.getServer().getScheduler().scheduleSyncRepeatingTask(main, () -> {
			for(Radar radar : radars) {
				radar.checkMissiles();
			}
		}, 20, 10);
	}
}

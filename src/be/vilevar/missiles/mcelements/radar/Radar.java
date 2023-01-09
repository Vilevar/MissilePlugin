package be.vilevar.missiles.mcelements.radar;

import java.util.ArrayList;
import java.util.Iterator;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import be.vilevar.missiles.Main;
import be.vilevar.missiles.defense.Defender;
import be.vilevar.missiles.defense.DefenseNetwork;
import be.vilevar.missiles.defense.Target;
import be.vilevar.missiles.mcelements.ElectricBlock;
import be.vilevar.missiles.missile.ballistic.ReentryVehicle;

public class Radar implements ElectricBlock {

	public static final ArrayList<Radar> radars = new ArrayList<>();
	private static final int area = 5;
	private static final double range = 300;
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
	
	private long offTime;
	
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
				Location block = this.loc.clone().add(i, i == 0 & j == 0 ? 1 : 0, j);
				if(block.getBlock().getLightFromSky() != 15) {
					System.out.println(block+" radar out");
					return this.isEffective = false;
				}
			}
		}
		return this.isEffective = true;
	}
	
	public void checkMissiles() {
		if(this.getTimeOut() > 0 || ((test++) % 4 == 0 && !this.isEffective()) || !this.isEffective) {
			return;
		}
		
		for(ReentryVehicle rv : ReentryVehicle.air) {
			Location loc = rv.getLocation();
			if(loc != null && loc.distanceSquared(this.loc) <= sRange) {
				Target target = this.network.getTarget(rv);
				
				if(this.sound  && !this.isSounding) {
					this.loc.getWorld().playSound(this.loc, Sound.ENTITY_MAGMA_CUBE_JUMP, 2, 1);
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
	
	
	
	public static void destroyAll() {
		Iterator<Radar> it = radars.iterator();
		while(it.hasNext()) {
			Radar radar = it.next();
			it.remove();
			radar.getLocation().getBlock().setType(Material.AIR);
			if(radar.open != null)
				radar.open.closeInventory();
			radar.network.delRadar(radar);
		}
	}
	
	public static boolean checkDestroy(Location loc) {
		Iterator<Radar> it = radars.iterator();
		while(it.hasNext()) {
			Radar radar = it.next();
			if(radar.getLocation().equals(loc)) {
				it.remove();
				if(radar.open != null)
					radar.open.closeInventory();
				radar.network.delRadar(radar);
				return radar.getTimeOut() == 0;
			}
		}
		return true;
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
		}, 20, 5);
	}
}

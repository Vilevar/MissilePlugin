package be.vilevar.missiles.mcelements.data;

import java.util.ArrayList;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import be.vilevar.missiles.mcelements.CustomElementManager;
import be.vilevar.missiles.mcelements.data.persistanttype.MIRVPersistantDataType;
import be.vilevar.missiles.missile.ballistic.Explosive;
import be.vilevar.missiles.missile.ballistic.ReentryVehicle;
import io.netty.buffer.ByteBuf;

public class MIRVData {

	private int mirvs;
	private int[] altitudes;
	private int[] pitch;
	private int[] yaw;
	private Explosive[] explosives;
	
	public MIRVData(int mirvs, int[] altitudes, int[] pitch, int[] yaw, Explosive[] explosives) {
		this.mirvs = mirvs;
		this.altitudes = altitudes;
		this.pitch = pitch;
		this.yaw = yaw;
		this.explosives = explosives;
	}

	public int getMIRVs() {
		return mirvs;
	}
	
	public int getAltitude(int mirv) {
		return altitudes[mirv];
	}
	
	public void setAltitude(int mirv, int altitude) {
		this.altitudes[mirv] = altitude;
	}
	
	public int getPitch(int mirv) {
		return pitch[mirv];
	}
	
	public void setPitch(int mirv, int pitch) {
		this.pitch[mirv] = pitch;
	}
	
	public int getYaw(int mirv) {
		return yaw[mirv];
	}
	
	public void setYaw(int mirv, int yaw) {
		this.yaw[mirv] = yaw;
	}
	
	public Explosive getExplosive(int mirv) {
		return explosives[mirv];
	}
	
	public void setExplosive(int mirv, Explosive explosive) {
		this.explosives[mirv] = explosive;
	}
	
	public boolean isReadyForMissile() {
		boolean hasExplosive = false;
		for(int i = 0; i < this.mirvs; i++) {
			if(this.getExplosive(i) != null) {
				hasExplosive = true;
				int pitch = this.getPitch(i);
				int yaw = this.getYaw(i);
				
				for(int j = i + 1; j < this.mirvs; j++) {
					if(this.getPitch(j) == pitch && this.getYaw(j) == yaw && this.getExplosive(j) != null) {
						return false;
					}
				}
			}
		}
		return hasExplosive;
	}
	
	public void saveIn(ByteBuf buffer) {
		buffer.writeInt(mirvs);
		for(int i = 0; i < mirvs; i++) {
			buffer.writeInt(altitudes[i]);
			buffer.writeInt(pitch[i]);
			buffer.writeInt(yaw[i]);
			if(explosives[i] != null) {
				buffer.writeInt(1);
				explosives[i].saveIn(buffer);
			} else {
				buffer.writeInt(0);
			}
		}
	}
	
	public ReentryVehicle[] toMIRV() {
		ArrayList<ReentryVehicle> mirv = new ArrayList<>();
		for(int i = 0; i < this.mirvs; i++) {
			if(this.explosives[i] != null)
				mirv.add(new ReentryVehicle(Math.toRadians(this.pitch[i]), Math.toRadians(this.yaw[i]), this.explosives[i], this.altitudes[i]));
		}
		return mirv.toArray(new ReentryVehicle[mirv.size()]);
	}
	
	public ItemStack toItem() {
		ItemStack is = this.mirvs == 1 ? CustomElementManager.REENTRY_VEHICLE.create() : CustomElementManager.MIRV.create();
		ItemMeta im = is.getItemMeta();
		im.getPersistentDataContainer().set(MIRVPersistantDataType.MIRV_KEY, MIRVPersistantDataType.MIRV, this);
		is.setItemMeta(im);
		return is;
	}
	
	
	public static MIRVData readFrom(ByteBuf buffer) {
		int mirvs = buffer.readInt();
		int[] altitudes = new int[mirvs];
		int[] pitch = new int[mirvs];
		int[] yaw = new int[mirvs];
		Explosive[] explosives = new Explosive[mirvs];
		for(int i = 0; i < mirvs; i++) {
			altitudes[i] = buffer.readInt();
			pitch[i] = buffer.readInt();
			yaw[i] = buffer.readInt();
			if(buffer.readInt() == 1) {
				explosives[i] = Explosive.readFrom(buffer);
			}
		}
		return new MIRVData(mirvs, altitudes, pitch, yaw, explosives);
	}
	
	public static MIRVData getMIRVData(ItemStack is) {
		if(is == null) {
			return null;
		} else if(CustomElementManager.REENTRY_VEHICLE.isParentOf(is)) {
			ItemMeta im = is.getItemMeta();
			return im.getPersistentDataContainer().getOrDefault(MIRVPersistantDataType.MIRV_KEY, MIRVPersistantDataType.MIRV, 
					new MIRVData(1, new int[1], new int[1], new int[1], new Explosive[1]));
		} else if(CustomElementManager.MIRV.isParentOf(is)) {
			ItemMeta im = is.getItemMeta();
			return im.getPersistentDataContainer().getOrDefault(MIRVPersistantDataType.MIRV_KEY, MIRVPersistantDataType.MIRV, 
					new MIRVData(5, new int[5], new int[5], new int[5], new Explosive[5]));
		}
		return null;
	}
	
}

package be.vilevar.missiles.mcelements.data;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import be.vilevar.missiles.mcelements.CustomElementManager;
import be.vilevar.missiles.mcelements.data.persistanttype.BallisticMissilePersistantDataType;
import be.vilevar.missiles.missile.BallisticMissile;
import be.vilevar.missiles.missile.ballistic.MissileStage;

public class BallisticMissileData implements Cloneable {
	
	public static final int[] neededFuel = {18, 2, 2};
	
	private int stages;
	private int[] impulses;
	private int[] nFuels;
	private int[] ejects;
	private MIRVData warhead;
	

	public BallisticMissileData(int stages, int[] impulses, int[] nFuels, int[] ejects, MIRVData warhead) {
		this.stages = stages;
		this.impulses = impulses;
		this.nFuels = nFuels;
		this.ejects = ejects;
		this.warhead = warhead;
	}
	
	public int getStages() {
		return stages;
	}
	
	public int getImpulse(int stage) {
		return impulses[stage];
	}
	
	public void setImpulse(int stage, int impulse) {
		this.impulses[stage] = impulse;
	}
	
	public int getNFuel(int stage) {
		return nFuels[stage];
	}
	
	public int getMaxFuel(int stage) {
		return neededFuel[stage];
	}
	
	public void setNFuel(int stage, int nFuel) {
		this.nFuels[stage] = nFuel;
	}
	
	public int getEject(int stage) {
		return ejects[stage];
	}
	
	public void setEject(int stage, int eject) {
		this.ejects[stage] = eject;
	}
	
	public MIRVData getWarhead() {
		return warhead;
	}
	
	public void setWarhead(MIRVData warhead) {
		this.warhead = warhead;
	}
	
	public boolean isReady() {
		if(this.stages > 0 && this.stages < 4 && this.warhead != null) {
			for(int i = 0; i < this.stages; i++) {
				if(this.impulses[i] <= 0 || this.ejects[i] <= 0 || this.nFuels[i] != neededFuel[i]) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

	public BallisticMissile toBallisticMissile() {
		if(!this.isReady()) {
			return null;
		}
		MissileStage[] stages = new MissileStage[this.stages];
		for(int i = 0; i < this.stages; i++) {
			stages[i] = MissileStage.createStage(i + 1, this.impulses[i], this.ejects[i]);
		}
		return new BallisticMissile(stages, this.warhead.toMIRV());
	}
	
	public ItemStack toItemStack() {
		ItemStack is;
		switch(stages) {
		case 1:
			is = CustomElementManager.SRBM.create();
			break;
		case 2:
			is = CustomElementManager.MRBM.create();
			break;
		case 3:
			is = CustomElementManager.ICBM.create();
			break;
		default: 
			is = null;
		}
		
		ItemMeta im = is.getItemMeta();
		im.getPersistentDataContainer().set(BallisticMissilePersistantDataType.BALLISTIC_MISSILE_KEY,
				BallisticMissilePersistantDataType.BALLISTIC_MISSILE, this);
		is.setItemMeta(im);
		return is;
	}
	
	@Override
	public BallisticMissileData clone() {
		return new BallisticMissileData(this.stages, this.impulses, this.nFuels, this.ejects, this.warhead);
	}
	
	
	public static BallisticMissileData getBallisticMissileData(ItemStack is) {
		if(is == null) {
			return null;
		} else if(CustomElementManager.SRBM.isParentOf(is)) {
			ItemMeta im = is.getItemMeta();
			return im.getPersistentDataContainer().getOrDefault(BallisticMissilePersistantDataType.BALLISTIC_MISSILE_KEY,
					BallisticMissilePersistantDataType.BALLISTIC_MISSILE, 
					new BallisticMissileData(1, new int[1], new int[1], new int[1], null));
		} else if(CustomElementManager.MRBM.isParentOf(is)) {
			ItemMeta im = is.getItemMeta();
			return im.getPersistentDataContainer().getOrDefault(BallisticMissilePersistantDataType.BALLISTIC_MISSILE_KEY,
					BallisticMissilePersistantDataType.BALLISTIC_MISSILE, 
					new BallisticMissileData(2, new int[2], new int[2], new int[2], null));
		} else if(CustomElementManager.ICBM.isParentOf(is)) {
			ItemMeta im = is.getItemMeta();
			return im.getPersistentDataContainer().getOrDefault(BallisticMissilePersistantDataType.BALLISTIC_MISSILE_KEY,
					BallisticMissilePersistantDataType.BALLISTIC_MISSILE, 
					new BallisticMissileData(3, new int[3], new int[3], new int[3], null));
		}
		return null;
	}
}

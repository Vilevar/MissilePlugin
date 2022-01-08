package be.vilevar.missiles.mcelements.merchant;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;

public interface WeaponsMerchant {
	
	static final ArrayList<WeaponsMerchant> merchants = new ArrayList<>();
	
	public static WeaponsMerchant getMerchant(Villager villager) {
		for(WeaponsMerchant merchant : merchants) {
			if(merchant.getVillager().getUniqueId().equals(villager.getUniqueId())) {
				return merchant;
			}
		}
		return null;
	}
	
	public static void killMerchants() {
		for(WeaponsMerchant merchant : merchants) {
			merchant.getVillager().damage(500000);
		}
	}
	
	public static ArrayList<WeaponsMerchant> getMerchants() {
		return new ArrayList<>(merchants);
	}
	
	
	Villager getVillager();
	default MissileMerchant getAsMissileMerchant() {
		return null;
	}
	
	boolean canBeHurtBy(Player p);
	
	Location getLocation();
	void testLocation();
	
	boolean open(Player p);
	WeaponsMerchantStage getOpenStage();
	void close();
	
	void addMoney(int money);
	int getMoney();
	
	
	
	static enum WeaponsMerchantStage {
		HOME,
		UTILITARIES,
		RESEARCH,
		DEVELOPMENT;
	}
	
}

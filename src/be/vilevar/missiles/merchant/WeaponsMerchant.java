package be.vilevar.missiles.merchant;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;

import be.vilevar.missiles.game.missile.merchant.MissileMerchant;
import be.vilevar.missiles.game.siege.merchant.SiegeMerchant;


public interface WeaponsMerchant {

	Villager getVillager();
	
	default MissileMerchant getAsMissileMerchant() {
		return null;
	}
	default SiegeMerchant getAsSiegeMerchant() {
		return null;
	}
	
	boolean canBeHurtBy(Player p);
	
	Location getLocation();
	void testLocation();
	
	boolean open(Player p);
	boolean isOpen();
	void close();
	
	void addMoney(int money);
	int getMoney();
	
	
	
	
	
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
			merchant.getVillager().remove();
		}
	}
	
	public static ArrayList<WeaponsMerchant> getMerchants() {
		return new ArrayList<>(merchants);
	}
}

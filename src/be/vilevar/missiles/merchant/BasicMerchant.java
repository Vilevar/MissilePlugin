package be.vilevar.missiles.merchant;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;

import be.vilevar.missiles.defense.Defender;

public class BasicMerchant implements WeaponsMerchant {

	public BasicMerchant(Defender defender, Location location) {
		// TODO Auto-generated constructor stub
	}

	@Override
	public Villager getVillager() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean canBeHurtBy(Player p) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Location getLocation() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void testLocation() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean open(Player p) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isOpen() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub

	}

	@Override
	public void addMoney(int money) {
		// TODO Auto-generated method stub

	}

	@Override
	public int getMoney() {
		// TODO Auto-generated method stub
		return 0;
	}

}

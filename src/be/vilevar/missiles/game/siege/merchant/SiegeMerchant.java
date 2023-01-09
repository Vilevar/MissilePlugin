package be.vilevar.missiles.game.siege.merchant;

import java.util.Arrays;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import be.vilevar.missiles.Main;
import be.vilevar.missiles.defense.defender.BigTeamDefender;
import be.vilevar.missiles.merchant.WeaponsMerchant;

public class SiegeMerchant implements WeaponsMerchant {

	private Main main = Main.i;
	private BigTeamDefender team;
	private Villager villager;
	private Location loc;
	private boolean defender;
	private int money;
	private Player open;
	
	private ItemStack moneyItem;
	
	public SiegeMerchant(BigTeamDefender team, Location loc, boolean defender) {
		this.team = team;
		
		double health = defender ? 500 : 250;
		
		this.villager = (Villager) loc.getWorld().spawnEntity(loc, EntityType.VILLAGER);
		this.villager.setAI(false);
		this.villager.setGravity(false);
		this.villager.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(health);
		this.villager.setHealth(health);
		this.loc = loc;
		
		this.defender = defender;
		
		this.moneyItem = new ItemStack(Material.EMERALD);
		ItemMeta im = this.moneyItem.getItemMeta();
		im.setLore(Arrays.asList("§6Clickez ici pour §dme soigner§6."));
		this.moneyItem.setItemMeta(im);
		
		merchants.add(this);
	}
	
	@Override
	public Villager getVillager() {
		return this.villager;
	}
	
	@Override
	public SiegeMerchant getAsSiegeMerchant() {
		return this;
	}
	
	public boolean isDefender() {
		return defender;
	}
	
	@Override
	public boolean canBeHurtBy(Player p) {
		if(this.team.equals(this.main.getTeamDefender(p)) || (main.hasGame() && !main.getGame().isStarted())) {
			return false;
		}
		return true;
	}
	
	@Override
	public Location getLocation() {
		return loc;
	}
	
	@Override
	public void testLocation() {
		if(villager.isDead())
			return;
		if(villager.getLocation().distance(loc) > 1) {
			villager.teleport(loc);
		}
	}
	
	
	@Override
	public boolean open(Player p) {
		// Enemies can open too
		if(open != null)
			return false;
		
		// Ally
		if(this.team.equals(this.main.getTeamDefender(p))) {
			this.main.getCustomElementManager().getSiegeMerchantListener().registerOpenMerchant(p, this);
			this.open = p;
			return true;
		}
		// Enemy (to destroy it)
		else if(this.defender) {
			this.main.getCustomElementManager().getSiegeMerchantListener().registerOpenEnemyMerchant(p, this);
			this.open = p;
			return true;
		}
		
		return false;
	}
	
	@Override
	public boolean isOpen() {
		return this.open != null;
	}
	
	public Player getOpenPlayer() {
		return this.open;
	}
	
	@Override
	public void close() {
		this.open = null;
	}
	
	
	@Override
	public void addMoney(int money) {
		this.money += money;
		if(this.open != null) {
			this.updateMoneyItem();
		}
	}
	
	@Override
	public int getMoney() {
		return money;
	}
	
	
	public ItemStack updateMoneyItem() {
		ItemMeta im = this.moneyItem.getItemMeta();
		im.setDisplayName("§aArgent : §6"+money);
		im.setLore(Arrays.asList("§6Vie : §a"+this.villager.getHealth()+"§6/§c"+this.villager.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue()));
		this.moneyItem.setItemMeta(im);
		return this.moneyItem;
	}
	

}

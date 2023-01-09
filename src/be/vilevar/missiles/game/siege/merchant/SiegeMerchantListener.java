package be.vilevar.missiles.game.siege.merchant;

import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import be.vilevar.missiles.Main;
import be.vilevar.missiles.mcelements.CustomElementManager;

public class SiegeMerchantListener implements Listener {

	private Main main = Main.i;
	private HashMap<Integer, SiegeMerchantItem> defenderItems = new HashMap<>();
	private HashMap<Integer, SiegeMerchantItem> attackerItems = new HashMap<>();
	private HashMap<UUID, SiegeMerchant> allyOpen = new HashMap<>();
	private HashMap<UUID, SiegeMerchant> enemyOpen = new HashMap<>();
	private HashSet<UUID> attacks = new HashSet<>();
	
	private Inventory enemyInv;
	
	public SiegeMerchantListener() {
		this.registerItem(new SiegeMerchantItem(CustomElementManager.SNIPER.createItem(), 1, 0));
		this.registerItem(new SiegeMerchantItem(CustomElementManager.SNIPER.getAmmunition().create(5), 1, 9));
		this.registerItem(new SiegeMerchantItem(CustomElementManager.MACHINE_GUN.createItem(), 1, 1));
		this.registerItem(new SiegeMerchantItem(CustomElementManager.MACHINE_GUN.getAmmunition().create(5), 1, 10));
		this.registerItem(new SiegeMerchantItem(CustomElementManager.SHOTGUN.createItem(), 1, 2));
		this.registerItem(new SiegeMerchantItem(CustomElementManager.SHOTGUN.getAmmunition().create(5), 1, 11));
		this.registerItem(new SiegeMerchantItem(CustomElementManager.PISTOL.createItem(), 1, 3));
		this.registerItem(new SiegeMerchantItem(CustomElementManager.PISTOL.getAmmunition().create(5), 1, 12));
		
		this.registerItem(new SiegeMerchantItem(CustomElementManager.BOMB.create(3), 1, 5));
		this.registerItem(new SiegeMerchantItem(CustomElementManager.SMOKE_BOMB.create(1), 1, 14));
		
		this.registerItem(new SiegeMerchantItem(CustomElementManager.MINE.create(3), 1, 6), true, false);
		this.registerItem(new SiegeMerchantItem(CustomElementManager.CLAYMORE.create(1), 1, 15), true, false);
		
		this.registerItem(new SiegeMerchantItem(CustomElementManager.PLIERS.create(), 3, 6), false, true);
		this.registerItem(new SiegeMerchantItem(this.createExplosionArmor(), 10, 15), false, true);
		
		this.registerItem(new SiegeMerchantItem(new ItemStack(Material.LADDER, 16), 1, 8));
		this.registerItem(new SiegeMerchantItem(new ItemStack(Material.IRON_PICKAXE), 1, 17));
		
		this.registerItem(new SiegeMerchantItem(new ItemStack(Material.IRON_HELMET), 1, 36));
		this.registerItem(new SiegeMerchantItem(new ItemStack(Material.IRON_CHESTPLATE), 1, 37));
		this.registerItem(new SiegeMerchantItem(new ItemStack(Material.IRON_LEGGINGS), 1, 38));
		this.registerItem(new SiegeMerchantItem(new ItemStack(Material.IRON_BOOTS), 1, 39));
		
		this.registerItem(new SiegeMerchantItem(new ItemStack(Material.IRON_SWORD), 1, 28));
		this.registerItem(new SiegeMerchantItem(new ItemStack(Material.COOKED_BEEF, 32), 1, 46));
		
		this.registerItem(new SiegeMerchantItem(CustomElementManager.HOWITZER.create(), 2, 32));
		this.registerItem(new SiegeMerchantItem(CustomElementManager.RANGEFINDER.create(), 1, 41));
		this.registerItem(new SiegeMerchantItem(CustomElementManager.SMALL_SHELL.create(3), 1, 33));
		this.registerItem(new SiegeMerchantItem(CustomElementManager.BIG_SHELL.create(), 2, 42));
		
		this.enemyInv = main.getServer().createInventory(null, 9, "§cEnemy SiegeMerchant");
		
		ItemStack damage = new ItemStack(Material.REDSTONE_BLOCK);
		ItemMeta im = damage.getItemMeta();
		im.setDisplayName("§450 dégâts§c à l'ennemi.");
		damage.setItemMeta(im);
		this.enemyInv.setItem(4, damage);
	}

	
	
	private void registerItem(SiegeMerchantItem item) {
		this.registerItem(item, false, false);
	}
	
	private void registerItem(SiegeMerchantItem item, boolean defenderOnly, boolean attackerOnly) {
		if(!attackerOnly)
			this.defenderItems.put(item.getSlot(), item);
		if(!defenderOnly)
			this.attackerItems.put(item.getSlot(), item);
	}
	
	
	public void registerOpenMerchant(Player p, SiegeMerchant merchant) {
		Inventory inv = main.getServer().createInventory(merchant.getVillager(), 54, "§eSiegeMerchant");
		
		for(SiegeMerchantItem item : (merchant.isDefender() ? this.defenderItems : this.attackerItems).values()) {
			inv.setItem(item.getSlot(), item.getItem());
		}
		inv.setItem(53, merchant.updateMoneyItem());
		
		p.openInventory(inv);
		
		this.allyOpen.put(p.getUniqueId(), merchant);
	}
	
	public void registerOpenEnemyMerchant(Player p, SiegeMerchant merchant) {
		p.openInventory(this.enemyInv);
		this.enemyOpen.put(p.getUniqueId(), merchant);
	}
	
	
	@EventHandler
	public void onClose(InventoryCloseEvent e) {
		UUID id = e.getPlayer().getUniqueId();
		if(this.allyOpen.containsKey(id)) {
			SiegeMerchant merchant = this.allyOpen.get(id);
			merchant.close();
			this.allyOpen.remove(id);
		} else if(this.enemyOpen.containsKey(id)) {
			SiegeMerchant merchant = this.enemyOpen.get(id);
			merchant.close();
			this.enemyOpen.remove(id);
		}
	}
	
	@EventHandler
	public void onClick(InventoryClickEvent e) {
		HumanEntity p = e.getWhoClicked();
		UUID id = p.getUniqueId();
		if(this.allyOpen.containsKey(id)) {
			e.setCancelled(true);
			SiegeMerchant merchant = this.allyOpen.get(id);
			
			HashMap<Integer, SiegeMerchantItem> items = merchant.isDefender() ? this.defenderItems : this.attackerItems;
			
			if(e.getRawSlot() == 53) {
				Villager villager = merchant.getVillager();
				if(villager.isDead()) {
					p.sendMessage("§cVotre marchand est §4mort§c, c'est trop tard.");
				} else {
					double d = villager.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue() - villager.getHealth();
					if(d > 0) {
						double bonus = Math.max(d, merchant.getMoney());
						villager.setHealth(villager.getHealth() + bonus);
						merchant.addMoney((int) -bonus);
						p.sendMessage("§aVotre marchand est §dguéri§a de §b"+bonus+" points de vie§a.");
						e.getClickedInventory().setItem(53, merchant.updateMoneyItem());
					} else {
						p.sendMessage("§6Votre marchand a déjà sa vie §emaximale§6.");
					}
				}
			} else if(items.containsKey(e.getSlot()) && e.getCurrentItem() != null && e.getCurrentItem().getType() != Material.AIR) {
				SiegeMerchantItem item = items.get(e.getSlot());
				if(merchant.getMoney() >= item.getPrice()) {
					merchant.addMoney(-item.getPrice());
					p.getInventory().addItem(e.getCurrentItem());
					e.getClickedInventory().setItem(53, merchant.updateMoneyItem());
				} else {
					p.sendMessage("§cPas assez d'argent (§6"+merchant.getMoney()+"§c/§4"+item.getPrice()+"§c).");
				}
			}
			
		} else if(this.enemyOpen.containsKey(id)) {
			e.setCancelled(true);
			SiegeMerchant merchant = this.enemyOpen.get(id);
			
			if(e.getRawSlot() == 4) {
				if(this.attacks.add(id) && !merchant.getVillager().isDead()) {
					merchant.getVillager().damage(50, p);
					this.main.getServer().getScheduler().runTaskLater(main, () -> this.attacks.remove(id), 25);
					for(Player online : this.main.getServer().getOnlinePlayers()) {
						online.sendTitle("§4BOOM !", null, 0, 20, 0);
					}
				}
			}
		}
	}
	

	private ItemStack createExplosionArmor() {
		ItemStack is = new ItemStack(Material.CHAINMAIL_HELMET);
		ItemMeta im = is.getItemMeta();
		im.addEnchant(Enchantment.PROTECTION_EXPLOSIONS, 2, true);
		is.setItemMeta(im);
		return is;
	}

}

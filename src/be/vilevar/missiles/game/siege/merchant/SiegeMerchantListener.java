package be.vilevar.missiles.game.siege.merchant;

import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import be.vilevar.missiles.Main;
import be.vilevar.missiles.mcelements.CustomElementManager;

public class SiegeMerchantListener implements Listener {

	private Main main = Main.i;
	private HashMap<Integer, SiegeMerchantItem> defenderItems = new HashMap<>();
	private HashMap<Integer, SiegeMerchantItem> attackerItems = new HashMap<>();
	private HashMap<UUID, SiegeMerchant> allyOpen = new HashMap<>();
	private HashMap<UUID, SiegeMerchant> enemyOpen = new HashMap<>();
	private HashSet<UUID> attacks = new HashSet<>();
	
	private Color defenseArmorColor = Color.OLIVE;
	private Color attackArmorColor = Color.MAROON;
	
	private AttributeModifier helmetArmor = new AttributeModifier(new UUID(13072025, 0), "armor-helmet-defense", 2, Operation.ADD_NUMBER, EquipmentSlot.HEAD);
	private AttributeModifier chestplateArmor = new AttributeModifier(new UUID(13072025, 1), "armor-chestplate-defense", 6, Operation.ADD_NUMBER, EquipmentSlot.HEAD);
	private AttributeModifier leggingsArmor = new AttributeModifier(new UUID(13072025, 2), "armor-leggings-defense", 5, Operation.ADD_NUMBER, EquipmentSlot.HEAD);
	private AttributeModifier bootsArmor = new AttributeModifier(new UUID(13072025, 3), "armor-boots-defense", 2, Operation.ADD_NUMBER, EquipmentSlot.HEAD);
	
	private Inventory enemyInv;
	
	public SiegeMerchantListener() {
		this.registerItem(new SiegeMerchantItem(CustomElementManager.SNIPER.createItem(), 60, 0));
		this.registerItem(new SiegeMerchantItem(CustomElementManager.SNIPER.getAmmunition().create(), 20, 9));
		this.registerItem(new SiegeMerchantItem(CustomElementManager.MACHINE_GUN.createItem(), 45, 1));
		this.registerItem(new SiegeMerchantItem(CustomElementManager.MACHINE_GUN.getAmmunition().create(), 10, 10));
		this.registerItem(new SiegeMerchantItem(CustomElementManager.SHOTGUN.createItem(), 45, 2));
		this.registerItem(new SiegeMerchantItem(CustomElementManager.SHOTGUN.getAmmunition().create(), 10, 11));
		this.registerItem(new SiegeMerchantItem(CustomElementManager.PISTOL.createItem(), 30, 3));
		this.registerItem(new SiegeMerchantItem(CustomElementManager.PISTOL.getAmmunition().create(), 5, 12));
		
		this.registerItem(new SiegeMerchantItem(CustomElementManager.BOMB.create(), 5, 5));
		this.registerItem(new SiegeMerchantItem(CustomElementManager.SMOKE_BOMB.create(), 10, 14));
		
		this.registerItem(new SiegeMerchantItem(CustomElementManager.MINE.create(), 10, 6), true, false);
		this.registerItem(new SiegeMerchantItem(CustomElementManager.CLAYMORE.create(), 15, 15), true, false);
		
		this.registerItem(new SiegeMerchantItem(CustomElementManager.PLIERS.create(), 15, 6), false, true);
		this.registerItem(new SiegeMerchantItem(this.createExplosionArmor(), 10, 15), false, true);
		
		this.registerItem(new SiegeMerchantItem(new ItemStack(Material.LADDER, 16), 1, 8));
		this.registerItem(new SiegeMerchantItem(new ItemStack(Material.IRON_PICKAXE), 2, 17));
		
		this.registerItem(new SiegeMerchantItem(this.createHelmet(false, false), 10, 36), true, false);
		this.registerItem(new SiegeMerchantItem(this.createChestplate(false, false), 25, 37), true, false);
		this.registerItem(new SiegeMerchantItem(this.createLeggings(false, false), 20, 38), true, false);
		this.registerItem(new SiegeMerchantItem(this.createBoots(false, false), 10, 39), true, false);
		this.registerItem(new SiegeMerchantItem(this.createHelmet(true, false), 10, 36), false, true);
		this.registerItem(new SiegeMerchantItem(this.createChestplate(true, false), 25, 37), false, true);
		this.registerItem(new SiegeMerchantItem(this.createLeggings(true, false), 20, 38), false, true);
		this.registerItem(new SiegeMerchantItem(this.createBoots(true, false), 10, 39), false, true);
		
		this.registerItem(new SiegeMerchantItem(this.createHelmet(false, true), 20, 45), true, false);
		this.registerItem(new SiegeMerchantItem(this.createChestplate(false, true), 50, 46), true, false);
		this.registerItem(new SiegeMerchantItem(this.createLeggings(false, true), 40, 47), true, false);
		this.registerItem(new SiegeMerchantItem(this.createBoots(false, true), 20, 48), true, false);
		this.registerItem(new SiegeMerchantItem(this.createHelmet(true, true), 20, 45), false, true);
		this.registerItem(new SiegeMerchantItem(this.createChestplate(true, true), 50, 46), false, true);
		this.registerItem(new SiegeMerchantItem(this.createLeggings(true, true), 40, 47), false, true);
		this.registerItem(new SiegeMerchantItem(this.createBoots(true, true), 20, 48), false, true);
		
		this.registerItem(new SiegeMerchantItem(new ItemStack(Material.IRON_SWORD), 10, 28));
		this.registerItem(new SiegeMerchantItem(new ItemStack(Material.COOKED_BEEF, 32), 1, 29));
		
		this.registerItem(new SiegeMerchantItem(CustomElementManager.HOWITZER.create(), 60, 32));
		this.registerItem(new SiegeMerchantItem(CustomElementManager.RANGEFINDER.create(), 20, 41));
		this.registerItem(new SiegeMerchantItem(CustomElementManager.SMALL_SHELL.create(2), 5, 33));
		this.registerItem(new SiegeMerchantItem(CustomElementManager.BIG_SHELL.create(), 5, 42));
		
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
	
	
	
	private ItemStack createHelmet(boolean attacker, boolean arrowProtection) {
		ItemStack is = new ItemStack(Material.LEATHER_HELMET);
		LeatherArmorMeta im = (LeatherArmorMeta) is.getItemMeta();
		im.addAttributeModifier(Attribute.GENERIC_ARMOR, this.helmetArmor);
		im.setColor(attacker ? this.attackArmorColor : this.defenseArmorColor);
		if(arrowProtection) {
			im.addEnchant(Enchantment.PROTECTION_PROJECTILE, 2, true);
		}
		is.setItemMeta(im);
		return is;
	}
	
	private ItemStack createChestplate(boolean attacker, boolean arrowProtection) {
		ItemStack is = new ItemStack(Material.LEATHER_CHESTPLATE);
		LeatherArmorMeta im = (LeatherArmorMeta) is.getItemMeta();
		im.addAttributeModifier(Attribute.GENERIC_ARMOR, this.chestplateArmor);
		im.setColor(attacker ? this.attackArmorColor : this.defenseArmorColor);
		if(arrowProtection) {
			im.addEnchant(Enchantment.PROTECTION_PROJECTILE, 2, true);
		}
		is.setItemMeta(im);
		return is;
	}
	
	private ItemStack createLeggings(boolean attacker, boolean arrowProtection) {
		ItemStack is = new ItemStack(Material.LEATHER_LEGGINGS);
		LeatherArmorMeta im = (LeatherArmorMeta) is.getItemMeta();
		im.addAttributeModifier(Attribute.GENERIC_ARMOR, this.leggingsArmor);
		im.setColor(attacker ? this.attackArmorColor : this.defenseArmorColor);
		if(arrowProtection) {
			im.addEnchant(Enchantment.PROTECTION_PROJECTILE, 2, true);
		}
		is.setItemMeta(im);
		return is;
	}
	
	private ItemStack createBoots(boolean attacker, boolean arrowProtection) {
		ItemStack is = new ItemStack(Material.LEATHER_BOOTS);
		LeatherArmorMeta im = (LeatherArmorMeta) is.getItemMeta();
		im.addAttributeModifier(Attribute.GENERIC_ARMOR, this.bootsArmor);
		im.setColor(attacker ? this.attackArmorColor : this.defenseArmorColor);
		if(arrowProtection) {
			im.addEnchant(Enchantment.PROTECTION_PROJECTILE, 2, true);
		}
		is.setItemMeta(im);
		return is;
	}

}

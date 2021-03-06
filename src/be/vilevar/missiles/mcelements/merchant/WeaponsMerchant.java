package be.vilevar.missiles.mcelements.merchant;

import static be.vilevar.missiles.mcelements.merchant.RecipeAdvancement.ENGINE_1;
import static be.vilevar.missiles.mcelements.merchant.RecipeAdvancement.HOWITZER;
import static be.vilevar.missiles.mcelements.merchant.RecipeAdvancement.PISTOL;
import static be.vilevar.missiles.mcelements.merchant.RecipeAdvancement.PROPELLANT_1;
import static be.vilevar.missiles.mcelements.merchant.RecipeAdvancement.RADAR;
import static be.vilevar.missiles.mcelements.merchant.RecipeAdvancement.TNT;

import java.util.ArrayList;
import java.util.Arrays;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Merchant;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SpawnEggMeta;
import org.bukkit.scoreboard.Team;

import be.vilevar.missiles.Main;
import be.vilevar.missiles.mcelements.CustomElementManager;

public class WeaponsMerchant {

	private static final ArrayList<WeaponsMerchant> merchants = new ArrayList<>();
	
	public static WeaponsMerchant getMerchant(Villager villager) {
		for(WeaponsMerchant merchant : merchants) {
			if(merchant.villager.getUniqueId().equals(villager.getUniqueId())) {
				return merchant;
			}
		}
		return null;
	}
	
	public static void killMerchants() {
		for(WeaponsMerchant merchant : merchants) {
			merchant.villager.damage(500000);
		}
	}
	
	public static ArrayList<WeaponsMerchant> getMerchants() {
		return new ArrayList<>(merchants);
	}
	
	private Main main = Main.i;
	private ArrayList<RecipeAdvancement> researched = new ArrayList<>();
	private ArrayList<RecipeAdvancement> canResearch = new ArrayList<>();
	private Merchant research = main.getServer().createMerchant("§2Recherche");
	private Merchant development = main.getServer().createMerchant("§1Développement");
	private Merchant utilitaries = main.getServer().createMerchant("§eUtilitaires");
	private Team team;
	private Villager villager;
	private Location loc;
	private int money;
	private WeaponsMerchantStage open;
	
	private ItemStack utilitariesItem, researchItem, developmentItem, moneyItem;
	
	public WeaponsMerchant(Team team, Location loc) {
		this.canResearch.addAll(Arrays.asList(PISTOL, TNT, ENGINE_1, PROPELLANT_1, RADAR, HOWITZER));
		
		this.updateResearchMerchant();
		
		this.development.setRecipes(new ArrayList<>());
		
		this.utilitaries.setRecipes(this.getUtilitaries(team));
		
		this.team = team;
		this.villager = (Villager) loc.getWorld().spawnEntity(loc, EntityType.VILLAGER);
		this.villager.setAI(false);
		this.villager.setGravity(false);
		this.villager.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(500);
		this.villager.setHealth(500);
		this.loc = loc;
		
		
		this.moneyItem = new ItemStack(Material.EMERALD);
		
		this.utilitariesItem = new ItemStack(Material.STICK);
		ItemMeta im = utilitariesItem.getItemMeta();
		im.setDisplayName("§eUtilitaires");
		utilitariesItem.setItemMeta(im);
		
		this.researchItem = new ItemStack(Material.STICK);
		im = researchItem.getItemMeta();
		im.setDisplayName("§2Recherche");
		researchItem.setItemMeta(im);
		
		this.developmentItem = new ItemStack(Material.STICK);
		im = developmentItem.getItemMeta();
		im.setDisplayName("§1Développement");
		developmentItem.setItemMeta(im);
		
		merchants.add(this);
	}
	
	public void testLocation() {
		if(villager.getLocation().distance(loc) > 1) {
			villager.teleport(loc);
		}
	}
	
	public boolean canBeHurtBy(Player p) {
		if(team != null && team.hasEntry(p.getName())) {
			return false;
		}
		this.testLocation();
		if(p.getLocation().distance(loc) < 7) {
			return false;
		}
		return true;
	}
	
	public Location getLocation() {
		return loc;
	}
	
	public boolean open(HumanEntity p) {
		if(open != null || (team != null && !team.hasEntry(p.getName())))
			return false;
		
		p.openInventory(this.createMenuInventory());
		this.open = WeaponsMerchantStage.HOME;
		
		return true;
	}
	
	public WeaponsMerchantStage getOpenStage() {
		return this.open;
	}
	
	public void close() {
		this.open = null;
	}
	
	
	public RecipeAdvancement getResearchRecipeAdvancement(int index) {
		return this.canResearch.get(index);
	}
	
	public boolean hasResearched(RecipeAdvancement adv) {
		return this.researched.contains(adv);
	}
	
	public void research(RecipeAdvancement adv) {
		this.researched.add(adv);
		
		for(RecipeAdvancement next : adv.getNext()) {
			if(next.isUnlocked(this)) {
				this.canResearch.add(next);
			}
		}
		this.updateResearchMerchant();
		
		ArrayList<MerchantRecipe> developments = new ArrayList<>(this.development.getRecipes());
		developments.addAll(Arrays.asList(adv.getDevelopment()));
		this.development.setRecipes(developments);
	}
	
	
	
	private void updateResearchMerchant() {
		ArrayList<MerchantRecipe> research = new ArrayList<>();
		for(RecipeAdvancement recipe : this.canResearch) {
			if(this.researched.contains(recipe)) {
				MerchantRecipe r = new MerchantRecipe(recipe.getResult(), 1, 1, false);
				r.setIngredients(recipe.getIngredients());
				research.add(r);
			} else {
				research.add(recipe);
			}
		}
		this.research.setRecipes(research);
	}
	
	
	public void addMoney(int money) {
		this.money += money;
		if(this.open != null) {
			this.updateMoneyItem();
		}
	}
	
	public int getMoney() {
		return money;
	}
	
	public ItemStack updateMoneyItem() {
		ItemMeta im = this.moneyItem.getItemMeta();
		im.setDisplayName("§6"+money+" §aÉmeraude");
		this.moneyItem.setItemMeta(im);
		return this.moneyItem;
	}
	
	private Inventory createMenuInventory() {
		Inventory inv = main.getServer().createInventory(villager, 9, "§eMenu");
		
		inv.setItem(8, this.updateMoneyItem());
		
		inv.setItem(2, this.utilitariesItem);
		inv.setItem(3, this.researchItem);
		inv.setItem(4, this.developmentItem);
		
		return inv;
	}
	
	public void openUtilitaries(HumanEntity p) {
		this.open = WeaponsMerchantStage.UTILITARIES;
		InventoryView inv = p.openMerchant(utilitaries, false);
		if(inv == null) {
			this.open = WeaponsMerchantStage.HOME;
		}
	}
	
	public void openResearch(HumanEntity p) {
		this.open = WeaponsMerchantStage.RESEARCH;
		InventoryView inv = p.openMerchant(research, false);
		if(inv == null) {
			this.open = WeaponsMerchantStage.HOME;
		}
	}
	
	public void openDevelopment(HumanEntity p) {
		this.open = WeaponsMerchantStage.DEVELOPMENT;
		InventoryView inv = p.openMerchant(development, false);
		if(inv == null) {
			this.open = WeaponsMerchantStage.HOME;
		}
	}
	
	
	
	public static enum WeaponsMerchantStage {
		HOME,
		UTILITARIES,
		RESEARCH,
		DEVELOPMENT;
	}

	
	private ArrayList<MerchantRecipe> getUtilitaries(Team team) {
		ArrayList<MerchantRecipe> utilitaries = new ArrayList<>(Arrays.asList(
				new DevelopmentRecipe(new ItemStack(Material.OBSIDIAN, 32), 5),
				new DevelopmentRecipe(CustomElementManager.RANGEFINDER.create(), 2)));
		
		ItemStack is = new ItemStack(Material.HORSE_SPAWN_EGG);
		SpawnEggMeta horse = (SpawnEggMeta) is.getItemMeta();
		is.setItemMeta(horse);
		utilitaries.add(new DevelopmentRecipe(is, 20));
		
		utilitaries.addAll(Arrays.asList(
				new DevelopmentRecipe(new ItemStack(Material.CHEST), 1),
				new DevelopmentRecipe(new ItemStack(Material.LADDER, 32), 2),
				
				new DevelopmentRecipe(new ItemStack(Material.COOKED_BEEF, 10), 2),
				new DevelopmentRecipe(new ItemStack(Material.GOLDEN_APPLE), 2),
				new DevelopmentRecipe(new ItemStack(Material.ENCHANTED_GOLDEN_APPLE), 2),
				
				new DevelopmentRecipe(new ItemStack(Material.IRON_SWORD), 2),
				new DevelopmentRecipe(new ItemStack(Material.DIAMOND_SWORD), 2),
				new DevelopmentRecipe(new ItemStack(Material.DIAMOND_AXE), 2)));
		
		is = new ItemStack(Material.IRON_PICKAXE);
		ItemMeta im = is.getItemMeta();
		im.addEnchant(Enchantment.DIG_SPEED, 6, true);
		is.setItemMeta(im);
		utilitaries.add(new DevelopmentRecipe(is, 20));
		
		is = new ItemStack(Material.IRON_AXE);
		im = is.getItemMeta();
		im.addEnchant(Enchantment.DIG_SPEED, 6, true);
		is.setItemMeta(im);
		utilitaries.add(new DevelopmentRecipe(is, 20));
		
		is = new ItemStack(Material.DIAMOND_PICKAXE);
		im = is.getItemMeta();
		im.addEnchant(Enchantment.DIG_SPEED, 4, true);
		is.setItemMeta(im);
		utilitaries.add(new DevelopmentRecipe(is, 20));
		
		utilitaries.addAll(Arrays.asList(
				new DevelopmentRecipe(new ItemStack(Material.CHEST), 1),
				new DevelopmentRecipe(new ItemStack(Material.IRON_HELMET), 2),
				new DevelopmentRecipe(new ItemStack(Material.IRON_CHESTPLATE), 2),
				new DevelopmentRecipe(new ItemStack(Material.IRON_LEGGINGS), 2),
				new DevelopmentRecipe(new ItemStack(Material.IRON_BOOTS), 2),
				
				new DevelopmentRecipe(new ItemStack(Material.DIAMOND_HELMET), 2),
				new DevelopmentRecipe(new ItemStack(Material.DIAMOND_CHESTPLATE), 2),
				new DevelopmentRecipe(new ItemStack(Material.DIAMOND_LEGGINGS), 2),
				new DevelopmentRecipe(new ItemStack(Material.DIAMOND_BOOTS), 2),
				
				new DevelopmentRecipe(new ItemStack(Material.NETHERITE_HELMET), 2),
				new DevelopmentRecipe(new ItemStack(Material.NETHERITE_CHESTPLATE), 2),
				new DevelopmentRecipe(new ItemStack(Material.NETHERITE_LEGGINGS), 2),
				new DevelopmentRecipe(new ItemStack(Material.NETHERITE_BOOTS), 2)));
		
		return utilitaries;
	}
}

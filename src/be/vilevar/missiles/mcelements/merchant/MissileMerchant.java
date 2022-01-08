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
import org.bukkit.craftbukkit.v1_17_R1.inventory.CraftItemStack;
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
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;

import com.mojang.brigadier.exceptions.CommandSyntaxException;

import be.vilevar.missiles.Main;
import be.vilevar.missiles.defense.Defender;
import be.vilevar.missiles.defense.defender.TeamDefender;
import be.vilevar.missiles.game.Game;
import be.vilevar.missiles.mcelements.CustomElementManager;
import net.minecraft.nbt.MojangsonParser;
import net.minecraft.nbt.NBTTagCompound;

public class MissileMerchant implements WeaponsMerchant {

	private Main main = Main.i;
	private ArrayList<RecipeAdvancement> researched = new ArrayList<>();
	private ArrayList<RecipeAdvancement> canResearch = new ArrayList<>();
	private Merchant research = main.getServer().createMerchant("§2Recherche");
	private Merchant development = main.getServer().createMerchant("§1Développement");
	private Merchant utilitaries = main.getServer().createMerchant("§eUtilitaires");
	private Defender defender;
	private Villager villager;
	private Location loc;
	private int money;
	private TeamDefender enemy;
	private WeaponsMerchantStage open;
	
	private ItemStack utilitariesItem, researchItem, developmentItem, attackItem, healthItem, moneyItem;
	
	public MissileMerchant(Defender defender, Location loc) {
		this.defender = defender;
		
		this.canResearch.addAll(Arrays.asList(PISTOL, TNT, ENGINE_1, PROPELLANT_1, RADAR, HOWITZER));
		
		this.updateResearchMerchant();
		
		this.development.setRecipes(new ArrayList<>());
		
		this.utilitaries.setRecipes(this.getUtilitaries(defender));
		
		this.villager = (Villager) loc.getWorld().spawnEntity(loc, EntityType.VILLAGER);
		this.villager.setAI(false);
		this.villager.setGravity(false);
		this.villager.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(500);
		this.villager.setHealth(500);
		this.loc = loc;
		
		
		this.utilitariesItem = new ItemStack(Material.DIAMOND_PICKAXE);
		ItemMeta im = utilitariesItem.getItemMeta();
		im.setDisplayName("§eUtilitaires");
		utilitariesItem.setItemMeta(im);
		
		this.researchItem = new ItemStack(Material.BREWING_STAND);
		im = researchItem.getItemMeta();
		im.setDisplayName("§2Recherche");
		researchItem.setItemMeta(im);
		
		this.developmentItem = new ItemStack(Material.ANVIL);
		im = developmentItem.getItemMeta();
		im.setDisplayName("§1Développement");
		im.setCustomModelData(3);
		developmentItem.setItemMeta(im);
		
		this.attackItem = new ItemStack(Material.IRON_SWORD);
		Game game = main.getGame();
		if(game != null) {
			this.enemy = game.getTeamCapitalism().equals(this.defender) ? game.getTeamCommunism() : game.getTeamCapitalism();
		}
		
		this.healthItem = new ItemStack(Material.POTION);
		PotionMeta potion = (PotionMeta) this.healthItem.getItemMeta();
		potion.setBasePotionData(new PotionData(PotionType.REGEN));
		this.healthItem.setItemMeta(potion);
		
		this.moneyItem = new ItemStack(Material.EMERALD);
		
		merchants.add(this);
	}
	
	@Override
	public Villager getVillager() {
		return this.villager;
	}
	
	@Override
	public MissileMerchant getAsMissileMerchant() {
		return this;
	}
	
	
	public boolean canBeHurtBy(Player p) {
		if(this.defender.equals(this.main.getDefender(p)) || (main.getGame() != null && !main.getGame().isStarted())) {
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
	
	public void testLocation() {
		if(villager.getLocation().distance(loc) > 1) {
			villager.teleport(loc);
		}
	}
	
	
	public boolean open(Player p) {
		if(open != null || !this.defender.equals(this.main.getDefender(p)))
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
	
	public ItemStack updateAttackItem() {
		if(this.enemy != null && this.enemy.getMerchant() != null) {
			ItemMeta im = this.attackItem.getItemMeta();
			im.setDisplayName("§6Il reste §c"+Main.round(this.enemy.getMerchant().villager.getHealth())+" PV§6 à mettre.");
			this.attackItem.setItemMeta(im);
			return this.attackItem;
		}
		return null;
	}
	
	public ItemStack updateHealthItem() {
		ItemMeta im = this.healthItem.getItemMeta();
		im.setDisplayName("§6"+Main.round(this.villager.getHealth())+" §dPV");
		this.healthItem.setItemMeta(im);
		return this.healthItem;
	}
	
	public ItemStack updateMoneyItem() {
		ItemMeta im = this.moneyItem.getItemMeta();
		im.setDisplayName("§6"+money+" §aÉmeraude");
		this.moneyItem.setItemMeta(im);
		return this.moneyItem;
	}
	
	private Inventory createMenuInventory() {
		Inventory inv = main.getServer().createInventory(villager, 9, "§eMenu");
		
		inv.setItem(2, this.utilitariesItem);
		inv.setItem(3, this.researchItem);
		inv.setItem(4, this.developmentItem);
		
		inv.setItem(6, this.updateAttackItem());
		inv.setItem(7, this.updateHealthItem());
		inv.setItem(8, this.updateMoneyItem());
		
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
	
	
	

	
	private ArrayList<MerchantRecipe> getUtilitaries(Defender defender) {
		ArrayList<MerchantRecipe> utilitaries = new ArrayList<>(Arrays.asList(
				new DevelopmentRecipe(new ItemStack(Material.OBSIDIAN, 32), 64),
				new DevelopmentRecipe(CustomElementManager.RANGEFINDER.create(), 5)));
		
		ItemStack is = new ItemStack(Material.HORSE_SPAWN_EGG);
		try {
			net.minecraft.world.item.ItemStack nmsIs = CraftItemStack.asNMSCopy(is);
			NBTTagCompound tagCompound = nmsIs.getTag();
			if(tagCompound == null) {
				tagCompound = new NBTTagCompound();
			}
			tagCompound.set("EntityTag", MojangsonParser.parse(this.defender.getHorseTag()));
			nmsIs.setTag(tagCompound);
			is = CraftItemStack.asBukkitCopy(nmsIs);
		} catch (CommandSyntaxException e) {
			e.printStackTrace();
		}
		utilitaries.add(new DevelopmentRecipe(is, 20));
		
		utilitaries.addAll(Arrays.asList(
				new DevelopmentRecipe(new ItemStack(Material.CHEST), 1),
				new DevelopmentRecipe(new ItemStack(Material.LADDER, 32), 2),
				
				new DevelopmentRecipe(new ItemStack(Material.COOKED_BEEF, 5), 1),
				new DevelopmentRecipe(new ItemStack(Material.GOLDEN_APPLE), 10),
				new DevelopmentRecipe(new ItemStack(Material.ENCHANTED_GOLDEN_APPLE), 30),
				
				new DevelopmentRecipe(new ItemStack(Material.IRON_SWORD), 5),
				new DevelopmentRecipe(new ItemStack(Material.DIAMOND_SWORD), 7),
				new DevelopmentRecipe(new ItemStack(Material.DIAMOND_AXE), 8)));
		
		is = new ItemStack(Material.IRON_PICKAXE);
		ItemMeta im = is.getItemMeta();
		im.addEnchant(Enchantment.DIG_SPEED, 6, true);
		is.setItemMeta(im);
		utilitaries.add(new DevelopmentRecipe(is, 15));
		
		is = new ItemStack(Material.IRON_AXE);
		im = is.getItemMeta();
		im.addEnchant(Enchantment.DIG_SPEED, 6, true);
		is.setItemMeta(im);
		utilitaries.add(new DevelopmentRecipe(is, 10));
		
		is = new ItemStack(Material.DIAMOND_PICKAXE);
		im = is.getItemMeta();
		im.addEnchant(Enchantment.DIG_SPEED, 4, true);
		is.setItemMeta(im);
		utilitaries.add(new DevelopmentRecipe(is, 20));
		
		utilitaries.addAll(Arrays.asList(
				new DevelopmentRecipe(new ItemStack(Material.IRON_HELMET), 5),
				new DevelopmentRecipe(new ItemStack(Material.IRON_CHESTPLATE), 10),
				new DevelopmentRecipe(new ItemStack(Material.IRON_LEGGINGS), 7),
				new DevelopmentRecipe(new ItemStack(Material.IRON_BOOTS), 5),
				
				new DevelopmentRecipe(new ItemStack(Material.DIAMOND_HELMET), 10),
				new DevelopmentRecipe(new ItemStack(Material.DIAMOND_CHESTPLATE), 20),
				new DevelopmentRecipe(new ItemStack(Material.DIAMOND_LEGGINGS), 15),
				new DevelopmentRecipe(new ItemStack(Material.DIAMOND_BOOTS), 10),
				
				new DevelopmentRecipe(new ItemStack(Material.NETHERITE_HELMET), 20),
				new DevelopmentRecipe(new ItemStack(Material.NETHERITE_CHESTPLATE), 50),
				new DevelopmentRecipe(new ItemStack(Material.NETHERITE_LEGGINGS), 30),
				new DevelopmentRecipe(new ItemStack(Material.NETHERITE_BOOTS), 20)));
		
		return utilitaries;
	}

}

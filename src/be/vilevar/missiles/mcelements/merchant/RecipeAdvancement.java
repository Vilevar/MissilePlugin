package be.vilevar.missiles.mcelements.merchant;

import java.util.ArrayList;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;

import be.vilevar.missiles.mcelements.CustomElementManager;

public class RecipeAdvancement extends MerchantRecipe {

	private RecipeAdvancement[] previous;
	private ArrayList<RecipeAdvancement> next = new ArrayList<>();
	private DevelopmentRecipe[] development;
	
	public RecipeAdvancement(RecipeAdvancement[] previous, ResearchRecipe recipe, DevelopmentRecipe[] development) {
		super(recipe.getResult(), 1);
		this.setIngredients(recipe.getPrice());
		
		this.previous = previous;
		this.development = development;
		
		for(RecipeAdvancement cond : previous)
			cond.addNext(this);
	}
	
	public RecipeAdvancement[] getPrevious() {
		return previous;
	}
	
	public ArrayList<RecipeAdvancement> getNext() {
		return next;
	}
	
	public DevelopmentRecipe[] getDevelopment() {
		return development;
	}
	
	public boolean isUnlocked(WeaponsMerchant merchant) {
		for(RecipeAdvancement cond : this.previous) {
			if(!merchant.hasResearched(cond)) {
				return false;
			}
		}
		return true;
	}
	
	private void addNext(RecipeAdvancement recipe) {
		this.next.add(recipe);
	}
	
	
	
	
	
	public static final RecipeAdvancement
		PISTOL = new RecipeAdvancement(new RecipeAdvancement[0], new ResearchRecipe("Pistolet", 2), new DevelopmentRecipe[] {
						new DevelopmentRecipe(CustomElementManager.PISTOL.createItem(), 1),
						new DevelopmentRecipe(CustomElementManager.PISTOL.getAmmunition().create(), 1)}),
		
		TNT = new RecipeAdvancement(new RecipeAdvancement[0], new ResearchRecipe("TNT", 1), new DevelopmentRecipe[] {
						new DevelopmentRecipe(new ItemStack(Material.TNT), 1),
						new DevelopmentRecipe(CustomElementManager.RV_CRAFT.create(), 5),
						new DevelopmentRecipe(CustomElementManager.REENTRY_VEHICLE.create(), 5)}),
		
		HOWITZER = new RecipeAdvancement(new RecipeAdvancement[0], new ResearchRecipe("Canon", 1), new DevelopmentRecipe[] {
						new DevelopmentRecipe(CustomElementManager.HOWITZER.create(), 1),
						new DevelopmentRecipe(CustomElementManager.WEATHER_FORECASTER.create(), 1)}),
		
		ENGINE_1 = new RecipeAdvancement(new RecipeAdvancement[0], new ResearchRecipe("Moteur 1", 3), new DevelopmentRecipe[] {
						new DevelopmentRecipe(CustomElementManager.ENGINE_1.create(), 6)}),
		
		PROPELLANT_1 = new RecipeAdvancement(new RecipeAdvancement[0], new ResearchRecipe("Carburant 1", 3), new DevelopmentRecipe[] {
						new DevelopmentRecipe(CustomElementManager.FUEL_1.create(), 5)}),
		
		RADAR = new RecipeAdvancement(new RecipeAdvancement[0], new ResearchRecipe("Radar", 4), new DevelopmentRecipe[] {
						new DevelopmentRecipe(CustomElementManager.RADAR.create(), 5)}),
		
		MACHINE_GUN = new RecipeAdvancement(new RecipeAdvancement[] {PISTOL}, new ResearchRecipe("Mitraillette", 3), new DevelopmentRecipe[] {
						new DevelopmentRecipe(CustomElementManager.MACHINE_GUN.createItem(), 1),
						new DevelopmentRecipe(CustomElementManager.MACHINE_GUN.getAmmunition().create(), 1)}),
		
		SHOTGUN = new RecipeAdvancement(new RecipeAdvancement[] {PISTOL}, new ResearchRecipe("Fusil à pompe", 3), new DevelopmentRecipe[] {
						new DevelopmentRecipe(CustomElementManager.SHOTGUN.createItem(), 1),
						new DevelopmentRecipe(CustomElementManager.SHOTGUN.getAmmunition().create(), 1)}),
		
		BOMB = new RecipeAdvancement(new RecipeAdvancement[] {PISTOL, TNT}, new ResearchRecipe("Grenade", 2), new DevelopmentRecipe[] {
						new DevelopmentRecipe(CustomElementManager.BOMB.create(), 1)}),
		
		SMALL_SHELL = new RecipeAdvancement(new RecipeAdvancement[] {HOWITZER, TNT}, new ResearchRecipe("Obus léger", 3), new DevelopmentRecipe[] {
						new DevelopmentRecipe(CustomElementManager.SMALL_SHELL.create(), 1)}),
		
		BIG_SHELL = new RecipeAdvancement(new RecipeAdvancement[] {SMALL_SHELL}, new ResearchRecipe("Obus lourd", 4), new DevelopmentRecipe[] {
						new DevelopmentRecipe(CustomElementManager.BIG_SHELL.create(), 1)}),
		
		ENGINE_2 = new RecipeAdvancement(new RecipeAdvancement[] {ENGINE_1}, new ResearchRecipe("Moteur 2", 4), new DevelopmentRecipe[] {
						new DevelopmentRecipe(CustomElementManager.ENGINE_2.create(), 6)}),
		
		PROPELLANT_2 = new RecipeAdvancement(new RecipeAdvancement[] {PROPELLANT_1}, new ResearchRecipe("Carburant 2", 4), new DevelopmentRecipe[] {
						new DevelopmentRecipe(CustomElementManager.FUEL_2.create(), 5)}),
		
		ENGINE_3 = new RecipeAdvancement(new RecipeAdvancement[] {ENGINE_2}, new ResearchRecipe("Moteur 3", 5), new DevelopmentRecipe[] {
						new DevelopmentRecipe(CustomElementManager.ENGINE_3.create(), 6)}),
		
		PROPELLANT_3 = new RecipeAdvancement(new RecipeAdvancement[] {PROPELLANT_2}, new ResearchRecipe("Carburant 3", 5), new DevelopmentRecipe[] {
						new DevelopmentRecipe(CustomElementManager.FUEL_3.create(), 5)}),
		
		SRBM = new RecipeAdvancement(new RecipeAdvancement[] {ENGINE_1, PROPELLANT_1}, new ResearchRecipe("SRBM", 6), new DevelopmentRecipe[] {
						new DevelopmentRecipe(CustomElementManager.SRBM.create(), 5),
						new DevelopmentRecipe(CustomElementManager.MISSILE_CRAFT.create(), 2),
						new DevelopmentRecipe(CustomElementManager.MISSILE_LAUNCHER.create(), 2)}),
		
		SNIPER = new RecipeAdvancement(new RecipeAdvancement[] {MACHINE_GUN, SHOTGUN}, new ResearchRecipe("Sniper", 5), new DevelopmentRecipe[] {
						new DevelopmentRecipe(CustomElementManager.SNIPER.createItem(), 1),
						new DevelopmentRecipe(CustomElementManager.SNIPER.getAmmunition().create(), 1)}),
		
		SMOKE = new RecipeAdvancement(new RecipeAdvancement[] {BOMB}, new ResearchRecipe("Fumigène", 2), new DevelopmentRecipe[] {
						new DevelopmentRecipe(CustomElementManager.SMOKE_BOMB.create(), 1)}),
		
		MINE = new RecipeAdvancement(new RecipeAdvancement[] {BOMB}, new ResearchRecipe("Mine", 2), new DevelopmentRecipe[] {
						new DevelopmentRecipe(CustomElementManager.MINE.create(), 2)}),
		
		CLAYMORE = new RecipeAdvancement(new RecipeAdvancement[] {MINE}, new ResearchRecipe("Claymore", 3), new DevelopmentRecipe[] {
						new DevelopmentRecipe(CustomElementManager.CLAYMORE.create(), 2)}),
		
		A_BOMB = new RecipeAdvancement(new RecipeAdvancement[] {TNT}, new ResearchRecipe("Bombe A", 5), new DevelopmentRecipe[] {
						new DevelopmentRecipe(CustomElementManager.A_BOMB.create(), 5)}),
		
		ABM = new RecipeAdvancement(new RecipeAdvancement[] {RADAR, SRBM}, new ResearchRecipe("ABM", 10), new DevelopmentRecipe[] {
						new DevelopmentRecipe(CustomElementManager.ABM_LAUNCHER.create(), 5),
						new DevelopmentRecipe(CustomElementManager.ABM.create(), 3)}),
		
		MRBM = new RecipeAdvancement(new RecipeAdvancement[] {SRBM}, new ResearchRecipe("MRBM", 10), new DevelopmentRecipe[] {
						new DevelopmentRecipe(CustomElementManager.MRBM.create(), 5)}),
		
		H_BOMB = new RecipeAdvancement(new RecipeAdvancement[] {A_BOMB}, new ResearchRecipe("Bombe H", 10), new DevelopmentRecipe[] {
						new DevelopmentRecipe(CustomElementManager.H_BOMB.create(), 5)}),
		
		MIRV = new RecipeAdvancement(new RecipeAdvancement[] {A_BOMB, MRBM}, new ResearchRecipe("Mirvage", 10), new DevelopmentRecipe[] {
						new DevelopmentRecipe(CustomElementManager.MIRV.create(), 5)}),
		
		ICBM = new RecipeAdvancement(new RecipeAdvancement[] {MRBM}, new ResearchRecipe("ICBM", 15), new DevelopmentRecipe[] {
						new DevelopmentRecipe(CustomElementManager.ICBM.create(), 5)});
}

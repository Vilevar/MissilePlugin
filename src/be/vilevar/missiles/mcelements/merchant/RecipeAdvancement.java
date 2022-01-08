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
	
	public boolean isUnlocked(MissileMerchant merchant) {
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
		PISTOL = new RecipeAdvancement(new RecipeAdvancement[0], new ResearchRecipe("Pistolet", 10), new DevelopmentRecipe[] {
						new DevelopmentRecipe(CustomElementManager.PISTOL.createItem(), 5),
						new DevelopmentRecipe(CustomElementManager.PISTOL.getAmmunition().create(), 1)}),
		
		TNT = new RecipeAdvancement(new RecipeAdvancement[0], new ResearchRecipe("TNT", 10), new DevelopmentRecipe[] {
						new DevelopmentRecipe(new ItemStack(Material.TNT), 10),
						new DevelopmentRecipe(CustomElementManager.RV_CRAFT.create(), 7),
						new DevelopmentRecipe(CustomElementManager.REENTRY_VEHICLE.create(), 5)}),
		
		HOWITZER = new RecipeAdvancement(new RecipeAdvancement[0], new ResearchRecipe("Canon", 20), new DevelopmentRecipe[] {
						new DevelopmentRecipe(CustomElementManager.HOWITZER.create(), 10),
						new DevelopmentRecipe(CustomElementManager.WEATHER_FORECASTER.create(), 5)}),
		
		ENGINE_1 = new RecipeAdvancement(new RecipeAdvancement[0], new ResearchRecipe("Moteur 1", 20), new DevelopmentRecipe[] {
						new DevelopmentRecipe(CustomElementManager.ENGINE_1.create(), 12)}),
		
		PROPELLANT_1 = new RecipeAdvancement(new RecipeAdvancement[0], new ResearchRecipe("Carburant 1", 10), new DevelopmentRecipe[] {
						new DevelopmentRecipe(CustomElementManager.FUEL_1.create(), 2)}),
		
		RADAR = new RecipeAdvancement(new RecipeAdvancement[0], new ResearchRecipe("Radar", 25), new DevelopmentRecipe[] {
						new DevelopmentRecipe(CustomElementManager.RADAR.create(), 10)}),
		
		MACHINE_GUN = new RecipeAdvancement(new RecipeAdvancement[] {PISTOL}, new ResearchRecipe("Mitraillette", 12), new DevelopmentRecipe[] {
						new DevelopmentRecipe(CustomElementManager.MACHINE_GUN.createItem(), 7),
						new DevelopmentRecipe(CustomElementManager.MACHINE_GUN.getAmmunition().create(), 2)}),
		
		SHOTGUN = new RecipeAdvancement(new RecipeAdvancement[] {PISTOL}, new ResearchRecipe("Fusil à pompe", 10), new DevelopmentRecipe[] {
						new DevelopmentRecipe(CustomElementManager.SHOTGUN.createItem(), 6),
						new DevelopmentRecipe(CustomElementManager.SHOTGUN.getAmmunition().create(), 1)}),
		
		BOMB = new RecipeAdvancement(new RecipeAdvancement[] {PISTOL, TNT}, new ResearchRecipe("Grenade", 5), new DevelopmentRecipe[] {
						new DevelopmentRecipe(CustomElementManager.BOMB.create(5), 2)}),
		
		SMALL_SHELL = new RecipeAdvancement(new RecipeAdvancement[] {HOWITZER, TNT}, new ResearchRecipe("Obus léger", 12), new DevelopmentRecipe[] {
						new DevelopmentRecipe(CustomElementManager.SMALL_SHELL.create(5), 5)}),
		
		BIG_SHELL = new RecipeAdvancement(new RecipeAdvancement[] {SMALL_SHELL}, new ResearchRecipe("Obus lourd", 17), new DevelopmentRecipe[] {
						new DevelopmentRecipe(CustomElementManager.BIG_SHELL.create(5), 8)}),
		
		ENGINE_2 = new RecipeAdvancement(new RecipeAdvancement[] {ENGINE_1}, new ResearchRecipe("Moteur 2", 25), new DevelopmentRecipe[] {
						new DevelopmentRecipe(CustomElementManager.ENGINE_2.create(), 15)}),
		
		PROPELLANT_2 = new RecipeAdvancement(new RecipeAdvancement[] {PROPELLANT_1}, new ResearchRecipe("Carburant 2", 15), new DevelopmentRecipe[] {
						new DevelopmentRecipe(CustomElementManager.FUEL_2.create(), 5)}),
		
		ENGINE_3 = new RecipeAdvancement(new RecipeAdvancement[] {ENGINE_2}, new ResearchRecipe("Moteur 3", 30), new DevelopmentRecipe[] {
						new DevelopmentRecipe(CustomElementManager.ENGINE_3.create(), 20)}),
		
		PROPELLANT_3 = new RecipeAdvancement(new RecipeAdvancement[] {PROPELLANT_2}, new ResearchRecipe("Carburant 3", 20), new DevelopmentRecipe[] {
						new DevelopmentRecipe(CustomElementManager.FUEL_3.create(), 10)}),
		
		SRBM = new RecipeAdvancement(new RecipeAdvancement[] {ENGINE_1, PROPELLANT_1}, new ResearchRecipe("SRBM", 60), new DevelopmentRecipe[] {
						new DevelopmentRecipe(CustomElementManager.SRBM.create(), 30),
						new DevelopmentRecipe(CustomElementManager.MISSILE_LAUNCHER.create(), 15),
						new DevelopmentRecipe(CustomElementManager.MISSILE_CRAFT.create(), 8)}),
		
		SNIPER = new RecipeAdvancement(new RecipeAdvancement[] {MACHINE_GUN, SHOTGUN}, new ResearchRecipe("Sniper", 20), new DevelopmentRecipe[] {
						new DevelopmentRecipe(CustomElementManager.SNIPER.createItem(), 12),
						new DevelopmentRecipe(CustomElementManager.SNIPER.getAmmunition().create(), 5)}),
		
		SMOKE = new RecipeAdvancement(new RecipeAdvancement[] {BOMB}, new ResearchRecipe("Fumigène", 7), new DevelopmentRecipe[] {
						new DevelopmentRecipe(CustomElementManager.SMOKE_BOMB.create(), 5)}),
		
		MINE = new RecipeAdvancement(new RecipeAdvancement[] {BOMB}, new ResearchRecipe("Mine", 6), new DevelopmentRecipe[] {
						new DevelopmentRecipe(CustomElementManager.MINE.create(), 2)}),
		
		CLAYMORE = new RecipeAdvancement(new RecipeAdvancement[] {MINE}, new ResearchRecipe("Claymore", 10), new DevelopmentRecipe[] {
						new DevelopmentRecipe(CustomElementManager.CLAYMORE.create(), 5)}),
		
		A_BOMB = new RecipeAdvancement(new RecipeAdvancement[] {TNT}, new ResearchRecipe("Bombe A", 50), new DevelopmentRecipe[] {
						new DevelopmentRecipe(CustomElementManager.A_BOMB.create(), 40)}),
		
		ABM = new RecipeAdvancement(new RecipeAdvancement[] {RADAR, SRBM}, new ResearchRecipe("ABM", 60), new DevelopmentRecipe[] {
						new DevelopmentRecipe(CustomElementManager.ABM_LAUNCHER.create(), 20),
						new DevelopmentRecipe(CustomElementManager.ABM.create(), 15)}),
		
		MRBM = new RecipeAdvancement(new RecipeAdvancement[] {SRBM}, new ResearchRecipe("MRBM", 1, 16), new DevelopmentRecipe[] {
						new DevelopmentRecipe(CustomElementManager.MRBM.create(), 50)}),
		
		E_BOMB = new RecipeAdvancement(new RecipeAdvancement[] {A_BOMB}, new ResearchRecipe("Bombe à IEM", 40), new DevelopmentRecipe[] {
						new DevelopmentRecipe(CustomElementManager.E_BOMB.create(), 20)}),
		
		H_BOMB = new RecipeAdvancement(new RecipeAdvancement[] {A_BOMB}, new ResearchRecipe("Bombe H", 2, 22), new DevelopmentRecipe[] {
						new DevelopmentRecipe(CustomElementManager.H_BOMB.create(), 1, 36)}),
		
		MIRV = new RecipeAdvancement(new RecipeAdvancement[] {A_BOMB, MRBM}, new ResearchRecipe("Mirvage", 50), new DevelopmentRecipe[] {
						new DevelopmentRecipe(CustomElementManager.MIRV.create(), 20)}),
		
		ICBM = new RecipeAdvancement(new RecipeAdvancement[] {MRBM}, new ResearchRecipe("ICBM", 1, 56), new DevelopmentRecipe[] {
						new DevelopmentRecipe(CustomElementManager.ICBM.create(), 1, 26)});
}

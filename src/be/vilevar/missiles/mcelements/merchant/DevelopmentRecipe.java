package be.vilevar.missiles.mcelements.merchant;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;

public class DevelopmentRecipe extends MerchantRecipe {

	public DevelopmentRecipe(ItemStack result, int price) {
		super(result, Integer.MAX_VALUE);
		this.setExperienceReward(false);
		this.addIngredient(new ItemStack(Material.EMERALD, price));
	}

}

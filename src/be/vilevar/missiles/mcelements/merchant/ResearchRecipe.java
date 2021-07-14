package be.vilevar.missiles.mcelements.merchant;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ResearchRecipe {

	private ItemStack result;
	private ItemStack price;
	
	public ResearchRecipe(String patent, int price) {
		ItemStack is = new ItemStack(Material.PAPER);
		ItemMeta im = is.getItemMeta();
		im.setDisplayName("§6Brevet sur §a"+patent);
		is.setItemMeta(im);
		this.result = is;
		
		this.price = new ItemStack(Material.EMERALD, price);
	}

	public ItemStack getResult() {
		return result;
	}
	
	public ItemStack getPrice() {
		return price;
	}
	
}

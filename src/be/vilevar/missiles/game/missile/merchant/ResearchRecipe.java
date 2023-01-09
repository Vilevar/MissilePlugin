package be.vilevar.missiles.game.missile.merchant;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ResearchRecipe {

	private ItemStack result;
	private List<ItemStack> price;
	
	public ResearchRecipe(String patent, int price) {
		ItemStack is = new ItemStack(Material.PAPER);
		ItemMeta im = is.getItemMeta();
		im.setDisplayName("§6Brevet sur §a"+patent);
		is.setItemMeta(im);
		this.result = is;
		
		this.price = Arrays.asList(new ItemStack(Material.EMERALD, price));
	}
	
	public ResearchRecipe(String patent, int blocks, int emerald) {
		ItemStack is = new ItemStack(Material.PAPER);
		ItemMeta im = is.getItemMeta();
		im.setDisplayName("§6Brevet sur §a"+patent);
		is.setItemMeta(im);
		this.result = is;
		
		this.price = Arrays.asList(new ItemStack(Material.EMERALD_BLOCK, blocks), new ItemStack(Material.EMERALD, emerald));
	}

	public ItemStack getResult() {
		return result;
	}
	
	public List<ItemStack> getPrice() {
		return price;
	}
	
}

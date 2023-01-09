package be.vilevar.missiles.game.siege.merchant;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class SiegeMerchantItem {

	private final ItemStack is;
	private final int price;
	private final int slot;
	
	public SiegeMerchantItem(ItemStack is, int price, int slot) {
		this.is = is;
		this.price = price;
		this.slot = slot;
		
		ItemMeta im = is.getItemMeta();
		List<String> lore = im.getLore();
		if(lore == null)
			lore = new ArrayList<>();
		lore.add("§ePrix: §a"+price);
		im.setLore(lore);
		is.setItemMeta(im);
	}

	public ItemStack getItem() {
		return is;
	}

	public int getPrice() {
		return price;
	}

	public int getSlot() {
		return slot;
	}
	
}

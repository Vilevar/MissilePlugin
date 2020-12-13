package be.vilevar.missiles.mcelements;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class CustomItem {

	private final Material material;
	private final int model;
	private final String name;
	
	public CustomItem(Material material, int model) {
		this(material, model, null);
	}
	
	public CustomItem(Material material, int model, String name) {
		this.material = material;
		this.model = model;
		if(name != null)
			name = "§f" + name;
		this.name = name;
	}

	public Material getMaterial() {
		return material;
	}
	
	public int getModel() {
		return model;
	}
	
	public String getName() {
		return name;
	}
	
	public ItemStack create() {
		return this.create(1);
	}
	
	public ItemStack create(int amount) {
		ItemStack is = new ItemStack(material, amount);
		ItemMeta im = is.getItemMeta();
		im.setCustomModelData(model);
		if(name != null)
			im.setDisplayName(name);
		is.setItemMeta(im);
		return is;
	}

	public boolean isParentOf(ItemStack is) {
		if(is.getType() == material) {
			ItemMeta im = is.getItemMeta();
			return im.getCustomModelData() == model;
		}
		return false;
	}
}

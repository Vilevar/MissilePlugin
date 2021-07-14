package be.vilevar.missiles.mcelements;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class CustomBlock {

	private final Material material;
	private final String name;
	
	public CustomBlock(Material material) {
		this(material, null);
	}
	
	public CustomBlock(Material material, String name) {
		this.material = material;
		if(name != null)
			name = "§f" + name;
		this.name = name;
	}

	public Material getMaterial() {
		return material;
	}
	
	public String getName() {
		return name;
	}
	
	public ItemStack create() {
		return this.create(1);
	}
	
	public ItemStack create(int amount) {
		ItemStack is = new ItemStack(material, amount);
		if(name != null) {
			ItemMeta im = is.getItemMeta();
			if(name != null)
				im.setDisplayName(name);
			is.setItemMeta(im);
		}
		return is;
	}
	
	public boolean isParentOf(ItemStack is) {
		return is != null && is.getType() == material;
	}
	
	public boolean isParentOf(Block block) {
		return block != null && block.getType() == material;
	}
}

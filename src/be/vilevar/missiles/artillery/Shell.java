package be.vilevar.missiles.artillery;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import be.vilevar.missiles.Main;
import be.vilevar.missiles.mcelements.CustomElementManager;
import be.vilevar.missiles.mcelements.CustomItem;

public class Shell {

	public static final Shell CLASSIC = 
			new Shell(48, 57, 1.37, .155, new ShellExplosiveManager(6f, 8f, 0f, false), CustomElementManager.SHELL);
	public static final Shell INCENDIARY = 
			new Shell(50, 56, 1.37, .155, new ShellExplosiveManager(5f, 8f, 10f, false), CustomElementManager.INCENDIARY_SHELL);
	public static final Shell PERFORATING =
			new Shell(54, 53, 1.49, .155, new ShellExplosiveManager(8f, false, true), CustomElementManager.PERFORATING_SHELL);
	public static final Shell EXPLOSIVE =
			new Shell(56, 50, 1.57, .155, new ShellExplosiveManager(10f, true), CustomElementManager.EXPLOSIVE_SHELL);
	
//	public static final Shell SMALL = new Shell(43, 65, 1.26, .155, 7.5f, 7.5f, 0.f, false, CustomElementManager.SMALL_SHELL);
//	public static final Shell BIG = new Shell(56, 50, 1.57, .155, 9.5f, 9.5f, 9.5f, false, CustomElementManager.BIG_SHELL);
//	public static final Shell ROCKET = new Shell(75, 100, 1.42, .155, 15f, 15f, 0f, false, null);
	
	private double mass;
	private double v0;
	private double cd;
	private double S;
	private final ShellExplosiveManager explosiveManager;
	private final CustomItem item;
	
	public Shell(double mass, double v0, double cd, double diam, ShellExplosiveManager explosiveManager, CustomItem item) {
		this.mass = mass;
		this.v0 = v0;
		this.cd = cd;
		this.S = Math.PI * Math.pow(diam / 2, 2);
		this.explosiveManager = explosiveManager;
		this.item = item;
	}
	
	public CustomItem getItemStack() {
		return item;
	}
	
	public double getMass() {
		return mass;
	}

	public double getInitialSpeed() {
		return v0;
	}

	public double getDragCoefficient() {
		return cd;
	}
	
	public double getSurface() {
		return S;
	}

	public static Shell fromItem(ItemStack is) {
		if(CLASSIC.getItemStack().isParentOf(is)) {
			return CLASSIC;
		} else if(INCENDIARY.getItemStack().isParentOf(is)) {
			return INCENDIARY;
		} else if(PERFORATING.getItemStack().isParentOf(is)) {
			return PERFORATING;
		} else if(EXPLOSIVE.getItemStack().isParentOf(is)) {
			return EXPLOSIVE;
		} else {
			return null;
		}
//		return SMALL.getItemStack().isParentOf(is) ? SMALL : BIG.getItemStack().isParentOf(is) ? BIG : null;
	}
	
	public void explode(Location loc, Player p, Vector perforationDirection) {
		this.explosiveManager.explode(Main.i, loc, p, perforationDirection);
	}
}

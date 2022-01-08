package be.vilevar.missiles.artillery;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import be.vilevar.missiles.Main;
import be.vilevar.missiles.mcelements.CustomElementManager;
import be.vilevar.missiles.mcelements.CustomItem;
import be.vilevar.missiles.missile.ballistic.explosives.TraditionalExplosive;

public class Shell {

	public static final Shell SMALL = new Shell(43, 65, 1.26, .155, 7.5f, false, CustomElementManager.SMALL_SHELL);
	public static final Shell BIG = new Shell(56, 50, 1.57, .155, 9.5f, true, CustomElementManager.BIG_SHELL);
	public static final Shell ROCKET = new Shell(75, 100, 1.42, .155, 15f, false, null);
	
	private double mass;
	private double v0;
	private double cd;
	private double S;
	private float power;
	private boolean fire;
	private CustomItem item;
	
	public Shell(double mass, double v0, double cd, double d, float power, boolean fire, CustomItem item) {
		this.mass = mass;
		this.v0 = v0;
		this.cd = cd;
		this.S = Math.PI * Math.pow(d / 2, 2);
		this.power = power;
		this.fire = fire;
		this.item = item;
	}
	
	public CustomItem getItemStack() {
		return item;
	}
	
	public double getMass() {
		return mass;
	}

	public double getV0() {
		return v0;
	}

	public double getCd() {
		return cd;
	}
	
	public double getS() {
		return S;
	}
	
	public float getPower() {
		return power;
	}
	
	public boolean setFire() {
		return fire;
	}

	public static Shell fromItem(ItemStack is) {
		return SMALL.getItemStack().isParentOf(is) ? SMALL : BIG.getItemStack().isParentOf(is) ? BIG : null;
	}
	
	public void explode(Location loc, Player p) {
		new TraditionalExplosive(Main.i, power, fire).explode(loc, p);
	}
}

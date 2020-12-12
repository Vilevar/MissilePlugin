package be.vilevar.missiles.mcelements;

import org.bukkit.Bukkit;
import org.bukkit.FluidCollisionMode;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import be.vilevar.missiles.mcelements.data.LaserPointerData;

public class LaserPointerListener implements Listener {

	
	@EventHandler
	public void onTarget(PlayerInteractEvent e) {
		ItemStack is = e.getItem();
		Player p = e.getPlayer();
		if(is != null && is.getType() == CustomElementManager.LASER_POINTER && is.equals(p.getInventory().getItemInMainHand()) && p.isSneaking() 
				&& (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK)) {
			int amount = is.getAmount();
			LaserPointerData lp = LaserPointerData.getLaserPointerData(is);
			lp.setTarget(p.rayTraceBlocks(lp.getRange(), FluidCollisionMode.NEVER).getHitBlock().getLocation());
			is = lp.toItemStack();
			is.setAmount(amount);
			p.getInventory().setItemInMainHand(is);
			p.updateInventory();
			e.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onCommand(AsyncPlayerChatEvent e) {
		Player p = e.getPlayer();
		String msg = e.getMessage();
		String[] args = msg.split(" ");
		ItemStack is = p.getInventory().getItemInMainHand();
		if(args[0].equalsIgnoreCase("%lp")) {
			e.setCancelled(true);
			if(is!=null && is.getType() == CustomElementManager.LASER_POINTER) {
				if(args.length < 4) {
					p.sendMessage("§c%lp <x> <y> <z>");
					return;
				}
				try {
					double x = Double.parseDouble(args[1]);
					double y = Double.parseDouble(args[2]);
					double z = Double.parseDouble(args[3]);
					int amount = is.getAmount();
					LaserPointerData lp = LaserPointerData.getLaserPointerData(is);
					lp.setTarget(new Location(p.getWorld(), x, y, z));
					is = lp.toItemStack();
					is.setAmount(amount);
					p.getInventory().setItemInMainHand(is);
					p.updateInventory();
				} catch (Exception ex) {
					p.sendMessage("§cLes coordonnées sont incorrectes.");
				}
			} else {
				p.sendMessage("§cVous n'avez pas de pointeur laser en main.");
			}
			return;
		}
		if(args[0].equalsIgnoreCase("%msg") && p.getGameMode()==GameMode.SURVIVAL) {
			e.setCancelled(true);
			if(args.length<3) {
				p.sendMessage("§c%msg <player> [msg]");
				return;
			}
			Player s = Bukkit.getPlayer(args[1]);
			if(s==null) {
				p.sendMessage("§cJoueur introuvable.");
				return;
			}
			StringBuilder m = new StringBuilder();
			for(int i = 2; i < args.length; i++) {
				if(i!=2) m.append(' ');
				m.append(args[i]);
			}
			s.sendMessage("§6["+p.getName()+" -> vous] §r"+m.toString());
			p.sendMessage("§6[Vous -> "+s.getName()+"] §r"+m.toString());
			return;
		}
		if(args[0].equalsIgnoreCase("%dist")) {
			e.setCancelled(true);
			if(args.length < 3) {
				p.sendMessage("§c%dist <x> <z>");
				return;
			}
			try {
				double x = Double.parseDouble(args[1]);
				double z = Double.parseDouble(args[2]);
				p.sendMessage("§a"+p.getLocation().toVector().setY(0).subtract(new Vector(x, 0, z)).length()+" mètres.");
			} catch (Exception ex) {
				p.sendMessage("§cLes coordonnées sont incorrectes.");
			}
		}
		if(args[0].equalsIgnoreCase("%help")) {
			e.setCancelled(true);
			p.sendMessage("§6%msg <to> [message] §c(uniquement en survie)");
			p.sendMessage("§6%lp <x> <y> <z> §a: configurer le pointer laser");
			p.sendMessage("§6%dist <x> <z>");
		}
	}
}

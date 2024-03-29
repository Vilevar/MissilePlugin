package be.vilevar.missiles.mcelements;

import static be.vilevar.missiles.Main.round;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.toRadians;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Random;
import java.util.UUID;

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
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import be.vilevar.missiles.Main;
import be.vilevar.missiles.artillery.Shell;
import be.vilevar.missiles.artillery.ShellPath;
import be.vilevar.missiles.mcelements.data.RangefinderData;
import be.vilevar.missiles.utils.Vec3d;

public class RangefinderListener implements Listener {

	private ArrayList<UUID> zooming = new ArrayList<>();
	private ScoreboardManager manager;
	private Scoreboard mainSB;
	
	public RangefinderListener() {
		this.manager = Bukkit.getScoreboardManager();
		this.mainSB = manager.getMainScoreboard();
	}
	
	@EventHandler
	public void onZoomAndTarget(PlayerInteractEvent e) {
		Player p = e.getPlayer();
		ItemStack is = e.getItem();
		if(CustomElementManager.RANGEFINDER.isParentOf(is) && e.getHand() == EquipmentSlot.HAND) {
			e.setCancelled(true);
			RangefinderData lp = RangefinderData.getRangefinderData(is);
			if((e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) && zooming.contains(p.getUniqueId())) {
				int amount = is.getAmount();
				
				RayTraceResult trace = p.rayTraceBlocks(lp.getRange(), FluidCollisionMode.NEVER);
				lp.setTarget(trace == null ? null : trace.getHitPosition().toLocation(p.getWorld()));
				
				Scoreboard zoomSB = p.getScoreboard();
				zoomSB.getEntries().forEach(ent -> zoomSB.resetScores(ent));
				this.updateScoreboard(zoomSB.getObjective(DisplaySlot.SIDEBAR), lp, p.getLocation());
				
				is = lp.toItemStack();
				is.setAmount(amount);
				p.getInventory().setItemInMainHand(is);
				p.updateInventory();
				
				
			} else if(e.getAction() == Action.LEFT_CLICK_AIR || e.getAction() == Action.LEFT_CLICK_BLOCK) {
				if(p.hasPotionEffect(PotionEffectType.SLOW)) {
					PotionEffect slow = p.getPotionEffect(PotionEffectType.SLOW);
					if(slow.getAmplifier() == 2) {
						p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 999999, 9, true, false, false));
						if(!zooming.contains(p.getUniqueId()))
							zooming.add(p.getUniqueId());
					} else {
						p.removePotionEffect(PotionEffectType.SLOW);
						if(p.hasPotionEffect(PotionEffectType.NIGHT_VISION))
							p.removePotionEffect(PotionEffectType.NIGHT_VISION);
						if(zooming.contains(p.getUniqueId()))
							zooming.remove(p.getUniqueId());
						p.setScoreboard(mainSB);
					}
				} else {
					p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 999999, 2, true, false, false));
					p.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 999999, 10, true, false, false));
					zooming.add(p.getUniqueId());
					
					Scoreboard zoomSB = this.manager.getNewScoreboard();
					Objective obj = zoomSB.registerNewObjective("Rangefinder", "dummy", "§6[§3Rangefinder§6]");
					this.updateScoreboard(obj, lp, p.getLocation());
					p.setScoreboard(zoomSB);
				}
			}
		}
	}
	
	private void updateScoreboard(Objective obj, RangefinderData lp, Location current) {
		obj.setDisplaySlot(DisplaySlot.SIDEBAR);
		
		Score world;
		Score x;
		Score y;
		Score z;
		Score dist;
		Score distXZ;
		
		Location target = lp.getTarget();
		if(target == null) {
			world = obj.getScore("§dWorld : §enull");
			x = obj.getScore("§dX : §enull");
			y = obj.getScore("§dY : §enull");
			z = obj.getScore("§dZ : §enull");
			dist = obj.getScore("§dDistance: §enull");
			distXZ = obj.getScore("§6Distance XZ; §enull");
		} else {
			world = obj.getScore("§dWorld : §e"+target.getWorld().getName());
			x = obj.getScore("§dX : §e"+round(target.getX()));
			y = obj.getScore("§dY : §e"+round(target.getY()));
			z = obj.getScore("§dZ : §e"+round(target.getZ()));
			try {
				dist = obj.getScore("§dDistance: §e"+round(target.distance(current)));
				Location xzTarget = target.clone();
				xzTarget.setY(current.getY());
				distXZ = obj.getScore("§dDistance XZ: §e"+round(xzTarget.distance(current)));
			} catch (Exception e) {
				dist = obj.getScore("§dDistance: §enull");
				distXZ = obj.getScore("§6Distance XZ; §enull");
			}
		}
		
		world.setScore(5);
		x.setScore(4);
		y.setScore(3);
		z.setScore(2);
		dist.setScore(1);
		distXZ.setScore(0);
	}
	
	@EventHandler
	public void onSwitchItem(PlayerItemHeldEvent e) {
		Player p = e.getPlayer();
		if(e.getNewSlot() != e.getPreviousSlot() && zooming.contains(p.getUniqueId()) && p.hasPotionEffect(PotionEffectType.SLOW)) {
			p.removePotionEffect(PotionEffectType.SLOW);
			if(p.hasPotionEffect(PotionEffectType.NIGHT_VISION))
				p.removePotionEffect(PotionEffectType.NIGHT_VISION);
			zooming.remove(p.getUniqueId());
			p.setScoreboard(mainSB);
		}
	}
	
	@EventHandler
	public void onTarget(PlayerInteractEvent e) {
		ItemStack is = e.getItem();
		Player p = e.getPlayer();
		if(is != null && CustomElementManager.RANGEFINDER.isParentOf(is) && is.equals(p.getInventory().getItemInMainHand()) && p.isSneaking() 
				&& (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK)) {
			int amount = is.getAmount();
			RangefinderData lp = RangefinderData.getRangefinderData(is);
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
		if(args[0].equalsIgnoreCase("%rf")) {
			e.setCancelled(true);
			if(is!=null && CustomElementManager.RANGEFINDER.isParentOf(is)) {
				if(args.length < 4) {
					p.sendMessage("§c%rf <x> <y> <z>");
					return;
				}
				try {
					double x = Double.parseDouble(args[1]);
					double y = Double.parseDouble(args[2]);
					double z = Double.parseDouble(args[3]);
					int amount = is.getAmount();
					RangefinderData lp = RangefinderData.getRangefinderData(is);
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
			p.sendMessage("§6%rf <x> <y> <z> §a: configurer le pointer laser");
			p.sendMessage("§6%dist <x> <z>");
		}
		if(args[0].equalsIgnoreCase("%mrl")) {
			e.setCancelled(true);
			ArrayList<Vec3d> loc = new ArrayList<>();
			for(double x = -1.5; x <= 1.5; x += 0.333) {
				for(double y = 0.333; y <= 1.333; y += 0.333) {
					loc.add(new Vec3d(p.getEyeLocation().add(rotate(new Vector(x, y, 0), p.getEyeLocation()))));
				}
			}
			Random r = new Random();
			Collections.shuffle(loc, r);
			Iterator<Vec3d> l = loc.iterator();
			new BukkitRunnable() {
				@Override
				public void run() {
					if(l.hasNext()) {
						new ShellPath(p.getWorld(), Shell.ROCKET, l.next(),
								Math.toRadians(-p.getEyeLocation().getPitch()) + r.nextGaussian()*0.025, 
								Math.toRadians(p.getEyeLocation().getYaw() + 90) + r.nextGaussian()*0.025, p).runTaskTimer(Main.i, 1, 0);
					} else {
						this.cancel();
					}
				}
			}.runTaskTimer(Main.i, 0, 0);
		}
	}
	
	
	public static Vector rotate(Vector v, Location loc) {
		double yaw = toRadians(loc.getYaw());
		double pitch = toRadians(loc.getPitch());
		v = rotateAboutX(v, pitch);
		v = rotateAboutY(v, -yaw);
		return v;
	}
	
	public static Vector rotateAboutX(Vector v, double a) {
		double y = cos(a)*v.getY() - sin(a)*v.getZ();
		double z = sin(a)*v.getY() + cos(a)*v.getZ();
		return v.setY(y).setZ(z);
	}
	
	public static Vector rotateAboutY(Vector v, double b) {
		double x = cos(b)*v.getX() + sin(b)*v.getZ();
		double z = -sin(b)*v.getX() + cos(b)*v.getZ();
		return v.setX(x).setZ(z);
	}
}

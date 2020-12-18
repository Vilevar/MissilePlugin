package be.vilevar.missiles.game;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.scoreboard.Team;

import be.vilevar.missiles.Main;

public class GameListener implements Listener {

	private Main main = Main.i;
	private Team communism = main.getCommunism();
	private Team capitalism = main.getCapitalism();
	
	@EventHandler
	public void onPlaceBanner(BlockPlaceEvent e) {
		Game game = main.getGame();
		if(game != null && !game.isStarted()) {
			if(e.getBlock().getType() == Material.RED_BANNER && game.getBanner(communism) == null && communism.hasEntry(e.getPlayer().getName())) {
				game.setBanner(communism, e.getBlock().getLocation());
			} else if(e.getBlock().getType() == Material.BLUE_BANNER && game.getBanner(capitalism) == null &&
					capitalism.hasEntry(e.getPlayer().getName())) {
				game.setBanner(capitalism, e.getBlock().getLocation());
			}
		}
	}
	
	@EventHandler
	public void onBreakBanner(BlockBreakEvent e) {
		Game game = main.getGame();
		if(game != null) {
			if(e.getBlock().getType() == Material.RED_BANNER && e.getBlock().getLocation().equals(game.getBanner(communism))) {
				if(!game.isStarted())
					if(e.getPlayer() != null && communism.hasEntry(e.getPlayer().getName()))
						game.removeBanner(communism);
					else
						e.setCancelled(true);
				else
					game.stop(capitalism, true);
			} else if(e.getBlock().getType() == Material.BLUE_BANNER && e.getBlock().getLocation().equals(game.getBanner(capitalism))) {
				if(!game.isStarted())
					if(e.getPlayer() != null && capitalism.hasEntry(e.getPlayer().getName()))
						game.removeBanner(communism);
					else
						e.setCancelled(true);
				else
					game.stop(communism, true);
			}
		}
	}
	
	@EventHandler
	public void onExplodeBannerByBlock(BlockExplodeEvent e) {
		this.testExplosion(main.getGame(), e.blockList());
	}
	
	@EventHandler
	public void onExplodeBannerByEntity(EntityExplodeEvent e) {
		this.testExplosion(main.getGame(), e.blockList());
	}
	
	private void testExplosion(Game game, List<Block> blocks) {
		if(game != null) {
			blocks.removeIf(block -> {
				if(block.getType() == Material.RED_BANNER && block.getLocation().equals(game.getBanner(communism))) {
					if(game.isStarted())
						game.stop(capitalism, true);
					else
						return true;
				} else if(block.getType() == Material.BLUE_BANNER && block.getLocation().equals(game.getBanner(capitalism))) {
					if(game.isStarted())
						game.stop(communism, true);
					else
						return true;
				}
				return false;
			});
		}
	}
	
	@EventHandler
	public void onNoDamagePreparation(EntityDamageEvent e) {
		Game game;
		if(e.getEntityType() == EntityType.PLAYER && (game = main.getGame()) != null && !game.isStarted()) {
			e.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onRespawn(PlayerRespawnEvent e) {
		final Game game = main.getGame();
		if(game != null) {
			final Player p = e.getPlayer();
			final Location spawn = communism.hasEntry(p.getName()) ? game.getBanner(communism) :
				capitalism.hasEntry(p.getName()) ? game.getBanner(capitalism) : null;
			if(spawn != null) {
				p.setGameMode(GameMode.SPECTATOR);
				e.setRespawnLocation(spawn);
				Bukkit.getScheduler().runTaskLater(main, () -> {
					if(main.getGame() != null && p.isOnline()) {
						p.setGameMode(GameMode.SURVIVAL);
					}
				}, 60);
			}
		}
	}
}

package be.vilevar.missiles.utils;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_15_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import net.minecraft.server.v1_15_R1.Block;
import net.minecraft.server.v1_15_R1.Blocks;
import net.minecraft.server.v1_15_R1.IBlockData;
import net.minecraft.server.v1_15_R1.IRegistry;
import net.minecraft.server.v1_15_R1.ItemStack;
import net.minecraft.server.v1_15_R1.MinecraftKey;
import net.minecraft.server.v1_15_R1.Packet;
import net.minecraft.server.v1_15_R1.PacketPlayOutWorldParticles;
import net.minecraft.server.v1_15_R1.Particle;
import net.minecraft.server.v1_15_R1.ParticleParam;
import net.minecraft.server.v1_15_R1.ParticleParamBlock;
import net.minecraft.server.v1_15_R1.ParticleParamItem;
import net.minecraft.server.v1_15_R1.ParticleParamRedstone;

/**
 * <b>ParticleEffect Library</b>
 * <p>
 * This library was created by @DarkBlade12 and allows you to display all
 * Minecraft particle effects on a Bukkit server
 * <p>
 * You are welcome to use it, modify it and redistribute it under the following
 * conditions:
 * <ul>
 * <li>Don't claim this class as your own
 * <li>Don't remove this disclaimer
 * </ul>
 * <p>
 * Special thanks:
 * <ul>
 * <li>@microgeek (original idea, names and packet parameters)
 * <li>@ShadyPotato (1.8 names, ids and packet parameters)
 * <li>@RingOfStorms (specific particle direction)
 * </ul>
 * <p>
 * <i>It would be nice if you provide credit to me if you use this class in a
 * published project</i>
 *
 * @author DarkBlade12
 * @version 1.6
 * 
 *          All the comments in the class are removed by Vilevar for space and
 *          because he modified and arranged the plug-in for Minecraft 1.13.2
 */
public enum ParticleEffect {

	/**
	 * Particle explosion normal
	 * 	alias poof
	 */
	EXPLOSION_NORMAL("poof"),
	/**
	 * Particle explosion large
	 * 	alias explosion
	 */
	EXPLOSION_LARGE("explosion"),
	/**
	 * Particle explosion huge
	 * 	alias explosion emitter
	 */
	EXPLOSION_HUGE("explosion_emitter"),
	/**
	 * Particle fireworks spark
	 * 	alias firework
	 */
	FIREWORKS_SPARK("firework"),
	/**
	 * Particle water bubble
	 * 	alias bubble
	 */
	WATER_BUBBLE("bubble"),
	/**
	 * Particle water splash
	 * 	alias splash
	 */
	WATER_SPLASH("splash"),
	/**
	 * Particle water wake
	 * 	alias fishing
	 */
	WATER_WAKE("fishing"), 
	/**
	 * Particle suspended
	 * 	alias underwater
	 */
	SUSPENDED("underwater"), 
	/**
	 * Particle suspended depth
	 * 	alias underwater
	 */
	SUSPENDED_DEPTH("underwater"), 
	/**
	 * Particle crit
	 * 	alias crit
	 */
	CRIT("crit"), 
	/**
	 * Particle crit magic
	 * 	alias enchanted hit
	 */
	CRIT_MAGIC("enchanted_hit"), 
	/**
	 * Particle smoke normal
	 * 	alias smoke
	 */
	SMOKE_NORMAL("smoke"), 
	/**
	 * Particle smoke large
	 * 	alias large smoke
	 */
	SMOKE_LARGE("large_smoke"), 
	/**
	 * Particle speed
	 * 	alias effect
	 */
	SPELL("effect"),
	/**
	 * Particle speed instant
	 * 	alias instant effect
	 */
	SPELL_INSTANT("instant_effect"), 
	/**
	 * Particle spell mob
	 * 	alias entity effect
	 */
	SPELL_MOB("entity_effect"), 
	/**
	 * Particle spell mob ambient
	 * 	alias ambient entity effect
	 */
	SPELL_MOB_AMBIENT("ambient_entity_effect"), 
	/**
	 * Particle spell witch
	 * 	alias witch
	 */
	SPELL_WITCH("witch"), 
	/**
	 * Particle drip water
	 * 	alias dripping water
	 */
	DRIP_WATER("dripping_water"), 
	/**
	 * Particle drip lava
	 * 	alias dripping lava
	 */
	DRIP_LAVA("dripping_lava"), 
	/**
	 * Particle villager angry
	 * 	alias angry villager
	 */
	VILLAGER_ANGRY("angry_villager"), 
	/**
	 * Particle villager happy
	 * 	alias happy villager
	 */
	VILLAGER_HAPPY("happy_villager"), 
	/**
	 * Particle town aura
	 * 	alias mycelium
	 */
	TOWN_AURA("mycelium"), 
	/**
	 * Particle note
	 * 	alias note
	 */
	NOTE("note"), 
	/**
	 * Particle portal
	 * 	alias portal
	 */
	PORTAL("portal"), 
	/**
	 * Particle enchantment table
	 * 	alias enchant
	 */
	ENCHANTMENT_TABLE("enchant"), 
	/**
	 * Particle flame
	 * 	alias flame
	 */
	FLAME("flame"),
	/**
	 * Particle lava
	 * 	alias lava
	 */
	LAVA("lava"), 
	/**
	 * Particle cloud
	 * 	alias cloud
	 */
	CLOUD("cloud"),
	/**
	 * Particle redstone
	 * 	alias dust
	 * Requires RedstoneData
	 */
	REDSTONE("dust", RedstoneData.class), 
	/**
	 * Particle snowball
	 * 	alias item snowball
	 */
	SNOWBALL("item_snowball"), 
	/**
	 * Particle snow shovel
	 * 	alias item snowball
	 */
	SNOW_SHOVEL("item_snowball"), 
	/**
	 * Particle slime
	 * 	alias item slime
	 */
	SLIME("item_slime"),
	/**
	 * Particle heart
	 * 	alias heart
	 */
	HEART("heart"),
	/**
	 * Particle barrier
	 * 	alias barrier
	 */
	BARRIER("barrier"), 
	/**
	 * Particle item crack
	 * 	alias item
	 * Requires ItemData
	 */
	ITEM_CRACK("item", ItemData.class), 
	/**
	 * Particle block crack
	 * 	alias block
	 * Requires BlockData
	 */
	BLOCK_CRACK("block", BlockData.class), 
	/**
	 * Particle block dust
	 * 	alias block
	 * Requires BlockData
	 */
	BLOCK_DUST("block", BlockData.class), 
	/**
	 * Particle water drop
	 * 	alias rain
	 */
	WATER_DROP("rain"), 
	/**
	 * Particle mob appearance
	 * 	alias elder guardian
	 */
	MOB_APPEARANCE("elder_guardian"), 
	/**
	 * Particle dragon breath
	 * 	alias dragon breath
	 */
	DRAGON_BREATH("dragon_breath"), 
	/**
	 * Particle end rod
	 * 	alias end rod
	 */
	END_ROD("end_rod"),
	/**
	 * Particle damage indicator
	 * 	alias damage indicator
	 */
	DAMAGE_INDICATOR("damage_indicator"), 
	/**
	 * Particle sweep attack
	 * 	alias sweep attack
	 */
	SWEEP_ATTACK("sweep_attack"), 
	/**
	 * Particle falling dust
	 * 	alias falling dust
	 * Requires BlockData
	 */
	FALLING_DUST("falling_dust", BlockData.class),
	/**
	 * Particle totem
	 * 	alias totem of undying
	 */
	TOTEM("totem_of_undying"), 
	/**
	 * Particle spit
	 * 	alias spit
	 */
	SPIT("spit"), 
	/**
	 * Particle squid ink
	 * 	alias squid ink
	 */
	SQUID_INK("squid_ink"), 
	/**
	 * Particle bubble pop
	 * 	alias bubble pop
	 */
	BUBBLE_POP("bubble_pop"), 
	/**
	 * Particle current down
	 * 	alias current down
	 */
	CURRENT_DOWN("current_down"), 
	/**
	 * Particle bubble column up
	 * 	alias bubble column up
	 */
	BUBBLE_COLUMN_UP("bubble_column_up"), 
	/**
	 * Particle nautilus
	 * 	alias nautilus
	 */
	NAUTILUS("nautilus"), 
	/**
	 * Particle dolphin
	 * 	alias dolphin
	 */
	DOLPHIN("dolphin");

	private static final int LONG_DISTANCE = 16;
	private static final int LONG_DISTANCE_SQUARED = LONG_DISTANCE * LONG_DISTANCE;
	private static final Map<String, ParticleEffect> NAME_MAP = new HashMap<String, ParticleEffect>();
	private final String name;
	private final Class<? extends ParticleData<?>> data;
	private final boolean requiresData;

	// Initialize map for quick name and id lookup
	static {
		for (ParticleEffect effect : values()) {
			NAME_MAP.put(effect.name, effect);
		}
	}

	/**
	 * Construct a new particle effect
	 *
	 * @param name
	 *            Name of this particle effect
	 * @param id
	 *            Id of this particle effect
	 * @param requiredVersion
	 *            Version which is required (1.x)
	 * @param requiresData
	 *            Indicates whether additional data is required for this
	 *            particle effect
	 * @param requiresWater
	 *            Indicates whether water is required for this particle effect
	 *            to display properly
	 */
	private ParticleEffect(String name, Class<? extends ParticleData<?>> dataType) {
		this.name = name;
		this.data = dataType;
		this.requiresData = dataType!=null;
	}

	/**
	 * Construct a new particle effect with {@link #requiresData} and
	 * {@link #requiresWater} set to <code>false</code>
	 *
	 * @param name
	 *            Name of this particle effect
	 * @param id
	 *            Id of this particle effect
	 * @param requiredVersion
	 *            Version which is required (1.x)
	 */
	private ParticleEffect(String name) {
		this(name, null);
	}

	/**
	 * Returns the name of this particle effect
	 *
	 * @return The name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Determine if additional data is required for this particle effect
	 *
	 * @return Whether additional data is required or not
	 */
	public boolean getRequiresData() {
		return requiresData;
	}
	
	/**
	 * 
	 * @return
	 */
	public Class<? extends ParticleData<?>> getRequiredDataType() {
		return data;
	}


	/**
	 * Returns the particle effect with the given name
	 *
	 * @param name
	 *            Name of the particle effect
	 * @return The particle effect
	 */
	public static ParticleEffect fromName(String name) {
		for (Entry<String, ParticleEffect> entry : NAME_MAP.entrySet()) {
			if (!entry.getKey().equalsIgnoreCase(name)) {
				continue;
			}
			return entry.getValue();
		}
		return null;
	}


	/**
	 * Determine if the distance between @param location and one of the players
	 * exceeds LONG_DISTANCE
	 *
	 * @param location
	 *            Location to check
	 * @return Whether the distance exceeds 16 or not
	 */
	private static boolean isLongDistance(Location location, List<Player> players) {
		for (Player player : players) {
			if (player.getLocation().distanceSquared(location) > LONG_DISTANCE_SQUARED) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Determine if the data type for a particle effect is correct
	 *
	 * @param effect
	 *            Particle effect
	 * @param data
	 *            Particle data
	 * @return Whether the data type is correct or not
	 */
	private static boolean isDataCorrect(ParticleEffect effect, ParticleData<?> data) {
		return ((effect == BLOCK_CRACK || effect == BLOCK_DUST || effect == FALLING_DUST) && data instanceof BlockData)
				|| (effect == ITEM_CRACK && data instanceof ItemData)
				|| (effect == REDSTONE && data instanceof RedstoneData);
	}

	/**
	 * Displays a particle effect which is only visible for all players within a
	 * certain range in the world of @param center
	 *
	 * @param offsetX
	 *            Maximum distance particles can fly away from the center on the
	 *            x-axis
	 * @param offsetY
	 *            Maximum distance particles can fly away from the center on the
	 *            y-axis
	 * @param offsetZ
	 *            Maximum distance particles can fly away from the center on the
	 *            z-axis
	 * @param speed
	 *            Display speed of the particles
	 * @param amount
	 *            Amount of particles
	 * @param center
	 *            Center location of the effect
	 * @param range
	 *            Range of the visibility
	 * @throws ParticleVersionException
	 *             If the particle effect is not supported by the server version
	 * @throws ParticleDataException
	 *             If the particle effect requires additional data
	 * @throws IllegalArgumentException
	 *             If the particle effect requires water and none is at the
	 *             center location
	 * @see ParticlePacket
	 * @see ParticlePacket#sendTo(Location, double)
	 */
	public void display(float offsetX, float offsetY, float offsetZ, float speed, int amount, Location center, 
			double range) throws ParticleDataException {
		if (requiresData) {
			throw new ParticleDataException("The " + this + " particle effect requires additional data");
		}
		new ParticlePacket(this, offsetX, offsetY, offsetZ, speed, amount, range > 16, null).sendTo(center, range);
	}

	/**
	 * Displays a particle effect which is only visible for the specified
	 * players
	 *
	 * @param offsetX
	 *            Maximum distance particles can fly away from the center on the
	 *            x-axis
	 * @param offsetY
	 *            Maximum distance particles can fly away from the center on the
	 *            y-axis
	 * @param offsetZ
	 *            Maximum distance particles can fly away from the center on the
	 *            z-axis
	 * @param speed
	 *            Display speed of the particles
	 * @param amount
	 *            Amount of particles
	 * @param center
	 *            Center location of the effect
	 * @param players
	 *            Receivers of the effect
	 * @throws ParticleVersionException
	 *             If the particle effect is not supported by the server version
	 * @throws ParticleDataException
	 *             If the particle effect requires additional data
	 * @throws IllegalArgumentException
	 *             If the particle effect requires water and none is at the
	 *             center location
	 * @see ParticlePacket
	 * @see ParticlePacket#sendTo(Location, List)
	 */
	public void display(float offsetX, float offsetY, float offsetZ, float speed, int amount, Location center, 
			List<Player> players) throws ParticleDataException {
		if (requiresData) {
			throw new ParticleDataException("The " + this + " particle effect requires additional data");
		}
		new ParticlePacket(this, offsetX, offsetY, offsetZ, speed, amount, isLongDistance(center, players), null)
				.sendTo(center, players);
	}

	/**
	 * Displays a particle effect which is only visible for the specified
	 * players
	 *
	 * @param offsetX
	 *            Maximum distance particles can fly away from the center on the
	 *            x-axis
	 * @param offsetY
	 *            Maximum distance particles can fly away from the center on the
	 *            y-axis
	 * @param offsetZ
	 *            Maximum distance particles can fly away from the center on the
	 *            z-axis
	 * @param speed
	 *            Display speed of the particles
	 * @param amount
	 *            Amount of particles
	 * @param center
	 *            Center location of the effect
	 * @param players
	 *            Receivers of the effect
	 * @throws ParticleVersionException
	 *             If the particle effect is not supported by the server version
	 * @throws ParticleDataException
	 *             If the particle effect requires additional data
	 * @throws IllegalArgumentException
	 *             If the particle effect requires water and none is at the
	 *             center location
	 * @see #display(float, float, float, float, int, Location, List)
	 */
	public void display(float offsetX, float offsetY, float offsetZ, float speed, int amount, Location center,
			Player... players) throws ParticleDataException {
		display(offsetX, offsetY, offsetZ, speed, amount, center, Arrays.asList(players));
	}

	/**
	 * Displays a single particle which flies into a determined direction and is
	 * only visible for all players within a certain range in the world
	 * of @param center
	 *
	 * @param direction
	 *            Direction of the particle
	 * @param speed
	 *            Display speed of the particle
	 * @param center
	 *            Center location of the effect
	 * @param range
	 *            Range of the visibility
	 * @throws ParticleVersionException
	 *             If the particle effect is not supported by the server version
	 * @throws ParticleDataException
	 *             If the particle effect requires additional data
	 * @throws IllegalArgumentException
	 *             If the particle effect requires water and none is at the
	 *             center location
	 * @see ParticlePacket
	 * @see ParticlePacket#sendTo(Location, double)
	 */
	public void display(Vector direction, float speed, Location center, double range) throws ParticleDataException {
		if (requiresData) {
			throw new ParticleDataException("The " + this + " particle effect requires additional data");
		}
		new ParticlePacket(this, direction, speed, range > LONG_DISTANCE, null).sendTo(center, range);
	}

	/**
	 * Displays a single particle which flies into a determined direction and is
	 * only visible for the specified players
	 *
	 * @param direction
	 *            Direction of the particle
	 * @param speed
	 *            Display speed of the particle
	 * @param center
	 *            Center location of the effect
	 * @param players
	 *            Receivers of the effect
	 * @throws ParticleVersionException
	 *             If the particle effect is not supported by the server version
	 * @throws ParticleDataException
	 *             If the particle effect requires additional data
	 * @throws IllegalArgumentException
	 *             If the particle effect requires water and none is at the
	 *             center location
	 * @see ParticlePacket
	 * @see ParticlePacket#sendTo(Location, List)
	 */
	public void display(Vector direction, float speed, Location center, List<Player> players) throws ParticleDataException {
		if (requiresData) {
			throw new ParticleDataException("The " + this + " particle effect requires additional data");
		}
		new ParticlePacket(this, direction, speed, isLongDistance(center, players), null).sendTo(center, players);
	}

	/**
	 * Displays a single particle which flies into a determined direction and is
	 * only visible for the specified players
	 *
	 * @param direction
	 *            Direction of the particle
	 * @param speed
	 *            Display speed of the particle
	 * @param center
	 *            Center location of the effect
	 * @param players
	 *            Receivers of the effect
	 * @throws ParticleVersionException
	 *             If the particle effect is not supported by the server version
	 * @throws ParticleDataException
	 *             If the particle effect requires additional data
	 * @throws IllegalArgumentException
	 *             If the particle effect requires water and none is at the
	 *             center location
	 * @see #display(Vector, float, Location, List)
	 */
	public void display(Vector direction, float speed, Location center, Player... players) throws ParticleDataException {
		display(direction, speed, center, Arrays.asList(players));
	}

	/**
	 * Displays a particle effect which requires additional data and is only
	 * visible for all players within a certain range in the world of @param
	 * center
	 *
	 * @param data
	 *            Data of the effect
	 * @param offsetX
	 *            Maximum distance particles can fly away from the center on the
	 *            x-axis
	 * @param offsetY
	 *            Maximum distance particles can fly away from the center on the
	 *            y-axis
	 * @param offsetZ
	 *            Maximum distance particles can fly away from the center on the
	 *            z-axis
	 * @param speed
	 *            Display speed of the particles
	 * @param amount
	 *            Amount of particles
	 * @param center
	 *            Center location of the effect
	 * @param range
	 *            Range of the visibility
	 * @throws ParticleVersionException
	 *             If the particle effect is not supported by the server version
	 * @throws ParticleDataException
	 *             If the particle effect does not require additional data or if
	 *             the data type is incorrect
	 * @see ParticlePacket
	 * @see ParticlePacket#sendTo(Location, double)
	 */
	public void display(ParticleData<?> data, float offsetX, float offsetY, float offsetZ, float speed, int amount,
			Location center, double range) throws ParticleDataException {
		if (!requiresData) {
			throw new ParticleDataException("The " + this + " particle effect does not require additional data");
		}
		// Just skip it if there's no data, rather than throwing an exception
		if (data == null)
			return;

		if (!isDataCorrect(this, data)) {
			throw new ParticleDataException("The particle data type is incorrect: " + data + " for " + this);
		}
		new ParticlePacket(this, offsetX, offsetY, offsetZ, speed, amount, range > LONG_DISTANCE, data).sendTo(center,
				range);
	}

	/**
	 * Displays a particle effect which requires additional data and is only
	 * visible for the specified players
	 *
	 * @param data
	 *            Data of the effect
	 * @param offsetX
	 *            Maximum distance particles can fly away from the center on the
	 *            x-axis
	 * @param offsetY
	 *            Maximum distance particles can fly away from the center on the
	 *            y-axis
	 * @param offsetZ
	 *            Maximum distance particles can fly away from the center on the
	 *            z-axis
	 * @param speed
	 *            Display speed of the particles
	 * @param amount
	 *            Amount of particles
	 * @param center
	 *            Center location of the effect
	 * @param players
	 *            Receivers of the effect
	 * @throws ParticleVersionException
	 *             If the particle effect is not supported by the server version
	 * @throws ParticleDataException
	 *             If the particle effect does not require additional data or if
	 *             the data type is incorrect
	 * @see ParticlePacket
	 * @see ParticlePacket#sendTo(Location, List)
	 */
	public void display(ParticleData<?> data, float offsetX, float offsetY, float offsetZ, float speed, int amount,
			Location center, List<Player> players) throws ParticleDataException {
		if (!requiresData) {
			throw new ParticleDataException("The " + this + " particle effect does not require additional data");
		}
		if (!isDataCorrect(this, data)) {
			throw new ParticleDataException("The particle data type is incorrect: " + data + " for " + this);
		}
		new ParticlePacket(this, offsetX, offsetY, offsetZ, speed, amount, isLongDistance(center, players), data)
				.sendTo(center, players);
	}

	/**
	 * Displays a particle effect which requires additional data and is only
	 * visible for the specified players
	 *
	 * @param data
	 *            Data of the effect
	 * @param offsetX
	 *            Maximum distance particles can fly away from the center on the
	 *            x-axis
	 * @param offsetY
	 *            Maximum distance particles can fly away from the center on the
	 *            y-axis
	 * @param offsetZ
	 *            Maximum distance particles can fly away from the center on the
	 *            z-axis
	 * @param speed
	 *            Display speed of the particles
	 * @param amount
	 *            Amount of particles
	 * @param center
	 *            Center location of the effect
	 * @param players
	 *            Receivers of the effect
	 * @throws ParticleVersionException
	 *             If the particle effect is not supported by the server version
	 * @throws ParticleDataException
	 *             If the particle effect does not require additional data or if
	 *             the data type is incorrect
	 * @see #display(ParticleData, float, float, float, float, int, Location,
	 *      List)
	 */
	public void display(ParticleData<?> data, float offsetX, float offsetY, float offsetZ, float speed, int amount,
			Location center, Player... players) throws ParticleDataException {
		display(data, offsetX, offsetY, offsetZ, speed, amount, center, Arrays.asList(players));
	}

	/**
	 * Displays a single particle which requires additional data that flies into
	 * a determined direction and is only visible for all players within a
	 * certain range in the world of @param center
	 *
	 * @param data
	 *            Data of the effect
	 * @param direction
	 *            Direction of the particle
	 * @param speed
	 *            Display speed of the particles
	 * @param center
	 *            Center location of the effect
	 * @param range
	 *            Range of the visibility
	 * @throws ParticleVersionException
	 *             If the particle effect is not supported by the server version
	 * @throws ParticleDataException
	 *             If the particle effect does not require additional data or if
	 *             the data type is incorrect
	 * @see ParticlePacket
	 * @see ParticlePacket#sendTo(Location, double)
	 */
	public void display(ParticleData<?> data, Vector direction, float speed, Location center, double range)
			throws ParticleDataException {
		if (!requiresData) {
			throw new ParticleDataException("The " + this + " particle effect does not require additional data");
		}
		if (!isDataCorrect(this, data)) {
			throw new ParticleDataException("The particle data type is incorrect: " + data + " for " + this);
		}
		new ParticlePacket(this, direction, speed, range > LONG_DISTANCE, data).sendTo(center, range);
	}

	/**
	 * Displays a single particle which requires additional data that flies into
	 * a determined direction and is only visible for the specified players
	 *
	 * @param data
	 *            Data of the effect
	 * @param direction
	 *            Direction of the particle
	 * @param speed
	 *            Display speed of the particles
	 * @param center
	 *            Center location of the effect
	 * @param players
	 *            Receivers of the effect
	 * @throws ParticleVersionException
	 *             If the particle effect is not supported by the server version
	 * @throws ParticleDataException
	 *             If the particle effect does not require additional data or if
	 *             the data type is incorrect
	 * @see ParticlePacket
	 * @see ParticlePacket#sendTo(Location, List)
	 */
	public void display(ParticleData<?> data, Vector direction, float speed, Location center, List<Player> players)
			throws ParticleDataException {
		if (!requiresData) {
			throw new ParticleDataException("The " + this + " particle effect does not require additional data");
		}
		if (!isDataCorrect(this, data)) {
			throw new ParticleDataException("The particle data type is incorrect: " + data + " for " + this);
		}
		new ParticlePacket(this, direction, speed, isLongDistance(center, players), data).sendTo(center, players);
	}

	/**
	 * Displays a single particle which requires additional data that flies into
	 * a determined direction and is only visible for the specified players
	 *
	 * @param data
	 *            Data of the effect
	 * @param direction
	 *            Direction of the particle
	 * @param speed
	 *            Display speed of the particles
	 * @param center
	 *            Center location of the effect
	 * @param players
	 *            Receivers of the effect
	 * @throws ParticleVersionException
	 *             If the particle effect is not supported by the server version
	 * @throws ParticleDataException
	 *             If the particle effect does not require additional data or if
	 *             the data type is incorrect
	 * @see #display(ParticleData, Vector, float, Location, List)
	 */
	public void display(ParticleData<?> data, Vector direction, float speed, Location center, Player... players)
			throws ParticleDataException {
		display(data, direction, speed, center, Arrays.asList(players));
	}

	/**
	 * Represents the particle data for effects like
	 * {@link ParticleEffect#ITEM_CRACK}, {@link ParticleEffect#BLOCK_CRACK} and
	 * {@link ParticleEffect#BLOCK_DUST}
	 * <p>
	 * This class is part of the <b>ParticleEffect Library</b> and follows the
	 * same usage conditions
	 *
	 * @author DarkBlade12
	 * @since 1.6
	 */
	public static interface ParticleData<A extends ParticleParam> {

		/**
		 * Returns the ParticleParam adapted for the Particle.
		 * 
		 * @param particle
		 *            Particle which need a ParticleData.
		 * @return the ParticleParam
		 */
		public A getParticleParam(Particle<?> particle);
	}

	/**
	 * Represents the item data for the {@link ParticleEffect#ITEM_CRACK} effect
	 * <p>
	 * This class is part of the <b>ParticleEffect Library</b> and follows the
	 * same usage conditions
	 *
	 * @author DarkBlade12
	 * @since 1.6
	 */
	public static final class ItemData implements ParticleData<ParticleParamItem> {

		private ItemStack is;

		/**
		 * Construct a new item data
		 *
		 * @param material
		 *            Material of the item
		 */
		public ItemData(Material material) {
			this.is = CraftItemStack.asNMSCopy(new org.bukkit.inventory.ItemStack(material));
		}

		@Override
		@SuppressWarnings("unchecked")
		public ParticleParamItem getParticleParam(Particle<?> particle) {
			return new ParticleParamItem((Particle<ParticleParamItem>) particle, is);
		}
	}

	/**
	 * Represents the block data for the {@link ParticleEffect#BLOCK_CRACK} and
	 * {@link ParticleEffect#BLOCK_DUST} effects
	 * <p>
	 * This class is part of the <b>ParticleEffect Library</b> and follows the
	 * same usage conditions
	 *
	 * @author DarkBlade12
	 * @since 1.6
	 */
	public static final class BlockData implements ParticleData<ParticleParamBlock> {

		private IBlockData block;

		/**
		 * Construct a new block data
		 *
		 * @param material
		 *            Material of the block
		 * @param data
		 *            Data value of the block
		 * @throws IllegalArgumentException
		 *             If the material is not a block
		 */
		public BlockData(Material material) throws IllegalArgumentException {
			if (!material.isBlock()) {
				throw new IllegalArgumentException("The material is not a block");
			}
			this.block = getBlock(material).getBlockData();
		}

		@SuppressWarnings("unchecked")
		@Override
		public ParticleParamBlock getParticleParam(Particle<?> particle) {
			return new ParticleParamBlock((Particle<ParticleParamBlock>) particle, block);
		}

		private static Method getBlock;

		public static Block getBlock(Material mat) {
			try {
				if (getBlock == null) {
					getBlock = Blocks.class.getMethod("get", String.class);
					getBlock.setAccessible(true);
				}
				return (Block) getBlock.invoke(null, mat.toString().toLowerCase());
			} catch (Exception e) {
				throw new IllegalArgumentException(e);
			}
		}
	}

	/**
	 * Represents the redstone data.
	 * 
	 * @author Vilevar
	 * @since 1.13.2
	 */
	public static final class RedstoneData implements ParticleData<ParticleParamRedstone> {

		private float f1, f2, f3, f4;

		/**
		 * Construct a new RedstoneData
		 * 
		 * @param f1
		 * @param f2
		 * @param f3
		 * @param f4
		 */
		public RedstoneData(float f1, float f2, float f3, float f4) {
			this.f1 = f1;
			this.f2 = f2;
			this.f3 = f3;
			this.f4 = f4;
		}

		@Override
		public ParticleParamRedstone getParticleParam(Particle<?> particle) {
			return new ParticleParamRedstone(f1, f2, f3, f4);
		}

	}

	/**
	 * Represents a runtime exception that is thrown either if the displayed
	 * particle effect requires data and has none or vice-versa or if the data
	 * type is wrong
	 * <p>
	 * This class is part of the <b>ParticleEffect Library</b> and follows the
	 * same usage conditions
	 *
	 * @author DarkBlade12
	 * @since 1.6
	 */
	private static final class ParticleDataException extends RuntimeException {

		private static final long serialVersionUID = 3203085387160737484L;

		/**
		 * Construct a new particle data exception
		 *
		 * @param message
		 *            Message that will be logged
		 */
		public ParticleDataException(String message) {
			super(message);
		}
	}

	/**
	 * Represents a particle effect packet with all attributes which is used for
	 * sending packets to the players
	 * <p>
	 * This class is part of the <b>ParticleEffect Library</b> and follows the
	 * same usage conditions
	 *
	 * @author DarkBlade12
	 * @since 1.5
	 */
	public static final class ParticlePacket {

		private static int version;
		// @SuppressWarnings("unused")
		// private static boolean isKcauldron;
		// private static final int[] emptyData = new int[0];
		private static Particle<?>[] enumParticles;
		private static boolean initialized;
		private final ParticleEffect effect;
		private final float offsetX;
		private final float offsetY;
		private final float offsetZ;
		private final float speed;
		private final int amount;
		private final boolean longDistance;
		private final ParticleData<?> data;
		private Object packet;

		/**
		 * Construct a new particle packet
		 *
		 * @param effect
		 *            Particle effect
		 * @param offsetX
		 *            Maximum distance particles can fly away from the center on
		 *            the x-axis
		 * @param offsetY
		 *            Maximum distance particles can fly away from the center on
		 *            the y-axis
		 * @param offsetZ
		 *            Maximum distance particles can fly away from the center on
		 *            the z-axis
		 * @param speed
		 *            Display speed of the particles
		 * @param amount
		 *            Amount of particles
		 * @param longDistance
		 *            Indicates whether the maximum distance is increased from
		 *            256 to 65536
		 * @param data
		 *            Data of the effect
		 * @throws IllegalArgumentException
		 *             If the speed is lower than 0 or the amount is lower than
		 *             1
		 * @see #initialize()
		 */
		public ParticlePacket(ParticleEffect effect, float offsetX, float offsetY, float offsetZ, float speed,
				int amount, boolean longDistance, ParticleData<?> data) throws IllegalArgumentException {
			initialize();
			if (speed < 0) {
				throw new IllegalArgumentException("The speed is lower than 0");
			}
			if (amount < 0) {
				throw new IllegalArgumentException("The amount is lower than 0");
			}
			this.effect = effect;
			this.offsetX = offsetX;
			this.offsetY = offsetY;
			this.offsetZ = offsetZ;
			this.speed = speed;
			this.amount = amount;
			this.longDistance = longDistance;
			this.data = data;
		}

		/**
		 * Construct a new particle packet of a single particle flying into a
		 * determined direction
		 *
		 * @param effect
		 *            Particle effect
		 * @param direction
		 *            Direction of the particle
		 * @param speed
		 *            Display speed of the particle
		 * @param longDistance
		 *            Indicates whether the maximum distance is increased from
		 *            256 to 65536
		 * @param data
		 *            Data of the effect
		 * @throws IllegalArgumentException
		 *             If the speed is lower than 0
		 * @see #initialize()
		 */
		public ParticlePacket(ParticleEffect effect, Vector direction, float speed, boolean longDistance,
				ParticleData<?> data) throws IllegalArgumentException {
			initialize();
			if (speed < 0) {
				throw new IllegalArgumentException("The speed is lower than 0");
			}
			this.effect = effect;
			this.offsetX = (float) direction.getX();
			this.offsetY = (float) direction.getY();
			this.offsetZ = (float) direction.getZ();
			this.speed = speed;
			this.amount = 0;
			this.longDistance = longDistance;
			this.data = data;
		}

		/**
		 * Initializes {@link #packetConstructor}, {@link #getHandle},
		 * {@link #playerConnection} and {@link #sendPacket} and sets
		 * {@link #initialized} to <code>true</code> if it succeeds
		 * <p>
		 * <b>Note:</b> These fields only have to be initialized once, so it
		 * will return if {@link #initialized} is already set to
		 * <code>true</code>
		 *
		 * @throws VersionIncompatibleException
		 *             if your bukkit version is not supported by this library
		 */
		public static void initialize() {
			if (initialized) {
				return;
			}
			ParticleEffect[] particles = values();
			enumParticles = new Particle[particles.length];
			for (int i = 0; i < particles.length; i++) {
				enumParticles[i] = IRegistry.PARTICLE_TYPE.get(new MinecraftKey(particles[i].name));
			}
			initialized = true;
		}

		/**
		 * Returns the version of your server (1.x)
		 *
		 * @return The version number
		 */
		public static int getVersion() {
			return version;
		}

		/**
		 * Determine if {@link #packetConstructor}, {@link #getHandle},
		 * {@link #playerConnection} and {@link #sendPacket} are initialized
		 *
		 * @return Whether these fields are initialized or not
		 * @see #initialize()
		 */
		public static boolean isInitialized() {
			return initialized;
		}

		/**
		 * Sends the packet to a single player and caches it
		 *
		 * @param center
		 *            Center location of the effect
		 * @param player
		 *            Receiver of the packet
		 * @throws PacketInstantiationException
		 *             if instantion fails due to an unknown error
		 * @throws PacketSendingException
		 *             if sending fails due to an unknown error
		 */
		public void sendTo(Location center, Player player) throws PacketInstantiationException, PacketSendingException {
			if (packet == null) {
				try {
					Particle<?> particle = enumParticles[effect.ordinal()];
					ParticleParam pp = null;
					if (particle instanceof ParticleParam) {
						pp = (ParticleParam) particle;
					} else if (data != null) {
						pp = data.getParticleParam(particle);
					} else {
						throw new PacketInstantiationException("The type of particle required requires data.");
					}
					packet = new PacketPlayOutWorldParticles(pp, longDistance, (float) center.getX(),
							(float) center.getY(), (float) center.getZ(), offsetX, offsetY, offsetZ, speed, amount);
				} catch (Exception exception) {
					throw new PacketInstantiationException("Packet instantiation failed", exception);
				}
			}
			try {
				((CraftPlayer) player).getHandle().playerConnection.sendPacket((Packet<?>) packet);
			} catch (Exception exception) {
				throw new PacketSendingException("Failed to send the packet to player '" + player.getName() + "'",
						exception);
			}
		}

		/**
		 * Sends the packet to all players in the list
		 *
		 * @param center
		 *            Center location of the effect
		 * @param players
		 *            Receivers of the packet
		 * @throws IllegalArgumentException
		 *             If the player list is empty
		 * @see #sendTo(Location center, Player player)
		 */
		public void sendTo(Location center, List<Player> players) throws IllegalArgumentException {
			if (players.isEmpty()) {
				throw new IllegalArgumentException("The player list is empty");
			}
			for (Player player : players) {
				sendTo(center, player);
			}
		}

		/**
		 * Sends the packet to all players in a certain range
		 *
		 * @param center
		 *            Center location of the effect
		 * @param range
		 *            Range in which players will receive the packet (Maximum
		 *            range for particles is usually 16, but it can differ for
		 *            some types)
		 * @throws IllegalArgumentException
		 *             If the range is lower than 1
		 * @see #sendTo(Location center, Player player)
		 */
		public void sendTo(Location center, double range) throws IllegalArgumentException {
			if (range < 1) {
				throw new IllegalArgumentException("The range is lower than 1");
			}
			String worldName = center.getWorld().getName();
			double squared = range * range;
			for (Player player : Bukkit.getOnlinePlayers()) {
				if (!player.getWorld().getName().equals(worldName)
						|| player.getLocation().distanceSquared(center) > squared) {
					continue;
				}
				sendTo(center, player);
			}
		}

		/**
		 * Represents a runtime exception that is thrown if packet instantiation
		 * fails
		 * <p>
		 * This class is part of the <b>ParticleEffect Library</b> and follows
		 * the same usage conditions
		 *
		 * @author DarkBlade12
		 * @since 1.4
		 */
		private static final class PacketInstantiationException extends RuntimeException {

			private static final long serialVersionUID = 3203085387160737484L;

			/**
			 * Construct a new packet instantiation exception
			 *
			 * @param message
			 *            Message that will be logged
			 * @param cause
			 *            Cause of the exception
			 */
			public PacketInstantiationException(String message, Throwable cause) {
				super(message, cause);
			}

			/**
			 * Construct a new packet instantiation exception
			 *
			 * @param message
			 *            Message that will be logged
			 */
			public PacketInstantiationException(String message) {
				super(message);
			}
		}

		/**
		 * Represents a runtime exception that is thrown if packet sending fails
		 * <p>
		 * This class is part of the <b>ParticleEffect Library</b> and follows
		 * the same usage conditions
		 *
		 * @author DarkBlade12
		 * @since 1.4
		 */
		private static final class PacketSendingException extends RuntimeException {

			private static final long serialVersionUID = 3203085387160737484L;

			/**
			 * Construct a new packet sending exception
			 *
			 * @param message
			 *            Message that will be logged
			 * @param cause
			 *            Cause of the exception
			 */
			public PacketSendingException(String message, Throwable cause) {
				super(message, cause);
			}
		}

	}

	/**
	 * Helper method to migrate pre-3.0 Effects.
	 *
	 * @param center
	 * @param range
	 * @param offsetX
	 * @param offsetY
	 * @param offsetZ
	 * @param speed
	 * @param amount
	 */
	@Deprecated
	public void display(Location center, double range, float offsetX, float offsetY, float offsetZ, float speed,
			int amount) {
		display(offsetX, offsetY, offsetZ, speed, amount, center, range);
	}

	/**
	 * Helper method to migrate pre-3.0 Effects.
	 *
	 * @param center
	 * @param range
	 */
	@Deprecated
	public void display(Location center, double range) {
		display(0, 0, 0, 0, 1, center, range);
	}

	/**
	 * Helper method to migrate pre-3.0 Effects, and bridge parameterized
	 * effects.
	 *
	 * @param data
	 * @param center
	 * @param range
	 * @param offsetX
	 * @param offsetY
	 * @param offsetZ
	 * @param speed
	 * @param amount
	 */
	@Deprecated
	public void display(ParticleData<?> data, Location center, double range, float offsetX, float offsetY,
			float offsetZ, float speed, int amount) {
		if (this.requiresData) {
			display(data, offsetX, offsetY, offsetZ, speed, amount, center, range);
		} else {
			display(offsetX, offsetY, offsetZ, speed, amount, center, range);
		}
	}

	public void display(Location center, Color color, double range) {
		display(null, center, color, range, 0, 0, 0, 1, 0);
	}

	public void display(ParticleData<?> data, Location center, Color color, double range, float offsetX, float offsetY,
			float offsetZ, float speed, int amount) {
		// Colorizeable!
		if (color != null && (this == ParticleEffect.REDSTONE || this == ParticleEffect.SPELL_MOB
				|| this == ParticleEffect.SPELL_MOB_AMBIENT)) {
			amount = 0;
			// Colored particles can't have a speed of 0.
			if (speed == 0) {
				speed = 1;
			}
			offsetX = (float) color.getRed() / 255;
			offsetY = (float) color.getGreen() / 255;
			offsetZ = (float) color.getBlue() / 255;

			// The redstone particle reverts to red if R is 0!
			if (offsetX < Float.MIN_NORMAL) {
				offsetX = Float.MIN_NORMAL;
			}
		}

		if (this.requiresData) {
			display(data, offsetX, offsetY, offsetZ, speed, amount, center, range);
		} else {
			display(offsetX, offsetY, offsetZ, speed, amount, center, range);
		}
	}

	public boolean requiresData() {
		return requiresData;
	}

	public ParticleData<?> getData(Material material) {
		ParticleData<?> data = null;
		if (this == BLOCK_CRACK || this == ITEM_CRACK || this == BLOCK_DUST || this == FALLING_DUST) {
			if (material != null && material != Material.AIR) {
				if (this == ITEM_CRACK) {
					data = new ItemData(material);
				} else {
					data = new BlockData(material);
				}
			}
		}

		return data;
	}
}

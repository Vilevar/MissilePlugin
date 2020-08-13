package be.vilevar.missiles.utils;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.Locale;

import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.craftbukkit.libs.org.apache.commons.lang3.Validate;
import org.bukkit.craftbukkit.v1_15_R1.CraftServer;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.generator.ChunkGenerator;

import com.google.common.base.Preconditions;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import net.minecraft.server.v1_15_R1.Biomes;
import net.minecraft.server.v1_15_R1.DimensionManager;
import net.minecraft.server.v1_15_R1.EnumDifficulty;
import net.minecraft.server.v1_15_R1.EnumGamemode;
import net.minecraft.server.v1_15_R1.GeneratorSettingsDefault;
import net.minecraft.server.v1_15_R1.WorldData;
import net.minecraft.server.v1_15_R1.WorldGenFeatureVillageConfiguration;
import net.minecraft.server.v1_15_R1.WorldGenerator;
import net.minecraft.server.v1_15_R1.WorldNBTStorage;
import net.minecraft.server.v1_15_R1.WorldProvider;
import net.minecraft.server.v1_15_R1.WorldServer;
import net.minecraft.server.v1_15_R1.WorldSettings;
import net.minecraft.server.v1_15_R1.WorldType;

public class MissileWorldGenerator {

	@SuppressWarnings({ "deprecation", "resource" })
	public static World createWorld(CraftServer server, String name) {
		generateBiomes();
		
		WorldCreator creator = new WorldCreator(name);
		Preconditions.checkState(!server.getServer().worldServer.isEmpty(), "Cannot create additional worlds on STARTUP");
		Validate.notNull(creator, "Creator may not be null");
		name = creator.name();
		ChunkGenerator generator = creator.generator();
		File folder = new File(server.getWorldContainer(), name);
		World world = server.getWorld(name);
		WorldType type = WorldType.getType(creator.type().getName());
		boolean generateStructures = creator.generateStructures();
		if (world != null) {
			return world;
		} else if (folder.exists() && !folder.isDirectory()) {
			throw new IllegalArgumentException("File exists with the name '" + name + "' and isn't a folder");
		} else {
			if (generator == null) {
				generator = server.getGenerator(name);
			}

			server.getServer().convertWorld(name);
			int dimension = 10 + server.getServer().worldServer.size();
			boolean used = false;

			do {
				Iterator<WorldServer> var11 = server.getServer().getWorlds().iterator();

				while (var11.hasNext()) {
					WorldServer worldServer = (WorldServer) var11.next();
					used = worldServer.getWorldProvider().getDimensionManager().getDimensionID() == dimension;
					if (used) {
						++dimension;
						break;
					}
				}
			} while (used);

			boolean hardcore = false;
			WorldNBTStorage sdm = new WorldNBTStorage(server.getWorldContainer(), name, server.getServer(),
					server.getHandle().getServer().dataConverterManager);
			WorldData worlddata = sdm.getWorldData();
			WorldSettings worldSettings;
			if (worlddata == null) {
				worldSettings = new WorldSettings(creator.seed(),
						EnumGamemode.getById(server.getDefaultGameMode().getValue()), generateStructures, hardcore, type);
				JsonElement parsedSettings = (new JsonParser()).parse(creator.generatorSettings());
				if (parsedSettings.isJsonObject()) {
					worldSettings.setGeneratorSettings(parsedSettings.getAsJsonObject());
				}

				worlddata = new WorldData(worldSettings, name);
			} else {
				worlddata.setName(name);
				worldSettings = new WorldSettings(worlddata);
			}

			DimensionManager actualDimension = DimensionManager.a(creator.environment().getId());
			DimensionManager internalDimension = DimensionManager.register(name.toLowerCase(Locale.ENGLISH),
					new DimensionManager(dimension, actualDimension.getSuffix(), actualDimension.folder,
							(w, manager) -> {
								return (WorldProvider) actualDimension.providerFactory.apply(w, manager);
							}, actualDimension.hasSkyLight(), actualDimension.getGenLayerZoomer(), actualDimension));
			WorldServer internal = new WorldServer(server.getServer(), server.getServer().executorService, sdm, worlddata,
					internalDimension, server.getServer().getMethodProfiler(),
					server.getServer().worldLoadListenerFactory.create(11), creator.environment(), generator);
			
			try {
				Field distance = GeneratorSettingsDefault.class.getDeclaredField("a");
				distance.setAccessible(true);
				distance.set(internal.getChunkProvider().getChunkGenerator().getSettings(), 9);
				distance.setAccessible(false);
			//	WorldGenVillage
			} catch (Exception e) {
				System.out.println("Could not modify the distance between villages.");
				e.printStackTrace();
			}
			
			if (server.getWorld(name) == null) {
				return null;
			} else {
				server.getServer().initWorld(internal, worlddata, worldSettings);
				internal.worldData.setDifficulty(EnumDifficulty.EASY);
				internal.setSpawnFlags(true, true);
				server.getServer().worldServer.put(internal.getWorldProvider().getDimensionManager(), internal);
				server.getPluginManager().callEvent(new WorldInitEvent(internal.getWorld()));
				server.getServer().loadSpawn(internal.getChunkProvider().playerChunkMap.worldLoadListener, internal);
				server.getPluginManager().callEvent(new WorldLoadEvent(internal.getWorld()));
				return internal.getWorld();
			}
		}
	}
	
	private static void generateBiomes() {
		Biomes.BADLANDS.a(WorldGenerator.VILLAGE.b(new WorldGenFeatureVillageConfiguration("village/savanna/town_centers", 6)));
		Biomes.BADLANDS_PLATEAU.a(WorldGenerator.VILLAGE.b(new WorldGenFeatureVillageConfiguration("village/savanna/town_centers", 6)));
		Biomes.BAMBOO_JUNGLE.a(WorldGenerator.VILLAGE.b(new WorldGenFeatureVillageConfiguration("village/plains/town_centers", 6)));
		Biomes.BAMBOO_JUNGLE_HILLS.a(WorldGenerator.VILLAGE.b(new WorldGenFeatureVillageConfiguration("village/plains/town_centers", 6)));
		Biomes.BEACH.a(WorldGenerator.VILLAGE.b(new WorldGenFeatureVillageConfiguration("village/plains/town_centers", 6)));
		Biomes.BIRCH_FOREST.a(WorldGenerator.VILLAGE.b(new WorldGenFeatureVillageConfiguration("village/plains/town_centers", 6)));
		Biomes.BIRCH_FOREST_HILLS.a(WorldGenerator.VILLAGE.b(new WorldGenFeatureVillageConfiguration("village/plains/town_centers", 6)));
		Biomes.DARK_FOREST.a(WorldGenerator.VILLAGE.b(new WorldGenFeatureVillageConfiguration("village/plains/town_centers", 6)));
		Biomes.DARK_FOREST_HILLS.a(WorldGenerator.VILLAGE.b(new WorldGenFeatureVillageConfiguration("village/plains/town_centers", 6)));
		Biomes.DESERT_HILLS.a(WorldGenerator.VILLAGE.b(new WorldGenFeatureVillageConfiguration("village/desert/town_centers", 6)));
		Biomes.DESERT_LAKES.a(WorldGenerator.VILLAGE.b(new WorldGenFeatureVillageConfiguration("village/desert/town_centers", 6)));
		Biomes.ERODED_BADLANDS.a(WorldGenerator.VILLAGE.b(new WorldGenFeatureVillageConfiguration("village/savanna/town_centers", 6)));
		Biomes.FLOWER_FOREST.a(WorldGenerator.VILLAGE.b(new WorldGenFeatureVillageConfiguration("village/plains/town_centers", 6)));
		Biomes.FOREST.a(WorldGenerator.VILLAGE.b(new WorldGenFeatureVillageConfiguration("village/plains/town_centers", 6)));
		Biomes.GIANT_SPRUCE_TAIGA.a(WorldGenerator.VILLAGE.b(new WorldGenFeatureVillageConfiguration("village/taiga/town_centers", 6)));
		Biomes.GIANT_SPRUCE_TAIGA_HILLS.a(WorldGenerator.VILLAGE.b(new WorldGenFeatureVillageConfiguration("village/taiga/town_centers", 6)));
		Biomes.GIANT_TREE_TAIGA.a(WorldGenerator.VILLAGE.b(new WorldGenFeatureVillageConfiguration("village/taiga/town_centers", 6)));
		Biomes.GIANT_TREE_TAIGA_HILLS.a(WorldGenerator.VILLAGE.b(new WorldGenFeatureVillageConfiguration("village/taiga/town_centers", 6)));
		Biomes.GRAVELLY_MOUNTAINS.a(WorldGenerator.VILLAGE.b(new WorldGenFeatureVillageConfiguration("village/taiga/town_centers", 6)));
		Biomes.ICE_SPIKES.a(WorldGenerator.VILLAGE.b(new WorldGenFeatureVillageConfiguration("village/snowy/town_centers", 6)));
		Biomes.JUNGLE.a(WorldGenerator.VILLAGE.b(new WorldGenFeatureVillageConfiguration("village/plains/town_centers", 6)));
		Biomes.JUNGLE_HILLS.a(WorldGenerator.VILLAGE.b(new WorldGenFeatureVillageConfiguration("village/plains/town_centers", 6)));
		Biomes.MODIFIED_BADLANDS_PLATEAU.a(WorldGenerator.VILLAGE.b(new WorldGenFeatureVillageConfiguration("village/savanna/town_centers", 6)));
		Biomes.MODIFIED_GRAVELLY_MOUNTAINS.a(WorldGenerator.VILLAGE.b(new WorldGenFeatureVillageConfiguration("village/taiga/town_centers", 6)));
		Biomes.MODIFIED_JUNGLE.a(WorldGenerator.VILLAGE.b(new WorldGenFeatureVillageConfiguration("village/plains/town_centers", 6)));
		Biomes.MODIFIED_JUNGLE_EDGE.a(WorldGenerator.VILLAGE.b(new WorldGenFeatureVillageConfiguration("village/plains/town_centers", 6)));
		Biomes.MODIFIED_WOODED_BADLANDS_PLATEAU.a(WorldGenerator.VILLAGE.b(new WorldGenFeatureVillageConfiguration("village/savanna/town_centers", 6)));
		Biomes.MOUNTAIN_EDGE.a(WorldGenerator.VILLAGE.b(new WorldGenFeatureVillageConfiguration("village/taiga/town_centers", 6)));
		Biomes.MOUNTAINS.a(WorldGenerator.VILLAGE.b(new WorldGenFeatureVillageConfiguration("village/taiga/town_centers", 6)));
		Biomes.MUSHROOM_FIELD_SHORE.a(WorldGenerator.VILLAGE.b(new WorldGenFeatureVillageConfiguration("village/taiga/town_centers", 6)));
		Biomes.MUSHROOM_FIELDS.a(WorldGenerator.VILLAGE.b(new WorldGenFeatureVillageConfiguration("village/taiga/town_centers", 6)));
		Biomes.RIVER.a(WorldGenerator.VILLAGE.b(new WorldGenFeatureVillageConfiguration("village/plains/town_centers", 6)));
		Biomes.SAVANNA_PLATEAU.a(WorldGenerator.VILLAGE.b(new WorldGenFeatureVillageConfiguration("village/savanna/town_centers", 6)));
		Biomes.SHATTERED_SAVANNA.a(WorldGenerator.VILLAGE.b(new WorldGenFeatureVillageConfiguration("village/savanna/town_centers", 6)));
		Biomes.SHATTERED_SAVANNA_PLATEAU.a(WorldGenerator.VILLAGE.b(new WorldGenFeatureVillageConfiguration("village/savanna/town_centers", 6)));
		Biomes.SNOWY_BEACH.a(WorldGenerator.VILLAGE.b(new WorldGenFeatureVillageConfiguration("village/snowy/town_centers", 6)));
		Biomes.SNOWY_MOUNTAINS.a(WorldGenerator.VILLAGE.b(new WorldGenFeatureVillageConfiguration("village/snowy/town_centers", 6)));
		Biomes.SNOWY_TAIGA.a(WorldGenerator.VILLAGE.b(new WorldGenFeatureVillageConfiguration("village/snowy/town_centers", 6)));
		Biomes.SNOWY_TAIGA_HILLS.a(WorldGenerator.VILLAGE.b(new WorldGenFeatureVillageConfiguration("village/snowy/town_centers", 6)));
		Biomes.SNOWY_TAIGA_MOUNTAINS.a(WorldGenerator.VILLAGE.b(new WorldGenFeatureVillageConfiguration("village/snowy/town_centers", 6)));
		Biomes.SUNFLOWER_PLAINS.a(WorldGenerator.VILLAGE.b(new WorldGenFeatureVillageConfiguration("village/plains/town_centers", 6)));
		Biomes.SWAMP.a(WorldGenerator.VILLAGE.b(new WorldGenFeatureVillageConfiguration("village/plains/town_centers", 6)));
		Biomes.SWAMP_HILLS.a(WorldGenerator.VILLAGE.b(new WorldGenFeatureVillageConfiguration("village/plains/town_centers", 6)));
		Biomes.TAIGA_HILLS.a(WorldGenerator.VILLAGE.b(new WorldGenFeatureVillageConfiguration("village/taiga/town_centers", 6)));
		Biomes.TAIGA_MOUNTAINS.a(WorldGenerator.VILLAGE.b(new WorldGenFeatureVillageConfiguration("village/taiga/town_centers", 6)));
		Biomes.TALL_BIRCH_FOREST.a(WorldGenerator.VILLAGE.b(new WorldGenFeatureVillageConfiguration("village/plains/town_centers", 6)));
		Biomes.TALL_BIRCH_HILLS.a(WorldGenerator.VILLAGE.b(new WorldGenFeatureVillageConfiguration("village/plains/town_centers", 6)));
		Biomes.WOODED_BADLANDS_PLATEAU.a(WorldGenerator.VILLAGE.b(new WorldGenFeatureVillageConfiguration("village/savanna/town_centers", 6)));
		Biomes.WOODED_HILLS.a(WorldGenerator.VILLAGE.b(new WorldGenFeatureVillageConfiguration("village/taiga/town_centers", 6)));
		Biomes.WOODED_MOUNTAINS.a(WorldGenerator.VILLAGE.b(new WorldGenFeatureVillageConfiguration("village/taiga/town_centers", 6)));
	}
}

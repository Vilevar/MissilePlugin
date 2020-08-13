package be.vilevar.missiles.utils;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.craftbukkit.libs.org.apache.commons.lang3.tuple.Pair;
import org.bukkit.craftbukkit.libs.org.apache.commons.lang3.tuple.Triple;
import org.bukkit.craftbukkit.v1_15_R1.CraftServer;
import org.bukkit.craftbukkit.v1_15_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_15_R1.event.CraftEventFactory;
import org.bukkit.entity.EntityType;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.minecraft.server.v1_15_R1.AxisAlignedBB;
import net.minecraft.server.v1_15_R1.BlockPosition;
import net.minecraft.server.v1_15_R1.Blocks;
import net.minecraft.server.v1_15_R1.DamageSource;
import net.minecraft.server.v1_15_R1.EnchantmentProtection;
import net.minecraft.server.v1_15_R1.Entity;
import net.minecraft.server.v1_15_R1.EntityDamageSource;
import net.minecraft.server.v1_15_R1.EntityFallingBlock;
import net.minecraft.server.v1_15_R1.EntityHuman;
import net.minecraft.server.v1_15_R1.EntityLiving;
import net.minecraft.server.v1_15_R1.EntityPose;
import net.minecraft.server.v1_15_R1.EntitySize;
import net.minecraft.server.v1_15_R1.EntityTNTPrimed;
import net.minecraft.server.v1_15_R1.EntityTypes;
import net.minecraft.server.v1_15_R1.EnumCreatureType;
import net.minecraft.server.v1_15_R1.Explosion;
import net.minecraft.server.v1_15_R1.Fluid;
import net.minecraft.server.v1_15_R1.IBlockData;
import net.minecraft.server.v1_15_R1.IRegistry;
import net.minecraft.server.v1_15_R1.MathHelper;
import net.minecraft.server.v1_15_R1.NBTTagCompound;
import net.minecraft.server.v1_15_R1.Packet;
import net.minecraft.server.v1_15_R1.PacketPlayOutSpawnEntity;
import net.minecraft.server.v1_15_R1.Particles;
import net.minecraft.server.v1_15_R1.SoundCategory;
import net.minecraft.server.v1_15_R1.SoundEffects;
import net.minecraft.server.v1_15_R1.Vec3D;
import net.minecraft.server.v1_15_R1.World;

public class MegaExplosion extends Entity {

	public static EntityTypes<Entity> type;

	public static void registerMegaExplosion() {
		type = IRegistry.a(IRegistry.ENTITY_TYPE, "mega_explosion",
				EntityTypes.a.a(MegaExplosion::new, EnumCreatureType.MISC).b().c().a("mega_explosion"));
	}

	public static void createExplosion(Location loc, float power, org.bukkit.entity.Entity source) {
	//	System.out.println("create mega explosion "+type);
		CraftWorld craft = (CraftWorld) loc.getWorld();
		if (type != null) {
			MegaExplosion explosion = (MegaExplosion) type.a(craft.getHandle());
		//	System.out.println("new explosion created "+explosion);
			explosion.start(power, loc.getX(), loc.getY(), loc.getZ(), source);
		//	System.out.println("explosion started");
			craft.addEntity(explosion, SpawnReason.CUSTOM);
		}
	}
	
	private CraftEntity bukkitEntity;

	private Entity source;
	private float power;
	private State state = State.WAITING;

	private Map<Triple<Integer, Integer, Integer>, Pair<Float, Triple<Double, Double, Double>>> fs = Maps.newHashMap();
	private double radius;
	private List<Entity> listEntity;
	private Vec3D vec3d;
	private int entityIndex;
//	private int blockIndex;
//	private List<Block> blockList = Lists.newArrayList();
//	private float yield;
//	private ObjectArrayList<com.mojang.datafixers.util.Pair<ItemStack, BlockPosition>> objectarraylist;
	private Iterator<BlockPosition> destroyIterator;
//	private int dropsI;
//	private int dropsJ = 1;
	private Iterator<BlockPosition> igniteIterator;
	
	private final List<BlockPosition> blocks = Lists.newArrayList();
	private final Map<EntityHuman, Vec3D> l = Maps.newHashMap();
	

	public MegaExplosion(EntityTypes<?> entitytypes, World world) {
		super(entitytypes, world);
		this.setNoGravity(true);
	}

	public void start(float power, double x, double y, double z, org.bukkit.entity.Entity source) {
		this.lastX = x;
		this.lastY = y;
		this.lastZ = z;
		this.setPosition(x, y, z);
		this.power = power;
		if(source != null && source instanceof CraftEntity)
			this.source = ((CraftEntity) source).getHandle();
		this.state = State.START_A;
	}

	@Override
	public void tick() {
		System.out.println("Tick "+this.state);
		if(!this.world.isClientSide)
			if((this.state = this.state.doStep(this)) == State.DIE)
				this.state.stepper.doStep(this);
	}

	@Override
	public boolean isNoGravity() {
		return true;
	}

	@Override
	protected void initDatawatcher() {}

	@Override
	protected boolean playStepSound() {
		return false;
	}

	@Override
	public boolean isInteractable() {
		return false;
	}

	@Override
	protected void a(NBTTagCompound nbt) {
		this.power = nbt.getFloat("power");
		this.state = State.valueOf(nbt.getString("state"));
	}

	@Override
	protected void b(NBTTagCompound nbt) {
		nbt.setFloat("power", this.power);
		nbt.setString("state", this.state.toString());
	}

	@Override
	public Packet<?> L() {
		return new PacketPlayOutSpawnEntity(this);
	}

	@Override
	public CraftEntity getBukkitEntity() {
		return this.bukkitEntity == null ? this.bukkitEntity = new CraftMegaExplosion(this.world.getServer(), this) : this.bukkitEntity;
	}

	@Override
	protected float getHeadHeight(EntityPose entitypose, EntitySize entitysize) {
		return 0;
	}
	
	
	
	public String getBlocks() {
		if (this.power < 0.1F) return "START_B";
	//	org.bukkit.World bworld = this.world.getWorld();
		boolean toNext = true;
		Set<BlockPosition> set = Sets.newHashSet();
		for (int k = 0; k < 16; k++) {
			for (int i = 0; i < 16; i++) {
				for (int j = 0; j < 16; j++) {
					if (k == 0 || k == 15 || i == 0 || i == 15 || j == 0 || j == 15) {
						double d0 = k / 15.0F * 2.0F - 1.0F;
						double d1 = i / 15.0F * 2.0F - 1.0F;
						double d2 = j / 15.0F * 2.0F - 1.0F;
						double d3 = Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
						
						d0 /= d3;
						d1 /= d3;
						d2 /= d3;
						
						Triple<Integer, Integer, Integer> direction = Triple.of(k, i, j);
						Pair<Float, Triple<Double, Double, Double>> getDirection = this.fs.containsKey(direction) ? this.fs.get(direction) :
							Pair.of(this.power * (0.8F + this.world.random.nextFloat() * 0.4F), Triple.of(this.locX(), this.locY(), this.locZ()));
						
						float f = getDirection.getLeft();
						Triple<Double, Double, Double> pos = getDirection.getRight();
						
						double x = pos.getLeft();
						double y = pos.getMiddle();
						double z = pos.getRight();
							
						for (double var21 = 0.3F, test = 0; f > 0.0F && test < 200; f -= 0.22500001F, test++, x += d0 * var21, y += d1 * var21,
								z += d2 * var21) {
							if(y < 0 || y >= 256)
								continue;
							BlockPosition blockposition = new BlockPosition(x, y, z);
							IBlockData iblockdata = this.world.getType(blockposition);
							if(iblockdata.isAir())
								continue;
							
							Fluid fluid = this.world.getFluid(blockposition);
							if (!fluid.isEmpty()) {
								float f2 = Math.max(iblockdata.getBlock().getDurability(), fluid.k());
								if (this.source != null) {
									f2 = this.source.a(null, this.world, blockposition, iblockdata, fluid, f2);
								}
								f -= (f2 + var21) * var21;
							}
							if (f > 0.0F && (this.source == null || this.source.a(null, this.world, blockposition, iblockdata, f))) {
								set.add(blockposition);
								// Prepare bukkit
						//		Block bblock = bworld.getBlockAt((int) x, (int) y, (int) z);
						//		this.blockList.add(bblock);
							}
						}
						this.fs.put(direction, Pair.of(f, Triple.of(x, y, z)));
						toNext = toNext && f <= 0;
					}
				}
			}
		}
		this.blocks.addAll(set);
		if(toNext) {
		//	Collections.reverse(this.blocks);
	//		Collections.shuffle(this.blockList, this.world.random);
			return "START_B";
		}
		return "GET_BLOCKS";
	}
	
/*	public String prepareBukkit() {
		if(this.blockList == null) {
			Collections.shuffle(this.blocks, this.world.random);
			this.blockList = Lists.newArrayList();
			this.blockIndex = this.blocks.size() - 1;
		}
		org.bukkit.World bworld = this.world.getWorld();
		for (int i = 0; this.blockIndex >= 0 && i < 1000; this.blockIndex--, i++) {
			BlockPosition cpos = (BlockPosition) this.blocks.get(this.blockIndex);
			Block bblock = bworld.getBlockAt(cpos.getX(), cpos.getY(), cpos.getZ());
			if (bblock.getType() != Material.AIR) {
				blockList.add(bblock);
			}
		}
		
		return this.blockIndex >= 0 ? "PREPARE_BUKKIT" : "CREATE_EVENTS";
	}*/
	
/*	public String createEvents() {
		
		List<Block> blockList = this.blockList;
		org.bukkit.entity.Entity explode = this.source == null ? null : this.source.getBukkitEntity();
		Location location = new Location(this.world.getWorld(), this.locX(), this.locY(), this.locZ());
		
	//	List<Block> bukkitBlocks;
	//	float yield;
		if (explode != null) {
			EntityExplodeEvent event = new EntityExplodeEvent(explode, location, blockList, 1);
					// this.b == Effect.DESTROY ? 1.0F / this.size : 1.0F);
			this.world.getServer().getPluginManager().callEvent(event);
		//	bukkitBlocks = event.blockList();
		//	yield = event.getYield();
		} else {
			BlockExplodeEvent event = new BlockExplodeEvent(location.getBlock(), blockList, 1);
					// this.b == Effect.DESTROY ? 1.0F / this.size : 1.0F);
			this.world.getServer().getPluginManager().callEvent(event);
		//	bukkitBlocks = event.blockList();
			yield = event.getYield();
		}

	//	this.blocks.clear();
	//	Iterator<Block> var13 = bukkitBlocks.iterator();

	//	while (var13.hasNext()) {
	//		Block bblock = var13.next();
	//		BlockPosition coords = new BlockPosition(bblock.getX(), bblock.getY(), bblock.getZ());
	//		this.blocks.add(coords);
	//	}
		
	//	this.yield = yield;
		
		return "DESTROY_BLOCKS";
	}*/
	
	public String destroyBlocks() {
	//	if(this.objectarraylist == null)
	//		this.objectarraylist = new ObjectArrayList<>();
		if(this.destroyIterator == null)
			this.destroyIterator = this.blocks.iterator();
	//	float yield = this.yield;
		Iterator<BlockPosition> iterator = this.destroyIterator;

		for(int i = 0; i < 100 && iterator.hasNext(); i++) {
			BlockPosition blockposition = iterator.next();
			IBlockData iblockdata = this.world.getType(blockposition);
			if(iblockdata.isAir())
				continue;
			net.minecraft.server.v1_15_R1.Block block = iblockdata.getBlock();
		/*	do {
				if (!iterator.hasNext()) {
					return "ADAPT_DROPS";
				}
				blockposition = iterator.next();
				iblockdata = this.world.getType(blockposition);
				block = iblockdata.getBlock();
			} while (iblockdata.isAir());*/

		//	BlockPosition blockposition1 = blockposition.immutableCopy();
			this.world.getMethodProfiler().enter("explosion_blocks");
		/*	if (block.a((Explosion) null) && this.world instanceof WorldServer) {
				TileEntity tileentity = block.isTileEntity() ? this.world.getTileEntity(blockposition) : null;
				Builder loottableinfo_builder = (new Builder((WorldServer) this.world)).a(this.world.random)
						.set(LootContextParameters.POSITION, blockposition).set(LootContextParameters.TOOL, ItemStack.a)
						.setOptional(LootContextParameters.BLOCK_ENTITY, tileentity)
						.setOptional(LootContextParameters.THIS_ENTITY, this.source);
				if (yield < 1.0F) {
					loottableinfo_builder.set(LootContextParameters.EXPLOSION_RADIUS, 1.0F / yield);
				}

				iblockdata.a(loottableinfo_builder).forEach(itemstack -> {
					if(!itemstack.equals(ItemStack.a))
						this.objectarraylist.add(com.mojang.datafixers.util.Pair.of(itemstack, blockposition1));
				});
			}*/

			this.world.setTypeAndData(blockposition, Blocks.AIR.getBlockData(), 3);
			block.wasExploded(this.world, blockposition, null);
			this.world.getMethodProfiler().exit();
		}
		
		return iterator.hasNext() ? "DESTROY_BLOCKS" : "IGNITE_BLOCKS";
	}
	
	public String igniteBlocks() {
		if(this.igniteIterator == null) {
			this.igniteIterator = this.blocks.iterator();
		}
		Iterator<BlockPosition> it = this.igniteIterator;
		for (int i = 0; i < 150 && it.hasNext(); i++) {
			BlockPosition position = it.next();
			if (this.random.nextInt(3) == 0 && this.world.getType(position).isAir()
					&& this.world.getType(position.down()).g(this.world, position.down())) {
				org.bukkit.World bukkitWorld = this.world.getWorld();
				BlockIgniteEvent event = new BlockIgniteEvent(bukkitWorld.getBlockAt(position.getX(), position.getY(), position.getZ()),
						IgniteCause.EXPLOSION, this.source == null ? null : this.source.getBukkitEntity());
				world.getServer().getPluginManager().callEvent(event);
				this.world.setTypeUpdate(position, Blocks.FIRE.getBlockData());
			}
		}
		
		return it.hasNext() ? "IGNITE_BLOCKS" : "GET_ENTITIES";
	}
	
	public String getEntities() {
		double radius = this.power * 2.0F;
		double x = this.locX();
		double y = this.locY();
		double z = this.locZ();
		double minX = MathHelper.floor(x - radius - 1.0D);
		double maxX = MathHelper.floor(x + radius + 1.0D);
		double minY = MathHelper.floor(y - radius - 1.0D);
		double maxY = MathHelper.floor(y + radius + 1.0D);
		double minZ = MathHelper.floor(z - radius - 1.0D);
		double maxZ = MathHelper.floor(z + radius + 1.0D);
		List<Entity> list = this.world.getEntities(this.source, new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ));
		Vec3D vec3d = new Vec3D(x, y, z);
		
		this.radius = radius;
		this.listEntity = list;
		this.vec3d = vec3d;
		
		return "WORK_ENTITIES";
	}
	
	public String workEntities() {
		double f3 = this.radius;
		List<Entity> list = this.listEntity;
		Vec3D vec3d = this.vec3d;
		double x = this.locX();
		double y = this.locY();
		double z = this.locZ();
		
		for (int i = 0; this.entityIndex < list.size() && i < 100; this.entityIndex++, i++) {
			Entity entity = list.get(this.entityIndex);
			if (!entity.ca()) {
				double squaredDelta = entity.c(vec3d);
				double relativeDistance = MathHelper.sqrt(squaredDelta) / f3;
				if (relativeDistance <= 1.0D) {
					double dX = entity.locX() - x;
					double dY = entity.getHeadY() - y;
					double dZ = entity.locZ() - z;
					double distance = MathHelper.sqrt(dX * dX + dY * dY + dZ * dZ);
					if (distance != 0.0D) {
						dX /= distance;
						dY /= distance;
						dZ /= distance;
						double ejectPower = Explosion.a(vec3d, entity);
						double damage = (1.0D - relativeDistance) * ejectPower;
						CraftEventFactory.entityDamage = this;
						entity.forceExplosionKnockback = false;
						boolean wasDamaged = entity.damageEntity(this.source != null ? 
								new EntityDamageSource("explosion.player", this.source).r().setExplosion() : DamageSource.explosion(null),
								(float) (/*(int)*/ (damage * (damage + 1) / 2.0D * 7.0D * f3 + 1.0D)));
						CraftEventFactory.entityDamage = null;
						if (wasDamaged || entity instanceof EntityTNTPrimed || entity instanceof EntityFallingBlock || entity.forceExplosionKnockback) {
							double modifiedDamage = damage;
							if (entity instanceof EntityLiving) {
								modifiedDamage = EnchantmentProtection.a((EntityLiving) entity, damage);
							}

							entity.setMot(entity.getMot().add(dX * modifiedDamage, dY * modifiedDamage, dZ * modifiedDamage));
							if (entity instanceof EntityHuman) {
								EntityHuman entityhuman = (EntityHuman) entity;
								if (!entityhuman.isSpectator() && (!entityhuman.isCreative() || !entityhuman.abilities.isFlying)) {
									this.l.put(entityhuman, new Vec3D(dX * damage, dY * damage, dZ * damage));
								}
							}
						}
					}
				}
			}
		}
		
		return this.entityIndex < list.size() ? "WORK_ENTITIES" : "PARTICLES";
	}
	
/*	public String adaptDrops() {
		System.out.println(this.objectarraylist.size());
		all: for(int test = 0; test < 100 && this.dropsI < this.objectarraylist.size(); test++, this.dropsI++, this.dropsJ = this.dropsI + 1) {
			com.mojang.datafixers.util.Pair<ItemStack, BlockPosition> pair1 = this.objectarraylist.get(this.dropsI);
			ItemStack is1 = pair1.getFirst();
			for(; test < 100 && this.dropsJ < this.objectarraylist.size(); this.dropsJ++, test++) {
				com.mojang.datafixers.util.Pair<ItemStack, BlockPosition> pair2 = this.objectarraylist.get(this.dropsI);
				ItemStack is2 = pair2.getFirst();
				if(EntityItem.a(is1, is2)) {
					is1 = EntityItem.a(is1, is2, 16);
					if(is2.isEmpty()) {
						this.objectarraylist.remove(this.dropsJ);
					}
					if(is1.getCount() == 16) {
						this.objectarraylist.set(this.dropsI, com.mojang.datafixers.util.Pair.of(is1, pair1.getSecond()));
						continue all;
					}
				}
			}
			this.objectarraylist.set(this.dropsI, com.mojang.datafixers.util.Pair.of(is1, pair1.getSecond()));
		}
		
		return "DROP_ITEMS";
	}*/
	
/*	public String dropItems() {
		System.out.println(this.objectarraylist.size());
		ObjectListIterator<com.mojang.datafixers.util.Pair<ItemStack, BlockPosition>> objectlistiterator = this.objectarraylist.iterator();

		while (objectlistiterator.hasNext()) {
			com.mojang.datafixers.util.Pair<ItemStack, BlockPosition> pair = objectlistiterator.next();
			net.minecraft.server.v1_15_R1.Block.a(this.world, pair.getSecond(), pair.getFirst());
		}
		return "IGNITE_BLOCKS";
	}*/
	
	public String particles() {
		double x = this.locX();
		double y = this.locY();
		double z = this.locZ();
		
	//	if (this.world.isClientSide) {
			this.world.a(x, y, z, SoundEffects.ENTITY_GENERIC_EXPLODE, SoundCategory.BLOCKS, 4.0F,
					(1.0F + (this.world.random.nextFloat() - this.world.random.nextFloat()) * 0.2F) * 0.7F, false);
	//	}

		if (this.power >= 2.0) {
			this.world.addParticle(Particles.EXPLOSION_EMITTER, x, y, z, 1.0D, 0.0D, 0.0D);
		} else {
			this.world.addParticle(Particles.EXPLOSION, x, y, z, 1.0D, 0.0D, 0.0D);
		}
		return "DIE";
	}
	
	public String dieByExploding() {
		this.die();
		return null;
	}
	
	
	
	
	
	public static enum State {
		WAITING(null) {
			@Override
			public State doStep(MegaExplosion explosion) {
				return WAITING;
			}
		},
		START_A(e -> "GET_BLOCKS"),
		GET_BLOCKS(e -> e.getBlocks()),
		START_B(e -> "DESTROY_BLOCKS"),
	//	PREPARE_BUKKIT(e -> e.prepareBukkit()),
	//	CREATE_EVENTS(e -> e.createEvents()),
		DESTROY_BLOCKS(e -> e.destroyBlocks()),
		IGNITE_BLOCKS(e -> e.igniteBlocks()),
		GET_ENTITIES(e -> e.getEntities()),
		WORK_ENTITIES(e -> e.workEntities()),
	//	ADAPT_DROPS(e -> e.adaptDrops()),
	//	DROP_ITEMS(e -> e.dropItems()),
		PARTICLES(e -> e.particles()),
		DIE(e -> e.dieByExploding());

		private IStateStepper stepper;

		private State(IStateStepper stepper) {
			this.stepper = stepper;
		}

		public State doStep(MegaExplosion explosion) {
			return valueOf(this.stepper.doStep(explosion));
		}

		@FunctionalInterface
		static interface IStateStepper {
			String doStep(MegaExplosion explosion);
		}
	}
	
	public class CraftMegaExplosion extends CraftEntity {

		public CraftMegaExplosion(CraftServer server, Entity entity) {
			super(server, entity);
		}
		
		@Override
		public EntityType getType() {
			return EntityType.PRIMED_TNT;
		}
	}
	
/*	private static void a(ObjectArrayList<com.mojang.datafixers.util.Pair<ItemStack, BlockPosition>> objectarraylist, ItemStack itemstack,
			BlockPosition blockposition) {
		int i = objectarraylist.size();

		for (int j = 0; j < i; ++j) {
			com.mojang.datafixers.util.Pair<ItemStack, BlockPosition> pair = objectarraylist.get(j);
			ItemStack itemstack1 = (ItemStack) pair.getFirst();
			
			if (EntityItem.a(itemstack1, itemstack)) {
				ItemStack itemstack2 = EntityItem.a(itemstack1, itemstack, 16);
				objectarraylist.set(j, com.mojang.datafixers.util.Pair.of(itemstack2, (BlockPosition) pair.getSecond()));
				if (itemstack.isEmpty()) {
					return;
				}
			}
		}

		objectarraylist.add(com.mojang.datafixers.util.Pair.of(itemstack, blockposition));
	}*/

/*	public void explode() {
		// a()
		
		// First step (getBlocks)
		if (this.power >= 0.1F) {
			Set<BlockPosition> set = Sets.newHashSet();

			int i;
			int j;
			for (int k = 0; k < 16; k++) {
				for (i = 0; i < 16; i++) {
					for (j = 0; j < 16; j++) {
						if (k == 0 || k == 15 || i == 0 || i == 15 || j == 0 || j == 15) {
							double d0 = (double) ((float) k / 15.0F * 2.0F - 1.0F);
							double d1 = (double) ((float) i / 15.0F * 2.0F - 1.0F);
							double d2 = (double) ((float) j / 15.0F * 2.0F - 1.0F);
							double d3 = Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
							d0 /= d3;
							d1 /= d3;
							d2 /= d3;
							float f = this.power * (0.7F + this.world.random.nextFloat() * 0.6F);
							double d4 = this.locX();
							double d5 = this.locY();
							double d6 = this.locZ();

							for (float var21 = 0.3F; f > 0.0F; f -= 0.22500001F) {
								BlockPosition blockposition = new BlockPosition(d4, d5, d6);
								IBlockData iblockdata = this.world.getType(blockposition);
								Fluid fluid = this.world.getFluid(blockposition);
								if (!iblockdata.isAir() || !fluid.isEmpty()) {
									float f2 = Math.max(iblockdata.getBlock().getDurability(), fluid.k());
									if (this.source != null) {
										f2 = this.source.a(null, this.world, blockposition, iblockdata, fluid, f2);
									}
									f -= (f2 + var21) * var21;
								}

								if (f > 0.0F && (this.source == null || this.source.a(null, this.world, blockposition, iblockdata, f))
										&& blockposition.getY() < 256 && blockposition.getY() >= 0) {
									set.add(blockposition);
								}

								d4 += d0 * (double) (var21);
								d5 += d1 * (double) (var21);
								d6 += d2 * (double) (var21);
							}
						}
					}
				}
			}

			this.blocks.addAll(set);
			
			// Second step (getEntities)
			
			float f3 = this.power * 2.0F;
			i = MathHelper.floor(this.locX() - (double) f3 - 1.0D);
			j = MathHelper.floor(this.locX() + (double) f3 + 1.0D);
			int l = MathHelper.floor(this.locY() - (double) f3 - 1.0D);
			int i1 = MathHelper.floor(this.locY() + (double) f3 + 1.0D);
			int j1 = MathHelper.floor(this.locZ() - (double) f3 - 1.0D);
			int k1 = MathHelper.floor(this.locZ() + (double) f3 + 1.0D);
			List<Entity> list = this.world.getEntities(this.source,
					new AxisAlignedBB((double) i, (double) l, (double) j1, (double) j, (double) i1, (double) k1));
			Vec3D vec3d = new Vec3D(this.locX(), this.locY(), this.locZ());

			// Third step (workEntities)
			
			for (int l1 = 0; l1 < list.size(); ++l1) {
				Entity entity = (Entity) list.get(l1);
				if (!entity.ca()) {
					double d7 = (double) (MathHelper.sqrt(entity.c(vec3d)) / f3);
					if (d7 <= 1.0D) {
						double d8 = entity.locX() - this.locX();
						double d9 = entity.getHeadY() - this.locY();
						double d10 = entity.locZ() - this.locZ();
						double d11 = (double) MathHelper.sqrt(d8 * d8 + d9 * d9 + d10 * d10);
						if (d11 != 0.0D) {
							d8 /= d11;
							d9 /= d11;
							d10 /= d11;
							double d12 = (double) Explosion.a(vec3d, entity);
							double d13 = (1.0D - d7) * d12;
							CraftEventFactory.entityDamage = this;
							entity.forceExplosionKnockback = false;
							boolean wasDamaged = entity.damageEntity(null,
									(float) ((int) ((d13 * d13 + d13) / 2.0D * 7.0D * (double) f3 + 1.0D)));
							CraftEventFactory.entityDamage = null;
							if (wasDamaged || entity instanceof EntityTNTPrimed || entity instanceof EntityFallingBlock
									|| entity.forceExplosionKnockback) {
								double d14 = d13;
								if (entity instanceof EntityLiving) {
									d14 = EnchantmentProtection.a((EntityLiving) entity, d13);
								}

								entity.setMot(entity.getMot().add(d8 * d14, d9 * d14, d10 * d14));
								if (entity instanceof EntityHuman) {
									EntityHuman entityhuman = (EntityHuman) entity;
									if (!entityhuman.isSpectator()
											&& (!entityhuman.isCreative() || !entityhuman.abilities.isFlying)) {
										this.l.put(entityhuman, new Vec3D(d8 * d13, d9 * d13, d10 * d13));
									}
								}
							}
						}
					}
				}
			}

		}

		// a(boolean)
		
		// Fourth step (particlesAndBukkitPreparation)
		
		if (this.world.isClientSide) {
			this.world.a(this.locX(), this.locY(), this.locZ(), SoundEffects.ENTITY_GENERIC_EXPLODE,
					SoundCategory.BLOCKS, 4.0F,
					(1.0F + (this.world.random.nextFloat() - this.world.random.nextFloat()) * 0.2F) * 0.7F, false);
		}

		if (this.power >= 2.0) {
			this.world.addParticle(Particles.EXPLOSION_EMITTER, this.locX(), this.locY(), this.locZ(), 1.0D, 0.0D,
					0.0D);
		} else {
			this.world.addParticle(Particles.EXPLOSION, this.locX(), this.locY(), this.locZ(), 1.0D, 0.0D, 0.0D);
		}

		ObjectArrayList<Pair<ItemStack, BlockPosition>> objectarraylist = new ObjectArrayList<>();
		Collections.shuffle(this.blocks, this.world.random);
		Iterator<BlockPosition> iterator = this.blocks.iterator();
		org.bukkit.World bworld = this.world.getWorld();
		org.bukkit.entity.Entity explode = this.source == null ? null : this.source.getBukkitEntity();
		Location location = new Location(bworld, this.locX(), this.locY(), this.locZ());
		List<org.bukkit.block.Block> blockList = Lists.newArrayList();

		for (int i1 = this.blocks.size() - 1; i1 >= 0; --i1) {
			BlockPosition cpos = (BlockPosition) this.blocks.get(i1);
			org.bukkit.block.Block bblock = bworld.getBlockAt(cpos.getX(), cpos.getY(), cpos.getZ());
			if (bblock.getType() != org.bukkit.Material.AIR) {
				blockList.add(bblock);
			}
		}
		
		// Fifth step (createEvents)

		List<org.bukkit.block.Block> bukkitBlocks;
		float yield;
		if (explode != null) {
			EntityExplodeEvent event = new EntityExplodeEvent(explode, location, blockList, 1);// this.b
																								// ==
																								// Effect.DESTROY
																								// ?
																								// 1.0F
																								// /
																								// this.size
																								// :
																								// 1.0F);
			this.world.getServer().getPluginManager().callEvent(event);
			bukkitBlocks = event.blockList();
			yield = event.getYield();
		} else {
			BlockExplodeEvent event = new BlockExplodeEvent(location.getBlock(), blockList, 1);// this.b
																								// ==
																								// Effect.DESTROY
																								// ?
																								// 1.0F
																								// /
																								// this.size
																								// :
																								// 1.0F);
			this.world.getServer().getPluginManager().callEvent(event);
			bukkitBlocks = event.blockList();
			yield = event.getYield();
		}

		this.blocks.clear();
		Iterator<Block> var13 = bukkitBlocks.iterator();

		while (var13.hasNext()) {
			Block bblock = var13.next();
			BlockPosition coords = new BlockPosition(bblock.getX(), bblock.getY(), bblock.getZ());
			this.blocks.add(coords);
		}

		// Sixth step (destroyBlocks)
		
		iterator = this.blocks.iterator();

		label111: while (true) {
			BlockPosition blockposition;
			IBlockData iblockdata;
			net.minecraft.server.v1_15_R1.Block block;
			do {
				if (!iterator.hasNext()) {
					ObjectListIterator<Pair<ItemStack, BlockPosition>> objectlistiterator = objectarraylist.iterator();

					while (objectlistiterator.hasNext()) {
						Pair<ItemStack, BlockPosition> pair = objectlistiterator.next();
						net.minecraft.server.v1_15_R1.Block.a(this.world, pair.getSecond(), pair.getFirst());
					}
					break label111;
				}

				blockposition = (BlockPosition) iterator.next();
				iblockdata = this.world.getType(blockposition);
				block = iblockdata.getBlock();
			} while (iblockdata.isAir());

			BlockPosition blockposition1 = blockposition.immutableCopy();
			this.world.getMethodProfiler().enter("explosion_blocks");
			if (block.a((Explosion) null) && this.world instanceof WorldServer) {
				TileEntity tileentity = block.isTileEntity() ? this.world.getTileEntity(blockposition) : null;
				Builder loottableinfo_builder = (new Builder((WorldServer) this.world)).a(this.world.random)
						.set(LootContextParameters.POSITION, blockposition).set(LootContextParameters.TOOL, ItemStack.a)
						.setOptional(LootContextParameters.BLOCK_ENTITY, tileentity)
						.setOptional(LootContextParameters.THIS_ENTITY, null);
				if (yield < 1.0F) {
					loottableinfo_builder.set(LootContextParameters.EXPLOSION_RADIUS, 1.0F / yield);
				}

				iblockdata.a(loottableinfo_builder).forEach((itemstack) -> {
					a(objectarraylist, itemstack, blockposition1);
				});
			}

			this.world.setTypeAndData(blockposition, Blocks.AIR.getBlockData(), 3);
			block.wasExploded(this.world, blockposition, null);
			this.world.getMethodProfiler().exit();
		}

		// Eighth step (igniteBlocks)
		
		Iterator<BlockPosition> iterator1 = this.blocks.iterator();
		while (iterator1.hasNext()) {
			BlockPosition blockposition2 = (BlockPosition) iterator1.next();
			if (this.random.nextInt(3) == 0 && this.world.getType(blockposition2).isAir()
					&& this.world.getType(blockposition2.down()).g(this.world, blockposition2.down())) {
				org.bukkit.World bukkitWorld = this.world.getWorld();
				BlockIgniteEvent event = new BlockIgniteEvent(
						bukkitWorld.getBlockAt(blockposition2.getX(), blockposition2.getY(), blockposition2.getZ()),
						IgniteCause.EXPLOSION, (org.bukkit.entity.Entity) null);
				world.getServer().getPluginManager().callEvent(event);
				this.world.setTypeUpdate(blockposition2, Blocks.FIRE.getBlockData());
			}
		}
	}*/

}

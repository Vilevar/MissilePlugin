package be.vilevar.missiles.utils;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.annotation.Nullable;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.libs.it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.bukkit.craftbukkit.libs.it.unimi.dsi.fastutil.objects.ObjectListIterator;
import org.bukkit.craftbukkit.v1_15_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_15_R1.event.CraftEventFactory;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Pair;

import net.minecraft.server.v1_15_R1.AxisAlignedBB;
import net.minecraft.server.v1_15_R1.BlockPosition;
import net.minecraft.server.v1_15_R1.Blocks;
import net.minecraft.server.v1_15_R1.DamageSource;
import net.minecraft.server.v1_15_R1.EnchantmentProtection;
import net.minecraft.server.v1_15_R1.Entity;
import net.minecraft.server.v1_15_R1.EntityFallingBlock;
import net.minecraft.server.v1_15_R1.EntityFireball;
import net.minecraft.server.v1_15_R1.EntityHuman;
import net.minecraft.server.v1_15_R1.EntityItem;
import net.minecraft.server.v1_15_R1.EntityLiving;
import net.minecraft.server.v1_15_R1.EntityPlayer;
import net.minecraft.server.v1_15_R1.EntityTNTPrimed;
import net.minecraft.server.v1_15_R1.Explosion;
import net.minecraft.server.v1_15_R1.Fluid;
import net.minecraft.server.v1_15_R1.IBlockData;
import net.minecraft.server.v1_15_R1.ItemStack;
import net.minecraft.server.v1_15_R1.LootContextParameters;
import net.minecraft.server.v1_15_R1.LootTableInfo.Builder;
import net.minecraft.server.v1_15_R1.MathHelper;
import net.minecraft.server.v1_15_R1.MovingObjectPosition.EnumMovingObjectType;
import net.minecraft.server.v1_15_R1.PacketPlayOutExplosion;
import net.minecraft.server.v1_15_R1.Particles;
import net.minecraft.server.v1_15_R1.RayTrace;
import net.minecraft.server.v1_15_R1.RayTrace.BlockCollisionOption;
import net.minecraft.server.v1_15_R1.RayTrace.FluidCollisionOption;
import net.minecraft.server.v1_15_R1.SoundCategory;
import net.minecraft.server.v1_15_R1.SoundEffects;
import net.minecraft.server.v1_15_R1.TileEntity;
import net.minecraft.server.v1_15_R1.Vec3D;
import net.minecraft.server.v1_15_R1.World;
import net.minecraft.server.v1_15_R1.WorldServer;

public class Explosion_1_12 extends Explosion {

	private final boolean a;
	private final boolean b;
	private final Random c;
	private final World world;
	private final double posX;
	private final double posY;
	private final double posZ;
	public final Entity source;
	private final float size;
	private final List<BlockPosition> blocks;
	private final Map<EntityHuman, Vec3D> k;
	public boolean wasCanceled;
	private DamageSource j;

	public Explosion_1_12(World world, Entity entity, double d0, double d1, double d2, float f, boolean flag,
			boolean flag1) {
		super(null, null, 0, 0, 0, 0, false, null);
		this.c = new Random();

		this.blocks = Lists.newArrayList();
		this.k = Maps.newHashMap();
		this.wasCanceled = false;

		this.world = world;
		this.source = entity;
		this.size = Math.max(f, 0.0F);
		this.posX = d0;
		this.posY = d1;
		this.posZ = d2;
		this.a = flag;
		this.b = flag1;
		this.j = DamageSource.explosion(this);
	}

	@Override
	public void a() {
		if (this.size < 0.1F) {
			return;
		}

		HashSet<BlockPosition> hashset = Sets.newHashSet();

		for (int k = 0; k < 16; k++) {
			for (int i = 0; i < 16; i++) {
				for (int j = 0; j < 16; j++) {
					if (k == 0 || k == 15 || i == 0 || i == 15 || j == 0 || j == 15) {
						double d0 = (k / 15.0F * 2.0F - 1.0F);
						double d1 = (i / 15.0F * 2.0F - 1.0F);
						double d2 = (j / 15.0F * 2.0F - 1.0F);
						double d3 = Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2);

						d0 /= d3;
						d1 /= d3;
						d2 /= d3;
						float f = this.size * (0.7F + this.world.random.nextFloat() * 0.6F);
						double d4 = this.posX;
						double d5 = this.posY;
						double d6 = this.posZ;

						for (; f > 0.0F; f -= 0.22500001F) {
							/*
							 * BlockPosition blockposition = new
							 * BlockPosition(d4, d5, d6); IBlockData iblockdata
							 * = this.world.getType(blockposition);
							 * 
							 * if (iblockdata.getMaterial() != Material.AIR) {
							 * float f2 = (this.source != null) ?
							 * this.source.a(this, this.world, blockposition,
							 * iblockdata) : iblockdata.getBlock().a(null);
							 * 
							 * f -= (f2 + 0.3F) * 0.3F; }
							 * 
							 * if (f > 0.0F && (this.source == null ||
							 * this.source.a(this, this.world, blockposition,
							 * iblockdata, f)) && blockposition.getY() < 256 &&
							 * blockposition.getY() >= 0) {
							 * hashset.add(blockposition); }
							 * 
							 * d4 += d0 * 0.30000001192092896D; d5 += d1 *
							 * 0.30000001192092896D; d6 += d2 *
							 * 0.30000001192092896D;
							 */
							BlockPosition blockposition = new BlockPosition(d4, d5, d6);
							IBlockData iblockdata = this.world.getType(blockposition);
							Fluid fluid = this.world.getFluid(blockposition);
							if (!iblockdata.isAir() || !fluid.isEmpty()) {
								float f2 = Math.max(iblockdata.getBlock().getDurability(), fluid.k());
								if (this.source != null) {
									f2 = this.source.a(this, this.world, blockposition, iblockdata, fluid, f2);
								}

								f -= (f2 + 0.3F) * 0.3F;
							}

							if (f > 0.0F
									&& (this.source == null
											|| this.source.a(this, this.world, blockposition, iblockdata, f))
									&& blockposition.getY() < 256 && blockposition.getY() >= 0) {
								hashset.add(blockposition);
							}

							d4 += d0 * 0.30000001192092896D;
							d5 += d1 * 0.30000001192092896D;
							d6 += d2 * 0.30000001192092896D;
						}
					}
				}
			}
		}

		this.blocks.addAll(hashset);
		float f3 = this.size * 2.0F;

		int i = MathHelper.floor(this.posX - f3 - 1.0D);
		int j = MathHelper.floor(this.posX + f3 + 1.0D);
		int l = MathHelper.floor(this.posY - f3 - 1.0D);
		int i1 = MathHelper.floor(this.posY + f3 + 1.0D);
		int j1 = MathHelper.floor(this.posZ - f3 - 1.0D);
		int k1 = MathHelper.floor(this.posZ + f3 + 1.0D);
		List<Entity> list = this.world.getEntities(this.source, new AxisAlignedBB(i, l, j1, j, i1, k1));
		Vec3D vec3d = new Vec3D(this.posX, this.posY, this.posZ);

		for (int l1 = 0; l1 < list.size(); l1++) {
			Entity entity = list.get(l1);

			if (!entity.ca()) {
				double d7 = MathHelper.sqrt(entity.c(vec3d)) / f3;

				if (d7 <= 1.0D) {
					double d8 = entity.locX() - this.posX;
					double d9 = entity.locY() + entity.getHeadHeight() - this.posY;
					double d10 = entity.locZ() - this.posZ;
					double d11 = MathHelper.sqrt(d8 * d8 + d9 * d9 + d10 * d10);

					if (d11 != 0.0D) {
						d8 /= d11;
						d9 /= d11;
						d10 /= d11;
						double d12 = a(vec3d, entity);
						double d13 = (1.0D - d7) * d12;

						CraftEventFactory.entityDamage = this.source;
						entity.forceExplosionKnockback = false;
						boolean wasDamaged = entity.damageEntity(this.b(),
								((int) ((d13 * d13 + d13) / 2.0D * 7.0D * f3 + 1.0D)));
						CraftEventFactory.entityDamage = null;
						if (wasDamaged || entity instanceof EntityTNTPrimed || entity instanceof EntityFallingBlock
								|| entity.forceExplosionKnockback) {

							double d14 = d13;

							if (entity instanceof EntityLiving) {
								d14 = EnchantmentProtection.a((EntityLiving) entity, d13);
							}

							entity.setMot(d8 * d14, d9 * d14, d10 * d14);
							if (entity instanceof EntityHuman) {
								EntityHuman entityhuman = (EntityHuman) entity;

								if (!entityhuman.isSpectator()
										&& (!entityhuman.isCreative() || !entityhuman.abilities.isFlying)) {
									this.k.put(entityhuman, new Vec3D(d8 * d13, d9 * d13, d10 * d13));
								}
							}
						}
					}
				}
			}
		}
	}

	@Override
	public void a(boolean flag) {
		if (this.world.isClientSide) {
			this.world.a(this.posX, this.posY, this.posZ, SoundEffects.ENTITY_GENERIC_EXPLODE, SoundCategory.BLOCKS,
					4.0F, (1.0F + (this.world.random.nextFloat() - this.world.random.nextFloat()) * 0.2F) * 0.7F,
					false);
		}

		if (flag) {
			if (this.size >= 2.0F && this.b) {
				this.world.addParticle(Particles.EXPLOSION_EMITTER, this.posX, this.posY, this.posZ, 1.0D, 0.0D, 0.0D);
			} else {
				this.world.addParticle(Particles.EXPLOSION, this.posX, this.posY, this.posZ, 1.0D, 0.0D, 0.0D);
			}
		}

		if (this.b) {
			float yield;
			List<Block> bukkitBlocks;
			CraftWorld craftWorld = this.world.getWorld();
			CraftEntity craftEntity = (this.source == null) ? null : this.source.getBukkitEntity();
			Location location = new Location(craftWorld, this.posX, this.posY, this.posZ);
			boolean cancelled;

			List<Block> blockList = Lists.newArrayList();
			for (int i = this.blocks.size() - 1; i >= 0; i--) {
				BlockPosition bukkitBlock = this.blocks.get(i);
				Block bblock = craftWorld.getBlockAt(bukkitBlock.getX(), bukkitBlock.getY(), bukkitBlock.getZ());
				if (bblock.getType() != Material.AIR) {
					blockList.add(bblock);
				}
			}

			if (craftEntity != null) {
				EntityExplodeEvent event = new EntityExplodeEvent(craftEntity, location, blockList, 1.0F / this.size);
				this.world.getServer().getPluginManager().callEvent(event);
				cancelled = event.isCancelled();
				bukkitBlocks = event.blockList();
				yield = event.getYield();
			} else {
				BlockExplodeEvent event = new BlockExplodeEvent(location.getBlock(), blockList, 1.0F / this.size);
				this.world.getServer().getPluginManager().callEvent(event);
				cancelled = event.isCancelled();
				bukkitBlocks = event.blockList();
				yield = event.getYield();
			}

			this.blocks.clear();

			for (Block bblock : bukkitBlocks) {
				BlockPosition coords = new BlockPosition(bblock.getX(), bblock.getY(), bblock.getZ());
				this.blocks.add(coords);
			}

			if (cancelled) {
				this.wasCanceled = true;
				return;
			}
			Iterator<BlockPosition> iterator = this.blocks.iterator();

			/*
			 * while (iterator.hasNext()) { BlockPosition blockposition =
			 * (BlockPosition) iterator.next(); IBlockData iblockdata =
			 * this.world.getType(blockposition);
			 * net.minecraft.server.v1_15_R1.Block block =
			 * iblockdata.getBlock();
			 * 
			 * if (flag) { double d0 = (blockposition.getX() +
			 * this.world.random.nextFloat()); double d1 = (blockposition.getY()
			 * + this.world.random.nextFloat()); double d2 =
			 * (blockposition.getZ() + this.world.random.nextFloat()); double d3
			 * = d0 - this.posX; double d4 = d1 - this.posY; double d5 = d2 -
			 * this.posZ; double d6 = MathHelper.sqrt(d3 * d3 + d4 * d4 + d5 *
			 * d5);
			 * 
			 * d3 /= d6; d4 /= d6; d5 /= d6; double d7 = 0.5D / (d6 / this.size
			 * + 0.1D);
			 * 
			 * d7 *= (this.world.random.nextFloat() *
			 * this.world.random.nextFloat() + 0.3F); d3 *= d7; d4 *= d7; d5 *=
			 * d7; this.world.addParticle(Particles.EXPLOSION, (d0 + this.posX)
			 * / 2.0D, (d1 + this.posY) / 2.0D, (d2 + this.posZ) / 2.0D, d3, d4,
			 * d5); this.world.addParticle(Particles.SMOKE, d0, d1, d2, d3, d4,
			 * d5); }
			 * 
			 * if (iblockdata.getMaterial() !=
			 * net.minecraft.server.v1_15_R1.Material.AIR) { if (block.a(this)
			 * && this.world instanceof WorldServer) {
			 * block.dropNaturally(this.world, blockposition,
			 * this.world.getType(blockposition), yield, 0);
			 * this.world.setTypeAndData(blockposition,
			 * Blocks.AIR.getBlockData(), 3); block.wasExploded(this.world,
			 * blockposition, this); } } }
			 */

			ObjectArrayList<Pair<ItemStack, BlockPosition>> objectarraylist = new ObjectArrayList<>();
			label111: while (true) {
				BlockPosition blockposition;
				IBlockData iblockdata;
				net.minecraft.server.v1_15_R1.Block block;
				do {
					if (!iterator.hasNext()) {
						ObjectListIterator<Pair<ItemStack, BlockPosition>> objectlistiterator = objectarraylist
								.iterator();

						while (objectlistiterator.hasNext()) {
							Pair<ItemStack, BlockPosition> pair = objectlistiterator
									.next();
							net.minecraft.server.v1_15_R1.Block.a(this.world, pair.getSecond(),
									pair.getFirst());
						}
						break label111;
					}

					blockposition = iterator.next();
					iblockdata = this.world.getType(blockposition);
					block = iblockdata.getBlock();
				} while (iblockdata.isAir());

				BlockPosition blockposition1 = blockposition.immutableCopy();
				this.world.getMethodProfiler().enter("explosion_blocks");
				if (block.a(this) && this.world instanceof WorldServer) {
					TileEntity tileentity = block.isTileEntity() ? this.world.getTileEntity(blockposition) : null;
					Builder loottableinfo_builder = (new Builder((WorldServer) this.world)).a(this.world.random)
							.set(LootContextParameters.POSITION, blockposition)
							.set(LootContextParameters.TOOL, ItemStack.a)
							.setOptional(LootContextParameters.BLOCK_ENTITY, tileentity)
							.setOptional(LootContextParameters.THIS_ENTITY, this.source);
					if (this.b/* == Effect.DESTROY */ || yield < 1.0F) {
						loottableinfo_builder.set(LootContextParameters.EXPLOSION_RADIUS, 1.0F / yield);
					}

					iblockdata.a(loottableinfo_builder).forEach((itemstack) -> {
						a(objectarraylist, itemstack, blockposition1);
					});
				}

				this.world.setTypeAndData(blockposition, Blocks.AIR.getBlockData(), 3);
				block.wasExploded(this.world, blockposition, this);
				this.world.getMethodProfiler().exit();
			}

			if (this.a) {
				iterator = this.blocks.iterator();

				while (iterator.hasNext()) {
					BlockPosition blockposition = iterator.next();
					if (this.world.getType(blockposition).getMaterial() == net.minecraft.server.v1_15_R1.Material.AIR
							&& this.world.getType(blockposition.down()).g(this.world, blockposition.down())
							&& this.c.nextInt(3) == 0) {
						if (!CraftEventFactory.callBlockIgniteEvent(this.world, blockposition.getX(),
								blockposition.getY(), blockposition.getZ(), this).isCancelled()) {
							this.world.setTypeUpdate(blockposition, Blocks.FIRE.getBlockData());
						}
					}
				}
			}
		}
	}

	@Override
	public DamageSource b() {
		return this.j;
	}

	@Override
	public void a(DamageSource damagesource) {
		this.j = damagesource;
	}

	@Override
	public Map<EntityHuman, Vec3D> c() {
		return this.k;
	}

	@Override
	@Nullable
	public EntityLiving getSource() {
		return (this.source == null) ? null
				: ((this.source instanceof EntityTNTPrimed) ? ((EntityTNTPrimed) this.source).getSource()
						: ((this.source instanceof EntityLiving) ? (EntityLiving) this.source
								: ((this.source instanceof EntityFireball) ? ((EntityFireball) this.source).shooter
										: null)));
	}

	@Override
	public void clearBlocks() {
		this.blocks.clear();
	}

	@Override
	public List<BlockPosition> getBlocks() {
		return this.blocks;
	}

	public static float a(Vec3D vec3d, Entity entity) {
		AxisAlignedBB axisalignedbb = entity.getBoundingBox();
		double d0 = 1.0D / ((axisalignedbb.maxX - axisalignedbb.minX) * 2.0D + 1.0D);
		double d1 = 1.0D / ((axisalignedbb.maxY - axisalignedbb.minY) * 2.0D + 1.0D);
		double d2 = 1.0D / ((axisalignedbb.maxZ - axisalignedbb.minZ) * 2.0D + 1.0D);
		double d3 = (1.0D - Math.floor(1.0D / d0) * d0) / 2.0D;
		double d4 = (1.0D - Math.floor(1.0D / d2) * d2) / 2.0D;
		if (d0 >= 0.0D && d1 >= 0.0D && d2 >= 0.0D) {
			int i = 0;
			int j = 0;

			for (float f = 0.0F; f <= 1.0F; f = (float) (f + d0)) {
				for (float f1 = 0.0F; f1 <= 1.0F; f1 = (float) (f1 + d1)) {
					for (float f2 = 0.0F; f2 <= 1.0F; f2 = (float) (f2 + d2)) {
						double d5 = MathHelper.d(f, axisalignedbb.minX, axisalignedbb.maxX);
						double d6 = MathHelper.d(f1, axisalignedbb.minY, axisalignedbb.maxY);
						double d7 = MathHelper.d(f2, axisalignedbb.minZ, axisalignedbb.maxZ);
						Vec3D vec3d1 = new Vec3D(d5 + d3, d6, d7 + d4);
						if (entity.world.rayTrace(new RayTrace(vec3d1, vec3d, BlockCollisionOption.OUTLINE,
								FluidCollisionOption.NONE, entity)).getType() == EnumMovingObjectType.MISS) {
							++i;
						}

						++j;
					}
				}
			}

			return (float) i / (float) j;
		} else {
			return 0.0F;
		}
	}

	private static void a(ObjectArrayList<Pair<ItemStack, BlockPosition>> objectarraylist, ItemStack itemstack,
			BlockPosition blockposition) {
		int i = objectarraylist.size();

		for (int j = 0; j < i; ++j) {
			Pair<ItemStack, BlockPosition> pair = objectarraylist.get(j);
			ItemStack itemstack1 = pair.getFirst();
			if (EntityItem.a(itemstack1, itemstack)) {
				ItemStack itemstack2 = EntityItem.a(itemstack1, itemstack, 16);
				objectarraylist.set(j, Pair.of(itemstack2, pair.getSecond()));
				if (itemstack.isEmpty()) {
					return;
				}
			}
		}

		objectarraylist.add(Pair.of(itemstack, blockposition));
	}


	public static Explosion_1_12 createExplosion(org.bukkit.entity.Entity ent, Location loc, float size, boolean setFire, boolean breakBlocks) {
		World world = ((CraftWorld) loc.getWorld()).getHandle();
		double x = loc.getX(), y = loc.getY(), z = loc.getZ();
		Entity entity = ((CraftEntity) ent).getHandle();
		Explosion_1_12 explosion = new Explosion_1_12(world, entity, x, y, z, size, setFire, breakBlocks);
		explosion.a();
		explosion.a(true);
		
		if (explosion.wasCanceled) {
			return explosion;
		}
		
		if (!breakBlocks) {
			explosion.clearBlocks();
		}

		Iterator<? extends EntityHuman> iterator = world.getPlayers().iterator();
		while (iterator.hasNext()) {
			EntityPlayer entityplayer = (EntityPlayer) iterator.next();
			if (entityplayer.g(x, y, z) < 4096.0D) {
				entityplayer.playerConnection.sendPacket(new PacketPlayOutExplosion(x, y, z, size,
						explosion.getBlocks(), explosion.c().get(entityplayer)));
			}
		}

		return explosion;
	}
}

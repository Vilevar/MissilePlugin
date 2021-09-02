package be.vilevar.missiles.missile.ballistic.explosives;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

import org.bukkit.craftbukkit.v1_16_R3.event.CraftEventFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import net.minecraft.server.v1_16_R3.AxisAlignedBB;
import net.minecraft.server.v1_16_R3.BlockFireAbstract;
import net.minecraft.server.v1_16_R3.BlockPosition;
import net.minecraft.server.v1_16_R3.Blocks;
import net.minecraft.server.v1_16_R3.DamageSource;
import net.minecraft.server.v1_16_R3.EnchantmentProtection;
import net.minecraft.server.v1_16_R3.Entity;
import net.minecraft.server.v1_16_R3.EntityFallingBlock;
import net.minecraft.server.v1_16_R3.EntityLiving;
import net.minecraft.server.v1_16_R3.EntityTNTPrimed;
import net.minecraft.server.v1_16_R3.Fluid;
import net.minecraft.server.v1_16_R3.IBlockData;
import net.minecraft.server.v1_16_R3.MathHelper;
import net.minecraft.server.v1_16_R3.MovingObjectPosition.EnumMovingObjectType;
import net.minecraft.server.v1_16_R3.Particles;
import net.minecraft.server.v1_16_R3.RayTrace;
import net.minecraft.server.v1_16_R3.RayTrace.BlockCollisionOption;
import net.minecraft.server.v1_16_R3.RayTrace.FluidCollisionOption;
import net.minecraft.server.v1_16_R3.Vec3D;
import net.minecraft.server.v1_16_R3.World;

public class TestTraditional {

	private final Random rand = new Random();
	private final World world;
	private final double x;
	private final double y;
	private final double z;
	public final EntityLiving source;
	private final float power;
	private final DamageSource damageSrc;
	private final List<BlockPosition> blocks = Lists.newArrayList();

	public TestTraditional(World world, double x, double y, double z, float power) {
		this.world = world;
		this.power = power;
		this.x = x;
		this.y = y;
		this.z = z;
		this.source = null;
		this.damageSrc = DamageSource.d(this.source);
	}



	public void a() {
		if (this.power >= 0.1F) {
			
			// Blocks
			Set<BlockPosition> set = Sets.newHashSet();

			for (int i = 0; i < 16; i++) {
				for (int j = 0; j < 16; j++) {
					for (int k = 0; k < 16; k++) {
						if (i == 0 || i == 15 || j == 0 || j == 15 || k == 0 || k == 15) {
							
							double dx = k / 15.0F * 2.0F - 1.0F;
							double dy = i / 15.0F * 2.0F - 1.0F;
							double dz = j / 15.0F * 2.0F - 1.0F;
							
							double multiplier = 0.30000001192092896 / Math.sqrt(dx * dx + dy * dy + dz * dz);
							
							dx *= multiplier;
							dy *= multiplier;
							dz *= multiplier;
							
							double x = this.x;
							double y = this.y;
							double z = this.z;

							for (float f = this.power * (0.7F + this.world.random.nextFloat() * 0.6F); f > 0.0F; f -= 0.22500001F) {
								
								BlockPosition blockposition = new BlockPosition(x, y, z);
								IBlockData block = this.world.getType(blockposition);
								Fluid fluid = this.world.getFluid(blockposition);
								
								Optional<Float> optional = block.isAir() && fluid.isEmpty() ? Optional.empty()
												: Optional.of(Math.max(block.getBlock().getDurability(), fluid.i()));
								
								if (optional.isPresent()) {
									f -= ((Float) optional.get() + 0.3F) * 0.3F;
								}

								if (f > 0.0F && y < 256 && y >= 0 && !block.isAir()) {
									set.add(blockposition);
								}
								
								x += dx;
								y += dy;
								z += dz;
							}
						}
					}
				}
			}
			this.blocks.addAll(set);
			
			
			// Entities
			float range = this.power * 2.0F;
			
			double bounding = range + 1;
			int minX = MathHelper.floor(this.x - bounding);
			int maxX = MathHelper.floor(this.x + bounding);
			int minY = MathHelper.floor(this.y - bounding);
			int maxY = MathHelper.floor(this.y + bounding);
			int minZ = MathHelper.floor(this.z - bounding);
			int maxZ = MathHelper.floor(this.z + bounding);
			
			List<Entity> entities = this.world.getEntities(this.source, new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ));
			
			Vec3D vec3d = new Vec3D(this.x, this.y, this.z);

			for (Entity entity : entities) {
				if (!entity.ci()) {
					double relativeSquareDistance = MathHelper.sqrt(entity.e(vec3d)) / range;
					
					if (relativeSquareDistance <= 1.0D) {
						double dx = entity.locX() - this.x;
						double dy = (entity instanceof EntityTNTPrimed ? entity.locY() : entity.getHeadY()) - this.y;
						double dz = entity.locZ() - this.z;
						
						double dist = MathHelper.sqrt(dx * dx + dy * dy + dz * dz);
						
						if (dist != 0.0D) {
							dx /= dist;
							dy /= dist;
							dz /= dist;
							
							double obstacles = a(vec3d, entity);
							double d13 = (1.0D - relativeSquareDistance) * obstacles;
							
							CraftEventFactory.entityDamage = this.source;
							entity.forceExplosionKnockback = false;
							
							boolean wasDamaged = entity.damageEntity(this.damageSrc, 
									((int) ((d13 * d13 + d13) / 2.0D * 7.0D * range + 1.0D)));
							
							CraftEventFactory.entityDamage = null;
							
							if (wasDamaged || entity instanceof EntityTNTPrimed || entity instanceof EntityFallingBlock
									|| entity.forceExplosionKnockback) {
								
								double ejection = d13;
								if (entity instanceof EntityLiving) {
									ejection = EnchantmentProtection.a((EntityLiving) entity, d13);
								}

								entity.setMot(entity.getMot().add(dx * ejection, dy * ejection, dz * ejection));
							}
						}
					}
				}
			}

		}
	}

	public void A() {
		System.out.println("Test world client side (normally false) : "+this.world.isClientSide);
//		if (this.world.isClientSide) {
//			this.world.a(this.x, this.y, this.z, SoundEffects.ENTITY_GENERIC_EXPLODE, SoundCategory.BLOCKS,
//					4.0F, (1.0F + (this.world.random.nextFloat() - this.world.random.nextFloat()) * 0.2F) * 0.7F,
//					false);
//		}

		this.world.addParticle(Particles.EXPLOSION, this.x, this.y, this.z, 1.0D, 0.0D, 0.0D);

		for(BlockPosition pos : this.blocks) {
			this.world.setTypeAndData(pos, Blocks.AIR.getBlockData(), 3);
		}

		for(BlockPosition pos : this.blocks) {
			if (this.rand.nextInt(3) == 0 && this.world.getType(pos).isAir() 
					&& this.world.getType(pos.down()).i(this.world, pos.down())) {
				this.world.setTypeUpdate(pos, BlockFireAbstract.a(this.world, pos));
			}
		}
	}

	public void clearBlocks() {
		this.blocks.clear();
	}

	public List<BlockPosition> getBlocks() {
		return this.blocks;
	}
	
	public static float a(Vec3D vec3d, Entity entity) {
		AxisAlignedBB axisalignedbb = entity.getBoundingBox();
		
		double dx = 1.0D / ((axisalignedbb.maxX - axisalignedbb.minX) * 2.0D + 1.0D);
		double dy = 1.0D / ((axisalignedbb.maxY - axisalignedbb.minY) * 2.0D + 1.0D);
		double dz = 1.0D / ((axisalignedbb.maxZ - axisalignedbb.minZ) * 2.0D + 1.0D);
		
		double d3 = (1.0D - Math.floor(1.0D / dx) * dx) / 2.0D;
		double d4 = (1.0D - Math.floor(1.0D / dz) * dz) / 2.0D;
		
		if (dx >= 0.0D && dy >= 0.0D && dz >= 0.0D) {
			int i = 0;
			int j = 0;

			for (float x = 0.0F; x <= 1.0F; x += dx) {
				for (float y = 0.0F; y <= 1.0F; y += dy) {
					for (float z = 0.0F; z <= 1.0F; z += dz) {
						
						double mX = MathHelper.d((double) x, axisalignedbb.minX, axisalignedbb.maxX);
						double mY = MathHelper.d((double) y, axisalignedbb.minY, axisalignedbb.maxY);
						double mZ = MathHelper.d((double) z, axisalignedbb.minZ, axisalignedbb.maxZ);
						
						Vec3D vec3d1 = new Vec3D(mX + d3, mY, mZ + d4);
						
						if (entity.world.rayTrace(new RayTrace(vec3d1, vec3d, BlockCollisionOption.COLLIDER,
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
}

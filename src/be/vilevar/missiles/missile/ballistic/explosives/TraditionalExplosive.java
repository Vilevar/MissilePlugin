package be.vilevar.missiles.missile.ballistic.explosives;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_17_R1.event.CraftEventFactory;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import be.vilevar.missiles.Main;
import be.vilevar.missiles.mcelements.CustomElementManager;
import be.vilevar.missiles.missile.ballistic.Explosive;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.particles.Particles;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.util.MathHelper;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.item.EntityFallingBlock;
import net.minecraft.world.entity.item.EntityTNTPrimed;
import net.minecraft.world.item.enchantment.EnchantmentProtection;
import net.minecraft.world.level.RayTrace;
import net.minecraft.world.level.RayTrace.BlockCollisionOption;
import net.minecraft.world.level.RayTrace.FluidCollisionOption;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.BlockFireAbstract;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.AxisAlignedBB;
import net.minecraft.world.phys.MovingObjectPosition.EnumMovingObjectType;
import net.minecraft.world.phys.Vec3D;

public class TraditionalExplosive implements Explosive {

	private final Main main;
	private final CustomElementManager cem;
	private final float power;
	private final boolean fire;

	private boolean isDone;
	private Explosive interception;

	public TraditionalExplosive(Main main, float power) {
		this(main, power, true);
	}
	
	public TraditionalExplosive(Main main, float power, boolean fire) {
		this.main = main;
		this.cem = main.getCustomElementManager();
		this.power = power;
		this.fire = fire;
	}

	@Override
	public void explode(Location loc, Player damager) {
		World world = ((CraftWorld) loc.getWorld()).getHandle();
		EntityPlayer source = ((CraftPlayer) damager).getHandle();
		DamageSource damageSrc = DamageSource.d(source);
		
		// Destroy impact block in order to avoid the slab aberration
		org.bukkit.World bukkitWorld = world.getWorld();
		cem.blockBreak(bukkitWorld.getBlockAt(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
		world.setTypeAndData(new BlockPosition(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()), Blocks.a.getBlockData(), 3);
		
		// Get blocks
		ArrayList<BlockPosition> blocks = new ArrayList<>();

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

						double x = loc.getX();
						double y = loc.getY();
						double z = loc.getZ();

						for (float f = this.power * (0.7F + world.w.nextFloat() * 0.6F); f > 0.0F; f -= 0.22500001F) {

							BlockPosition blockposition = new BlockPosition(x, y, z);
							IBlockData block = world.getType(blockposition);
							Fluid fluid = world.getFluid(blockposition);

							Optional<Float> optional = block.isAir() && fluid.isEmpty() ? Optional.empty()
									: Optional.of(Math.max(block.getBlock().getDurability(), fluid.i()));

							if (optional.isPresent()) {
								f -= ((Float) optional.get() + 0.3F) * 0.3F;
							}

							if (f > 0.0F && y < 256 && y >= 0 && !block.isAir()) {
								blocks.add(blockposition);
							}

							x += dx;
							y += dy;
							z += dz;
						}
					}
				}
			}
		}
		
		// Entities
		float range = this.power * 2.0F;

		double bounding = range + 1;
		int minX = MathHelper.floor(loc.getX() - bounding);
		int maxX = MathHelper.floor(loc.getX() + bounding);
		int minY = MathHelper.floor(loc.getY() - bounding);
		int maxY = MathHelper.floor(loc.getY() + bounding);
		int minZ = MathHelper.floor(loc.getZ() - bounding);
		int maxZ = MathHelper.floor(loc.getZ() + bounding);

		List<Entity> entities = world.getEntities(source,
				new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ));

		Vec3D vec3d = new Vec3D(loc.getX(), loc.getY(), loc.getZ());

		for (Entity entity : entities) {
			if (!entity.cx()) {
				double relativeSquareDistance = Math.sqrt(entity.e(vec3d)) / range;

				if (relativeSquareDistance <= 1.0D) {
					double dx = entity.locX() - loc.getX();
					double dy = (entity instanceof EntityTNTPrimed ? entity.locY() : entity.getHeadY()) - loc.getY();
					double dz = entity.locZ() - loc.getZ();

					double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);

					double d13;
					if (dist != 0.0D) {
						dx /= dist;
						dy /= dist;
						dz /= dist;

						double obstacles = getObstacleImportance(vec3d, entity);
						d13 = (1.0D - relativeSquareDistance) * obstacles * 1.5;
					} else {
						d13 = 1;
					}
					
					CraftEventFactory.entityDamage = source;
					entity.forceExplosionKnockback = false;

					boolean wasDamaged = entity.damageEntity(damageSrc, ((int) ((d13 * d13 + d13) / 2.0D * 8.0D * range + 1.0D)));

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
		
		// Destroy blocks
		world.addParticle(Particles.y, loc.getX(), loc.getY(), loc.getZ(), 1.0D, 0.0D, 0.0D);
		
//		org.bukkit.World bukkitWorld = world.getWorld();
		
		for(BlockPosition pos : blocks) {
			cem.blockBreak(bukkitWorld.getBlockAt(pos.getX(), pos.getY(), pos.getZ()));
			world.setTypeAndData(pos, Blocks.a.getBlockData(), 3);
		}

		// Set fire
		if(this.fire) {
			for(BlockPosition pos : blocks) {
				if (world.w.nextInt(3) == 0 && world.getType(pos).isAir() && world.getType(pos.down()).i(world, pos.down())) {
					world.setTypeUpdate(pos, BlockFireAbstract.a(world, pos));
				}
			}
		}
		
		main.getServer().getScheduler().runTaskLater(main, () -> {
			this.isDone = true;
		}, 20);
	}
	
	
	private static float getObstacleImportance(Vec3D vec3d, Entity entity) {
		AxisAlignedBB axisalignedbb = entity.getBoundingBox();
		
		double dx = 1.0D / ((axisalignedbb.d - axisalignedbb.a) * 2.0D + 1.0D);
		double dy = 1.0D / ((axisalignedbb.e - axisalignedbb.b) * 2.0D + 1.0D);
		double dz = 1.0D / ((axisalignedbb.f - axisalignedbb.c) * 2.0D + 1.0D);
		
		double shiftX = (1.0D - Math.floor(1.0D / dx) * dx) / 2.0D;
		double shiftY = (1.0D - Math.floor(1.0D / dz) * dz) / 2.0D;
		
		if (dx >= 0.0D && dy >= 0.0D && dz >= 0.0D) {
			int i = 0;
			int j = 0;

			for (float x = 0.0F; x <= 1.0F; x = (float) ((double) x + dx)) {
				for (float y = 0.0F; y <= 1.0F; y = (float) ((double) y + dy)) {
					for (float z = 0.0F; z <= 1.0F; z = (float) ((double) z + dz)) {
						
						double mX = MathHelper.d((double) x, axisalignedbb.a, axisalignedbb.d);
						double mY = MathHelper.d((double) y, axisalignedbb.b, axisalignedbb.e);
						double mZ = MathHelper.d((double) z, axisalignedbb.c, axisalignedbb.f);
						
						Vec3D vec3d1 = new Vec3D(mX + shiftX, mY, mZ + shiftY);
						
						if (entity.t.rayTrace(new RayTrace(vec3d1, vec3d, BlockCollisionOption.a, FluidCollisionOption.a, entity))
								.getType() == EnumMovingObjectType.a) {
							i++;
						}
						
						j++;
					}
				}
			}

			return (float) i / (float) j;
		} else {
			return 0.0F;
		}
	}
	
	

	@Override
	public void explodeByInterception(Location loc, Player damager) {
		this.interception = new TraditionalExplosive(main, this.power / 4);
		this.interception.explode(loc, damager);
	}

	@Override
	public boolean isDone() {
		return (this.interception != null && (this.isDone = this.interception.isDone())) || this.isDone;
	}

	@Override
	public void saveIn(ByteBuf buffer) {
		buffer.writeInt(0);
		buffer.writeFloat(power);
	}

	@Override
	public ItemStack toItem() {
		return new ItemStack(Material.TNT);
	}

}

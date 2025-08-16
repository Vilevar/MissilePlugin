package be.vilevar.missiles.artillery;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_17_R1.event.CraftEventFactory;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import be.vilevar.missiles.Main;
import be.vilevar.missiles.mcelements.CustomElementManager;
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
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.BlockFireAbstract;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.AxisAlignedBB;
import net.minecraft.world.phys.Vec3D;

public class ShellExplosiveManager {

	private static final int SQUARE_CUTTING = 16;
	private static final int DIRECTED_SQUARE_CUTTING = 32;
	private static final float BLOCK_AIR_RESISTANCE = 0.225f;
	private static final float BLOCK_DIRECTED_AIR_RESISTANCE = 0.145f;
	private static final double DIRECTED_ANGLE_TRESHOLD = Math.sqrt(3)/2;
	private static final double STEP_SIZE = 0.30000001192092896;
	private static final float DAMAGE_AIR_RESISTANCE = 0.098f;
	private static final float DAMAGE_DIRECTED_AIR_RESISTANCE = 0.076f;
	private static final float DAMAGE_MULTIPLIER = 4;
	private static final float EJECTION_MULTIPLIER = 0.1f;
	
	
	private final float materialPower;
	private final float damagePower;
	private final float firePower;
	private final boolean directed;

	public ShellExplosiveManager(float power) {
		this(power, false);
	}
	
	public ShellExplosiveManager(float power, boolean fire) {
		this(power, fire, false);
	}
	
	public ShellExplosiveManager(float power, boolean fire, boolean directed) {
		this(power, power, fire ? power : 0f, directed);
	}
	
	public ShellExplosiveManager(float materialPower, float damagePower, float firePower, boolean directed) {
		this.materialPower = materialPower;
		this.damagePower = damagePower;
		this.firePower = firePower;
		this.directed = directed;
	}
	
	public void explode(Main main, Location loc, Player damager, Vector perforationDirection) {
		if(this.directed) {
			loc.subtract(perforationDirection);
		}
		CustomElementManager cem = main.getCustomElementManager();
		
		World world = ((CraftWorld) loc.getWorld()).getHandle();
		EntityPlayer source = ((CraftPlayer) damager).getHandle();
		DamageSource damageSrc = DamageSource.d(source);
		
		// Destroy impact block in order to avoid the slab aberration
		org.bukkit.World bukkitWorld = world.getWorld();
		float hitBlockResistance = this.getBlockResistance(world, loc.getX(), loc.getY(), loc.getZ());
		BlockPosition hitBlock = new BlockPosition(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
		if(hitBlockResistance < this.materialPower) {
			cem.blockBreak(bukkitWorld.getBlockAt(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
			world.setTypeAndData(hitBlock, Blocks.a.getBlockData(), 3);
		}
		
		// Get blocks
		ArrayList<BlockPosition> brokenBlocks = new ArrayList<>();
		ArrayList<BlockPosition> firedBlocks = new ArrayList<>();
		
		int precision = this.directed ? DIRECTED_SQUARE_CUTTING : SQUARE_CUTTING;
		int dPrecision = precision - 1;
		float airResistance =  this.directed ? BLOCK_DIRECTED_AIR_RESISTANCE : BLOCK_AIR_RESISTANCE;
		
		for (int i = 0; i < precision; i++) {
			for (int j = 0; j < precision; j++) {
				for (int k = 0; k < precision; k++) {
					if (i == 0 || i == dPrecision || j == 0 || j == dPrecision || k == 0 || k == dPrecision) {

						double dx = k * 2.0 / dPrecision - 1.0;
						double dy = i * 2.0 / dPrecision - 1.0;
						double dz = j * 2.0 / dPrecision - 1.0;

						double multiplier = STEP_SIZE / Math.sqrt(dx * dx + dy * dy + dz * dz);

						dx *= multiplier;
						dy *= multiplier;
						dz *= multiplier;
						
						Vector relDirection = new Vector(dx, dy, dz).normalize();
						double dot = perforationDirection.dot(relDirection);
						
						if(!this.directed || dot > DIRECTED_ANGLE_TRESHOLD) {
							float powerModifier;
							if(this.directed) {
								powerModifier = 0.85f + world.w.nextFloat() * 0.3F;
								powerModifier *= 2 * (1 + Math.pow((dot - DIRECTED_ANGLE_TRESHOLD) / (1 - DIRECTED_ANGLE_TRESHOLD), 3));
							} else {
								powerModifier = 0.7f + world.w.nextFloat() * 0.6F;
							}

							double x = loc.getX();
							double y = loc.getY();
							double z = loc.getZ();

							for (float materialEnergy = this.materialPower * powerModifier, fireEnergy = this.firePower * powerModifier;
									materialEnergy > 0.0F || fireEnergy > 0.0f;
									materialEnergy -= airResistance, fireEnergy -= airResistance) {

								float energyLoss = this.getBlockResistance(world, x, y, z);
								materialEnergy -= energyLoss;
								fireEnergy -= energyLoss;
								
								BlockPosition blockposition = new BlockPosition(x, y, z);
								IBlockData block = world.getType(blockposition);

								if (y < 256 && y >= 0) {
									if(materialEnergy > 0.0f && !block.isAir())
										brokenBlocks.add(blockposition);
									if(fireEnergy > 0.0f && world.w.nextInt(3) == 0)
										firedBlocks.add(blockposition);
								}

								x += dx;
								y += dy;
								z += dz;
							}
						}
					}
				}
			}
		}
		
		// Add the hit block to the fired list
		if(hitBlockResistance < this.firePower) {
			firedBlocks.add(hitBlock);
		}
		
		
		// Entities
		float range = this.damagePower * 2.0F;
		if(this.directed) {
			range *= 2.5;
		}

		double bounding = range + 1;
		int minX = MathHelper.floor(loc.getX() - bounding);
		int maxX = MathHelper.floor(loc.getX() + bounding);
		int minY = MathHelper.floor(loc.getY() - bounding);
		int maxY = MathHelper.floor(loc.getY() + bounding);
		int minZ = MathHelper.floor(loc.getZ() - bounding);
		int maxZ = MathHelper.floor(loc.getZ() + bounding);

		List<Entity> entities = world.getEntities(source, new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ));

		Vec3D vec3d = new Vec3D(loc.getX(), loc.getY(), loc.getZ());

		for (Entity entity : entities) {
			if (!entity.cx()) {
				double relativeDistance = Math.sqrt(entity.e(vec3d)) / range;

				if (relativeDistance <= 1.0D) {
					double dx = entity.locX() - loc.getX();
					double dy = (entity instanceof EntityTNTPrimed ? entity.locY() : entity.getHeadY()) - loc.getY();
					double dz = entity.locZ() - loc.getZ();
					Vector distance = new Vector(dx, dy, dz);
					
					if(!this.directed || perforationDirection.dot(distance) > 0) {
						float damage = this.getDamage(world, entity, loc, perforationDirection);
						
						CraftEventFactory.entityDamage = source;
						entity.forceExplosionKnockback = false;

						boolean wasDamaged = entity.damageEntity(damageSrc, damage * DAMAGE_MULTIPLIER);

						CraftEventFactory.entityDamage = null;

						if (wasDamaged || entity instanceof EntityTNTPrimed || entity instanceof EntityFallingBlock 
								|| entity.forceExplosionKnockback) {

							double ejection = (1 - relativeDistance) * damage * EJECTION_MULTIPLIER;
							if (entity instanceof EntityLiving) {
								ejection = EnchantmentProtection.a((EntityLiving) entity, ejection);
							}

							Vector vectorEjection = distance.normalize().multiply(ejection);
							entity.setMot(entity.getMot().add(vectorEjection.getX(), vectorEjection.getY(), vectorEjection.getZ()));
						}
					}
				}
			}
		}
		
		// Destroy blocks
		world.addParticle(Particles.y, loc.getX(), loc.getY(), loc.getZ(), 1.0D, 0.0D, 0.0D);
		
		for(BlockPosition pos : brokenBlocks) {
			cem.blockBreak(bukkitWorld.getBlockAt(pos.getX(), pos.getY(), pos.getZ()));
			world.setTypeAndData(pos, Blocks.a.getBlockData(), 3);
		}
		for(BlockPosition pos : firedBlocks) {
			if (world.getType(pos).isAir() && world.getType(pos.down()).i(world, pos.down())) {
				world.setTypeUpdate(pos, BlockFireAbstract.a(world, pos));
			}
		}
	}
	
	
	
	
	
	private float getBlockResistance(World world, double x, double y, double z) {
		BlockPosition blockposition = new BlockPosition(x, y, z);
		IBlockData block = world.getType(blockposition);
		Fluid fluid = world.getFluid(blockposition);

		Optional<Float> optional = block.isAir() && fluid.isEmpty() ? Optional.empty()
				: Optional.of(Math.max(block.getBlock().getDurability(), fluid.i()));

		if (optional.isPresent()) {
			float energyLoss = ((Float) optional.get() + 0.3F) * 0.3F;
			return energyLoss;
		}
		return 0;
	}
	
	
	private float getDamage(World world, Entity entity, Location center, Vector perforationDirection) {
		AxisAlignedBB axisalignedbb = entity.getBoundingBox();
		
		double minX = axisalignedbb.a;
		double maxX = axisalignedbb.d;
		double minY = axisalignedbb.b;
		double maxY = axisalignedbb.e;
		double minZ = axisalignedbb.c;
		double maxZ = axisalignedbb.f;
		
		double[] xs = {minX, maxX}, ys = {minY, maxY}, zs = {minZ, maxZ};
		
		double damage = 0;
		for(double x : xs) {
			for(double y : ys) {
				for(double z : zs) {
					damage += this.getDamageTo(world, x, y, z, center, perforationDirection);
				}
			}
		}
		damage += 2*this.getDamageTo(world, (minX + maxX) / 2, (minY + maxY) / 2, (minZ + maxZ) / 2, center, perforationDirection);

		return (float) (damage / 10);
	}
	
	
	private double getDamageTo(World world, double x, double y, double z, Location center, Vector perforationDirection) {
		float airResistance = this.directed ? DAMAGE_DIRECTED_AIR_RESISTANCE : DAMAGE_AIR_RESISTANCE;
		
		Location target = new Location(center.getWorld(), x, y, z);
		Vector direction = target.subtract(center).toVector();
		Vector relativeDirection = direction.clone().normalize();
		
		double dot = perforationDirection.dot(relativeDirection);
		if(!this.directed || dot > DIRECTED_ANGLE_TRESHOLD) {
			float powerModifier = 1;
			if(this.directed) {
				powerModifier = 0.925f + world.w.nextFloat() * 0.15F;
				powerModifier *= 2 * (1 + Math.pow((dot - DIRECTED_ANGLE_TRESHOLD) / (1 - DIRECTED_ANGLE_TRESHOLD), 3));
			} else {
				powerModifier = 0.85f + world.w.nextFloat() * 0.3F;
			}
			
			double distance = direction.length();
			int steps = (int) (distance / STEP_SIZE) + 1;
//			System.out.println(distance+" "+steps+" "+(this.damagePower-steps*airResistance));
			
			float energy = this.damagePower * powerModifier;
			double X = center.getX();
			double Y = center.getY();
			double Z = center.getZ();
			
			double dX = relativeDirection.getX() * STEP_SIZE;
			double dY = relativeDirection.getY() * STEP_SIZE;
			double dZ = relativeDirection.getZ() * STEP_SIZE;

			for (int i = 0; i < steps && energy > 0.0F; i++, energy -= airResistance) {
				energy -= this.getBlockResistance(world, X, Y, Z);

				X += dX;
				Y += dY;
				Z += dZ;
			}
			
			return Math.max(0, energy);
		}
		return 0;
	}
	
	
	
	
//	private static float getObstacleImportance(Vec3D vec3d, Entity entity) {
//		AxisAlignedBB axisalignedbb = entity.getBoundingBox();
//		
//		double dx = 1.0D / ((axisalignedbb.d - axisalignedbb.a) * 2.0D + 1.0D);
//		double dy = 1.0D / ((axisalignedbb.e - axisalignedbb.b) * 2.0D + 1.0D);
//		double dz = 1.0D / ((axisalignedbb.f - axisalignedbb.c) * 2.0D + 1.0D);
//		
//		double shiftX = (1.0D - Math.floor(1.0D / dx) * dx) / 2.0D;
//		double shiftY = (1.0D - Math.floor(1.0D / dz) * dz) / 2.0D;
//		
//		if (dx >= 0.0D && dy >= 0.0D && dz >= 0.0D) {
//			int i = 0;
//			int j = 0;
//
//			for (double x = 0.0F; x <= 1.0F; x += dx) {
//				for (double y = 0.0F; y <= 1.0F; y += dy) {
//					for (double z = 0.0F; z <= 1.0F; z += dz) {
//						
//						double mX = MathHelper.d(x, axisalignedbb.a, axisalignedbb.d);
//						double mY = MathHelper.d(y, axisalignedbb.b, axisalignedbb.e);
//						double mZ = MathHelper.d(z, axisalignedbb.c, axisalignedbb.f);
//						
//						Vec3D vec3d1 = new Vec3D(mX + shiftX, mY, mZ + shiftY);
//						
//						if (entity.t.rayTrace(new RayTrace(vec3d1, vec3d, BlockCollisionOption.a, FluidCollisionOption.a, entity))
//								.getType() == EnumMovingObjectType.a) {
//							i++;
//						}
//						
//						j++;
//					}
//				}
//			}
//
//			return (float) i / (float) j;
//		} else {
//			return 0.0F;
//		}
//	}
	
	
}

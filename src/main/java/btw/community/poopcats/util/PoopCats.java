package btw.community.poopcats.util;

import btw.block.BTWBlocks;
import btw.community.poopcats.mixin.access.EntityLivingBaseAccess;
import btw.item.BTWItems;
import net.minecraft.src.*;

public class PoopCats {

	/**
	 * A callback interface that the EntityOcelotMixin implements.
	 * This allows our static utility class to set the "fed" state on the cat.
	 */
	public interface PoopCallback {
		void cats$setIsFed(boolean fed);
	}

	/**
	 * This method is now only called if the cat is already in the "fed" state.
	 * @param callback The mixin instance used to reset the fed state.
	 */
	public static void maybePoop(EntityLiving entity, World world, float yawHead, boolean isNocturnal, PoopCallback callback) {
		if (world.isRemote) {
			return;
		}

		int chance = 1;

		if (isDark(world, entity)) {
			chance *= 2;
		}

		// Frequency: 1/24000 (wolf), 1/36000 (cat)
		int baseRate = (entity instanceof EntityOcelot) ? 36000 : 24000;
		if (world.rand.nextInt(baseRate) < chance) {
			// Pass the callback down to the checking method
			handlePoopCheck(entity, world, yawHead, callback);
		}
	}

	/**
	 * Checks the block under the entity and decides whether to poop or explode.
	 * @param callback The callback used to reset the fed state.
	 */
	private static void handlePoopCheck(EntityLiving entity, World world, float yawHead, PoopCallback callback) {
		// Get the block directly under the cat's feet
		int i = MathHelper.floor_double(entity.posX);
		int j = MathHelper.floor_double(entity.posY) - 1; // -1 to get the block *under* the entity
		int k = MathHelper.floor_double(entity.posZ);

		int blockIdBelow = world.getBlockId(i, j, k);

		if (blockIdBelow == Block.sand.blockID) {
			// Safe! Poop normally.
			spawnPoop(entity, world, yawHead);
			// After pooping, reset the fed status
			callback.cats$setIsFed(false);
		} else {
			// Not on sand! KABOOM!
			explode(entity, world);
			// Cat is dead, no need to reset fed status (it's gone)
		}
	}

	/**
	 * Creates an explosion at the cat's location and removes the cat.
	 */
	private static void explode(EntityLiving entity, World world) {
		if (!world.isRemote) {
			// Create an explosion (3.0F is creeper-sized, 'true' allows block damage)
			world.createExplosion(entity, entity.posX, entity.posY, entity.posZ, 3.0F, true);
			// Remove the cat from the world
			entity.setDead();
		}
	}

	private static boolean isDark(World world, Entity entity) {
		int light = world.getBlockLightValue(
				MathHelper.floor_double(entity.posX),
				MathHelper.floor_double(entity.posY),
				MathHelper.floor_double(entity.posZ)
		);
		return light < 8;
	}

	/**
	 * Spawns the dung item and plays pooping effects.
	 * (This method no longer needs the callback, as it's handled one level up)
	 */
	private static void spawnPoop(EntityLiving entity, World world, float yawHead) {
		float dx = MathHelper.sin(yawHead / 180.0f * (float)Math.PI);
		float dz = -MathHelper.cos(yawHead / 180.0f * (float)Math.PI);
		double x = entity.posX + dx;
		double y = entity.posY + 0.25;
		double z = entity.posZ + dz;

		int i = MathHelper.floor_double(x);
		int j = MathHelper.floor_double(y);
		int k = MathHelper.floor_double(z);

		if (!isBlockOpen(world, i, j, k)) return;

		EntityItem poop = new EntityItem(world, x, y, z, new ItemStack(BTWItems.dung));
		float v = 0.05f;
		poop.motionX = dx * 10.0f * v;
		poop.motionZ = dz * 10.0f * v;
		poop.motionY = (float)world.rand.nextGaussian() * v + 0.2f;
		poop.delayBeforeCanPickup = 10;
		world.spawnEntityInWorld(poop);

		float vol = ((EntityLivingBaseAccess)entity).invokeGetSoundVolume();
		world.playSoundAtEntity(entity, "mob.witch.death1", vol, (world.rand.nextFloat() - world.rand.nextFloat()) * 0.2F + 0.5F);

		for (int n = 0; n < 5; ++n) {
			double px = entity.posX + (dx * 0.5f) + world.rand.nextDouble() * 0.25;
			double py = entity.posY + world.rand.nextDouble() * 0.5 + 0.25;
			double pz = entity.posZ + (dz * 0.5f) + world.rand.nextDouble() * 0.25;
			world.spawnParticle("smoke", px, py, pz, 0.0, 0.0, 0.0);
		}
	}

	private static boolean isBlockOpen(World world, int i, int j, int k) {
		Block block = Block.blocksList[world.getBlockId(i, j, k)];
		if (block != null && (block == Block.waterMoving || block == Block.waterStill ||
				block == Block.lavaMoving || block == Block.lavaStill ||
				block == Block.fire || block.blockMaterial.isReplaceable() ||
				block == BTWBlocks.detectorLogic || block == BTWBlocks.glowingDetectorLogic ||
				block == BTWBlocks.stokedFire)) {
			block = null;
		}
		return block == null;
	}
}
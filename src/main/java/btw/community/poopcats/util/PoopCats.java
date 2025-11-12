package btw.community.poopcats.util;

import btw.block.BTWBlocks;
import btw.community.poopcats.mixin.access.EntityLivingBaseAccess;
import btw.item.BTWItems;
import btw.util.sounds.AddonSoundRegistryEntry;
import net.minecraft.src.*;

public class PoopCats {

	// Sound constants
	public static final AddonSoundRegistryEntry CAT_POOP_SOUND =
			new AddonSoundRegistryEntry("btw:entity.witch.death1", 1);
	public static final AddonSoundRegistryEntry CAT_WARNING_SOUND =
			new AddonSoundRegistryEntry("mob.cat.hiss", 1);
	public static final AddonSoundRegistryEntry CAT_EXPLOSION_SOUND =
			new AddonSoundRegistryEntry("random.explosion", 2);

	// Game balance constants
	//private static final int BASE_POOP_RATE = 6000; // 5 minutes
	private static final int BASE_POOP_RATE = 1200; // 1 minute
	private static final int DARK_MULTIPLIER = 2;
	private static final float EXPLOSION_POWER = 3.0F;
	private static final int WARNING_DURATION = 100; // 5 seconds before explosion

	/**
	 * A callback interface that the EntityOcelotMixin implements.
	 * This allows our static utility class to interact with the cat's state.
	 */
	public interface PoopCallback {
		void cats$setIsFed(boolean fed);
		void cats$setWarningTicks(int ticks);
	}

	/**
	 * Main poop checking method, only called when cat is fed.
	 * Now with more reasonable frequency.
	 *
	 * @param entity The cat entity
	 * @param world The world
	 * @param yawHead The cat's head rotation
	 * @param callback The mixin instance used to update the cat's state
	 */
	public static void maybePoop(EntityLiving entity, World world, float yawHead, PoopCallback callback) {
		if (world.isRemote) {
			return;
		}

		int chance = 1;

		// Double chance in darkness
		if (isDark(world, entity)) {
			chance *= DARK_MULTIPLIER;
		}

		// More frequent pooping (5 minutes instead of 30)
		if (world.rand.nextInt(BASE_POOP_RATE) < chance) {
			handlePoopCheck(entity, world, yawHead, callback);
		}
	}

	/**
	 * Checks the block under the entity and decides whether to poop, warn, or explode.
	 *
	 * @param entity The cat entity
	 * @param world The world
	 * @param yawHead The cat's head rotation
	 * @param callback The callback used to update the cat's state
	 */
	private static void handlePoopCheck(EntityLiving entity, World world, float yawHead, PoopCallback callback) {
		// Get the block directly under the cat's feet
		int i = MathHelper.floor_double(entity.posX);
		int j = MathHelper.floor_double(entity.posY) - 1;
		int k = MathHelper.floor_double(entity.posZ);

		int blockIdBelow = world.getBlockId(i, j, k);

		if (blockIdBelow == Block.sand.blockID) {
			// Safe! Poop normally.
			spawnPoop(entity, world, yawHead);
			// After pooping, reset the fed status
			callback.cats$setIsFed(false);
			callback.cats$setWarningTicks(0);
		} else {
			// Not on sand! Check if we're already in warning state
			// We need to get the warning ticks from somewhere - the callback should provide a getter
			// For now, we'll start the warning
			startWarning(entity, world, callback);
		}
	}

	/**
	 * Called when warning timer reaches zero from the mixin.
	 * Creates an explosion if still not on sand, or poops if now on sand.
	 */
	public static void handleWarningExpired(EntityLiving entity, World world, float yawHead, PoopCallback callback) {
		if (world.isRemote) {
			return;
		}

		// Check one more time if they're on sand (player might have moved cat)
		int i = MathHelper.floor_double(entity.posX);
		int j = MathHelper.floor_double(entity.posY) - 1;
		int k = MathHelper.floor_double(entity.posZ);
		int blockIdBelow = world.getBlockId(i, j, k);

		if (blockIdBelow == Block.sand.blockID) {
			// Player saved the cat! Just poop normally
			spawnPoop(entity, world, yawHead);
			callback.cats$setIsFed(false);
			callback.cats$setWarningTicks(0);
		} else {
			// Still not on sand - KABOOM!
			world.createExplosion(entity, entity.posX, entity.posY, entity.posZ,
					EXPLOSION_POWER, true);
			entity.setDead();
		}
	}

	/**
	 * Starts the warning phase before explosion.
	 * Gives the player a chance to move the cat to safety.
	 */
	private static void startWarning(EntityLiving entity, World world, PoopCallback callback) {
		// Set warning ticks - we need to expose this value
		// Using reflection or a getter through callback
		callback.cats$setWarningTicks(WARNING_DURATION);

		// Play warning sound
		world.playSoundAtEntity(entity, CAT_WARNING_SOUND.sound(), 1.0F, 0.8F);

		// Spawn warning particles
		for (int n = 0; n < 10; ++n) {
			double px = entity.posX + (world.rand.nextDouble() - 0.5) * 0.5;
			double py = entity.posY + world.rand.nextDouble() * 0.5 + 0.25;
			double pz = entity.posZ + (world.rand.nextDouble() - 0.5) * 0.5;
			world.spawnParticle("reddust", px, py, pz, 1.0, 0.0, 0.0);
		}

		// Schedule explosion check for later
		// The mixin will handle counting down and calling explode when ready
	}

	/**
	 * Called when warning timer reaches zero.
	 * Creates an explosion and removes the cat.
	 */
	public static void explode(EntityLiving entity, World world) {
		if (!world.isRemote) {
			// Check one more time if they're on sand (player might have moved cat)
			int i = MathHelper.floor_double(entity.posX);
			int j = MathHelper.floor_double(entity.posY) - 1;
			int k = MathHelper.floor_double(entity.posZ);
			int blockIdBelow = world.getBlockId(i, j, k);

			if (blockIdBelow == Block.sand.blockID) {
				// Player saved the cat! Just poop normally
				return;
			}

			// Still not on sand - KABOOM!
			world.createExplosion(entity, entity.posX, entity.posY, entity.posZ,
					EXPLOSION_POWER, true);
			entity.setDead();
		}
	}

	/**
	 * Checks if the entity is in darkness (light level < 8).
	 */
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

		// Play pooping sound
		float vol = ((EntityLivingBaseAccess)entity).invokeGetSoundVolume();
		world.playSoundAtEntity(entity, CAT_POOP_SOUND.sound(), vol * 8,
				(world.rand.nextFloat() - world.rand.nextFloat()) * 0.2F + 0.75F);

		// Spawn poop particles
		for (int n = 0; n < 5; ++n) {
			double px = entity.posX + (dx * 0.5f) + world.rand.nextDouble() * 0.25;
			double py = entity.posY + world.rand.nextDouble() * 0.5 + 0.25;
			double pz = entity.posZ + (dz * 0.5f) + world.rand.nextDouble() * 0.25;
			world.spawnParticle("smoke", px, py, pz, 0.0, 0.0, 0.0);
		}
	}

	/**
	 * Checks if a block position is open for dropping items.
	 */
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
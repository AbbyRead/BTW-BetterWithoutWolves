package btw.community.poopcats.util;

import btw.block.BTWBlocks;
import btw.community.poopcats.mixin.access.EntityLivingBaseAccess;
import btw.item.BTWItems;
import btw.util.sounds.AddonSoundRegistryEntry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.src.*;

public class PoopCats {

	public static final AddonSoundRegistryEntry CAT_POOP_SOUND =
			new AddonSoundRegistryEntry("btw:entity.witch.death1", 1);
	public static final AddonSoundRegistryEntry CAT_WARNING_SOUND =
			new AddonSoundRegistryEntry("mob.cat.hiss", 1);
	public static final AddonSoundRegistryEntry CAT_EXPLOSION_SOUND =
			new AddonSoundRegistryEntry("random.explosion", 2);

	private static final int BASE_POOP_RATE = 60;
	private static final int DARK_MULTIPLIER = 2;
	private static final float EXPLOSION_POWER = 1.0F;
	private static final int WARNING_DURATION = 80;

	public static final byte POOP_PARTICLE_ID = 9;
	public static final byte SYNC_YAW_BEFORE_POOP = 21;

	public interface PoopCallback {
		void cats$setIsFed(boolean fed);
		void cats$setWarningTicks(int ticks);
		int cats$getWarningTicks();
		// NEW: Add accessors for swell time interpolation
		int cats$getSwellTime();
		int cats$getLastSwellTime();
	}

	public static void maybePoop(EntityLiving entity, World world, float yawOffset, PoopCallback callback) {
		if (world.isRemote) return;

		int chance = 1;
		if (isDark(world, entity)) chance *= DARK_MULTIPLIER;

		if (world.rand.nextInt(BASE_POOP_RATE) < chance) {
			handlePoopCheck(entity, world, yawOffset, callback);
		}
	}

	private static void handlePoopCheck(EntityLiving entity, World world, float yawOffset, PoopCallback callback) {
		int i = MathHelper.floor_double(entity.posX);
		int j = MathHelper.floor_double(entity.posY) - 1;
		int k = MathHelper.floor_double(entity.posZ);

		int blockIdBelow = world.getBlockId(i, j, k);

		if (blockIdBelow == Block.sand.blockID) {
			spawnPoop(entity, world, yawOffset);
			callback.cats$setIsFed(false);
			callback.cats$setWarningTicks(0);
		} else {
			startWarning(entity, world, callback);
		}
	}

	public static void handleWarningExpired(EntityLiving entity, World world, float yawOffset, PoopCallback callback) {
		if (world.isRemote) return;

		int i = MathHelper.floor_double(entity.posX);
		int j = MathHelper.floor_double(entity.posY) - 1;
		int k = MathHelper.floor_double(entity.posZ);
		int blockIdBelow = world.getBlockId(i, j, k);

		if (blockIdBelow == Block.sand.blockID) {
			// Made it to sand in time!
			spawnPoop(entity, world, yawOffset);
			callback.cats$setIsFed(false);
			callback.cats$setWarningTicks(0);
		} else {
			// EXPLOSION with custom red particle effect
			explodeWithRedParticles(entity, world);
			entity.setDead();
		}
	}

	/**
	 * Creates a custom explosion with red particle effects
	 */
	private static void explodeWithRedParticles(EntityLiving entity, World world) {
		if (world.isRemote) return;

		// Play explosion sound
		world.playSoundAtEntity(entity, CAT_EXPLOSION_SOUND.sound(), 4.0F,
				(1.0F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.2F) * 0.7F);

		// Create actual explosion damage
		world.createExplosion(entity, entity.posX, entity.posY, entity.posZ,
				EXPLOSION_POWER, true);

		// Spawn lots of red particles on all clients
		if (!world.isRemote) {
			// Server tells clients to spawn particles
			world.setEntityState(entity, (byte)35); // Custom particle event ID


		}
	}


	private static void startWarning(EntityLiving entity, World world, PoopCallback callback) {
		callback.cats$setWarningTicks(WARNING_DURATION);
		world.playSoundAtEntity(entity, CAT_WARNING_SOUND.sound(), 1.0F, 0.8F);

		for (int n = 0; n < 10; ++n) {
			double px = entity.posX + (world.rand.nextDouble() - 0.5) * 0.5;
			double py = entity.posY + world.rand.nextDouble() * 0.5 + 0.25;
			double pz = entity.posZ + (world.rand.nextDouble() - 0.5) * 0.5;
			world.spawnParticle("reddust", px, py, pz, 1.0, 0.0, 0.0);
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

	private static void spawnPoop(EntityLiving entity, World world, float yawOffset) {
		if (world.isRemote) return;

		// Tell clients to spawn particles
		world.setEntityState(entity, POOP_PARTICLE_ID);
		world.setEntityState(entity, SYNC_YAW_BEFORE_POOP);

		// Server-side: spawn poop item
		float dx = MathHelper.sin(yawOffset / 180.0f * (float)Math.PI);
		float dz = -MathHelper.cos(yawOffset / 180.0f * (float)Math.PI);
		double x = entity.posX + dx;
		double y = entity.posY + 0.25;
		double z = entity.posZ + dz;

		if (!isBlockOpen(world, MathHelper.floor_double(x), MathHelper.floor_double(y), MathHelper.floor_double(z))) return;

		EntityItem poop = new EntityItem(world, x, y, z, new ItemStack(BTWItems.dung));
		float v = 0.05f;
		poop.motionX = dx * 10.0f * v;
		poop.motionZ = dz * 10.0f * v;
		poop.motionY = (float)world.rand.nextGaussian() * v + 0.2f;
		poop.delayBeforeCanPickup = 10;
		world.spawnEntityInWorld(poop);

		float vol = ((EntityLivingBaseAccess)entity).invokeGetSoundVolume();
		world.playSoundAtEntity(entity, CAT_POOP_SOUND.sound(), vol * 7,
				(world.rand.nextFloat() - world.rand.nextFloat()) * 0.2F + 1.6F);
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


	@Environment(EnvType.CLIENT)
	public static void handlePoopParticles(EntityOcelot cat) {

		float poopVectorX = MathHelper.sin(cat.renderYawOffset / 180.0F * (float)Math.PI);
		float poopVectorZ = -MathHelper.cos(cat.renderYawOffset / 180.0F * (float)Math.PI);
		double baseX = cat.posX + poopVectorX;
		double baseY = cat.posY + 0.25;
		double baseZ = cat.posZ + poopVectorZ;

		EntityFX mainParticle = new EntitySmokeFX(
				cat.worldObj,
				baseX + cat.worldObj.rand.nextDouble() * 0.25 - 0.125,
				baseY + cat.worldObj.rand.nextDouble() * 0.5,
				baseZ + cat.worldObj.rand.nextDouble() * 0.25 - 0.125,
				0, 0, 0,
				0.33f
		);
		mainParticle.setRBGColorF(0.4f + cat.rand.nextFloat() * 0.1f, 0.25f + cat.rand.nextFloat() * 0.05f, 0.1f + cat.rand.nextFloat() * 0.05f);
		Minecraft.getMinecraft().effectRenderer.addEffect(mainParticle);

		for (int n = 0; n < 7; ++n) {
			double offsetX = (cat.rand.nextDouble() - 0.5) * 0.5;
			double offsetY = cat.rand.nextDouble() * 0.5 + 0.25;
			double offsetZ = (cat.rand.nextDouble() - 0.5) * 0.5;

			EntityFX particle = new EntitySmokeFX(
					cat.worldObj,
					baseX + offsetX,
					baseY + offsetY,
					baseZ + offsetZ,
					cat.rand.nextGaussian() * 0.02,
					cat.rand.nextGaussian() * 0.02,
					cat.rand.nextGaussian() * 0.02,
					0.33f
			);

			particle.setRBGColorF(0.4f + cat.rand.nextFloat() * 0.1f, 0.25f + cat.rand.nextFloat() * 0.05f, 0.1f + cat.rand.nextFloat() * 0.05f);
			Minecraft.getMinecraft().effectRenderer.addEffect(particle);
		}
	}

	/**
	 * NEW: Client-side particle handler for the explosion
	 */
	@Environment(EnvType.CLIENT)
	public static void handleExplosionParticles(EntityOcelot cat) {
		World world = cat.worldObj;

		// Spawn standard "poof" particles
		for (int i = 0; i < 20; ++i) {
			double motionX = world.rand.nextGaussian() * 0.02D;
			double motionY = world.rand.nextGaussian() * 0.02D + 0.2D;
			double motionZ = world.rand.nextGaussian() * 0.02D;
			world.spawnParticle("explode",
					cat.posX + (world.rand.nextFloat() - 0.5D) * (double)cat.width,
					cat.posY + (double)world.rand.nextFloat() * (double)cat.height,
					cat.posZ + (world.rand.nextFloat() - 0.5D) * (double)cat.width,
					motionX, motionY, motionZ);
		}

		// Spawn red "blood" particles
		for (int i = 0; i < 30; i++) {
			double offsetX = (world.rand.nextDouble() - 0.5D) * 2.0D;
			double offsetY = world.rand.nextDouble() * 1.5D;
			double offsetZ = (world.rand.nextDouble() - 0.5D) * 2.0D;
			// Use reddust particle with full red color
			world.spawnParticle("reddust",
					cat.posX + offsetX,
					cat.posY + offsetY,
					cat.posZ + offsetZ,
					0.5, 0.0, 0.0); // R, G, B
		}

		for (int i = 0; i < 10; i++) {
			double offsetX = (world.rand.nextDouble() - 0.5D) * 1.5D;
			double offsetY = world.rand.nextDouble();
			double offsetZ = (world.rand.nextDouble() - 0.5D) * 1.5D;

			world.spawnParticle("smoke",
					cat.posX + offsetX,
					cat.posY + offsetY,
					cat.posZ + offsetZ,
					0.3, 0.0, 0.0);
		}
	}

}
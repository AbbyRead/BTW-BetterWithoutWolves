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
	private static final int WARNING_DURATION = 100;

	public static final byte POOP_PARTICLE_ID = 9;
	public static final byte SYNC_YAW_BEFORE_POOP = 21;

	public interface PoopCallback {
		void cats$setIsFed(boolean fed);
		void cats$setWarningTicks(int ticks);
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
			spawnPoop(entity, world, yawOffset);
			callback.cats$setIsFed(false);
			callback.cats$setWarningTicks(0);
		} else {
			world.createExplosion(entity, entity.posX, entity.posY, entity.posZ,
					EXPLOSION_POWER, true);
			entity.setDead();
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
		world.playSoundAtEntity(entity, CAT_POOP_SOUND.sound(), vol * 8,
				(world.rand.nextFloat() - world.rand.nextFloat()) * 0.2F + 0.75F);
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

}
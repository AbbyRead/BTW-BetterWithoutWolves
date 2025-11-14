package btw.community.poopcats.util;

import btw.block.BTWBlocks;
import btw.client.fx.BTWEffectManager;
import btw.community.poopcats.interfaces.PoopCallback;
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
			new AddonSoundRegistryEntry("random.explode", 2);

	// --- Main Logic (Moved from Mixin) ---

	/**
	 * Handles the feeding interaction logic.
	 * Called from EntityOcelotMixin.
	 *
	 * @return true if the interaction was handled (and should be cancelled)
	 */
	public static boolean handleCatInteraction(EntityOcelot cat, EntityPlayer player, PoopCallback callback) {
		ItemStack stack = player.inventory.getCurrentItem();

		if (cat.isTamed() && stack != null && cat.isBreedingItem(stack)) {
			if (!callback.cats$isFed()) {
				if (!player.capabilities.isCreativeMode) {
					--stack.stackSize;
					if (stack.stackSize <= 0) {
						player.inventory.setInventorySlotContents(player.inventory.currentItem, null);
					}
				}

				callback.cats$setIsFed(true);
				cat.heal(PoopCatsConstants.HEALING_AMOUNT);

				cat.worldObj.playAuxSFX(BTWEffectManager.BURP_SOUND_EFFECT_ID,
						MathHelper.floor_double(cat.posX),
						MathHelper.floor_double(cat.posY),
						MathHelper.floor_double(cat.posZ), 0);

				for (int i = 0; i < 7; ++i) {
					double px = cat.posX + (cat.worldObj.rand.nextDouble() - 0.5) * 0.5;
					double py = cat.posY + cat.worldObj.rand.nextDouble() * 0.5 + 0.5;
					double pz = cat.posZ + (cat.worldObj.rand.nextDouble() - 0.5) * 0.5;
					cat.worldObj.spawnParticle("heart", px, py, pz, 0.0, 0.0, 0.0);
				}
				return true; // Handled
			}
		}
		return false; // Not handled
	}

	/**
	 * Handles all cat pooping logic per-tick.
	 * Called from EntityOcelotMixin.
	 */
	public static void updateCatLogic(EntityOcelot cat) {
		PoopCallback callback = (PoopCallback) cat;

		// Store last swell time for interpolation
		callback.cats$setLastSwellTime(callback.cats$getSwellTime());

		if (!cat.isTamed()) {
			return;
		}

		int warningTicks = callback.cats$getWarningTicks();
		if (warningTicks > 0) {
			int nextTicks = warningTicks - 1;
			callback.cats$setWarningTicks(nextTicks);

			// Increase swell as warning progresses
			int swellTime = PoopCatsConstants.MAX_SWELL_TIME - (nextTicks * PoopCatsConstants.MAX_SWELL_TIME / PoopCatsConstants.WARNING_DURATION);
			callback.cats$setSwellTime(Math.min(swellTime, PoopCatsConstants.MAX_SWELL_TIME));

			if (warningTicks % 20 == 0 && warningTicks > 20) {
				cat.worldObj.spawnParticle("reddust",
						cat.posX, cat.posY + 0.5, cat.posZ,
						1.0, 0.0, 0.0);

				cat.worldObj.playSoundAtEntity(cat,
						PoopCats.CAT_WARNING_SOUND.sound(),
						1.0F, 0.8F);
			}

			if (nextTicks == 10) {
				cat.worldObj.spawnParticle("reddust",
						cat.posX, cat.posY + 0.5, cat.posZ,
						1.0, 0.0, 0.0);
			}

			if (nextTicks <= 0) {
				PoopCats.handleWarningExpired(cat, cat.worldObj, cat.renderYawOffset, callback);
				callback.cats$setSwellTime(0); // Reset swell
				return;
			}
		} else {
			// Reset swell when not warning
			callback.cats$setSwellTime(0);
		}

		if (callback.cats$isFed()) {
			PoopCats.maybePoop(cat, cat.worldObj, cat.renderYawOffset, callback);
		}
	}


	// --- Poop Event Handling ---

	public static void maybePoop(EntityLiving entity, World world, float yawOffset, PoopCallback callback) {
		if (world.isRemote) return;

		int chance = 1;
		if (isDark(world, entity)) chance *= PoopCatsConstants.DARK_MULTIPLIER;

		if (world.rand.nextInt(PoopCatsConstants.BASE_POOP_RATE) < chance) {
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

	private static void startWarning(EntityLiving entity, World world, PoopCallback callback) {
		callback.cats$setWarningTicks(PoopCatsConstants.WARNING_DURATION);
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
		world.setEntityState(entity, PoopCatsConstants.POOP_PARTICLE_ID);
		world.setEntityState(entity, PoopCatsConstants.SYNC_YAW_BEFORE_POOP);

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

	// --- Client-Side Particle Handlers ---

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

	private static void explodeWithRedParticles(EntityLiving entity, World world) {
		if (world.isRemote) return;

		Explosion explosion = new Explosion(world, entity, entity.posX, entity.posY, entity.posZ, PoopCatsConstants.EXPLOSION_POWER);
		explosion.isFlaming = false;
		explosion.isSmoking = true;
		explosion.doExplosionA();
		explosion.doExplosionB(false);

		// Tell clients to spawn our custom particles
		world.setEntityState(entity, PoopCatsConstants.EXPLOSION_PARTICLE_ID);
	}

	@Environment(EnvType.CLIENT)
	public static void handleExplosionParticles(EntityOcelot cat) {
		World world = cat.worldObj;
		Minecraft mc = Minecraft.getMinecraft();
		int poofCount = 20;
		float brownRed = 0.4f;
		float brownGreen = 0.25f;
		float brownBlue = 0.1f;

		for (int i = 0; i < poofCount; ++i) {
			double motionX = (world.rand.nextGaussian() * 0.02D) + (world.rand.nextDouble() - 0.5D) * 0.2D;
			double motionY = (world.rand.nextGaussian() * 0.02D) + world.rand.nextDouble() * 0.4D;
			double motionZ = (world.rand.nextGaussian() * 0.02D) + (world.rand.nextDouble() - 0.5D) * 0.2D;

			double posX = cat.posX + (world.rand.nextFloat() - 0.5D) * (double)cat.width;
			double posY = cat.posY + world.rand.nextDouble() * (double)cat.height;
			double posZ = cat.posZ + (world.rand.nextFloat() - 0.5D) * (double)cat.width;

			EntityFX poof = new EntitySmokeFX(
					world,
					posX,
					posY,
					posZ,
					motionX,
					motionY,
					motionZ,
					1.5f
			);
			poof.setRBGColorF(
					brownRed + world.rand.nextFloat() * 0.1f,
					brownGreen + world.rand.nextFloat() * 0.05f,
					brownBlue + world.rand.nextFloat() * 0.05f
			);
			mc.effectRenderer.addEffect(poof);
		}

		// --- Globs (using EntityDropParticleFX) ---
		int globCount = 30;
		float bloodRed = 0.5F;
		float bloodGreen = 0.0F;
		float bloodBlue = 0.0F;

		for (int i = 0; i < globCount; ++i) {
			double motionX = (world.rand.nextDouble() - 0.5D) * 1.0D;
			double motionY = world.rand.nextDouble() * 0.5D + 0.5D;
			double motionZ = (world.rand.nextDouble() - 0.5D) * 1.0D;

			EntityFX glob = new EntityDropParticleFX(
					world,
					cat.posX,
					cat.posY + 0.5D,
					cat.posZ,
					Material.lava,
					0,
					bloodRed,
					bloodGreen,
					bloodBlue,
					1.0F
			);

			glob.motionX = motionX;
			glob.motionY = motionY;
			glob.motionZ = motionZ;

			mc.effectRenderer.addEffect(glob);
		}

		// --- Spray/Mist (using EntityReddustFX) ---
		int sprayCount = 50;

		for (int i = 0; i < sprayCount; i++) {
			double posX = cat.posX + (world.rand.nextDouble() - 0.5D) * (double)cat.width;
			double posY = cat.posY + world.rand.nextDouble() * (double)cat.height;
			double posZ = cat.posZ + (world.rand.nextDouble() - 0.5D) * (double)cat.width;

			EntityFX spray = new EntityReddustFX(
					world,
					posX,
					posY,
					posZ,
					1.0F,  // Scale
					1.0F,  // R
					0.0F,  // G
					0.0F   // B
			);

			spray.motionX = (world.rand.nextDouble() - 0.5D) * 0.5D;
			spray.motionY = world.rand.nextDouble() * 0.5D;
			spray.motionZ = (world.rand.nextDouble() - 0.5D) * 0.5D;

			mc.effectRenderer.addEffect(spray);
		}
	}
}
package btw.community.poopcats.util;

import btw.block.BTWBlocks;
import btw.community.poopcats.mixin.EntityLivingBaseInvoker;
import btw.item.BTWItems;
import net.minecraft.src.*;

public class PoopCats {

	public static void maybePoop(EntityLiving entity, World world, float yawHead, boolean isNocturnal) {
		int chance = 1;

		if (isDark(world, entity)) {
			chance *= 2;
		}

		// Frequency: 1/24000 (wolf), 1/36000 (cat)
		int baseRate = (entity instanceof EntityOcelot) ? 36000 : 24000;
		if (world.rand.nextInt(baseRate) < chance) {
			poop(entity, world, yawHead);
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

	private static void poop(EntityLiving entity, World world, float yawHead) {
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

		float vol = ((EntityLivingBaseInvoker)entity).invokeGetSoundVolume();
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
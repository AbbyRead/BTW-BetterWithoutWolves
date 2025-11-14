package btw.community.poopcats.util;

import btw.community.poopcats.interfaces.PoopCallback;
import net.minecraft.src.*;

public class EntityAIOcelotSeekSand extends EntityAIBase {

	private final EntityOcelot ocelot;
	private final double speed;
	private int searchTick;
	private int targetX;
	private int targetY;
	private int targetZ;
	private boolean foundSand;

	public EntityAIOcelotSeekSand(EntityOcelot ocelot, double speed) {
		this.ocelot = ocelot;
		this.speed = speed;
		this.setMutexBits(1); // Movement mutex
	}

	@Override
	public boolean shouldExecute() {
		if (!(ocelot instanceof PoopCallback callback)) { // Use new interface
			return false;
		}

		// Only seek sand when warning is active
		if (callback.cats$getWarningTicks() <= 0) {
			return false;
		}

		// Don't interrupt if already on sand
		int i = MathHelper.floor_double(ocelot.posX);
		int j = MathHelper.floor_double(ocelot.posY) - 1;
		int k = MathHelper.floor_double(ocelot.posZ);
		if (ocelot.worldObj.getBlockId(i, j, k) == Block.sand.blockID) {
			return false;
		}

		// Search every 20 ticks
		if (searchTick++ % 20 == 0) {
			foundSand = findNearbySand();
			return foundSand;
		}

		return foundSand && !ocelot.getNavigator().noPath();
	}

	@Override
	public boolean continueExecuting() {
		if (!(ocelot instanceof PoopCallback callback)) { // Use new interface
			return false;
		}

		// Stop if warning expired
		if (callback.cats$getWarningTicks() <= 0) {
			return false;
		}

		// Stop if reached sand
		int i = MathHelper.floor_double(ocelot.posX);
		int j = MathHelper.floor_double(ocelot.posY) - 1;
		int k = MathHelper.floor_double(ocelot.posZ);
		if (ocelot.worldObj.getBlockId(i, j, k) == Block.sand.blockID) {
			return false;
		}

		return !ocelot.getNavigator().noPath();
	}

	@Override
	public void startExecuting() {
		ocelot.getNavigator().tryMoveToXYZ(
				(double)targetX + 0.5D,
				(double)targetY + 1,
				(double)targetZ + 0.5D,
				speed
		);
	}

	@Override
	public void resetTask() {
		foundSand = false;
		ocelot.getNavigator().clearPathEntity();
	}

	/**
	 * Searches for nearby sand blocks within a 16 block radius
	 */
	private boolean findNearbySand() {
		int centerX = MathHelper.floor_double(ocelot.posX);
		int centerY = MathHelper.floor_double(ocelot.posY);
		int centerZ = MathHelper.floor_double(ocelot.posZ);

		double closestDistSq = Double.MAX_VALUE;
		boolean found = false;

		// Search in a 16x8x16 area
		for (int x = centerX - 16; x <= centerX + 16; x++) {
			for (int y = Math.max(0, centerY - 4); y <= Math.min(255, centerY + 4); y++) {
				for (int z = centerZ - 16; z <= centerZ + 16; z++) {

					// Check if this is sand and has air above
					if (ocelot.worldObj.getBlockId(x, y, z) == Block.sand.blockID &&
							ocelot.worldObj.isAirBlock(x, y + 1, z)) {

						double distSq = ocelot.getDistanceSq(x, y, z);

						if (distSq < closestDistSq) {
							closestDistSq = distSq;
							targetX = x;
							targetY = y;
							targetZ = z;
							found = true;
						}
					}
				}
			}
		}

		return found;
	}
}
package btw.community.poopcats.util;

import btw.community.poopcats.interfaces.PoopCatStateAccess;
import net.minecraft.src.EntityAIBase;
import net.minecraft.src.EntityLivingBase;
import net.minecraft.src.EntityOcelot;

public class PoopCatAISwell extends EntityAIBase {

	private final EntityOcelot ocelot;
	private EntityLivingBase target;
	private final double rangeSq;
	private int hissCooldown = 0;

	public PoopCatAISwell(EntityOcelot ocelot, double range) {
		this.ocelot = ocelot;
		this.rangeSq = range * range;
		this.setMutexBits(1);
	}

	@Override
	public boolean shouldExecute() {
		EntityLivingBase t = ocelot.getAttackTarget();
		return t != null && ocelot.getEntitySenses().canSee(t)
				&& ocelot.getDistanceSqToEntity(t) < rangeSq;
	}

	@Override
	public void startExecuting() {
		this.target = ocelot.getAttackTarget();
		ocelot.getNavigator().clearPathEntity();

		// Use new interface and constant
		((PoopCatStateAccess) ocelot).cats$setWarningTicks(PoopCatConstants.WARNING_DURATION);
		ocelot.playSound("mob.cat.hiss", 1.0F, 1.0F);
	}

	@Override
	public boolean continueExecuting() {
		if (target == null) return false;

		boolean canSee = ocelot.getEntitySenses().canSee(target);
		double distSq = ocelot.getDistanceSqToEntity(target);
		return canSee && distSq < rangeSq * 1.5D;
	}

	@Override
	public void resetTask() {
		((PoopCatStateAccess) ocelot).cats$setWarningTicks(0); // Use new interface
		target = null;
	}

	@Override
	public void updateTask() {
		if (target == null) return;

		ocelot.faceEntity(target, 30.0F, 30.0F);
		int ticks = ((PoopCatStateAccess) ocelot).cats$getWarningTicks();

		if (ticks <= 0) {
			PoopCatHelper.handleWarningExpired(ocelot, ocelot.worldObj, ocelot.renderYawOffset,
					(PoopCatStateAccess) ocelot);
			resetTask();
			return;
		}

		((PoopCatStateAccess) ocelot).cats$setWarningTicks(ticks - 1);
		// Occasional hiss
		if (hissCooldown-- <= 0) {
			ocelot.playSound("mob.cat.hiss", 1.0F, 1.0F);
			hissCooldown = 40 + ocelot.worldObj.rand.nextInt(20);
		}

		// Client particle
		if (ocelot.worldObj.isRemote && ticks % 10 == 0) {
			double px = ocelot.posX + (ocelot.worldObj.rand.nextDouble() - 0.5) * 0.5;
			double py = ocelot.posY + ocelot.worldObj.rand.nextDouble() * 0.5 + 0.5;
			double pz = ocelot.posZ + (ocelot.worldObj.rand.nextDouble() - 0.5) * 0.5;
			ocelot.worldObj.spawnParticle("reddust", px, py, pz, 1.0, 0.0, 0.0);
		}
	}
}

package btw.community.poopcats.mixin;

import net.minecraft.src.EntityWolf;
import net.minecraft.src.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityWolf.class)
public class EntityWolfMixin {

	@Inject(method = "updateShitState", at = @At("HEAD"), cancellable = true)
	private void makeShit(CallbackInfo ci) {
		EntityWolf wolf = (EntityWolf) (Object) this;
		if (wolf.worldObj.rand.nextInt(24) == 0) {
			wolf.attemptToShit();
			float poopVectorX = MathHelper.sin(wolf.rotationYawHead / 180.0f * (float)Math.PI);
			float poopVectorZ = -MathHelper.cos(wolf.rotationYawHead / 180.0f * (float)Math.PI);
			double smokeX = wolf.posX + (double)(poopVectorX * 0.5f) + wolf.worldObj.rand.nextDouble() * 0.25;
			double smokeY = wolf.posY + wolf.worldObj.rand.nextDouble() * 0.5 + 0.25;
			double smokeZ = wolf.posZ + (double)(poopVectorZ * 0.5f) + wolf.worldObj.rand.nextDouble() * 0.25;
			wolf.worldObj.spawnParticle("smoke", smokeX, smokeY, smokeZ, 0.0, 0.0, 0.0);

			String particleName = "smoke";
			for (int smokeTimer = 0; smokeTimer < 7; ++smokeTimer) {
				double sX = wolf.rand.nextGaussian() * 0.02;
				double sY = wolf.rand.nextGaussian() * 0.02;
				double sZ = wolf.rand.nextGaussian() * 0.02;
				wolf.worldObj.spawnParticle(particleName, wolf.posX + (double)(wolf.rand.nextFloat() * wolf.width * 2.0f) - (double)wolf.width, wolf.posY + 0.5 + (double)(wolf.rand.nextFloat() * wolf.height), wolf.posZ + (double)(wolf.rand.nextFloat() * wolf.width * 2.0f) - (double)wolf.width, sX, sY, sZ);
			}
		}
		ci.cancel();
	}
}

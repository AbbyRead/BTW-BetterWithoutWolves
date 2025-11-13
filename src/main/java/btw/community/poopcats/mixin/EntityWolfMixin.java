package btw.community.poopcats.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.src.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityWolf.class)
public class EntityWolfMixin {

	@Unique
	private static final byte POOP_PARTICLE_ID = 9; // Arbitrary unique ID for wolf poop particle

	@Inject(method = "updateShitState", at = @At("HEAD"), cancellable = true)
	private void makeShit(CallbackInfo ci) {
		EntityWolf wolf = (EntityWolf) (Object) this;

		if (wolf.worldObj.rand.nextInt(24) == 0) {
			wolf.attemptToShit();

			// Trigger client-side particle spawning via setEntityState
			if (!wolf.worldObj.isRemote) {
				wolf.worldObj.setEntityState(wolf, POOP_PARTICLE_ID);
			}
		}

		ci.cancel();
	}

	/**
	 * Client-only method for spawning the poop particles.
	 * Inject this into handleHealthUpdate on the client.
	 */
	@Unique
	@Environment(EnvType.CLIENT)
	public void handlePoopParticles() {
		EntityWolf wolf = (EntityWolf) (Object) this;

		double distanceBack = 1.0; // distance behind the wolf
		float yawRad = wolf.rotationYaw / 180.0f * (float) Math.PI;

		// Main smoke puff directly behind the wolf
		double smokeX = wolf.posX + MathHelper.sin(yawRad) * distanceBack + wolf.worldObj.rand.nextDouble() * 0.25 - 0.125;
		double smokeY = wolf.posY + wolf.worldObj.rand.nextDouble() * 0.5 + 0.25;
		double smokeZ = wolf.posZ - MathHelper.cos(yawRad) * distanceBack + wolf.worldObj.rand.nextDouble() * 0.25 - 0.125;

		EntityFX mainParticle = new EntitySmokeFX(wolf.worldObj, smokeX, smokeY, smokeZ, 0.0, 0.0, 0.0, 0.33f);
		mainParticle.setRBGColorF(0.4f + wolf.rand.nextFloat() * 0.1f, 0.25f + wolf.rand.nextFloat() * 0.05f, 0.1f + wolf.rand.nextFloat() * 0.05f);
		Minecraft.getMinecraft().effectRenderer.addEffect(mainParticle);

		// Random smoke bits around the back area
		for (int n = 0; n < 7; ++n) {
			double offsetX = (wolf.rand.nextDouble() - 0.5) * 0.5;
			double offsetY = wolf.rand.nextDouble() * 0.5 + 0.25;
			double offsetZ = (wolf.rand.nextDouble() - 0.5) * 0.5;

			EntityFX particle = new EntitySmokeFX(
					wolf.worldObj,
					wolf.posX + MathHelper.sin(yawRad) * distanceBack + offsetX,
					wolf.posY + offsetY,
					wolf.posZ - MathHelper.cos(yawRad) * distanceBack + offsetZ,
					wolf.rand.nextGaussian() * 0.02,
					wolf.rand.nextGaussian() * 0.02,
					wolf.rand.nextGaussian() * 0.02,
					0.33f
			);

			// Brownish RGB for poop particles
			particle.setRBGColorF(0.4f + wolf.rand.nextFloat() * 0.1f, 0.25f + wolf.rand.nextFloat() * 0.05f, 0.1f + wolf.rand.nextFloat() * 0.05f);
			Minecraft.getMinecraft().effectRenderer.addEffect(particle);
		}
	}


	@Inject(method = "handleHealthUpdate", at = @At("HEAD"))
	private void onHealthUpdate(byte id, CallbackInfo ci) {
		if (id == POOP_PARTICLE_ID) {
			this.handlePoopParticles();
		}
	}

	/**
	 * Replaces rotationYawHead with rotationYaw inside attemptToShit(),
	 * so poop direction follows body rotation instead of head direction.
	 */
	@ModifyExpressionValue(
			method = "attemptToShit",
			at = @At(value = "FIELD", target = "Lnet/minecraft/src/EntityWolf;rotationYawHead:F")
	)
	private float useBodyYawInstead(float originalYawHead) {
		EntityWolf wolf = (EntityWolf)(Object)this;
		Entity entity = wolf;
		float average = (wolf.rotationYawHead + entity.rotationYaw) / 2;
		return average; // replace the head rotation with body rotation
	}

}

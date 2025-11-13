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

	@Unique private static final byte POOP_PARTICLE_ID = 9;
	@Unique private static final byte SYNC_YAW_BEFORE_POOP = 10;

	@Inject(method = "attemptToShit", at = @At("RETURN"))
	private void capturePoopPosition(CallbackInfoReturnable<Boolean> cir) {
		EntityWolf wolf = (EntityWolf)(Object)this;
		if (!wolf.worldObj.isRemote && cir.getReturnValue()) {
			wolf.worldObj.setEntityState(wolf, POOP_PARTICLE_ID);
			wolf.worldObj.setEntityState(wolf, SYNC_YAW_BEFORE_POOP);
		}
	}

/*
	// ------------------------------------
	// 💩 Inject into updateShitState — triggers poop event
	// ------------------------------------
	@Inject(method = "updateShitState", at = @At("HEAD"), cancellable = true)
	private void makeShit(CallbackInfo ci) {
		EntityWolf wolf = (EntityWolf)(Object)this;
		if (wolf.worldObj.rand.nextInt(24) == 0) {
			wolf.attemptToShit();
		}
		ci.cancel();
	}
*/

	// ------------------------------------
	// 💨 Client-side poop particle effect
	// ------------------------------------
	@Unique
	@Environment(EnvType.CLIENT)
	public void handlePoopParticles() {
		EntityWolf wolf = (EntityWolf)(Object)this;

		// match the poop item spawn math
		float poopVectorX = MathHelper.sin(wolf.renderYawOffset / 180.0F * (float)Math.PI);
		float poopVectorZ = -MathHelper.cos(wolf.renderYawOffset / 180.0F * (float)Math.PI);
		double baseX = wolf.posX + poopVectorX;
		double baseY = wolf.posY + 0.25;
		double baseZ = wolf.posZ + poopVectorZ;

		// main particle
		EntityFX mainParticle = new EntitySmokeFX(
				wolf.worldObj,
				baseX + wolf.worldObj.rand.nextDouble() * 0.25 - 0.125,
				baseY + wolf.worldObj.rand.nextDouble() * 0.5,
				baseZ + wolf.worldObj.rand.nextDouble() * 0.25 - 0.125,
				0, 0, 0,
				0.33f
		);
		mainParticle.setRBGColorF(0.4f + wolf.rand.nextFloat() * 0.1f, 0.25f + wolf.rand.nextFloat() * 0.05f, 0.1f + wolf.rand.nextFloat() * 0.05f);
		Minecraft.getMinecraft().effectRenderer.addEffect(mainParticle);

		// extra particles
		for (int n = 0; n < 7; ++n) {
			double offsetX = (wolf.rand.nextDouble() - 0.5) * 0.5;
			double offsetY = wolf.rand.nextDouble() * 0.5 + 0.25;
			double offsetZ = (wolf.rand.nextDouble() - 0.5) * 0.5;

			EntityFX particle = new EntitySmokeFX(
					wolf.worldObj,
					baseX + offsetX,
					baseY + offsetY,
					baseZ + offsetZ,
					wolf.rand.nextGaussian() * 0.02,
					wolf.rand.nextGaussian() * 0.02,
					wolf.rand.nextGaussian() * 0.02,
					0.33f
			);

			particle.setRBGColorF(0.4f + wolf.rand.nextFloat() * 0.1f, 0.25f + wolf.rand.nextFloat() * 0.05f, 0.1f + wolf.rand.nextFloat() * 0.05f);
			Minecraft.getMinecraft().effectRenderer.addEffect(particle);
		}
	}

	// ------------------------------------
	// 🛰 Handle packet-triggered state updates
	// ------------------------------------
	@Inject(method = "handleHealthUpdate", at = @At("HEAD"))
	private void onHandleHealthUpdate(byte id, CallbackInfo ci) {
		if (id == POOP_PARTICLE_ID) {
			this.handlePoopParticles();
		}
	}

	// ------------------------------------
	// 🧩 Replace head-based poop vector with body yaw
	// ------------------------------------
	@ModifyExpressionValue(
			method = "attemptToShit",
			at = @At(value = "FIELD", target = "Lnet/minecraft/src/EntityWolf;rotationYawHead:F")
	)
	private float useBodyYaw(float originalHeadYaw) {
		EntityLivingBase wolf = (EntityWolf)(Object)this;
		return wolf.renderYawOffset;
	}

}

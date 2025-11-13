package btw.community.poopcats.mixin;

import btw.community.poopcats.util.PoopCats;
import net.minecraft.src.*;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderOcelot.class)
public abstract class RenderOcelotMixin {

	/**
	 * Apply swelling effect to cats that are about to explode
	 * Similar to how creepers swell before exploding
	 */
	@Inject(method = "preRenderCallback(Lnet/minecraft/src/EntityLivingBase;F)V",
			at = @At("TAIL"))
	private void applyCatSwelling(EntityLivingBase entity, float partialTicks, CallbackInfo ci) {
		if (entity instanceof EntityOcelot cat && entity instanceof PoopCats.PoopCallback callback) {
			int swellTime = callback.cats$getWarningTicks();

			if (swellTime > 0) {
				// Calculate swell progress (0.0 to 1.0)
				float swellProgress = 1.0F - (swellTime / 100.0F);

				// Apply exponential curve for dramatic effect
				swellProgress = swellProgress * swellProgress;

				// Calculate scale with pulsing effect
				float pulse = MathHelper.sin(swellProgress * 50.0F) * swellProgress * 0.02F;
				float scaleXZ = 1.0F + (swellProgress * 0.5F) + pulse; // Expand horizontally
				float scaleY = 1.0F + (swellProgress * 0.3F) - (pulse * 0.5F); // Expand vertically (less)

				// Limit maximum scale
				if (scaleXZ > 1.6F) scaleXZ = 1.6F;
				if (scaleY > 1.4F) scaleY = 1.4F;

				GL11.glScalef(scaleXZ, scaleY, scaleXZ);
			}
		}
	}

	/*
	 * Add red flash effect when cat is about to explode
	 * Similar to creeper charging effect
	 */
	// Replicate getColorMultiplier from RenderCreeper somehow.
}
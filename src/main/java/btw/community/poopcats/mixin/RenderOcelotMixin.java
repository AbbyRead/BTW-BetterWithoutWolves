package btw.community.poopcats.mixin;

import btw.community.poopcats.util.PoopCats;
import net.minecraft.src.*;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderOcelot.class)
public abstract class RenderOcelotMixin extends RenderLiving {

	// Required constructor for extending RenderLiving
	public RenderOcelotMixin(ModelBase par1ModelBase, float par2) {
		super(par1ModelBase, par2);
	}

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
	 * NEW: Add red flash effect when cat is about to explode
	 * Similar to creeper charging effect
	 */

	/**
	 * NEW: Helper method to get interpolated flash intensity
	 */
	@Unique
	private float poopcats$getCatFlashIntensity(EntityOcelot cat, float partialTicks) {
		if (cat instanceof PoopCats.PoopCallback callback) {
			int maxSwell = 30; // From EntityOcelotMixin

			// Interpolate swell time for smooth flashing
			float interpolatedSwell = (float)callback.cats$getLastSwellTime() +
					(float)(callback.cats$getSwellTime() - callback.cats$getLastSwellTime()) * partialTicks;

			// Mimic creeper logic (fuseTime - 2)
			float intensity = interpolatedSwell / (float)(maxSwell - 2);

			if (intensity < 0.0F) intensity = 0.0F;
			if (intensity > 1.0F) intensity = 1.0F;

			// Make it more dramatic, like the creeper
			intensity = intensity * intensity;

			return intensity;
		}
		return 0.0F;
	}

	/**
	 * NEW: This method is added to RenderOcelot by the mixin,
	 * overriding the RenderLiving implementation.
	 */
	protected int getColorMultiplier(EntityLivingBase par1EntityLivingBase, float par2, float par3)
	{
		if (par1EntityLivingBase instanceof EntityOcelot cat) {
			float flashIntensity = this.poopcats$getCatFlashIntensity(cat, par3);

			// Check if flash is active (blinking)
			if (flashIntensity > 0.0F && (int)(flashIntensity * 10.0F) % 2 != 0) {

				// Calculate alpha for the overlay
				int alpha = (int)(flashIntensity * 0.4F * 255.0F); // 0.4F alpha intensity
				if (alpha < 0) alpha = 0;
				if (alpha > 255) alpha = 255;

				short red = 255;
				short green = 100; // Gives a reddish-orange tint
				short blue = 100;

				return alpha << 24 | red << 16 | green << 8 | blue;
			}
		}

		// Return 0 for default (no multiplier),
		// which is the same as RenderLiving.getColorMultiplier
		return 0;
	}
}
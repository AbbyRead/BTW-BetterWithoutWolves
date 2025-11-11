package btw.community.betterwithoutwolves.mixin.client; // Use a 'client' sub-package for clarity

import btw.entity.mob.DireWolfEntity;
import btw.client.render.entity.DireWolfRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// IMPORTANT: This Mixin MUST be in a client-only configuration block in your mixins.json
@Mixin(DireWolfRenderer.class)
public abstract class DireWolfRendererMixin {

	// Target the custom method that handles drawing the glowing eyes.
	@Inject(method = "renderEyes", at = @At("HEAD"), cancellable = true, remap = false)
	private void blockRenderingEyes(DireWolfEntity wolf, int iRenderPass, CallbackInfoReturnable<Integer> cir) {
		// Return -1 to tell the rendering system to skip this render pass,
		// preventing the glowing eye texture from ever being bound or drawn.
		cir.setReturnValue(-1);
	}
}
package btw.community.betterwithoutwolves.mixin;

import btw.entity.mob.DireWolfEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DireWolfEntity.class)
public abstract class MixinDireWolfEntity {

	// Disable all AI to freeze movement and behavior
	@Inject(method = "isAIEnabled", at = @At("HEAD"), cancellable = true, remap = false)
	private void disableAI(CallbackInfoReturnable<Boolean> cir) {
		cir.setReturnValue(false);
	}

	// Silence living sound (growl)
	@Inject(method = "getLivingSound", at = @At("HEAD"), cancellable = true, remap = false)
	private void silenceLivingSound(CallbackInfoReturnable<String> cir) {
		cir.setReturnValue(null);
	}

	// Silence hurt sound (growl)
	@Inject(method = "getHurtSound", at = @At("HEAD"), cancellable = true, remap = false)
	private void silenceHurtSound(CallbackInfoReturnable<String> cir) {
		cir.setReturnValue(null);
	}

	// Silence death sound
	@Inject(method = "getDeathSound", at = @At("HEAD"), cancellable = true, remap = false)
	private void silenceDeathSound(CallbackInfoReturnable<String> cir) {
		cir.setReturnValue(null);
	}

	// Silence step sound
	@Inject(method = "playStepSound", at = @At("HEAD"), cancellable = true, remap = false)
	private void silenceStepSound(int x, int y, int z, int blockId, CallbackInfo ci) {
		ci.cancel();
	}

	// Prevent eating (checks for loose food)
	@Inject(method = "checkForLooseFood", at = @At("HEAD"), cancellable = true, remap = false)
	private void preventEating(CallbackInfo ci) {
		ci.cancel();
	}

	// Disable onLivingUpdate fully (which handles howling countdown and sun-fire damage)
	@Inject(method = "onLivingUpdate", at = @At("HEAD"), cancellable = true, remap = false)
	private void disableUpdate(CallbackInfo ci) {
		// This cancels all logic in the update tick, including sun damage,
		// howl countdowns, and the super.onLivingUpdate() call which would
		// otherwise contain movement.
		ci.cancel();
	}
}
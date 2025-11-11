package btw.community.betterwithoutwolves.mixin;

import btw.entity.mob.DireWolfEntity;
import net.minecraft.src.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// This class has a lot of obfuscated methods for some reason.
// And since the refmap is only for vanilla Minecraft, we have to use
//   the actual obfuscated names these methods end up as, rather than
//   what the official source code has them as pre-compilation.
@Mixin(DireWolfEntity.class)
public abstract class MixinDireWolfEntity {

	// 1. Disable all AI / Freeze movement and behavior
	// Target: isAIEnabled (obfuscated to method_2608)
	@Inject(method = "method_2608", at = @At("HEAD"), cancellable = true, remap = false)
	private void disableAI(CallbackInfoReturnable<Boolean> cir) {
		cir.setReturnValue(false);
	}

	// 2. Silence living sound (growl)
	// Target: getLivingSound (obfuscated to method_2603)
	@Inject(method = "method_2603", at = @At("HEAD"), cancellable = true, remap = false)
	private void silenceLivingSound(CallbackInfoReturnable<String> cir) {
		cir.setReturnValue(null);
	}

	// 3. Silence hurt sound (growl)
	// Target: getHurtSound (obfuscated to method_2604)
	@Inject(method = "method_2604", at = @At("HEAD"), cancellable = true, remap = false)
	private void silenceHurtSound(CallbackInfoReturnable<String> cir) {
		cir.setReturnValue(null);
	}

	// 4. Silence death sound
	// Target: getDeathSound (obfuscated to method_2605)
	@Inject(method = "method_2605", at = @At("HEAD"), cancellable = true, remap = false)
	private void silenceDeathSound(CallbackInfoReturnable<String> cir) {
		cir.setReturnValue(null);
	}

	// 5. Silence step sound
	// Target: playStepSound (obfuscated to method_2494)
	@Inject(method = "method_2494", at = @At("HEAD"), cancellable = true, remap = false)
	private void silenceStepSound(int x, int y, int z, int blockId, CallbackInfo ci) {
		ci.cancel();
	}

	// 6. Prevent eating (checks for loose food)
	// Target: checkForLooseFood (NOT obfuscated, remains 'checkForLooseFood')
	@Inject(method = "checkForLooseFood", at = @At("HEAD"), cancellable = true, remap = false)
	private void preventEating(CallbackInfo ci) {
		ci.cancel();
	}

	// 7. Disable onLivingUpdate fully (sun-fire damage, howling countdown)
	// Target: onLivingUpdate (obfuscated to method_2651)
	@Inject(method = "method_2651", at = @At("HEAD"), cancellable = true, remap = false)
	private void disableUpdate(CallbackInfo ci) {
		ci.cancel();
	}

	// New Injection to force the entity to be invisible every tick
	// Target: onUpdate() (obfuscated to method_2558)
	@Inject(method = "method_2558", at = @At("HEAD"))
	private void makeInvisible(CallbackInfo ci) {
		Entity entity = (Entity)(Object)this;
		entity.setInvisible(true);
	}
}
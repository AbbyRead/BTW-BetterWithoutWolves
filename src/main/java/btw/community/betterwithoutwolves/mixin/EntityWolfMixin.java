package btw.community.betterwithoutwolves.mixin;

import net.minecraft.src.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityWolf.class)
public abstract class EntityWolfMixin {

	// Since EntityWolf doesn't override these:
	// See EntityLivingBaseMixin.java for "canBeCollidedWith"
	// See EntityLivingBaseMixin.java for "canBePushed"

	// Make wolves invisible
	@Inject(method = "onLivingUpdate", at = @At("HEAD"))
	private void makeInvisible(CallbackInfo ci) {
		EntityWolf wolf = (EntityWolf)(Object)this;
		wolf.setInvisible(true);
	}

	// Disable all AI and movement by canceling the update tick
	@Inject(method = "updateAITick", at = @At("HEAD"), cancellable = true)
	private void disableAI(CallbackInfo ci) {
		// Cancel AI updates to freeze the wolf in place
		ci.cancel();
	}

	// Make wolves immortal - block all damage
	@Inject(method = "attackEntityFrom", at = @At("HEAD"), cancellable = true)
	private void preventDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
		cir.setReturnValue(false);
	}

	// Disable collision by setting noClip flag and making incorporeal
	@Inject(method = "onLivingUpdate", at = @At("TAIL"))
	private void makeIncorporeal(CallbackInfo ci) {
		EntityWolf wolf = (EntityWolf)(Object)this;
		// Make wolf pass through blocks and entities
		wolf.noClip = true;
	}

	// Silence all wolf sounds
	@Inject(method = "getLivingSound", at = @At("HEAD"), cancellable = true)
	private void silenceLivingSound(CallbackInfoReturnable<String> cir) {
		cir.setReturnValue(null);
	}

	@Inject(method = "getHurtSound", at = @At("HEAD"), cancellable = true)
	private void silenceHurtSound(CallbackInfoReturnable<String> cir) {
		cir.setReturnValue(null);
	}

	@Inject(method = "getDeathSound", at = @At("HEAD"), cancellable = true)
	private void silenceDeathSound(CallbackInfoReturnable<String> cir) {
		cir.setReturnValue(null);
	}

	@Inject(method = "playStepSound", at = @At("HEAD"), cancellable = true)
	private void silenceStepSound(int x, int y, int z, int blockId, CallbackInfo ci) {
		ci.cancel();
	}

	// Prevent interactions (no petting, feeding, dyeing collars, etc.)
	@Inject(method = "interact", at = @At("HEAD"), cancellable = true)
	private void preventInteraction(EntityPlayer player, CallbackInfoReturnable<Boolean> cir) {
		cir.setReturnValue(false);
	}

	// Disable eating
	@Inject(method = "attemptToEatLooseItem", at = @At("HEAD"), cancellable = true)
	private void preventEating(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
		cir.setReturnValue(false);
	}

	// Disable mating
	@Inject(method = "canMateWith", at = @At("HEAD"), cancellable = true)
	private void preventMating(EntityAnimal other, CallbackInfoReturnable<Boolean> cir) {
		cir.setReturnValue(false);
	}

	// Disable pooping
	@Inject(method = "attemptToShit", at = @At("HEAD"), cancellable = true)
	private void preventPooping(CallbackInfoReturnable<Boolean> cir) {
		cir.setReturnValue(false);
	}

	// Disable howling
	@Inject(method = "notifyOfWolfHowl", at = @At("HEAD"), cancellable = true)
	private void preventHowling(Entity source, CallbackInfo ci) {
		ci.cancel();
	}

	// Disable attacking
	@Inject(method = "attackEntityAsMob", at = @At("HEAD"), cancellable = true)
	private void preventAttacking(Entity target, CallbackInfoReturnable<Boolean> cir) {
		cir.setReturnValue(false);
	}

	// Disable possession mechanics
	@Inject(method = "handlePossession", at = @At("HEAD"), cancellable = true)
	private void preventPossession(CallbackInfo ci) {
		ci.cancel();
	}

	// Prevent transformation to dire wolf
	@Inject(method = "onLivingUpdate", at = @At("HEAD"), cancellable = true)
	private void disableLivingUpdate(CallbackInfo ci) {
		ci.cancel();
	}

}
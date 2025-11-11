package btw.community.betterwithoutwolves.mixin;

import btw.entity.mob.DireWolfEntity;
import net.minecraft.src.DamageSource;
import net.minecraft.src.EntityLivingBase;
import net.minecraft.src.EntityWolf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityLivingBase.class)
public abstract class EntityLivingBaseMixin {
	@Inject(method = "canBeCollidedWith", at = @At("HEAD"), cancellable = true)
	private void disableWolfCollision(CallbackInfoReturnable<Boolean> cir) {
		EntityLivingBase self = (EntityLivingBase) (Object) this;
		if (self instanceof EntityWolf || self instanceof DireWolfEntity) {
			cir.setReturnValue(false);
		}
	}

	@Inject(method = "canBePushed", at = @At("HEAD"), cancellable = true)
	private void disableWolfPushing(CallbackInfoReturnable<Boolean> cir) {
		EntityLivingBase self = (EntityLivingBase) (Object) this;
		if (self instanceof EntityWolf || self instanceof DireWolfEntity) {
			cir.setReturnValue(false);
		}
	}

	@Inject(method = "attackEntityFrom", at = @At("HEAD"), cancellable = true)
	private void preventWolfDamage(DamageSource par1DamageSource, float par2, CallbackInfoReturnable<Boolean> cir) {
		EntityLivingBase self = (EntityLivingBase) (Object) this;
		if (self instanceof EntityWolf || self instanceof DireWolfEntity) {
			cir.setReturnValue(false);
		}
	}
}

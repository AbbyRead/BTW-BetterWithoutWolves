package btw.community.poopcats.mixin;

import btw.community.poopcats.util.PoopCatHelper;
import btw.community.poopcats.util.PoopCatConstants;
import net.minecraft.src.Entity;
import net.minecraft.src.EntityLivingBase;
import net.minecraft.src.EntityOcelot;
import net.minecraft.src.EntityTameable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityTameable.class)
public abstract class EntityTameableMixin {

	@Inject(method = "handleHealthUpdate", at = @At("HEAD"))
	private void handlePoopEvents(byte id, CallbackInfo ci) {
		Entity self = (Entity)(Object)this;

		if (self instanceof EntityOcelot cat) {
			if (id == PoopCatConstants.SYNC_YAW_BEFORE_POOP && self.worldObj.isRemote) {
				// Sync cat's rotation with body orientation
				self.rotationYaw = ((EntityLivingBase)self).renderYawOffset;
			} else if (id == PoopCatConstants.POOP_PARTICLE_ID) {
				PoopCatHelper.handlePoopParticles(cat);
			} else if (id == PoopCatConstants.EXPLOSION_PARTICLE_ID && self.worldObj.isRemote) {
				// Handle explosion particles
				PoopCatHelper.handleExplosionParticles(cat);
			}
		}
	}
}
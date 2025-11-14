package btw.community.poopcats.mixin;

import btw.community.poopcats.util.PoopCats;
import btw.community.poopcats.util.PoopCatsConstants;
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
			if (id == PoopCatsConstants.SYNC_YAW_BEFORE_POOP && self.worldObj.isRemote) {
				// Sync cat's rotation with body orientation
				self.rotationYaw = ((EntityLivingBase)self).renderYawOffset;
			} else if (id == PoopCatsConstants.POOP_PARTICLE_ID) {
				PoopCats.handlePoopParticles(cat);
			} else if (id == PoopCatsConstants.EXPLOSION_PARTICLE_ID && self.worldObj.isRemote) {
				// Handle explosion particles
				PoopCats.handleExplosionParticles(cat);
			}
		}
	}
}
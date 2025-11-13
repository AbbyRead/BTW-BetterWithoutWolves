package btw.community.poopcats.mixin;

import btw.community.poopcats.util.CatParticleHandler;
import btw.community.poopcats.util.PoopCats;
import net.minecraft.src.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static btw.community.poopcats.util.PoopCats.POOP_PARTICLE_ID;

@Mixin(EntityTameable.class)
public abstract class EntityOcelot_EntityTameableMixin {

	@Unique private static final byte POOP_WATCH_ID = 27;
	@Unique private static final byte CAT_SYNC_YAW_BEFORE_POOP = 21;

	@Inject(method = "handleHealthUpdate", at = @At("HEAD"))
	private void handlePoopEvents(byte id, CallbackInfo ci) {
		Entity self = (Entity)(Object)this;

		if (self instanceof EntityOcelot cat) {
			if (id == POOP_WATCH_ID && self.worldObj.isRemote) {
				// Call the particle handler via interface
				if (self instanceof CatParticleHandler) {
					((CatParticleHandler)self).cats$handlePoopParticles();
				}
			} else if (id == CAT_SYNC_YAW_BEFORE_POOP && self.worldObj.isRemote) {
				// Sync cat's rotation with body orientation
				self.rotationYaw = ((EntityLivingBase)self).renderYawOffset;
			} else if (id == POOP_PARTICLE_ID) {
				PoopCats.handlePoopParticles(cat);
			}
		}
	}
}

package btw.community.wolfpoopcosmetics.mixin;

import net.minecraft.src.Entity;
import net.minecraft.src.EntityLivingBase;
import net.minecraft.src.EntityWolf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(Entity.class)
public class EntityMixin {
	@Unique
	private static final byte SYNC_YAW_BEFORE_POOP = 10;

	@Inject(method = "handleHealthUpdate", at = @At("HEAD"))
	private void doTheThing(byte id, CallbackInfo ci) {
		Entity self = (Entity) (Object) this;
		if (self instanceof EntityWolf) {
			if (id == SYNC_YAW_BEFORE_POOP && self.worldObj.isRemote) {
				// Force client’s rotationYaw to match the body orientation sent by server
				self.rotationYaw = ((EntityLivingBase) self).renderYawOffset;
			}
		}
	}
}


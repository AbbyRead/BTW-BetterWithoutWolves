package btw.community.poopcats.mixin;

import net.minecraft.src.EntityOcelot;
import net.minecraft.src.EntityTameable;
import net.minecraft.src.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityTameable.class)
public abstract class EntityTameableMixin {

	@Unique
	private static final byte POOP_STATE_ID = 8; // same as PoopCats

	@Inject(method = "handleHealthUpdate", at = @At("HEAD"))
	private void poop$handleState(byte id, CallbackInfo ci) {
		EntityTameable self = (EntityTameable) (Object) this;
		if (self instanceof EntityOcelot cat && id == POOP_STATE_ID) {
			// Spawn client-side smoke particles
			float dx = MathHelper.sin(cat.rotationYawHead / 180.0f * (float)Math.PI);
			float dz = -MathHelper.cos(cat.rotationYawHead / 180.0f * (float)Math.PI);

			for (int n = 0; n < 5; ++n) {
				double px = cat.posX + (dx * 0.5f) + cat.worldObj.rand.nextDouble() * 0.25;
				double py = cat.posY + cat.worldObj.rand.nextDouble() * 0.5 + 0.25;
				double pz = cat.posZ + (dz * 0.5f) + cat.worldObj.rand.nextDouble() * 0.25;
				cat.worldObj.spawnParticle("smoke", px, py, pz, 0.0, 0.0, 0.0);
			}
		}
	}
}


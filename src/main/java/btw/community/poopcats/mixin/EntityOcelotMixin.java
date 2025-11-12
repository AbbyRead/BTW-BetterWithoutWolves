package btw.community.poopcats.mixin;

import btw.community.poopcats.util.PoopCats;
import net.minecraft.src.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityOcelot.class)
public abstract class EntityOcelotMixin {

	@Inject(method = "updateAITick", at = @At("TAIL"))
	private void cats$maybePoop(CallbackInfo ci) {
		EntityOcelot cat = (EntityOcelot)(Object)this;
		PoopCats.maybePoop(cat, cat.worldObj, cat.rotationYawHead, false);
	}

}

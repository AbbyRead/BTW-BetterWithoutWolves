package btw.community.poopcats.mixin;

import net.minecraft.src.EntityLivingBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(EntityLivingBase.class)
public interface EntityLivingBaseInvoker {
	@Invoker("getSoundVolume")
	float invokeGetSoundVolume();
}

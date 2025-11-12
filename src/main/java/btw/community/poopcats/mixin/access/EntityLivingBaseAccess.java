package btw.community.poopcats.mixin.access;

import net.minecraft.src.EntityLivingBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(EntityLivingBase.class)
public interface EntityLivingBaseAccess {
	@Invoker("getSoundVolume")
	float invokeGetSoundVolume();
}

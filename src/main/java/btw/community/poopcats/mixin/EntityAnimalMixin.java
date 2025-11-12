package btw.community.poopcats.mixin;

import btw.community.poopcats.util.PoopCats;
import net.minecraft.src.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityAnimal.class)
public abstract class EntityAnimalMixin {

	/**
	 * Clear fed state when any animal dies.
	 * This catches ocelots (cats) when they die.
	 * We inject into the parent class's onDeath method since EntityOcelot doesn't override it.
	 */
	@Inject(method = "onDeath", at = @At("HEAD"))
	private void cats$clearPoopStateOnDeath(DamageSource source, CallbackInfo ci) {
		EntityAnimal animal = (EntityAnimal)(Object)this;

		// Only handle ocelots/cats
		if (animal instanceof EntityOcelot && animal instanceof PoopCats.PoopCallback callback) {
			callback.cats$setIsFed(false);
			callback.cats$setWarningTicks(0);
		}
	}
}
package btw.community.poopcats.mixin;

import btw.client.fx.BTWEffectManager;
import btw.community.poopcats.mixin.access.EntityAccess;
import btw.community.poopcats.util.PoopCats;
import net.minecraft.src.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityOcelot.class)
// IMPLEMENTATION CHANGE: Implement the EntityAccessor interface
public abstract class EntityOcelotMixin implements PoopCats.PoopCallback, EntityAccess {

	// Define a new, unused DataWatcher ID for our "fed" state
	@Unique	private static final int POOP_DATA_WATCHER_ID = 30;

	@Inject(method = "getLivingSound", at = @At("HEAD"), cancellable = true)
	private void cats$noMeow(CallbackInfoReturnable<String> cir) {
		String soundToReturn;
		EntityOcelot cat = (EntityOcelot) (Object) this;
		soundToReturn = cat.isTamed() ? (cat.isInLove() ? "mob.cat.purr" : (cat.rand.nextInt(4) == 0 ? "mob.witch.death1" : "mob.cat.purr")) : "";
		if (soundToReturn.equals("mob.witch.death1")) {
			// Something to play that specific sound
		}
		cir.setReturnValue(soundToReturn);
	}

	// Inject into entityInit to add our DataWatcher
	@Inject(method = "entityInit", at = @At("TAIL"))
	private void cats$addPoopDataWatcher(CallbackInfo ci) {
		// CHANGE: Use the accessor method getDataWatcher()
		getDataWatcher().addObject(POOP_DATA_WATCHER_ID, (byte) 0); // 0 = not fed, 1 = fed
	}

	// Inject into NBT writing to save our "fed" state
	@Inject(method = "writeEntityToNBT", at = @At("TAIL"))
	private void cats$writePoopNBT(NBTTagCompound nbt, CallbackInfo ci) {
		nbt.setBoolean("IsCatFed", cats$isFed());
	}

	// Inject into NBT reading to load our "fed" state
	@Inject(method = "readEntityFromNBT", at = @At("TAIL"))
	private void cats$readPoopNBT(NBTTagCompound nbt, CallbackInfo ci) {
		if (nbt.hasKey("IsCatFed")) {
			cats$setIsFed(nbt.getBoolean("IsCatFed"));
		}
	}

	/**
	 * Main pooping logic, called every tick.
	 * Now only checks for pooping if the cat is actually "fed".
	 */
	@Inject(method = "updateAITick", at = @At("TAIL"))
	private void cats$updatePoopLogic(CallbackInfo ci) {
		EntityOcelot cat = (EntityOcelot)(Object)this;

		// Only run for tamed cats on the server
		if (cat.worldObj.isRemote || !cat.isTamed()) {
			return;
		}

		// Only run the poop check IF the cat is fed
		if (cats$isFed()) {
			// Pass "this" (the mixin instance) to PoopCats so it can call us back
			PoopCats.maybePoop(cat, cat.worldObj, cat.rotationYawHead, false, this);
		}
	}

	/**
	 * This is the new feeding logic. It intercepts the "interact" call.
	 */
	@Inject(method = "interact", at = @At("HEAD"), cancellable = true)
	private void cats$handleFeeding(EntityPlayer player, CallbackInfoReturnable<Boolean> cir) {
		EntityOcelot cat = (EntityOcelot)(Object)this;
		ItemStack stack = player.inventory.getCurrentItem();

		// Check if player is interacting with a tamed cat using fish
		if (cat.isTamed() && stack != null && cat.isBreedingItem(stack)) {
			// If the cat is NOT fed, we feed it.
			if (!cats$isFed()) {
				// Consume the item (if not in creative)
				if (!player.capabilities.isCreativeMode) {
					--stack.stackSize;
					if (stack.stackSize <= 0) {
						player.inventory.setInventorySlotContents(player.inventory.currentItem, null);
					}
				}

				// Set the cat's state to "fed"
				cats$setIsFed(true);
				// Heal the cat (like wolves) - 5.0F is 2.5 hearts
				cat.heal(5.0F);

				// Play burp sound
				cat.worldObj.playAuxSFX(BTWEffectManager.BURP_SOUND_EFFECT_ID,
						MathHelper.floor_double(cat.posX), MathHelper.floor_double(cat.posY),
						MathHelper.floor_double(cat.posZ), 0);

				// Cancel the original interact call so breeding doesn't happen
				cir.setReturnValue(true);
				return;
			}
			// If the cat IS already fed, we do nothing.
			// The inject will finish, and the original "interact" method will run,
			// which will correctly trigger the breeding logic.
		}
	}

	// --- Helper methods for the DataWatcher and Interface ---

	// This is the implementation of PoopCats.PoopCallback
	@Override
	public void cats$setIsFed(boolean fed) {
		// CHANGE: Use the accessor method getDataWatcher()
		getDataWatcher().updateObject(POOP_DATA_WATCHER_ID, (byte) (fed ? 1 : 0));
	}

	// This is a helper to check the "fed" state
	@Unique
	public boolean cats$isFed() {
		// CHANGE: Use the accessor method getDataWatcher()
		return getDataWatcher().getWatchableObjectByte(POOP_DATA_WATCHER_ID) == 1;
	}
}
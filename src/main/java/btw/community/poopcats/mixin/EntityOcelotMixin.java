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

import static btw.community.poopcats.util.PoopCats.POOP_STATE_ID;

@Mixin(EntityOcelot.class)
public abstract class EntityOcelotMixin implements PoopCats.PoopCallback, EntityAccess {

	@Unique private static final int POOP_DATA_WATCHER_ID = 19;
	@Unique private static final int WARNING_DATA_WATCHER_ID = 20;

	@Unique private static final float HEALING_AMOUNT = 5.0F;
	@Unique private static final String NBT_FED_KEY = "IsCatFed";
	@Unique private static final String NBT_WARNING_KEY = "PoopWarningTicks";
	@Unique private static final int WARNING_DURATION = 100; // 5 seconds

	@Inject(method = "getLivingSound", at = @At("HEAD"), cancellable = true)
	private void cats$silenceFedCat(CallbackInfoReturnable<String> cir) {
		EntityOcelot cat = (EntityOcelot)(Object)this;

		if (cats$isFed()) {
			String sound = cat.isTamed()
					? (cat.isInLove() ? "mob.cat.purr"
					: (cat.rand.nextInt(4) == 0 ? "mob.cat.purreow" : "mob.cat.purr"))
					: "";
			cir.setReturnValue(sound);
		}

		// Otherwise, let vanilla logic handle the normal meow/purr
	}

	@Inject(method = "entityInit", at = @At("TAIL"))
	private void cats$addPoopDataWatcher(CallbackInfo ci) {
		getDataWatcher().addObject(POOP_DATA_WATCHER_ID, (byte) 0); // 0 = not fed, 1 = fed
		getDataWatcher().addObject(WARNING_DATA_WATCHER_ID, 0); // Warning countdown timer
	}

	@Inject(method = "writeEntityToNBT", at = @At("TAIL"))
	private void cats$writePoopNBT(NBTTagCompound nbt, CallbackInfo ci) {
		nbt.setBoolean(NBT_FED_KEY, cats$isFed());
		nbt.setInteger(NBT_WARNING_KEY, cats$getWarningTicks());
	}

	@Inject(method = "readEntityFromNBT", at = @At("TAIL"))
	private void cats$readPoopNBT(NBTTagCompound nbt, CallbackInfo ci) {
		if (nbt.hasKey(NBT_FED_KEY)) {
			cats$setIsFed(nbt.getBoolean(NBT_FED_KEY));
		}
		if (nbt.hasKey(NBT_WARNING_KEY)) {
			cats$setWarningTicks(nbt.getInteger(NBT_WARNING_KEY));
		}
	}

	/**
	 * Main pooping logic, called every tick.
	 * Handles both pooping checks and warning countdown.
	 */
	@Inject(method = "updateAITick", at = @At("TAIL"))
	private void cats$updatePoopLogic(CallbackInfo ci) {
		EntityOcelot cat = (EntityOcelot)(Object)this;

		// Only run for tamed cats on the server
		if (cat.worldObj.isRemote || !cat.isTamed()) {
			return;
		}

		// --- Handle warning countdown (Creeper-style fuse) ---
		int warningTicks = cats$getWarningTicks();
		if (warningTicks > 0) {
			int nextTicks = warningTicks - 1;
			cats$setWarningTicks(nextTicks);

			// Every 20 ticks (≈1s) hiss and flash, only for first 80 ticks
			if (warningTicks % 20 == 0 && warningTicks > 20) {
				// Visual fuse effect
				cat.worldObj.spawnParticle("reddust",
						cat.posX, cat.posY + 0.5, cat.posZ,
						1.0, 0.0, 0.0);

				// Continuous but spaced hiss
				cat.worldObj.playSoundAtEntity(cat,
						PoopCats.CAT_WARNING_SOUND.sound(),
						1.0F, 0.8F);
			}

			// Last red blink right before explosion
			if (nextTicks == 10) {
				cat.worldObj.spawnParticle("reddust",
						cat.posX, cat.posY + 0.5, cat.posZ,
						1.0, 0.0, 0.0);
			}

			// Trigger explosion when timer finishes
			if (nextTicks <= 0) {
				PoopCats.handleWarningExpired(cat, cat.worldObj, cat.rotationYawHead, this);
				return;
			}
		}


		// --- Regular poop logic if fed ---
		if (cats$isFed()) {
			PoopCats.maybePoop(cat, cat.worldObj, cat.rotationYawHead, this);
		}
	}

	@Inject(method = "interact", at = @At("HEAD"), cancellable = true)
	private void cats$handleFeeding(EntityPlayer player, CallbackInfoReturnable<Boolean> cir) {
		EntityOcelot cat = (EntityOcelot)(Object)this;
		ItemStack stack = player.inventory.getCurrentItem();

		// Check if player is interacting with a tamed cat using fish
		if (cat.isTamed() && stack != null && cat.isBreedingItem(stack)) {

			// If the cat is NOT fed, we feed it
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

				// Heal the cat
				cat.heal(HEALING_AMOUNT);

				// Play burp sound
				cat.worldObj.playAuxSFX(BTWEffectManager.BURP_SOUND_EFFECT_ID,
						MathHelper.floor_double(cat.posX),
						MathHelper.floor_double(cat.posY),
						MathHelper.floor_double(cat.posZ), 0);

				// Visual feedback - happy particles
				for (int i = 0; i < 7; ++i) {
					double px = cat.posX + (cat.worldObj.rand.nextDouble() - 0.5) * 0.5;
					double py = cat.posY + cat.worldObj.rand.nextDouble() * 0.5 + 0.5;
					double pz = cat.posZ + (cat.worldObj.rand.nextDouble() - 0.5) * 0.5;
					cat.worldObj.spawnParticle("heart", px, py, pz, 0.0, 0.0, 0.0);
				}

				// Cancel the original interact call so breeding doesn't happen
				cir.setReturnValue(true);
				return;
			}

			// If the cat IS already fed, let vanilla breeding logic handle it
			// (both cats need to be in love mode from being fed fish)
		}
	}

	// --- Implementation of PoopCats.PoopCallback ---

	@Override
	public void cats$setIsFed(boolean fed) {
		getDataWatcher().updateObject(POOP_DATA_WATCHER_ID, (byte)(fed ? 1 : 0));
	}

	@Override
	public void cats$setWarningTicks(int ticks) {
		getDataWatcher().updateObject(WARNING_DATA_WATCHER_ID, ticks);
	}

	@Unique
	public boolean cats$isFed() {
		return getDataWatcher().getWatchableObjectByte(POOP_DATA_WATCHER_ID) == 1;
	}

	@Unique
	public int cats$getWarningTicks() {
		return getDataWatcher().getWatchableObjectInt(WARNING_DATA_WATCHER_ID);
	}

	@Unique
	private static int cats$getWarningDuration() {
		return WARNING_DURATION;
	}

}

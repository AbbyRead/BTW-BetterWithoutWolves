package btw.community.poopcats.mixin;

import btw.community.poopcats.interfaces.PoopCatStateAccess;
import btw.community.poopcats.mixin.access.EntityAccess;
import btw.community.poopcats.mixin.access.EntityLivingAccess;
import btw.community.poopcats.util.PoopCatAISandSeek;
import btw.community.poopcats.util.PoopCatAISwell;
import btw.community.poopcats.util.PoopCatHelper;
import btw.community.poopcats.util.PoopCatConstants;
import net.minecraft.src.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityOcelot.class)
public abstract class EntityOcelotMixin implements PoopCatStateAccess, EntityAccess {

	@Unique private int lastSwellTime;

	// --- Mixin Injections ---

	@Inject(method = "<init>", at = @At("TAIL"))
	private void cats$addWarningAI(CallbackInfo ci) {
		EntityOcelot cat = (EntityOcelot)(Object)this;
		EntityLivingAccess access = (EntityLivingAccess) cat;
		access.getTasks().addTask(2, new PoopCatAISandSeek(cat, 1.2D));
		access.getTasks().addTask(3, new PoopCatAISwell(cat, 6.0D));
	}

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
	}

	@Inject(method = "entityInit", at = @At("TAIL"))
	private void cats$addPoopDataWatcher(CallbackInfo ci) {
		getDataWatcher().addObject(PoopCatConstants.POOP_DATA_WATCHER_ID, (byte) 0);
		getDataWatcher().addObject(PoopCatConstants.WARNING_DATA_WATCHER_ID, 0);
		getDataWatcher().addObject(PoopCatConstants.SWELL_DATA_WATCHER_ID, 0);
	}

	@Inject(method = "writeEntityToNBT", at = @At("TAIL"))
	private void cats$writePoopNBT(NBTTagCompound nbt, CallbackInfo ci) {
		nbt.setBoolean(PoopCatConstants.NBT_FED_KEY, cats$isFed());
		nbt.setInteger(PoopCatConstants.NBT_WARNING_KEY, cats$getWarningTicks());
		nbt.setInteger(PoopCatConstants.NBT_SWELL_KEY, cats$getSwellTime());
	}

	@Inject(method = "readEntityFromNBT", at = @At("TAIL"))
	private void cats$readPoopNBT(NBTTagCompound nbt, CallbackInfo ci) {
		if (nbt.hasKey(PoopCatConstants.NBT_FED_KEY)) {
			cats$setIsFed(nbt.getBoolean(PoopCatConstants.NBT_FED_KEY));
		}
		if (nbt.hasKey(PoopCatConstants.NBT_WARNING_KEY)) {
			cats$setWarningTicks(nbt.getInteger(PoopCatConstants.NBT_WARNING_KEY));
		}
		if (nbt.hasKey(PoopCatConstants.NBT_SWELL_KEY)) {
			int swellTime = nbt.getInteger(PoopCatConstants.NBT_SWELL_KEY);
			cats$setSwellTime(swellTime);
			this.lastSwellTime = swellTime;
		}
	}

	@Inject(method = "updateAITick", at = @At("TAIL"))
	private void cats$updatePoopLogic(CallbackInfo ci) {
		// Delegate all logic to the main PoopCats class
		PoopCatHelper.updateCatLogic((EntityOcelot)(Object)this);
	}

	@Inject(method = "interact", at = @At("HEAD"), cancellable = true)
	private void cats$handleFeeding(EntityPlayer player, CallbackInfoReturnable<Boolean> cir) {
		// Delegate all logic to the main PoopCats class
		if (PoopCatHelper.handleCatInteraction((EntityOcelot)(Object)this, player, this)) {
			cir.setReturnValue(true);
		}
	}

	// --- PoopCallback Interface Implementation ---

	@Override
	public boolean cats$isFed() {
		return getDataWatcher().getWatchableObjectByte(PoopCatConstants.POOP_DATA_WATCHER_ID) == 1;
	}

	@Override
	public void cats$setIsFed(boolean fed) {
		getDataWatcher().updateObject(PoopCatConstants.POOP_DATA_WATCHER_ID, (byte)(fed ? 1 : 0));
	}

	@Override
	public int cats$getWarningTicks() {
		return getDataWatcher().getWatchableObjectInt(PoopCatConstants.WARNING_DATA_WATCHER_ID);
	}

	@Override
	public void cats$setWarningTicks(int ticks) {
		getDataWatcher().updateObject(PoopCatConstants.WARNING_DATA_WATCHER_ID, ticks);
	}

	@Override
	public int cats$getSwellTime() {
		return getDataWatcher().getWatchableObjectInt(PoopCatConstants.SWELL_DATA_WATCHER_ID);
	}

	@Override
	public void cats$setSwellTime(int time) {
		getDataWatcher().updateObject(PoopCatConstants.SWELL_DATA_WATCHER_ID, time);
	}

	@Override
	public int cats$getLastSwellTime() {
		return this.lastSwellTime;
	}

	@Override
	public void cats$setLastSwellTime(int time) {
		this.lastSwellTime = time;
	}
}
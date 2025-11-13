package btw.community.poopcats.mixin;

import btw.client.fx.BTWEffectManager;
import btw.community.poopcats.mixin.access.EntityAccess;
import btw.community.poopcats.mixin.access.EntityLivingAccess;
import btw.community.poopcats.util.CatParticleHandler;
import btw.community.poopcats.util.EntityAIOcelotSwell;
import btw.community.poopcats.util.PoopCats;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.src.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityOcelot.class)
public abstract class EntityOcelotMixin implements PoopCats.PoopCallback, CatParticleHandler, EntityAccess {

	@Unique private static final byte POOP_WATCH_ID = 27;
	@Unique private static final byte WARNING_DATA_WATCHER_ID = 20;

	@Unique private static final float HEALING_AMOUNT = 5.0F;
	@Unique private static final String NBT_FED_KEY = "IsCatFed";
	@Unique private static final String NBT_WARNING_KEY = "PoopWarningTicks";

	@Inject(method = "<init>", at = @At("TAIL"))
	private void cats$addWarningAI(CallbackInfo ci) {
		EntityOcelot cat = (EntityOcelot)(Object)this;
		EntityLivingAccess access = (EntityLivingAccess) cat;
		access.getTasks().addTask(3, new EntityAIOcelotSwell(cat, 6.0D)); // 6 blocks warning radius
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
		getDataWatcher().addObject(POOP_WATCH_ID, (byte) 0);
		getDataWatcher().addObject(WARNING_DATA_WATCHER_ID, 0);
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

	@Inject(method = "updateAITick", at = @At("TAIL"))
	private void cats$updatePoopLogic(CallbackInfo ci) {
		EntityOcelot cat = (EntityOcelot)(Object)this;

		if (!cat.isTamed()) {
			return;
		}

		int warningTicks = cats$getWarningTicks();
		if (warningTicks > 0) {
			int nextTicks = warningTicks - 1;
			cats$setWarningTicks(nextTicks);

			if (warningTicks % 20 == 0 && warningTicks > 20) {
				cat.worldObj.spawnParticle("reddust",
						cat.posX, cat.posY + 0.5, cat.posZ,
						1.0, 0.0, 0.0);

				cat.worldObj.playSoundAtEntity(cat,
						PoopCats.CAT_WARNING_SOUND.sound(),
						1.0F, 0.8F);
			}

			if (nextTicks == 10) {
				cat.worldObj.spawnParticle("reddust",
						cat.posX, cat.posY + 0.5, cat.posZ,
						1.0, 0.0, 0.0);
			}

			if (nextTicks <= 0) {
				PoopCats.handleWarningExpired(cat, cat.worldObj, cat.renderYawOffset, this);
				return;
			}
		}

		if (cats$isFed()) {
			PoopCats.maybePoop(cat, cat.worldObj, cat.renderYawOffset, this);
		}
	}

	@Inject(method = "interact", at = @At("HEAD"), cancellable = true)
	private void cats$handleFeeding(EntityPlayer player, CallbackInfoReturnable<Boolean> cir) {
		EntityOcelot cat = (EntityOcelot)(Object)this;
		ItemStack stack = player.inventory.getCurrentItem();

		if (cat.isTamed() && stack != null && cat.isBreedingItem(stack)) {
			if (!cats$isFed()) {
				if (!player.capabilities.isCreativeMode) {
					--stack.stackSize;
					if (stack.stackSize <= 0) {
						player.inventory.setInventorySlotContents(player.inventory.currentItem, null);
					}
				}

				cats$setIsFed(true);
				cat.heal(HEALING_AMOUNT);

				cat.worldObj.playAuxSFX(BTWEffectManager.BURP_SOUND_EFFECT_ID,
						MathHelper.floor_double(cat.posX),
						MathHelper.floor_double(cat.posY),
						MathHelper.floor_double(cat.posZ), 0);

				for (int i = 0; i < 7; ++i) {
					double px = cat.posX + (cat.worldObj.rand.nextDouble() - 0.5) * 0.5;
					double py = cat.posY + cat.worldObj.rand.nextDouble() * 0.5 + 0.5;
					double pz = cat.posZ + (cat.worldObj.rand.nextDouble() - 0.5) * 0.5;
					cat.worldObj.spawnParticle("heart", px, py, pz, 0.0, 0.0, 0.0);
				}

				cir.setReturnValue(true);
			}
		}
	}

	// Client-side particle effect
	// Implement CatParticleHandler interface
	@Unique
	@Environment(EnvType.CLIENT)
	public void cats$handlePoopParticles() {
		EntityOcelot cat = (EntityOcelot)(Object)this;

		float poopVectorX = MathHelper.sin(cat.renderYawOffset / 180.0F * (float)Math.PI);
		float poopVectorZ = -MathHelper.cos(cat.renderYawOffset / 180.0F * (float)Math.PI);
		double baseX = cat.posX + poopVectorX;
		double baseY = cat.posY + 0.25;
		double baseZ = cat.posZ + poopVectorZ;

		EntityFX mainParticle = new EntitySmokeFX(
				cat.worldObj,
				baseX + cat.worldObj.rand.nextDouble() * 0.25 - 0.125,
				baseY + cat.worldObj.rand.nextDouble() * 0.5,
				baseZ + cat.worldObj.rand.nextDouble() * 0.25 - 0.125,
				0, 0, 0,
				0.33f
		);
		mainParticle.setRBGColorF(0.4f + cat.rand.nextFloat() * 0.1f, 0.25f + cat.rand.nextFloat() * 0.05f, 0.1f + cat.rand.nextFloat() * 0.05f);
		Minecraft.getMinecraft().effectRenderer.addEffect(mainParticle);

		for (int n = 0; n < 7; ++n) {
			double offsetX = (cat.rand.nextDouble() - 0.5) * 0.5;
			double offsetY = cat.rand.nextDouble() * 0.5 + 0.25;
			double offsetZ = (cat.rand.nextDouble() - 0.5) * 0.5;

			EntityFX particle = new EntitySmokeFX(
					cat.worldObj,
					baseX + offsetX,
					baseY + offsetY,
					baseZ + offsetZ,
					cat.rand.nextGaussian() * 0.02,
					cat.rand.nextGaussian() * 0.02,
					cat.rand.nextGaussian() * 0.02,
					0.33f
			);

			particle.setRBGColorF(0.4f + cat.rand.nextFloat() * 0.1f, 0.25f + cat.rand.nextFloat() * 0.05f, 0.1f + cat.rand.nextFloat() * 0.05f);
			Minecraft.getMinecraft().effectRenderer.addEffect(particle);
		}
	}

	// Client-side particle handler moved to EntityTameableMixin

	@Override
	public void cats$setIsFed(boolean fed) {
		getDataWatcher().updateObject(POOP_WATCH_ID, (byte)(fed ? 1 : 0));
	}

	@Override
	public void cats$setWarningTicks(int ticks) {
		getDataWatcher().updateObject(WARNING_DATA_WATCHER_ID, ticks);
	}

	@Unique
	public boolean cats$isFed() {
		return getDataWatcher().getWatchableObjectByte(POOP_WATCH_ID) == 1;
	}

	@Unique
	public int cats$getWarningTicks() {
		return getDataWatcher().getWatchableObjectInt(WARNING_DATA_WATCHER_ID);
	}
}
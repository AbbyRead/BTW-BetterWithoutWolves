package btw.community.poopcats.util;

// This class holds all magic numbers, NBT keys, and IDs for the mod.
public class PoopCatConstants {

	// --- Data Watcher IDs ---
	// From EntityOcelotMixin
	public static final byte POOP_DATA_WATCHER_ID = 27;
	public static final byte WARNING_DATA_WATCHER_ID = 20;
	public static final byte SWELL_DATA_WATCHER_ID = 24;

	// --- NBT Keys ---
	// From EntityOcelotMixin
	public static final String NBT_FED_KEY = "IsCatFed";
	public static final String NBT_WARNING_KEY = "PoopWarningTicks";
	public static final String NBT_SWELL_KEY = "SwellTime";

	// --- Entity State IDs (for particles/sync) ---
	// From PoopCats & EntityOcelot_EntityTameableMixin
	public static final byte POOP_PARTICLE_ID = 9;
	public static final byte SYNC_YAW_BEFORE_POOP = 21;
	public static final byte EXPLOSION_PARTICLE_ID = 35;

	// --- Logic & Tuning ---
	// From EntityOcelotMixin
	public static final float HEALING_AMOUNT = 5.0F;

	// From PoopCats
	public static final int BASE_POOP_RATE = 60;
	public static final int DARK_MULTIPLIER = 2;
	public static final float EXPLOSION_POWER = 1.0F;
	public static final int WARNING_DURATION = 100;

	// From EntityAIOcelotSwell / EntityOcelotMixin
	public static final int MAX_SWELL_TIME = 30;
}
package btw.community.poopcats.interfaces;

/**
 * Interface implemented by EntityOcelotMixin to provide
 * access to mod-added data and methods.
 */
public interface PoopCallback {
	boolean cats$isFed();
	void cats$setIsFed(boolean fed);

	int cats$getWarningTicks();
	void cats$setWarningTicks(int ticks);

	int cats$getSwellTime();
	void cats$setSwellTime(int time);

	int cats$getLastSwellTime();
	void cats$setLastSwellTime(int time);
}
package btw.community.betterwithoutwolves;

import btw.AddonHandler;
import btw.BTWAddon;

public class BetterWithoutWolvesAddon extends BTWAddon {

	private static BetterWithoutWolvesAddon instance;

	public BetterWithoutWolvesAddon() {
		super();
		instance = this;
	}

	public static BetterWithoutWolvesAddon getInstance() {
		return instance;
	}

	@Override
	public void initialize() {
		AddonHandler.logMessage(this.getName() + " Version " + this.getVersionString() + " Initializing...");
	}

}
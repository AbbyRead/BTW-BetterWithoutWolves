package btw.community.wolfpoopcosmetics;

import btw.AddonHandler;
import btw.BTWAddon;

public class WolfPoopCosmeticsAddon extends BTWAddon {

	private static WolfPoopCosmeticsAddon instance;

	public WolfPoopCosmeticsAddon() {
		super();
		instance = this;
	}

	public static WolfPoopCosmeticsAddon getInstance() {
		return instance;
	}

	@Override
	public void initialize() {
		AddonHandler.logMessage(this.getName() + " Version " + this.getVersionString() + " Initializing...");
	}

}
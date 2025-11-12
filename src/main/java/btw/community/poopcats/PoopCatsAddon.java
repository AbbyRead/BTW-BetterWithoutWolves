package btw.community.poopcats;

import btw.AddonHandler;
import btw.BTWAddon;

public class PoopCatsAddon extends BTWAddon {

	private static PoopCatsAddon instance;

	public PoopCatsAddon() {
		super();
		instance = this;
	}

	public static PoopCatsAddon getInstance() {
		return instance;
	}

	@Override
	public void initialize() {
		AddonHandler.logMessage(this.getName() + " Version " + this.getVersionString() + " Initializing...");
	}

}
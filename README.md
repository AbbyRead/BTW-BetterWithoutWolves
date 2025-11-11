# Better Without Wolves

**A reversible BTW CE addon that makes wolves completely disappear from the game without actually removing them.**

This Better Than Wolves CE addon makes it *seem* like wolves are gone from the game (without actually removing them). Wolves and Dire Wolves become paralyzed, invisible, immobile, immortal, incorporeal, and silent while the addon is installed. They do not eat, they do not poop, they do not howl, they cannot breed, and they cannot transform into dire wolves.

**Install or uninstall this addon to essentially toggle wolves on and off.**

---

## Features

When this addon is active, wolves and dire wolves are:

- ✓ **Invisible** - Cannot be seen by players (including the dire wolf's glowing eyes).
- ✓ **Immobile** - Frozen in place, no AI or movement.
- ✓ **Immortal** - Cannot take damage or die from any source.
- ✓ **Incorporeal** - No collision, you walk right through them.
- ✓ **Silent** - No barks, growls, howls, or footstep sounds.
- ✓ **Non-interactive** - Cannot be fed, petted, or dyed.
- ✓ **Sterile** - Cannot breed or produce offspring.
- ✓ **Inert** - No eating, pooping, transformation to dire wolves, dire wolf sun-fire damage, or howling countdowns.
- ✓ **Possession Effect Halted** - Truly nothing to worry about.

**Most importantly:** Wolves remain as valid entities in your save file. Uninstall the addon and they instantly return to normal behavior!

---

## Technical Details

This addon demonstrates advanced BTW modding techniques:

### Non-Destructive Approach
- Uses Mixin injections to modify behavior without replacing classes.
- Wolves remain in world save data, just inactive.
- 100% reversible - no data corruption or entity deletion.

### What Gets Disabled
All wolf behaviors are frozen:
- Core AI (movement, pathfinding, targeting)
- Interactions (feeding, taming, collar dyeing)
- Sounds (barking, growling, howling, footsteps)
- Physical presence (collision, pushing, damage)
- Biological needs (eating, hunger, mating, pooping)
- BTW-specific mechanics (possession, beast transformation, etc.)

---

## Installation

1. Build the addon or download the latest release.
2. Place `Better-Without-Wolves-<version>.jar` in your `mods/` folder.
3. Launch your copy of Minecraft 1.6.4 with BTW CE 3.0.0 and Legacy Fabric.  See the [BTW CE 3.0.0 Installation Guide](https://wiki.btwce.com/view/Installation) for those details if you haven't already.

**To restore wolves:** Simply remove the mod and restart the game!

---

## Project Structure

```
src/main/
├── java/btw/community/betterwithoutwolves/
│   ├── BetterWithoutWolvesAddon.java        # Addon entry point
│   └── mixin/
│       ├── MixinDireWolfEntity.java         # Core beast behavior hooks
│       ├── MixinEntityLivingBase.java       # Inherited behavior hooks
│       ├── MixinEntityWolf.java             # Core wolf behavior hooks
│       └── MixinDireWolfRenderer.java       # To hide beasts' glowing eyes
└── resources/
    ├── assets/betterwithoutwolves/icon.png  # Addon thumbnail
    ├── betterwithoutwolves.mixins.json      # Mixin configuration
    └── fabric.mod.json                      # Mod metadata
```

---

## Building from Source

Requirements:
- Java 17
- BTW CE 3.0.0 Intermediary Distribution (get from the Pinned section of [#learn-modding](https://discord.com/channels/252863009590870017/1222644129696059392/) on the BTW CE Discord channel)

```bash
# Clone the repository
git clone [https://github.com/AbbyRead/BTW-BetterWithoutWolves.git](https://github.com/AbbyRead/BTW-BetterWithoutWolves.git)
cd BTW-BetterWithoutWolves

# Build the mod
Drag&Drop the BTW intermediary .zip file onto the install.bat
Wait till it fully finishes
Run the gradle task "build": ./gradlew build

# Output: build/libs/Better-Without-Wolves-<version>.jar
```

---

## Compatibility

- **BTW CE Version:** 3.0.0
- **Java:** 17 or higher

**Cross-Addon Compatibility:** This addon is designed for high compatibility, as it doesn't replace wolf classes or remove entities. It uses Mixin `HEAD` injections to preemptively cancel specific behaviors.

This approach should work alongside most other mods. However, conflicts could arise if another addon attempts to modify the *exact same* methods. In cases where another mod also alters wolf behavior, this addon's disabling effects will likely take priority.

---

## Links

- [BTW Gradle Fabric Example](https://github.com/BTW-Community/BTW-gradle-fabric-example)
- [Legacy Fabric Wiki](https://fabricmc.net/wiki/)
- [BTW CE Wiki](https://wiki.btwce.com/)
- [BTW CE Discord](https://discord.btwce.com/)
- [GitHub Repository](https://github.com/AbbyRead/BTW-BetterWithoutWolves)

---

## License

Released under the **0BSD** license.
Free to use, copy, and modify — attribution appreciated but not required.

---

## Credits

Created by **Abigail Read**
Special thanks to the BTW CE community for documentation and support!
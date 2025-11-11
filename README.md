# Better Without Wolves

**An addon that finally delivers on the parent mod's titular gripe.**

The 'Better Than Wolves' mod was created because wolves are a "lame" and "bad idea."  I can't and won't argue with that; FC, I get you.

...Then as a manic thought experiment, FlowerChild made them loud, scary, sometimes literally possessed, and full of poop.  What a pain.

This addon puts it all to rest in more ways than figurative.  While this mod is active, wolves go into an invisible, harmless stasis. There they will stay for the duration: silent, paralyzed, immobile, immortal.

Need to progress with them still?  Alright, uninstall the mod.  I don't give a poop! 😤

For real though, this doesn't take away anything permanently.  Once the addon is uninstalled, your game goes back to the way things were.
- Hopefully you didn't put a block where your precious pup was standing though.

Some wolf-related stuff isn't even taken away at all:
- Like your wolfchop stockpile?  Good, I don't touch that.  
- Have a tier 4 dung beacon?  Congrats, you're all set.
- Companion cube unharmed (by me at least; you, I don't know).

---

## Features

When this addon is active, wolves are:

- ✓ **Invisible** - Cannot be seen by players (including the dire wolf's glowing eyes).
- ✓ **Immobile** - Frozen in place, no AI or movement.
- ✓ **Immortal** - Cannot take damage or die from any source.
- ✓ **Incorporeal** - No collision, you walk right through them.
- ✓ **Silent** - No barks, growls, howls, or footstep sounds.
- ✓ **Non-interactive** - Cannot be fed, petted, or dyed.
- ✓ **Sterile** - Cannot breed or produce offspring.
- ✓ **Inert** - No eating, pooping, transformation to dire wolves, dire wolf sun-fire damage, or howling countdowns.
- ✓ **Absent-of-Possession** - Truly nothing to worry about.

**Most importantly:** Wolves remain as valid entities in your save file. Uninstall the addon and they instantly return to their normal, questionable behavior.

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
│       └── client/
│           └── MixinDireWolfRenderer.java   # To hide beasts' glowing eyes
└── resources/
    ├── assets/betterwithoutwolves/icon.png  # Addon thumbnail
    ├── betterwithoutwolves.mixins.json      # Mixin configuration
    └── fabric.mod.json                      # Mod metadata
```

---

## Building from Source

Kind of a clusterfudge at the moment since I had to target the actual names of certain methods in DireWolfEntity.  Not sure what's up with that.  It builds and works, but running from the dev environment is broken, and I don't know how to fix it.

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
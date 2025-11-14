# Poop Cats

**The Fecal Contingency**

This Better Than Wolves CE 3.0.0 addon gives cats the ability to produce dung.  It is an unbalanced response to [Better Without Wolves](https://github.com/AbbyRead/BTW-BetterWithoutWolves) depriving the player of a source of those brown nuggets everyone seems to like so much.

**The Book of Shitpost: Verse 2, Version 1.0**
Lo, as the wolves were cast out, the flow of dung was stayed.
And Steve, Son of Sam, turned his gaze upon the ocelot,
that his great prospects might continue unbroken.

- **Cats poop now.**
- Don't fight me.

-----

## Features

When this addon is active:

### 🥩 Feeding and State

* Feeding a cat with raw fish now gives it a **"Fed"** state.
* The cat receives a small health boost (healing) upon being fed.
* Fed cats make quieter, purr-like sounds instead of the regular meows.

### ⏳ Pooping

* After being fed, cats have a chance to attempt to poop over time, with a higher chance in the dark.
* If the cat is standing on a **Sand block** when it needs to poop, it will successfully spawn a piece of **Dung** and the "Fed" state is removed.

### 💥 Warning and Consequences

* If the cat needs to poop but is **not** standing on sand, it enters a **"Warning"** state.
	* The cat will audibly hiss, visually **swell** up, and flash **red**.
	* A custom AI task kicks in, causing the cat to actively seek out nearby sand blocks.
* If the warning timer expires **without the cat reaching sand**, the cat becomes stressed and... **explodes**.
	* This explosion kills the cat and produces custom red/brown particle effects.

-----

## Installation

1.  Build the addon or download the latest release.
2.  Place `Poop-Cats-<version>.jar` in your `mods/` folder.
3.  Launch Minecraft 1.6.4 with BTW CE 3.0.0 and a Legacy Fabric instance. See the [BTW CE 3.0.0 Installation Guide](https://wiki.btwce.com/view/Installation) for details.

-----

## Project Structure

```
src/main/
├── java/btw/community/poopcats/
│   ├── PoopCatsAddon.java                        # Main mod entry point
│   ├── interfaces/
│   │   └── PoopCatStateAccess.java               # Interface for accessing cat's poop-related data
│   ├── mixin/
│   │   ├── EntityAnimalMixin.java                # Resets cat state when it dies
│   │   ├── EntityOcelotMixin.java                # Adds custom AI, state access, and delegates core logic
│   │   ├── EntityTameableMixin.java              # Handles server-to-client particle/state synchronization
│   │   ├── RenderOcelotMixin.java                # Renders swelling and flashing effects
│   │   └── access/
│   │       ├── EntityAccess.java                 # Accessor for DataWatcher (state tracking)
│   │       ├── EntityLivingAccess.java           # Accessor for AI Task list
│   │       └── EntityLivingBaseAccess.java       # Invoker for entity sound volume
│   └── util/
│       ├── PoopCatAISandSeek.java                # AI task: Moves the cat toward sand
│       ├── PoopCatAISwell.java                   # AI task: Manages the cat's warning/swelling behavior
│       ├── PoopCatConstants.java                 # Holds all mod constants and IDs (DataWatcher, NBT, etc.)
│       └── PoopCatHelper.java                    # Core logic for feeding, pooping, explosions, and particles
└── resources/
    ├── assets/poopcats/icon.png                  # Addon icon
    ├── fabric.mod.json                           # Mod metadata
    └── poopcats.mixins.json                      # Mixin configuration
```

-----

## Building from Source

Requirements:

* Java 17
* BTW CE 3.0.0 Intermediary Distribution (get from the Pinned section of `#learn-modding` on the BTW CE Discord channel)

<!-- end list -->

```
Build the addon:
(Windows) Drag and drop the BTW Intermediary .zip file onto the install.bat
(Unix-likes) ./install.sh <the BTW Intermediary .zip>
Wait until it fully finishes.
Run the gradle task "build": ./gradlew build

Compiled output:
build/libs/Poop-Cats-<version>.jar
```

-----

## Compatibility

* **BTW CE Version:** 3.0.0
* **Java:** 17 or higher

**Cross-Addon Compatibility:**
This addon exclusively modifies the behavior of **EntityOcelot** and its related rendering/parent classes using Mixin `HEAD`/`TAIL` injections. Conflicts are unlikely unless another addon modifies those same methods or also attempts to change Ocelot AI or rendering effects.

-----

## Links

* [BTW Gradle Fabric Example](https://github.com/BTW-Community/BTW-gradle-fabric-example)
* [Legacy Fabric Wiki](https://fabricmc.net/wiki/)
* [BTW CE Wiki](https://wiki.btwce.com/)
* [BTW CE Discord](https://discord.btwce.com/)
* [GitHub Repository](https://www.google.com/search?q=https://github.com/AbbyRead/BTW-PoopCats) *(Note: Update this link if you change your repository name)*

-----

## License

Released under the **0BSD** license.

You are completely free to use, copy, and modify it how you like.

-----

## Credits

Created by **Abigail Read**

Special thanks to the BTW CE community for documentation and support\!
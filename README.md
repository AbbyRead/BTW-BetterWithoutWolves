# Better Without Wolves

This Better Than Wolves CE addon makes it *seem* like wolves are gone from the game (without actually removing them).
Wolves become paralized, invisible, immobile, immortal, incorporeal, and silent while the addon is installed.  They do not eat, they do not poop, they do not howl.  Uninstall the addon and there they are again, good as whatever they were before.
Install or uninstall this addon to essentially toggle wolves.

---

## What this addon shows

This addon demonstrates how to:

* Use runtime hooks and mixins to modify entity behavior without replacing classes
* Non-destructively “disable” an entity’s visibility, AI, and interactions
* Create a reversible, compatibility-friendly addon that leaves saves untouched

---

```
src/main/
├── java/btw/community/betterwithoutwolves/
│   ├── BetterWithoutWolvesAddon.java
│   ├── WolfDisabler.java
│   └── mixin/MixinEntityWolf.java
└── resources/
    ├── assets/betterwithoutwolves/icon.png
    ├── betterwithoutwolves.mixins.json
    └── fabric.mod.json
```

---

## More info

* [BTW Gradle Fabric Example](https://github.com/BTW-Community/BTW-gradle-fabric-example)
* [Legacy Fabric wiki](https://fabricmc.net/wiki/)
* [BTW CE Wiki](https://wiki.btwce.com/)
* [BTW CE Discord](https://discord.btwce.com/)

---

## License

Released under the **0BSD** license.
Free to use, copy, and modify — attribution appreciated but not required.
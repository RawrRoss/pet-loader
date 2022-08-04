# <img alt="mod icon" src="src\main\resources\assets\petloader\icon.png" width=100> Pet Loader - Fabric

Pet Loader is a server-side mod that force-loads chunks that standing pets reside in. This allows them to still teleport to you when they otherwise would have been unloaded, such as when you're crossing a large ocean.

This only applies to pets that can teleport: wolves, cats, and parrots.

# Requires
- [Fabric API]( https://www.curseforge.com/minecraft/mc-mods/fabric-api)

# Known issues
- Chunks manually loaded, such as with the `forceload` command, will be overwritten by pets passing through them.
    - However, on world saving and loading, chunks do not remember how they were force loaded.

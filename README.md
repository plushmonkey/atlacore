# atlacore  
atlacore is a Sponge/Spigot plugin for Avatar bending in Minecraft. It is designed to be a reimplementation of ProjectKorra.  

### Releases
This is still under heavy development and isn't ready to be used on servers.  
Pre-releases can be found [here](https://github.com/plushmonkey/atlacore/releases) if you want to see how the development is going.  

### Features
- Written from scratch - it's a cleaner codebase that doesn't have the cruft of previous cores.  
- Platform agnostic - it can run on Spigot and Sponge servers.  
- General entity bending - normal monsters can be scripted to use the same abilities that players can use.  
- Actual colliders - each ability is implemented with real colliders instead of just point-sphere collision tests.  
- Swept colliders - projectiles won't fire through blocks because they are checked throughout their entire movement that tick.  
- Bending board - shows the bound slots on the side of the screen without requiring an addon.  

### Commands  
- `/atla` - The main command for accessing subcommands.  
- `/atla choose [element]` - Choose a single element to bend.  
- `/atla bind [ability] <slot>` - Binds an ability to a slot.  
- `/atla display [element]` - Display the available abilities for an element.  
- `/atla help [ability]` - Gives information about how to use an ability.  
- `/atla preset [bind/create/delete/list]` - Manage slot presets.  
- `/atla add [element]` - Add an element to a player.  
- `/atla reload` - Reloads the configuration.  
- `/atla modify` - Creates an attribute modifier that is active under some policy for a given player.  

##### Modify command examples
- `/atla modify add plushmonkey air cooldown multiply 0` - This multiples all air cooldowns for plushmonkey by 0, resulting in no cooldowns for any air abilities.  
- `/atla modify add plushmonkey earthsmash radius add 2` - This adds 2 to the radius of EarthSmash for plushmonkey.  
- `/atla modify clear plushmonkey` - This clears all modifiers for the given player.  

### Compiling  
- Install Java Development Kit 8  
- Open command prompt or a terminal  
- Navigate to the atlacore root folder  
- `./gradlew build`  
- The jars will be in `atlacore-bukkit/bin/libs` and `atlacore-sponge/bin/libs`  

### Links  
- [atlacore-mobs](https://github.com/plushmonkey/atlacore-mobs) - Spigot plugin for creating bending mobs.  
- [Basic bending mobs](https://gfycat.com/NeighboringScratchyBluetonguelizard) - Basic video example of bending mobs.  
- [Arena combat](https://www.youtube.com/watch?v=FSHestdRT_A) - Video of atlacore bending against scripted bending mobs.  

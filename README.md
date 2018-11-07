# atlacore  
atlacore is a Sponge/Spigot plugin for Avatar bending in Minecraft. It is designed to be a reimplementation of ProjectKorra.  

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

### Compiling  
- Install Java Development Kit 8  
- Open command prompt or a terminal  
- Navigate to the atlacore root folder  
- `./gradlew build`  

### Links  
- [atlacore-mobs](https://github.com/plushmonkey/atlacore-mobs) - Spigot plugin for creating bending mobs.  
- [Basic bending mobs](https://gfycat.com/NeighboringScratchyBluetonguelizard) - Basic video example of bending mobs.  
- [Arena combat](https://www.youtube.com/watch?v=FSHestdRT_A) - Video of atlacore bending against scripted bending mobs.  

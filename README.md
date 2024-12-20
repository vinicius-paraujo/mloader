# mDynamicLoader
- The plugin forces the loading of blocks, entities, and similar elements in contexts where worlds need to be loaded/unloaded. This is useful for plugins like **ExecutableBlocks**, **MythicMobs**, and others that don’t natively support these contexts.  
`Note`: For proper functionality, the **WorldLoadEvent** must be triggered in these scenarios.

## ExecutableBlocks
- The plugin save block position and player’s position when the player places an EB block. During world loading, it creates an invisible **NPC** (a Player entity) and places the block according to NPC's position.
- The process involves removing the block and placing another in the same location, which prevents data persistence for the block (e.g., chest contents). However, it’s still useful for storing custom blocks with functionality, even if inventory persistence isn't possible.
- The plugin uses SQL for storing positions, as it’s designed to work in environments where the same world may be loaded/unloaded across different servers, so it doesn't rely on the default file storage of **ExecutableBlocks**.
- Tables structure:
```sql
CREATE TABLE IF NOT EXISTS eb_blocks (
    loader_block_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    world_name VARCHAR(100) NOT NULL,
    position_serialized VARCHAR(100) NOT NULL UNIQUE,
    block_id VARCHAR(100) NOT NULL
);

CREATE TABLE IF NOT EXISTS loader_entities (
    loader_block_id INT NOT NULL,
    target_position_serialized VARCHAR(200) NOT NULL,
    FOREIGN KEY (loader_block_id) REFERENCES eb_blocks(loader_block_id)
);
```

### Issue 1
In the [LoadEvents](https://github.com/vinicius-paraujo/mloader/blob/main/src/main/kotlin/com/markineo/loader/events/LoadEvents.kt) class, note the following part:
```kotlin
private fun placeExecutableBlock(loaderBlockId: Int, executableBlockId: String, targetLocation: Location, blockLocation: Location) {
    val executableBlock: ExecutableBlock = ExecutableBlocksAPI.getExecutableBlocksManager().getExecutableBlock(executableBlockId).orElse(null) ?: return
    
    if (blockLocation.block.type != Material.AIR) {
        blockLocation.block.type = Material.AIR
    }
    
    val npc = InvisibleNPC(loaderBlockId, targetLocation, blockLocation)
    val npcPlayer = npc.generate()
    
    executableBlock.place(
        blockLocation,
        true,
        OverrideEBP.REMOVE_EXISTING_EBP,
        npcPlayer,
        null,
        null
    )
    
    //npc?.remove()
}
```

![img](https://i.imgur.com/FJT8sTB.png)
- There is an issue that compromises the plugin's functionality: the block is not generated based on the player's position. This results in the block being placed in a different position than intended.

### Issue 2
A significant issue arises when the plugin handles **ItemsAdder** items that generate composite structures, such as a 2x2 structure (four blocks), as shown in the image. When a player interacts with one of these blocks in the world, the standard `BlockPlaceEvent` is triggered, as well as the `BlockPlace` event from **ExecutableBlocks**. However, because this is a multi-block structure, **ExecutableBlocks** stores each of the four blocks individually, both in its files and plugin cache.

If the `<ExecutableBlock>.place()` method is called four times, it triggers the block creation event for **ItemsAdder** four times, leading to the duplication of the structure. Instead of generating a cohesive structure (four interactive blocks), the plugin creates multiple overlapping structures, as shown in the image below:

![img_2](https://i.imgur.com/Be8uKP6.png)
- In this scenario, the `place` method is invoked for each position stored by **ExecutableBlocks**. However, under normal plugin behavior, it does not generate new blocks; it only loads previously stored data. As a result, duplication issues only occur when blocks are manually inserted during the loading process.

![IMAGE 3](https://i.imgur.com/Q63AxeY.gif)
- It's essential preserve interaction across all blocks in the structure, as maintaining interaction on only one block is neither logical nor functional.


### Summary

The main issue is mapping interactive blocks in a structure and efficiently loading them. Iterating through files to find connected blocks is an option but highly inefficient and impractical long-term, especially with large structures or high data volumes.

An alternative would be adding a function to force a block load directly, assuming it's an ExecutableBlock, even if it wasn't manually placed in the world. This might depend on the plugin's internal logic but could solve the problem with large structures and enable better block management.

A more practical approach would be storing block data directly in a database instead of files, simplifying queries and load operations. Ideally, a general method like `<ExecutableBlock>.loadFromWorld()` could load interactive blocks directly from the world, assuming they are pre-registered in the database or files.

If possible, consider allowing values tied to blocks to be stored in the database for more dynamic and efficient queries. If that's not feasible, the previously discussed solutions are still viable and can address duplication and interaction issues in complex structures.

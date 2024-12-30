# mDynamicLoader
- The plugin forces the loading of blocks, entities, and similar elements in contexts where worlds need to be loaded/unloaded. This is useful for plugins like **ExecutableBlocks**, **MythicMobs**, and others that don’t natively support these contexts.  
  `Note`: For proper functionality, the **WorldLoadEvent** must be triggered in these scenarios.

## ExecutableBlocks
- The plugin stores the block position in a table and loads it with the ExecutableBlocks API.
- The plugin uses SQL for storing positions, as it’s designed to work in environments where the same world may be loaded/unloaded across different servers, so it doesn't rely on the default file storage of **ExecutableBlocks**.
- Tables structure:
```sql
CREATE TABLE IF NOT EXISTS eb_blocks (
    loader_block_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    world_name VARCHAR(100) NOT NULL,
    position_serialized VARCHAR(100) NOT NULL UNIQUE,
    block_id VARCHAR(100) NOT NULL
);
```

### Issue 1
In the [LoadEvents](https://github.com/vinicius-paraujo/mloader/blob/main/src/main/kotlin/com/markineo/loader/events/LoadEvents.kt) class, note the following part:
```kotlin
private fun placeExecutableBlock(executableBlockId: String, blockLocation: Location) {
    val executableBlock: ExecutableBlock = ExecutableBlocksAPI.getExecutableBlocksManager().getExecutableBlock(executableBlockId).orElse(null) ?: return
    
    logger.info("block type: ${blockLocation.block.type}")
    
    executableBlock.place(
        blockLocation,
        false,
        OverrideEBP.REMOVE_EXISTING_EBP,
        null,
        null,
        null
    )
}
```
- Some blocks have composite structures, which require particular attention (but this refinement is part of my plugin). The plugin stores each block's position and tries to load it with the previous code. The code works for every structure except an ItemsAdder block. The material of an ItemsAdder block is a BARRIER, in most cases, and is shown in the plugin log (this information is relevant if EB checks whether a material is AIR). The file is generated in the 'data' folder of ExecutableBlocks, but the plugin doesn’t recognize the block, and interactions with it don’t work.
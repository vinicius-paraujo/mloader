package com.markineo.loader.events

import com.markineo.loader.Loader
import com.markineo.loader.apis.ExecutableBlocksAPI
import com.markineo.loader.blocks.BlocksManager
import com.markineo.loader.util.DatabaseManager
import com.ssomar.executableblocks.executableblocks.ExecutableBlock
import com.ssomar.executableblocks.utils.OverrideEBP

import org.bukkit.Location
import org.bukkit.World
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.world.WorldLoadEvent

class LoadEvents : Listener {
    private val logger = Loader.instance.logger

    @EventHandler
    fun onWorldLoad(event: WorldLoadEvent) {
        logger.info("load world event")

        val world: World = event.world

        if (ExecutableBlocksAPI.is_active) {
            loadEBBlocks(world.name)
        }
    }

    private fun loadEBBlocks(worldName: String) {
        ExecutableBlocksAPI.clearFiles()

        DatabaseManager.getConnection().use { connection ->
            // Query to get all blocks from the world
            val query = """
                SELECT * FROM eb_blocks WHERE world_name = ?;
            """.trimIndent()

            connection.prepareStatement(query).use { preparedStatement ->
                preparedStatement.setString(1, worldName)

                preparedStatement.executeQuery().use { resultSet ->
                    while (resultSet.next()) {
                        // Get data from the query
                        val loaderBlockId = resultSet.getInt("loader_block_id")
                        val positionSerialized = resultSet.getString("position_serialized")

                        val blockId = resultSet.getString("block_id")
                        val blockLocation = BlocksManager.deserializeLocation(positionSerialized)

                        logger.warning("Processing block: $loaderBlockId, Block ID: $blockId")
                        logger.warning("Block location: $blockLocation")

                        placeExecutableBlock(blockId, blockLocation)
                    }
                }
            }
        }
    }

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
}
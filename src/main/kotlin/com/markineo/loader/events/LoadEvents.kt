package com.markineo.loader.events

import com.markineo.loader.Loader
import com.markineo.loader.apis.ExecutableBlocksAPI
import com.markineo.loader.blocks.BlocksManager
import com.markineo.loader.entity.InvisibleNPC
import com.markineo.loader.util.DatabaseManager
import com.ssomar.executableblocks.executableblocks.ExecutableBlock
import com.ssomar.executableblocks.utils.OverrideEBP

import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.entity.Player
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
            // Consulta unificada para buscar dados de `eb_blocks` e `loader_entities`
            val query = """
                SELECT 
                    eb.loader_block_id,
                    eb.position_serialized,
                    eb.block_id,
                    le.target_position_serialized
                FROM eb_blocks eb
                LEFT JOIN loader_entities le ON eb.loader_block_id = le.loader_block_id
                WHERE eb.world_name = ?;
            """.trimIndent()

            connection.prepareStatement(query).use { preparedStatement ->
                preparedStatement.setString(1, worldName)

                preparedStatement.executeQuery().use { resultSet ->
                    while (resultSet.next()) {
                        // Extração dos dados do ResultSet
                        val loaderBlockId = resultSet.getInt("loader_block_id")
                        val positionSerialized = resultSet.getString("position_serialized")
                        val blockId = resultSet.getString("block_id")
                        val targetPositionSerialized = resultSet.getString("target_position_serialized")

                        val blockLocation = BlocksManager.deserializeLocation(positionSerialized)
                        val targetLocation = BlocksManager.deserializeLocation(targetPositionSerialized)

                        // Exibição clara dos valores para debug (se necessário)
                        logger.warning("Processing block: $loaderBlockId, Block ID: $blockId")
                        logger.warning("Block location: $blockLocation, Target location: $targetLocation")

                        // Coloca o bloco no local deserializado
                        placeExecutableBlock(loaderBlockId, blockId, targetLocation, blockLocation)
                    }
                }
            }
        }
    }

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
}
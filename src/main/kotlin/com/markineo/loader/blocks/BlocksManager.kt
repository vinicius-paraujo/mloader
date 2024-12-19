package com.markineo.loader.blocks

import com.markineo.loader.Loader
import com.markineo.loader.util.DatabaseManager
import org.bukkit.Location
import org.bukkit.entity.Player

import org.json.JSONObject

object BlocksManager {
    private val logger = Loader.instance.logger

    fun saveCustomDefaultBlock(placer: Player, blockLocation: Location, ebId: String) {
        val world = blockLocation.world
        val playerLocationSerialized = serializeLocation(placer.location)

        logger.info(playerLocationSerialized)

        DatabaseManager.getConnection().use { connection ->
            // Inicia uma transação para garantir atomicidade
            connection.autoCommit = false

            try {
                // 1. Inserção ou atualização na tabela 'eb_blocks'
                val queryEbBlocks = """
                INSERT INTO eb_blocks (world_name, position_serialized, block_id)
                VALUES (?, ?, ?)
                ON DUPLICATE KEY UPDATE world_name = VALUES(world_name);
            """.trimIndent()

                connection.prepareStatement(queryEbBlocks).use { statement ->
                    statement.setString(1, world.name)
                    statement.setString(2, serializeLocation(blockLocation))
                    statement.setString(3, ebId)

                    statement.executeUpdate()
                }

                // 2. Inserção ou atualização na tabela 'loader_entities'
                val queryLoaderEntities = """
                INSERT INTO loader_entities (loader_block_id, target_position_serialized)
                VALUES ((SELECT loader_block_id FROM eb_blocks WHERE world_name = ? AND position_serialized = ?), ?)
                ON DUPLICATE KEY UPDATE target_position_serialized = VALUES(target_position_serialized);
            """.trimIndent()

                connection.prepareStatement(queryLoaderEntities).use { statement ->
                    statement.setString(1, world.name)
                    statement.setString(2, serializeLocation(blockLocation))
                    statement.setString(3, playerLocationSerialized)

                    statement.executeUpdate()
                }

                // Commit das alterações após ambos os updates
                connection.commit()
            } catch (e: Exception) {
                // Em caso de erro, realiza rollback
                connection.rollback()
                e.printStackTrace()
            } finally {
                // Restaura o autoCommit para o valor original
                connection.autoCommit = true
            }
        }
    }

    fun serializeLocation(location: Location): String {
        val locationMap = location.serialize()
        val jsonObject = JSONObject(locationMap)

        return jsonObject.toString()
    }

    fun deserializeLocation(locationSerialized: String): Location {
        val jsonObject = JSONObject(locationSerialized)
        val locationMap = jsonObject.toMap()

        return Location.deserialize(locationMap.mapValues { it.value })
    }
}
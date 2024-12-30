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
            connection.autoCommit = false

            try {
                // Insert block data
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

                connection.commit()
            } catch (e: Exception) {
                connection.rollback()
                e.printStackTrace()
            } finally {
                connection.autoCommit = true
            }
        }
    }

    /**
     * Serialize location to a JSON string
     */
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
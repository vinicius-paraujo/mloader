package com.markineo.loader.events

import com.markineo.loader.Loader
import com.markineo.loader.apis.ExecutableBlocksAPI
import com.markineo.loader.apis.ExecutableBlocksAPI.getExecutableBlockObject
import com.markineo.loader.apis.ExecutableBlocksAPI.getExecutableBlocksManager
import com.markineo.loader.blocks.BlocksManager
import com.ssomar.executableblocks.executableblocks.placedblocks.ExecutableBlockPlaced

import org.bukkit.Material
import org.bukkit.block.BlockFace

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.Vector
import java.util.UUID
import java.util.concurrent.CopyOnWriteArraySet


class BlocksEvents : Listener {
    private val plugin = Loader.instance
    private val logger = plugin.logger

    private val playersInDelay = CopyOnWriteArraySet<UUID>()

    @EventHandler
    fun placeCustomBlock(event: BlockPlaceEvent) {
        val handItem: ItemStack = event.player.inventory.getItem(event.hand).clone();
        if (handItem.type == Material.AIR) return;

        val ebBlock = getExecutableBlocksManager().getExecutableBlock(handItem).orElse(null);
        val player = event.player

        if (!playersInDelay.contains(player.uniqueId) && getExecutableBlockObject(handItem).isValid) {
            if (ebBlock == null) return;

            val block = event.blockPlaced

            logger.info("placed type: ${block.type}")
            logger.info("block position: ${block.location}")

            val key: String = ebBlock.id

            if (key.isNotEmpty()) {
                if (handItem.type == Material.PAPER) {
                    object : BukkitRunnable() {
                        override fun run() {
                            /**
                             * O plugin busca blocos do ExecutableBlocks próximo ao bloco colocado
                             * para o caso da entidade ser composta por mais de um bloco.
                             * Para funcionar de modo adequado, requer uma limitação de blocos do EB
                             * num raio.
                             */
                            val ebsBlocksNear: List<ExecutableBlockPlaced> = ExecutableBlocksAPI.getExecutableBlocksPlacedManager().getExecutableBlocksPlacedNear(block.location, 3.0).filter { it.executableBlockID == key }
                            ebsBlocksNear.forEach { blockNear ->
                                BlocksManager.saveCustomDefaultBlock(player, blockNear.location, key)}
                        }
                    }.runTaskLater(plugin, 20)
                } else {
                    BlocksManager.saveCustomDefaultBlock(player, block.location, key)
                }

                playersInDelay.add(player.uniqueId)

                object : BukkitRunnable() {
                    override fun run() {
                        playersInDelay.remove(player.uniqueId)
                    }
                }.runTaskLater(plugin, 20)

                ExecutableBlocksAPI.clearFiles()
            }
        }

        logger.info("handItem: ${handItem.type}")
    }

    private fun Vector.toClosestBlockFace(): BlockFace {
        val directions = mapOf(
            BlockFace.NORTH to Vector(0, 0, -1),
            BlockFace.SOUTH to Vector(0, 0, 1),
            BlockFace.EAST to Vector(1, 0, 0),
            BlockFace.WEST to Vector(-1, 0, 0),
            BlockFace.UP to Vector(0, 1, 0),
            BlockFace.DOWN to Vector(0, -1, 0)
        )

        return directions.minByOrNull { (_, vector) ->
            this.angle(vector)
        }?.key ?: BlockFace.SELF
    }

    /**
     * Note: default custom block is block without custom model (from eb)
     */
    @EventHandler
    fun deleteDefaultCustomBlock(event: BlockBreakEvent) {

    }
}
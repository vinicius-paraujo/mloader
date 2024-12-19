package com.markineo.loader.events

import com.markineo.loader.Loader
import com.markineo.loader.apis.ExecutableBlocksAPI
import com.markineo.loader.apis.ExecutableBlocksAPI.getExecutableBlockObject
import com.markineo.loader.apis.ExecutableBlocksAPI.getExecutableBlocksManager
import com.markineo.loader.blocks.BlocksManager

import org.bukkit.Material

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.inventory.ItemStack


class BlocksEvents : Listener {
    private val logger = Loader.instance.logger

    @EventHandler
    fun placeCustomBlock(event: BlockPlaceEvent) {
        val handItem: ItemStack = event.player.inventory.getItem(event.hand).clone();
        if (handItem.type == Material.AIR) return;

        val ebBlock = getExecutableBlocksManager().getExecutableBlock(handItem).orElse(null);

        if (getExecutableBlockObject(handItem).isValid) {
            if (ebBlock == null) return;

            val player = event.player
            val block = event.blockPlaced
            val key: String = ebBlock.id

            if (key.isNotEmpty()) {
                BlocksManager.saveCustomDefaultBlock(player, block.location, key)
                ExecutableBlocksAPI.clearFiles()
            }
        }

        logger.info("handItem: ${handItem.type}")
    }

    /**
     * Note: default custom block is block without custom model (from eb)
     */
    @EventHandler
    fun deleteDefaultCustomBlock(event: BlockBreakEvent) {

    }
}
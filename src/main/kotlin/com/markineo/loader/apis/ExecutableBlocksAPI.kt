package com.markineo.loader.apis

import com.markineo.loader.Loader
import com.ssomar.executableblocks.executableblocks.ExecutableBlockObject
import com.ssomar.executableblocks.executableblocks.ExecutableBlocksManager
import com.ssomar.executableblocks.executableblocks.placedblocks.ExecutableBlocksPlacedManager

import org.bukkit.inventory.ItemStack
import java.io.File


object ExecutableBlocksAPI {
    private val logger = Loader.instance.logger

    var is_active = false;

    fun init() {
        is_active = true;
    }

    /**
     * Get the ExecutableBlocks Manager,
     * It allows you to get / retrieve the ExecutableBlocks Configurations
     */
    fun getExecutableBlocksManager(): ExecutableBlocksManager {
        return ExecutableBlocksManager.getInstance()
    }

    /**
     * Get the ExecutableBlocksPlaced Manager,
     * It allows you to get / retrieve the ExecutableBlocks Placed
     */
    fun getExecutableBlocksPlacedManager(): ExecutableBlocksPlacedManager {
        return ExecutableBlocksPlacedManager.getInstance()
    }


    /**
     * Get the ExecutableBlockObject
     * It allows you to get / retrieve the ExecutableBlocks Configurations under its item form
     */
    fun getExecutableBlockObject(itemStack: ItemStack?): ExecutableBlockObject {
        return ExecutableBlockObject(itemStack!!)
    }

    /**
     * In this context, the plugin cannot store data in files.
     * Since the data will reside in the database, errors such as duplication
     * or similar issues may occur. To prevent this, for each addition, it will
     * delete the file created to avoid conflicts.
     */
    fun clearFiles() {
        val ebFolder = File("plugins/ExecutableBlocks/data")
        if (!ebFolder.exists() || !ebFolder.isDirectory) return;

        val files = ebFolder.listFiles() ?: return
        files.forEach { file ->
            if (file.name.endsWith(".yml")) {
                val deleted = file.delete()
                if (!deleted) {
                    logger.warning("Failed to delete file: ${file.name}")
                }
            }
        }
    }
}
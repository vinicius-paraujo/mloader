package com.markineo.loader.util


import com.markineo.loader.Loader
import org.bukkit.configuration.InvalidConfigurationException
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration

import java.io.File
import java.io.IOException
import java.util.logging.Logger

object FileManager {
    private val plugin = Loader.instance
    private val logger: Logger = plugin.logger

    var mainConfig: FileConfiguration? = null
        private set
    var messagesConfig: FileConfiguration? = null
        private set
    var databaseConfig: FileConfiguration? = null
        private set

    private fun getConfig(fileName: String): FileConfiguration {
        val configFile = File(plugin.dataFolder, fileName)

        if (!configFile.exists()) {
            plugin.saveResource(fileName, false)
        }

        val config = YamlConfiguration()

        try {
            config.load(configFile)
        } catch (e: IOException) {
            throw RuntimeException(e)
        } catch (e: InvalidConfigurationException) {
            throw RuntimeException(e)
        }

        return config
    }

    fun loadConfigurations() {
        messagesConfig = getConfig("messages.yml")
        mainConfig = getConfig("config.yml")
        databaseConfig = getConfig("database.yml")
    }

    fun reloadConfigurations() {
        messagesConfig = null
        mainConfig = null
        databaseConfig = null
        loadConfigurations()
    }

    fun getMessage(message: String): String? {
        val pluginMessage = messagesConfig?.getString(message)
        if (pluginMessage == null) {
            logger.info("A mensagem de configuração '$message' não foi encontrada no arquivo de configuração 'messages.yml'.")
            return null
        }

        return pluginMessage.replace("&", "§")
    }
}
package com.markineo.loader

import com.markineo.loader.apis.ExecutableBlocksAPI
import com.markineo.loader.blocks.BlocksManager
import com.markineo.loader.util.FileManager
import com.markineo.loader.util.DatabaseManager
import com.markineo.loader.util.DatabaseManager.setupDatabase
import com.markineo.loader.commands.LoaderCommand
import com.markineo.loader.events.BlocksEvents
import com.markineo.loader.events.LoadEvents

import kotlinx.coroutines.*

import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin

class Loader : JavaPlugin() {
    companion object {
        lateinit var instance: JavaPlugin
    }

    private lateinit var dependencies: List<String>

    @OptIn(DelicateCoroutinesApi::class)
    override fun onEnable() {
        instance = this
        dependencies = instance.pluginMeta.pluginSoftDependencies

        FileManager.loadConfigurations()

        DatabaseManager.setLogger(logger)

        GlobalScope.launch {
            val success = setupDatabase()
            if (success) {
                logger.info("Configuração do banco de dados concluída com sucesso.")

                checkDependencies()

                getCommand("mloader")?.setExecutor(LoaderCommand())
                server.pluginManager.registerEvents(LoadEvents(), instance)
                server.pluginManager.registerEvents(BlocksEvents(), instance)
            } else {
                logger.severe("Falha na configuração do banco de dados.")
            }
        }

        logger.info("Desenvolvido por: Markineo. Versão: ${instance.pluginMeta.version}")
    }

    override fun onDisable() {
        ExecutableBlocksAPI.clearFiles()
        logger.info("Plugin desativado.")
    }

    private fun checkDependencies() {
        dependencies.forEach{ dependency ->
            if (!isPluginActive(dependency)) {
                logger.warning("O plugin '$dependency' não está ativo como dependência, o comportamento do plugin pode ser incerto em determinadas situações.")
            } else {
                initDependency(dependency)
                logger.info("A dependência '$dependency' foi encontrada e registrada.")
            }
        }
    }

    private fun initDependency(dependencyName: String) {
        when (dependencyName) {
            "ExecutableBlocks" -> ExecutableBlocksAPI.init()
        }
    }

    private fun isPluginActive(pluginName: String):Boolean {
        val plugin: Plugin? = Bukkit.getPluginManager().getPlugin(pluginName)
        return plugin != null && plugin.isEnabled
    }
}
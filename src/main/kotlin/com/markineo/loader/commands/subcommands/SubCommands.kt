package com.markineo.loader.commands.subcommands

object SubCommands {
    val comandos = listOf("ajuda", "reload")

    fun getSubCommand(commandName: String): SubCommand? {
        return when (commandName) {
            "ajuda" -> AjudaSubcommand()
            "reload" -> ReloadSubcommand()
            else -> null
        }
    }
}
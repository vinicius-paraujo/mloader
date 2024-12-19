package com.markineo.loader.commands.subcommands

import org.bukkit.command.CommandSender

interface SubCommand {
    /**
     * Executa o subcomando em questão.
     * @param sender Quem executa o comando.
     * @param args Os argumentos do comando.
     */
    fun execute(sender: CommandSender, args: Array<out String>)

    /**
     * Exibe se um subcomando é possível de utilizar por um jogador.
     * @return se é possível utilizar por um jogador.
     */
    fun isOnlyPlayerCommand(): Boolean

    /**
     * Retorna se é um subcomando de administrador.
     * @return se é um comando de administrador.
     */
    fun isAdminCommand(): Boolean
}
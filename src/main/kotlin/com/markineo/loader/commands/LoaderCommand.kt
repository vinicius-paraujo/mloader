package com.markineo.loader.commands

import com.markineo.loader.commands.subcommands.SubCommand
import com.markineo.loader.commands.subcommands.SubCommands
import com.markineo.loader.util.Permissions
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player

class LoaderCommand : TabCompleter, CommandExecutor {
    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<out String>
    ): List<String> {
        return when {
            args.size <= 1 -> SubCommands.comandos
            else -> emptyList()
        }
    }

    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): Boolean {
        val subCommand: SubCommand? = if (args.isEmpty()) {
            SubCommands.getSubCommand("ajuda")
        } else {
            SubCommands.getSubCommand(args[0].lowercase())
        }

        when {
            subCommand == null || (subCommand.isAdminCommand() && !sender.hasPermission(Permissions.ADMIN_PERMISSION)) -> {
                sender.sendMessage("§cSubcomando não encontrado.")
            }
            subCommand.isOnlyPlayerCommand() && sender !is Player -> {
                sender.sendMessage("§cEsse comando deve ser executado por um jogador.")
            }
            else -> {
                subCommand.execute(sender, args)
            }
        }

        return true
    }
}
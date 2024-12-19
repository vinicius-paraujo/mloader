package com.markineo.loader.commands.subcommands

import com.markineo.loader.util.FileManager
import org.bukkit.command.CommandSender

class AjudaSubcommand : SubCommand {
    override fun execute(sender: CommandSender, args: Array<out String>) {
        val msg = FileManager.getMessage("plugin_messages.admin_sintaxe")
        if (msg != null) sender.sendMessage(msg)
    }

    override fun isOnlyPlayerCommand(): Boolean = false

    override fun isAdminCommand(): Boolean = true
}
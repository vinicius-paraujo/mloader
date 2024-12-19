package com.markineo.loader.commands.subcommands

import com.markineo.loader.util.FileManager
import org.bukkit.command.CommandSender

class ReloadSubcommand : SubCommand {
    override fun execute(sender: CommandSender, args: Array<out String>) {
        FileManager.reloadConfigurations()

        val msg: String = FileManager.getMessage("plugin_messages.reload").orEmpty()
        if (msg.isNotEmpty()) sender.sendMessage(msg)
    }

    override fun isOnlyPlayerCommand(): Boolean {
        return false
    }

    override fun isAdminCommand(): Boolean {
        return true
    }
}
package com.markineo.loader.entity

import net.citizensnpcs.api.CitizensAPI

import org.bukkit.Location
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player

class InvisibleNPC(private val loadBlockId: Int,
                   private val location: Location,
                   private val eyeTarget: Location)
{
    private var npcPlayer: Player? = null

    fun generate(): Player {
        val npc = CitizensAPI.getNPCRegistry().createNPC(EntityType.PLAYER, "InvisibleNPC")
        npc.spawn(location)
        val player = npc.entity as Player
        player.isInvisible = false

        npc.navigator.setTarget(eyeTarget)

        npcPlayer = player
        return player
    }

    fun remove() {
        npcPlayer?.let { player ->
            val npc = CitizensAPI.getNPCRegistry().getNPC(player)
            npc.despawn()
            CitizensAPI.getNPCRegistry().deregister(npc)
        }
        npcPlayer = null
    }
}
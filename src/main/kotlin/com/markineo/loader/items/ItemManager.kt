package com.markineo.loader.items

import org.bukkit.inventory.ItemStack
import org.json.JSONObject

object ItemManager {
    fun serializeItem(item: ItemStack): String {
        val itemMap = item.serialize()
        val jsonObject = JSONObject(itemMap)
        return jsonObject.toString()
    }

    fun deserializeItem(itemSerialized: String): ItemStack {
        val jsonObject = JSONObject(itemSerialized)
        val itemMap = jsonObject.toMap()
        return ItemStack.deserialize(itemMap.mapValues { it.value })
    }
}
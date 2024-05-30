package at.hannibal2.skyhanni.config

import at.hannibal2.skyhanni.events.utils.ConfigFixEvent
import at.hannibal2.skyhanni.utils.LorenzLogger
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive

object ConfigUpdaterMigrator {

    val logger = LorenzLogger("ConfigMigration")
    const val CONFIG_VERSION = 46

    private fun merge(originalObject: JsonObject, overrideObject: JsonObject): Int {
        var count = 0
        overrideObject.entrySet().forEach {
            val element = originalObject[it.key]
            val newElement = it.value
            if (element is JsonObject && newElement is JsonObject) {
                count += merge(element, newElement)
            } else {
                if (element != null) {
                    logger.log("Encountered destructive merge. Erasing $element in favour of $newElement.")
                    count++
                }
                originalObject.add(it.key, newElement)
            }
        }
        return count
    }

    fun fixConfig(config: JsonObject): JsonObject {
        val lastVersion = (config["lastVersion"] as? JsonPrimitive)?.takeIf { it.isNumber }?.asInt ?: -1
        if (lastVersion > CONFIG_VERSION) {
            error("Cannot downgrade config")
        }
        if (lastVersion == CONFIG_VERSION) return config
        return (lastVersion until CONFIG_VERSION).fold(config) { accumulator, i ->
            logger.log("Starting config transformation from $i to ${i + 1}")
            val storage = accumulator["storage"]?.asJsonObject
            val dynamicPrefix: Map<String, List<String>> = mapOf(
                "#profile" to
                    (storage?.get("players")?.asJsonObject?.entrySet()
                        ?.flatMap { player ->
                            player.value.asJsonObject["profiles"]?.asJsonObject?.entrySet()?.map {
                                "storage.players.${player.key}.profiles.${it.key}"
                            } ?: listOf()
                        }
                        ?: listOf()),
                "#player" to
                    (storage?.get("players")?.asJsonObject?.entrySet()?.map { "storage.players.${it.key}" }
                        ?: listOf()),
            )
            val migration = ConfigFixEvent(accumulator, JsonObject().also {
                it.add("lastVersion", JsonPrimitive(i + 1))
            }, i, 0, dynamicPrefix).also { it.post() }
            logger.log("Transformations scheduled: ${migration.new}")
            val mergesPerformed = merge(migration.old, migration.new)
            logger.log("Migration done with $mergesPerformed merges and ${migration.movesPerformed} moves performed")
            migration.old
        }.also {
            logger.log("Final config: $it")
        }
    }
}

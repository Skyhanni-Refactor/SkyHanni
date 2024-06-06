package at.hannibal2.skyhanni.events.utils

import at.hannibal2.skyhanni.api.event.SkyHanniEvent
import at.hannibal2.skyhanni.config.ConfigUpdaterMigrator.CONFIG_VERSION
import at.hannibal2.skyhanni.config.ConfigUpdaterMigrator.logger
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject

class ConfigFixEvent(
    val old: JsonObject,
    val new: JsonObject,
    val oldVersion: Int,
    var movesPerformed: Int,
    val dynamicPrefix: Map<String, List<String>>,
) : SkyHanniEvent() {

    init {
        dynamicPrefix.entries.filter { it.value.isEmpty() }.forEach {
            logger.log("Dynamic prefix ${it.key} does not resolve to anything.")
        }
    }

    fun transform(since: Int, path: String, transform: (JsonElement) -> JsonElement = { it }) {
        move(since, path, path, transform)
    }

    fun move(since: Int, oldPath: String, newPath: String, transform: (JsonElement) -> JsonElement = { it }) {
        if (since <= oldVersion) {
            logger.log("Skipping move from $oldPath to $newPath ($since <= $oldVersion)")
            return
        }
        if (since > CONFIG_VERSION) {
            error("Illegally new version $since > $CONFIG_VERSION")
        }
        if (since > oldVersion + 1) {
            logger.log("Skipping move from $oldPath to $newPath (will be done in another pass)")
            return
        }
        val op = oldPath.split(".")
        val np = newPath.split(".")
        if (op.first().startsWith("#")) {
            require(np.first() == op.first())
            val realPrefixes = dynamicPrefix[op.first()]
            if (realPrefixes == null) {
                logger.log("Could not resolve dynamic prefix $oldPath")
                return
            }
            for (realPrefix in realPrefixes) {
                move(
                    since,
                    "$realPrefix.${oldPath.substringAfter('.')}",
                    "$realPrefix.${newPath.substringAfter('.')}", transform
                )
                return
            }
        }
        val oldElem = old.at(op, false)
        if (oldElem == null) {
            logger.log("Skipping move from $oldPath to $newPath ($oldPath not present)")
            return
        }
        val newParentElement = new.at(np.dropLast(1), true)
        if (newParentElement !is JsonObject) {
            logger.log("Catastrophic: element at path $old could not be relocated to $new, since another element already inhabits that path")
            return
        }
        movesPerformed++
        newParentElement.add(np.last(), transform(oldElem.shDeepCopy()))
        logger.log("Moved element from $oldPath to $newPath")
    }

    private fun JsonElement.at(chain: List<String>, init: Boolean): JsonElement? {
        if (chain.isEmpty()) return this
        if (this !is JsonObject) return null
        var obj = this[chain.first()]
        if (obj == null && init) {
            obj = JsonObject()
            this.add(chain.first(), obj)
        }
        return obj?.at(chain.drop(1), init)
    }

    private fun JsonElement.shDeepCopy(): JsonElement = when (this) {
        is JsonObject -> JsonObject().also {
            for (entry in this.entrySet()) {
                it.add(entry.key, entry.value.shDeepCopy())
            }
        }

        is JsonArray -> JsonArray().also {
            for (entry in this) {
                it.add(entry.shDeepCopy())
            }
        }

        else -> this
    }
}

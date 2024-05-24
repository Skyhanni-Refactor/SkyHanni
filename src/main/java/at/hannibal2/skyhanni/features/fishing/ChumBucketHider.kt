package at.hannibal2.skyhanni.features.fishing

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.entity.CheckRenderEntityEvent
import at.hannibal2.skyhanni.events.minecraft.WorldChangeEvent
import at.hannibal2.skyhanni.events.utils.ConfigLoadEvent
import at.hannibal2.skyhanni.utils.ConditionalUtils
import at.hannibal2.skyhanni.utils.ItemUtils.name
import at.hannibal2.skyhanni.utils.TimeLimitedSet
import at.hannibal2.skyhanni.utils.getLorenzVec
import at.hannibal2.skyhanni.utils.mc.McPlayer
import net.minecraft.entity.Entity
import net.minecraft.entity.item.EntityArmorStand
import kotlin.time.Duration.Companion.seconds

object ChumBucketHider {

    private val config get() = SkyHanniMod.feature.fishing.chumBucketHider
    private val titleEntity = TimeLimitedSet<Entity>(5.seconds)
    private val hiddenEntities = TimeLimitedSet<Entity>(5.seconds)

    @HandleEvent
    fun onWorldChange(event: WorldChangeEvent) {
        reset()
    }

    @HandleEvent(onlyOnSkyblock = true)
    fun onCheckRender(event: CheckRenderEntityEvent<*>) {
        if (!config.enabled.get()) return

        val entity = event.entity
        if (entity !is EntityArmorStand) return

        if (entity in hiddenEntities) {
            event.cancel()
            return
        }

        val name = entity.name

        // First text line
        if (name.endsWith("'s Chum Bucket") || name.endsWith("'s Chumcap Bucket")) {
            if (name.contains(McPlayer.name) && !config.hideOwn.get()) return
            titleEntity.add(entity)
            hiddenEntities.add(entity)
            event.cancel()
            return
        }

        // Second text line
        if (name.contains("/10 §aChums")) {
            val entityLocation = entity.getLorenzVec()
            for (title in titleEntity.toSet()) {
                if (entityLocation.equalsIgnoreY(title.getLorenzVec())) {
                    hiddenEntities.add(entity)
                    event.cancel()
                    return
                }
            }
        }

        // Chum Bucket
        if (config.hideBucket.get() && entity.inventory.any { it != null && (it.name == "§fEmpty Chum Bucket" || it.name == "§aEmpty Chumcap Bucket") }) {
            val entityLocation = entity.getLorenzVec()
            for (title in titleEntity.toSet()) {
                if (entityLocation.equalsIgnoreY(title.getLorenzVec())) {
                    hiddenEntities.add(entity)
                    event.cancel()
                    return
                }
            }
        }
    }

    @HandleEvent
    fun onConfigLoad(event: ConfigLoadEvent) {
        ConditionalUtils.onToggle(config.enabled, config.hideBucket, config.hideOwn) { reset() }
    }

    private fun reset() {
        titleEntity.clear()
        hiddenEntities.clear()
    }
}

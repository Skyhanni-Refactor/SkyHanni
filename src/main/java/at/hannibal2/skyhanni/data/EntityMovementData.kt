package at.hannibal2.skyhanni.data

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.chat.SkyHanniChatEvent
import at.hannibal2.skyhanni.events.entity.EntityMoveEvent
import at.hannibal2.skyhanni.events.minecraft.ClientTickEvent
import at.hannibal2.skyhanni.events.minecraft.WorldChangeEvent
import at.hannibal2.skyhanni.events.skyblock.WarpEvent
import at.hannibal2.skyhanni.utils.DelayedRun
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.LorenzVec
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.getLorenzVec
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraft.client.Minecraft
import net.minecraft.entity.Entity

object EntityMovementData {

    private val warpingPattern by RepoPattern.pattern(
        "data.entity.warping",
        "§7(?:Warping|Warping you to your SkyBlock island|Warping using transfer token|Finding player|Sending a visit request)\\.\\.\\."
    )

    private val entityLocation = mutableMapOf<Entity, LorenzVec>()

    fun addToTrack(entity: Entity) {
        if (entity !in entityLocation) {
            entityLocation[entity] = entity.getLorenzVec()
        }
    }

    @HandleEvent
    fun onTick(event: ClientTickEvent) {
        if (!LorenzUtils.inSkyBlock) return
        addToTrack(Minecraft.getMinecraft().thePlayer)

        for (entity in entityLocation.keys) {
            if (entity.isDead) continue

            val newLocation = entity.getLorenzVec()
            val oldLocation = entityLocation[entity]!!
            val distance = newLocation.distance(oldLocation)
            if (distance > 0.01) {
                entityLocation[entity] = newLocation
                EntityMoveEvent(entity, oldLocation, newLocation, distance).post()
            }
        }
    }

    @HandleEvent
    fun onChat(event: SkyHanniChatEvent) {
        if (!LorenzUtils.inSkyBlock) return
        if (!warpingPattern.matches(event.message)) return
        DelayedRun.runNextTick {
            WarpEvent().post()
        }
    }

    @HandleEvent
    fun onWorldChange(event: WorldChangeEvent) {
        entityLocation.clear()
    }
}

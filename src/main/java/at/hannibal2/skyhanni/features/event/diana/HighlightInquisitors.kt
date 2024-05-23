package at.hannibal2.skyhanni.features.event.diana

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.api.skyblock.SkyBlockAPI
import at.hannibal2.skyhanni.events.entity.EntityEnterWorldEvent
import at.hannibal2.skyhanni.mixins.hooks.RenderLivingEntityHelper
import at.hannibal2.skyhanni.utils.ColourUtils.toChromaColourInt
import net.minecraft.entity.player.EntityPlayer

object HighlightInquisitors {

    private val config get() = SkyHanniMod.feature.event.diana

    @HandleEvent
    fun onJoinWorld(event: EntityEnterWorldEvent) {
        if (!SkyBlockAPI.isConnected) return
        if (!config.highlightInquisitors) return

        val entity = event.entity

        if (entity is EntityPlayer && entity.name == "Minos Inquisitor") {
            val color = config.color.toChromaColourInt()
            RenderLivingEntityHelper.setEntityColorWithNoHurtTime(entity, color) { config.highlightInquisitors }
        }
    }
}

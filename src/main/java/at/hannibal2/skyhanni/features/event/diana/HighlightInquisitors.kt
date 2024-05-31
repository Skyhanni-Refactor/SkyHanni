package at.hannibal2.skyhanni.features.event.diana

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.entity.EntityEnterWorldEvent
import at.hannibal2.skyhanni.mixins.hooks.RenderLivingEntityHelper
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ColourUtils.toChromaColourInt
import net.minecraft.entity.player.EntityPlayer

@SkyHanniModule
object HighlightInquisitors {

    private val config get() = SkyHanniMod.feature.event.diana

    @HandleEvent(onlyOnSkyblock = true)
    fun onInquisitorFound(event: InquisitorFoundEvent) {
        if (!config.highlightInquisitors) return

        val inquisitor = event.inquisitorEntity

        val color = config.color.toChromaColorInt()
        RenderLivingEntityHelper.setEntityColorWithNoHurtTime(inquisitor, color) { config.highlightInquisitors }
    }
}

package at.hannibal2.skyhanni.features.event.hoppity

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.data.mob.MobFilter.isRealPlayer
import at.hannibal2.skyhanni.events.render.entity.EntityRenderLayersEvent
import at.hannibal2.skyhanni.events.render.entity.SkyHanniRenderEntityEvent
import at.hannibal2.skyhanni.utils.LocationUtils.distanceTo
import at.hannibal2.skyhanni.utils.mc.McPlayer
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.player.EntityPlayer
import org.lwjgl.opengl.GL11

object HoppityEggDisplayManager {

    private val config get() = HoppityEggsManager.config
    private var shouldHidePlayer: Boolean = false

    private fun canChangeOpacity(entity: EntityPlayer): Boolean {
        if (!HoppityEggLocator.isEnabled()) return false
        if (entity == McPlayer.player) return false
        if (!entity.isRealPlayer()) return false
        return config.playerOpacity < 100
    }

    @HandleEvent(generic = EntityPlayer::class)
    fun onPreRenderPlayer(event: SkyHanniRenderEntityEvent.Pre<EntityPlayer>) {
        if (!canChangeOpacity(event.entity)) return

        shouldHidePlayer = HoppityEggLocator.sharedEggLocation?.let { event.entity.distanceTo(it) < 4.0 }
            ?: HoppityEggLocator.possibleEggLocations.any { event.entity.distanceTo(it) < 4.0 }

        if (!shouldHidePlayer) return
        if (config.playerOpacity <= 0) return event.cancel()

        GlStateManager.enableBlend()
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
        GlStateManager.color(1.0f, 1.0f, 1.0f, config.playerOpacity / 100f)
    }

    @HandleEvent(generic = EntityPlayer::class)
    fun onPostRenderPlayer(event: SkyHanniRenderEntityEvent.Post<EntityPlayer>) {
        if (!canChangeOpacity(event.entity)) return

        GlStateManager.color(1f, 1f, 1f, 1f)
        GlStateManager.disableBlend()
    }

    @HandleEvent(generic = EntityPlayer::class)
    fun onRenderPlayerLayers(event: EntityRenderLayersEvent.Pre<EntityPlayer>) {
        if (!canChangeOpacity(event.entity)) return
        if (!shouldHidePlayer) return
        event.cancel()
    }
}

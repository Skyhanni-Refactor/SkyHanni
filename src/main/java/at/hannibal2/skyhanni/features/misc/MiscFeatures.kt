package at.hannibal2.skyhanni.features.misc

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.minecraft.ReceiveParticleEvent
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.LorenzUtils
import net.minecraft.util.EnumParticleTypes
import net.minecraftforge.client.event.RenderBlockOverlayEvent
import net.minecraftforge.event.entity.living.EnderTeleportEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

/**
 *  I need these features in my dev env
 */
object MiscFeatures {

    @SubscribeEvent
    fun onEnderTeleport(event: EnderTeleportEvent) {
        if (!LorenzUtils.inSkyBlock) return
        if (!SkyHanniMod.feature.combat.mobs.endermanTeleportationHider) return

        event.isCanceled = true
    }

    @HandleEvent
    fun onReceiveParticle(event: ReceiveParticleEvent) {
        if (!LorenzUtils.inSkyBlock) return
        if (!SkyHanniMod.feature.misc.hideExplosions) return

        when (event.type) {
            EnumParticleTypes.EXPLOSION_LARGE,
            EnumParticleTypes.EXPLOSION_HUGE,
            EnumParticleTypes.EXPLOSION_NORMAL,
            -> event.cancel()

            else -> {}
        }
    }

    @SubscribeEvent
    fun onRenderBlockOverlay(event: RenderBlockOverlayEvent) {
        if (!LorenzUtils.inSkyBlock) return
        if (!SkyHanniMod.feature.misc.hideFireOverlay) return

        if (event.overlayType == RenderBlockOverlayEvent.OverlayType.FIRE) {
            event.isCanceled = true
        }
    }

    fun goToLimbo() {
        ChatUtils.sendMessageToServer("§")
    }
}

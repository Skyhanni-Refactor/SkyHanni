package at.hannibal2.skyhanni.features.rift.area.mirrorverse

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.data.jsonobjects.repo.ParkourJson
import at.hannibal2.skyhanni.events.chat.SkyHanniChatEvent
import at.hannibal2.skyhanni.events.entity.CheckRenderEntityEvent
import at.hannibal2.skyhanni.events.render.world.SkyHanniRenderWorldEvent
import at.hannibal2.skyhanni.events.utils.ConfigLoadEvent
import at.hannibal2.skyhanni.events.utils.RepositoryReloadEvent
import at.hannibal2.skyhanni.features.rift.RiftAPI
import at.hannibal2.skyhanni.utils.ColourUtils.toChromaColour
import at.hannibal2.skyhanni.utils.ConditionalUtils
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.ParkourHelper

object RiftUpsideDownParkour {

    private val config get() = RiftAPI.config.area.mirrorverse.upsideDownParkour
    private var parkourHelper: ParkourHelper? = null

    @HandleEvent
    fun onRepoReload(event: RepositoryReloadEvent) {
        val data = event.getConstant<ParkourJson>("RiftUpsideDownParkour")
        parkourHelper = ParkourHelper(
            data.locations.map { it.add(-1.0, -1.0, -1.0) },// TODO remove offset. change repo instead
            data.shortCuts,
            platformSize = 2.0,
            detectionRange = 2.0
        )
        updateConfig()
    }

    @HandleEvent
    fun onCheckRender(event: CheckRenderEntityEvent<*>) {
        if (!isEnabled()) return
        if (!config.hidePlayers) return

        parkourHelper?.let {
            if (it.inParkour()) {
                event.cancel()
            }
        }
    }

    @HandleEvent
    fun onChat(event: SkyHanniChatEvent) {
        if (!isEnabled()) return

        if (event.message == "§c§lOH NO! THE LAVA OOFED YOU BACK TO THE START!") {
            parkourHelper?.reset()
        }
    }

    @HandleEvent
    fun onConfigLoad(event: ConfigLoadEvent) {
        ConditionalUtils.onToggle(config.rainbowColor, config.monochromeColor, config.lookAhead) {
            updateConfig()
        }
    }

    private fun updateConfig() {
        parkourHelper?.run {
            rainbowColor = config.rainbowColor.get()
            monochromeColor = config.monochromeColor.get().toChromaColour()
            lookAhead = config.lookAhead.get() + 1
            outline = config.outline
        }
    }

    @HandleEvent
    fun onRenderWorld(event: SkyHanniRenderWorldEvent) {
        if (!isEnabled()) return

        parkourHelper?.render(event)
    }

    fun isEnabled() = RiftAPI.inRift() && LorenzUtils.skyBlockArea == "Mirrorverse" && config.enabled
}

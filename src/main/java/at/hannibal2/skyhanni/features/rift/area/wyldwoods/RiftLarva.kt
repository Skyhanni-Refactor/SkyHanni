package at.hannibal2.skyhanni.features.rift.area.wyldwoods

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.utils.SecondPassedEvent
import at.hannibal2.skyhanni.features.rift.RiftAPI
import at.hannibal2.skyhanni.mixins.hooks.RenderLivingEntityHelper
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ColourUtils.toChromaColour
import at.hannibal2.skyhanni.utils.ColourUtils.withAlpha
import at.hannibal2.skyhanni.utils.EntityUtils.hasSkullTexture
import at.hannibal2.skyhanni.utils.ItemUtils.getInternalName
import at.hannibal2.skyhanni.utils.mc.McPlayer
import at.hannibal2.skyhanni.utils.mc.McWorld
import net.minecraft.entity.item.EntityArmorStand

@SkyHanniModule
object RiftLarva {

    private val config get() = RiftAPI.config.area.wyldWoods.larvas
    private var hasHookInHand = false
    private const val LARVE_SKULL_TEXTURE =
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTgzYjMwZTlkMTM1YjA1MTkwZWVhMmMzYWM2MWUyYWI1NWEyZDgxZTFhNThkYmIyNjk4M2ExNDA4MjY2NCJ9fX0="

    @HandleEvent
    fun onSecondPassed(event: SecondPassedEvent) {
        if (!isEnabled()) return

        checkHand()
        if (!hasHookInHand) return
        findLarvas()
    }

    private fun checkHand() {
        hasHookInHand = McPlayer.heldItem?.getInternalName()?.equals("LARVA_HOOK") ?: false
    }

    private fun findLarvas() {
        for (stand in McWorld.getEntitiesOf<EntityArmorStand>()) {
            if (stand.hasSkullTexture(LARVE_SKULL_TEXTURE)) {
                RenderLivingEntityHelper.setEntityColor(
                    stand,
                    config.highlightColor.toChromaColour().withAlpha(1)
                ) { isEnabled() && hasHookInHand }
            }
        }
    }

    fun isEnabled() = RiftAPI.inRift() && config.highlight
}

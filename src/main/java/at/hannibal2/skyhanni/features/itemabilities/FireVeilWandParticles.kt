package at.hannibal2.skyhanni.features.itemabilities

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.features.itemability.FireVeilWandConfig.DisplayEntry
import at.hannibal2.skyhanni.data.ClickType
import at.hannibal2.skyhanni.data.item.SkyhanniItems
import at.hannibal2.skyhanni.events.minecraft.ReceiveParticleEvent
import at.hannibal2.skyhanni.events.minecraft.click.ItemClickEvent
import at.hannibal2.skyhanni.events.render.world.SkyHanniRenderWorldEvent
import at.hannibal2.skyhanni.features.nether.ashfang.AshfangFreezeCooldown
import at.hannibal2.skyhanni.utils.ColourUtils.toChromaColour
import at.hannibal2.skyhanni.utils.ItemUtils.getInternalName
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.RenderUtils
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import net.minecraft.client.Minecraft
import net.minecraft.util.EnumParticleTypes
import kotlin.time.Duration.Companion.seconds

object FireVeilWandParticles {

    private val config get() = SkyHanniMod.feature.inventory.itemAbilities.fireVeilWands

    private var lastClick = SimpleTimeMark.farPast()

    @HandleEvent
    fun onReceiveParticle(event: ReceiveParticleEvent) {
        if (!LorenzUtils.inSkyBlock) return
        if (config.display == DisplayEntry.PARTICLES) return
        if (lastClick.passedSince() > 5.5.seconds) return
        if (event.type == EnumParticleTypes.FLAME && event.speed == 0.55f) {
            event.cancel()
        }
    }

    @HandleEvent
    fun onItemClick(event: ItemClickEvent) {
        if (!LorenzUtils.inSkyBlock) return
        if (event.clickType != ClickType.RIGHT_CLICK) return
        val internalName = event.itemInHand?.getInternalName()

        if (AshfangFreezeCooldown.iscurrentlyFrozen()) return

        if (internalName == SkyhanniItems.FIRE_VEIL_WAND()) {
            lastClick = SimpleTimeMark.now()
        }
    }

    @HandleEvent
    fun onRenderWorld(event: SkyHanniRenderWorldEvent) {
        if (!LorenzUtils.inSkyBlock) return
        if (config.display != DisplayEntry.LINE) return
        if (lastClick.passedSince() > 5.5.seconds) return

        val color = config.displayColor.toChromaColour()
        RenderUtils.drawCircle(Minecraft.getMinecraft().thePlayer, event.partialTicks, 3.5, color)
    }
}

package at.hannibal2.skyhanni.features.rift.area.colosseum

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.minecraft.ClientTickEvent
import at.hannibal2.skyhanni.events.minecraft.WorldChangeEvent
import at.hannibal2.skyhanni.features.rift.RiftAPI
import at.hannibal2.skyhanni.mixins.hooks.RenderLivingEntityHelper
import at.hannibal2.skyhanni.utils.ColourUtils.withAlpha
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.mc.McWorld
import net.minecraft.client.entity.EntityOtherPlayerMP
import net.minecraftforge.event.entity.living.LivingDeathEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.awt.Color

object BlobbercystsHighlight {

    private val config get() = SkyHanniMod.feature.rift.area.colosseum
    private val entityList = mutableListOf<EntityOtherPlayerMP>()
    private val blobberName = "Blobbercyst "

    @HandleEvent
    fun onTick(event: ClientTickEvent) {
        if (!isEnabled()) return
        if (!event.isMod(5)) return
        McWorld.getEntitiesOf<EntityOtherPlayerMP>().forEach {
            if (it.name == blobberName) {
                RenderLivingEntityHelper.setEntityColorWithNoHurtTime(it, Color.RED.withAlpha(80)) { isEnabled() }
                entityList.add(it)
            }
        }
    }

    @HandleEvent
    fun onWorldChange(event: WorldChangeEvent) {
        if (!isEnabled()) return
        entityList.clear()
    }

    @SubscribeEvent
    fun onLivingDeath(event: LivingDeathEvent) {
        if (!isEnabled()) return
        if (entityList.contains(event.entity)) {
            entityList.remove(event.entity)
        }
    }

    fun isEnabled() = RiftAPI.inRift() && config.highlightBlobbercysts && LorenzUtils.skyBlockArea == "Colosseum"
}

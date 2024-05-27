package at.hannibal2.skyhanni.api

import at.hannibal2.skyhanni.events.entity.EntityEnterWorldEvent
import at.hannibal2.skyhanni.events.minecraft.ClientDisconnectEvent
import at.hannibal2.skyhanni.events.minecraft.ClientTickEvent
import at.hannibal2.skyhanni.events.minecraft.ScreenChangeEvent
import at.hannibal2.skyhanni.events.minecraft.WorldChangeEvent
import at.hannibal2.skyhanni.events.render.GameRenderEvent
import at.hannibal2.skyhanni.events.render.entity.SkyHanniRenderEntityEvent
import at.hannibal2.skyhanni.events.render.world.SkyHanniRenderWorldEvent
import at.hannibal2.skyhanni.loadmodule.LoadModule
import at.hannibal2.skyhanni.test.SkyHanniDebugsAndTests
import at.hannibal2.skyhanni.utils.DelayedRun
import at.hannibal2.skyhanni.utils.mc.McPlayer
import net.minecraftforge.client.event.GuiOpenEvent
import net.minecraftforge.client.event.RenderLivingEvent
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.event.entity.EntityJoinWorldEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.RenderTickEvent
import net.minecraftforge.fml.common.network.FMLNetworkEvent


/**
 * Handles all posting of FML events to SkyHanni events.
 */
@LoadModule
object FmlEventAPI {

    private var totalTicks = 0

    @SubscribeEvent
    fun onDisconnect(event: FMLNetworkEvent.ClientDisconnectionFromServerEvent) {
       ClientDisconnectEvent().post()
    }

    @SubscribeEvent
    fun onWorldChange(event: WorldEvent.Load) {
        WorldChangeEvent().post()
    }

    @SubscribeEvent
    fun onTick(event: TickEvent.ClientTickEvent) {
        if (event.phase == TickEvent.Phase.START) return
        if (McPlayer.player == null) return

        DelayedRun.checkRuns()
        totalTicks++
        ClientTickEvent(totalTicks).post()
    }

    @SubscribeEvent
    fun onRenderWorld(event: RenderWorldLastEvent) {
        if (!SkyHanniDebugsAndTests.globalRender) return
        SkyHanniRenderWorldEvent(event.partialTicks).post()
    }

    @SubscribeEvent
    fun onEntityRenderPre(event: RenderLivingEvent.Pre<*>) {
        if (SkyHanniRenderEntityEvent.Pre(event.entity, event.renderer, event.x, event.y, event.z).post()) {
            event.isCanceled = true
        }
    }

    @SubscribeEvent
    fun onEntityRenderPost(event: RenderLivingEvent.Post<*>) {
        SkyHanniRenderEntityEvent.Post(event.entity, event.renderer, event.x, event.y, event.z).post()
    }

    @SubscribeEvent
    fun onEntityRenderSpecialsPre(event: RenderLivingEvent.Specials.Pre<*>) {
        if (SkyHanniRenderEntityEvent.Specials.Pre(event.entity, event.renderer, event.x, event.y, event.z).post()) {
            event.isCanceled = true
        }
    }

    @SubscribeEvent
    fun onEntityRenderSpecialsPost(event: RenderLivingEvent.Specials.Post<*>) {
        SkyHanniRenderEntityEvent.Specials.Post(event.entity, event.renderer, event.x, event.y, event.z).post()
    }

    @SubscribeEvent
    fun onEntityJoinWorld(event: EntityJoinWorldEvent) {
        EntityEnterWorldEvent(event.entity).post()
    }

    @SubscribeEvent
    fun onRenderTick(event: RenderTickEvent) {
        if (event.phase == TickEvent.Phase.START) {
            GameRenderEvent.Start().post()
        } else {
            GameRenderEvent.End().post()
        }
    }

    @SubscribeEvent
    fun onScreenOpen(event: GuiOpenEvent) {
        ScreenChangeEvent(event.gui).post()
    }

}

package at.hannibal2.skyhanni.features.dungeon

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.chat.SkyHanniChatEvent
import at.hannibal2.skyhanni.events.SkyHanniRenderEntityEvent
import at.hannibal2.skyhanni.events.minecraft.ReceiveParticleEvent
import at.hannibal2.skyhanni.features.combat.damageindicator.DamageIndicatorManager
import net.minecraft.entity.EntityLivingBase
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object TerracottaPhase {

    private val config get() = SkyHanniMod.feature.dungeon.terracottaPhase

    private var inTerracottaPhase = false

    @HandleEvent
    fun onChat(event: SkyHanniChatEvent) {
        if (!isEnabled()) return

        if (event.message == "§c[BOSS] Sadan§r§f: So you made it all the way here... Now you wish to defy me? Sadan?!") {
            inTerracottaPhase = true
        }

        if (event.message == "§c[BOSS] Sadan§r§f: ENOUGH!") {
            inTerracottaPhase = false
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    fun onRenderLiving(event: SkyHanniRenderEntityEvent.Specials.Pre<EntityLivingBase>) {
        if (isActive() && config.hideDamageSplash && DamageIndicatorManager.isDamageSplash(event.entity)) {
            event.isCanceled = true
        }
    }

    @HandleEvent
    fun onReceiveParticle(event: ReceiveParticleEvent) {
        if (isActive() && config.hideParticles) {
            event.cancel()
        }
    }

    private fun isActive() = isEnabled() && inTerracottaPhase

    private fun isEnabled() =
        DungeonAPI.inDungeon() && DungeonAPI.inBossRoom && DungeonAPI.getCurrentBoss() == DungeonFloor.F6
}

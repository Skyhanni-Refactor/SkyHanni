package at.hannibal2.skyhanni.api

import at.hannibal2.skyhanni.events.LorenzTickEvent
import at.hannibal2.skyhanni.events.LorenzWorldChangeEvent
import at.hannibal2.skyhanni.events.minecraft.BossbarUpdateEvent
import net.minecraft.entity.boss.BossStatus
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object BossbarAPI {

    private var bossbar: String? = null
    private var previousServerBossbar = ""

    fun getBossbar() = bossbar ?: ""

    @SubscribeEvent
    fun onWorldChange(event: LorenzWorldChangeEvent) {
        previousServerBossbar = bossbar ?: return
        bossbar = null
    }

    @SubscribeEvent
    fun onTick(event: LorenzTickEvent) {
        val bossbarLine = BossStatus.bossName ?: return
        if (bossbarLine.isBlank()) return
        if (bossbarLine == bossbar) return
        if (bossbarLine == previousServerBossbar) return
        if (previousServerBossbar.isNotEmpty()) previousServerBossbar = ""

        bossbar = bossbarLine
        BossbarUpdateEvent(bossbarLine).post()
    }
}

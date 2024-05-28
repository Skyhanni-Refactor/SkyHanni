package at.hannibal2.skyhanni.api

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.minecraft.BossbarUpdateEvent
import at.hannibal2.skyhanni.events.minecraft.ClientTickEvent
import at.hannibal2.skyhanni.events.minecraft.WorldChangeEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import net.minecraft.entity.boss.BossStatus

@SkyHanniModule
object BossbarAPI {

    private var bossbar: String? = null
    private var previousServerBossbar = ""

    fun getBossbar() = bossbar ?: ""

    @HandleEvent
    fun onWorldChange(event: WorldChangeEvent) {
        previousServerBossbar = bossbar ?: return
        bossbar = null
    }

    @HandleEvent
    fun onTick(event: ClientTickEvent) {
        val bossbarLine = BossStatus.bossName ?: return
        if (bossbarLine.isBlank()) return
        if (bossbarLine == bossbar) return
        if (bossbarLine == previousServerBossbar) return
        if (previousServerBossbar.isNotEmpty()) previousServerBossbar = ""

        bossbar = bossbarLine
        BossbarUpdateEvent(bossbarLine).post()
    }
}

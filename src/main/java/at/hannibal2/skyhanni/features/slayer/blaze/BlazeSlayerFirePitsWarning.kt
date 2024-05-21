package at.hannibal2.skyhanni.features.slayer.blaze

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.data.TitleManager
import at.hannibal2.skyhanni.events.entity.BossHealthChangeEvent
import at.hannibal2.skyhanni.events.minecraft.ClientTickEvent
import at.hannibal2.skyhanni.features.combat.damageindicator.BossType
import at.hannibal2.skyhanni.features.combat.damageindicator.DamageIndicatorManager
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.mc.McSound
import kotlin.time.Duration.Companion.seconds

object BlazeSlayerFirePitsWarning {

    private val config get() = SkyHanniMod.feature.slayer.blazes

    private var lastFirePitsWarning = SimpleTimeMark.farPast()

    private fun fireFirePits() {
        TitleManager.sendTitle("§cFire Pits!", 2.seconds)
        lastFirePitsWarning = SimpleTimeMark.now()
    }

    @HandleEvent
    fun onTick(event: ClientTickEvent) {
        if (!isEnabled()) return
        if (!event.isMod(10)) return

        if (lastFirePitsWarning.passedSince() < 2.seconds) {
            McSound.play("random.orb", 0.8f, 1f)
        }
    }

    @HandleEvent
    fun onBossHealthChange(event: BossHealthChangeEvent) {
        if (!isEnabled()) return
        val entityData = event.entityData

        val health = event.health
        val maxHealth = event.maxHealth
        val lastHealth = event.lastHealth

        val percentHealth = maxHealth * 0.33
        if (health < percentHealth && lastHealth > percentHealth) {
            when (entityData.bossType) {
                BossType.SLAYER_BLAZE_3,
                BossType.SLAYER_BLAZE_4,
                -> {
                    fireFirePits()
                }

                else -> {}
            }
        }
    }

    private fun isEnabled() =
        LorenzUtils.inSkyBlock && config.firePitsWarning && DamageIndicatorManager.isBossSpawned(
            BossType.SLAYER_BLAZE_3,
            BossType.SLAYER_BLAZE_4,
            BossType.SLAYER_BLAZE_QUAZII_3,
            BossType.SLAYER_BLAZE_QUAZII_4,
            BossType.SLAYER_BLAZE_TYPHOEUS_3,
            BossType.SLAYER_BLAZE_TYPHOEUS_4,
        )
}

package at.hannibal2.skyhanni.features.nether.ashfang

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.LorenzTickEvent
import at.hannibal2.skyhanni.events.LorenzWorldChangeEvent
import at.hannibal2.skyhanni.events.render.gui.GuiRenderEvent
import at.hannibal2.skyhanni.features.combat.damageindicator.BossType
import at.hannibal2.skyhanni.features.combat.damageindicator.DamageIndicatorManager
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.RenderUtils.renderString
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.datetime.TimeUnit
import at.hannibal2.skyhanni.utils.datetime.TimeUtils.format
import at.hannibal2.skyhanni.utils.mc.McWorld
import net.minecraft.entity.item.EntityArmorStand
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.time.Duration.Companion.seconds

object AshfangNextResetCooldown {

    private val config get() = SkyHanniMod.feature.crimsonIsle.ashfang
    private var spawnTime = SimpleTimeMark.farPast()

    @SubscribeEvent
    fun onTick(event: LorenzTickEvent) {
        if (!isEnabled()) return

        if (McWorld.getEntitiesOf<EntityArmorStand>().any {
                it.posY > 145 && (it.name.contains("§c§9Ashfang Acolyte§r") || it.name.contains("§c§cAshfang Underling§r"))
            }) {
            spawnTime = SimpleTimeMark.now()
        }
    }

    @HandleEvent
    fun onRenderOverlay(event: GuiRenderEvent.GuiOverlayRenderEvent) {
        if (!isEnabled()) return
        if (spawnTime.isFarPast()) return

        val passedSince = spawnTime.passedSince()
        if (passedSince < 46.1.seconds) {
            val format = passedSince.format(TimeUnit.SECOND, showMilliSeconds = true)
            config.nextResetCooldownPos.renderString(
                "§cAshfang next reset in: §a$format",
                posLabel = "Ashfang Reset Cooldown"
            )
        } else {
            spawnTime = SimpleTimeMark.farPast()
        }
    }

    @SubscribeEvent
    fun onWorldChange(event: LorenzWorldChangeEvent) {
        spawnTime = SimpleTimeMark.farPast()
    }

    private fun isEnabled(): Boolean {
        return LorenzUtils.inSkyBlock && config.nextResetCooldown &&
            DamageIndicatorManager.isBossSpawned(BossType.NETHER_ASHFANG)
    }
}

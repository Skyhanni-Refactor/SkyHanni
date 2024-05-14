package at.hannibal2.skyhanni.features.nether.ashfang

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.events.LorenzRenderWorldEvent
import at.hannibal2.skyhanni.events.LorenzTickEvent
import at.hannibal2.skyhanni.events.LorenzWorldChangeEvent
import at.hannibal2.skyhanni.features.combat.damageindicator.BossType
import at.hannibal2.skyhanni.features.combat.damageindicator.DamageIndicatorManager
import at.hannibal2.skyhanni.utils.ColourUtils.toChromaColour
import at.hannibal2.skyhanni.utils.EntityUtils.hasSkullTexture
import at.hannibal2.skyhanni.utils.LocationUtils
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.RenderUtils
import at.hannibal2.skyhanni.utils.RenderUtils.drawString
import at.hannibal2.skyhanni.utils.getLorenzVec
import at.hannibal2.skyhanni.utils.mc.McWorld
import net.minecraft.entity.item.EntityArmorStand
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object AshfangGravityOrbs {

    private val config get() = SkyHanniMod.feature.crimsonIsle.ashfang.gravityOrbs

    private const val ORB_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV" +
        "0L3RleHR1cmUvMWE2OWNjZjdhZDkwNGM5YTg1MmVhMmZmM2Y1YjRlMjNhZGViZjcyZWQxMmQ1ZjI0Yjc4Y2UyZDQ0YjRhMiJ9fX0="
    private val orbs = mutableListOf<EntityArmorStand>()

    @SubscribeEvent
    fun onTick(event: LorenzTickEvent) {
        if (!isEnabled()) return

        McWorld.getEntitiesOf<EntityArmorStand>()
            .filter { it !in orbs && it.hasSkullTexture(ORB_TEXTURE) }
            .forEach { orbs.add(it) }
    }

    @SubscribeEvent
    fun onRenderWorld(event: LorenzRenderWorldEvent) {
        if (!isEnabled()) return

        val color = config.color.toChromaColour()
        val playerLocation = LocationUtils.playerLocation()
        for (orb in orbs) {
            if (orb.isDead) continue
            val orbLocation = orb.getLorenzVec()
            val center = orbLocation.add(-0.5, -2.0, -0.5)
            RenderUtils.drawCylinderInWorld(color, center.x, center.y, center.z, 3.5f, 4.5f, event.partialTicks)

            if (orbLocation.distance(playerLocation) < 15) {
                // TODO find way to dynamically change color
                event.drawString(orbLocation.add(y = 2.5), "§cGravity Orb")
            }
        }
    }

    @SubscribeEvent
    fun onWorldChange(event: LorenzWorldChangeEvent) {
        orbs.clear()
    }

    private fun isEnabled() = LorenzUtils.inSkyBlock && config.enabled &&
        DamageIndicatorManager.isBossSpawned(BossType.NETHER_ASHFANG)
}

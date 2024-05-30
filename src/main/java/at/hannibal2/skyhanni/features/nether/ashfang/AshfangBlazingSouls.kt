package at.hannibal2.skyhanni.features.nether.ashfang

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.api.skyblock.SkyBlockAPI
import at.hannibal2.skyhanni.events.minecraft.ClientTickEvent
import at.hannibal2.skyhanni.events.minecraft.WorldChangeEvent
import at.hannibal2.skyhanni.events.render.world.SkyHanniRenderWorldEvent
import at.hannibal2.skyhanni.features.combat.damageindicator.BossType
import at.hannibal2.skyhanni.features.combat.damageindicator.DamageIndicatorManager
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ColourUtils.toChromaColour
import at.hannibal2.skyhanni.utils.EntityUtils.hasSkullTexture
import at.hannibal2.skyhanni.utils.LocationUtils
import at.hannibal2.skyhanni.utils.RenderUtils.drawString
import at.hannibal2.skyhanni.utils.RenderUtils.drawWaypointFilled
import at.hannibal2.skyhanni.utils.getLorenzVec
import at.hannibal2.skyhanni.utils.mc.McWorld
import net.minecraft.entity.item.EntityArmorStand

@SkyHanniModule
object AshfangBlazingSouls {

    private val config get() = SkyHanniMod.feature.crimsonIsle.ashfang.blazingSouls

    private const val SOUL_TEXTURE =
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODI4N2IzOTdkYWY5NTE2YTBiZDc2ZjVmMWI3YmY5Nzk1MTVkZjNkNWQ4MzNlMDYzNWZhNjhiMzdlZTA4MjIxMiJ9fX0="
    private val souls = mutableListOf<EntityArmorStand>()

    @HandleEvent
    fun onTick(event: ClientTickEvent) {
        if (!isEnabled()) return

        McWorld.getEntitiesOf<EntityArmorStand>()
            .filter { it !in souls && it.hasSkullTexture(SOUL_TEXTURE) }
            .forEach { souls.add(it) }
    }

    @HandleEvent
    fun onRenderWorld(event: SkyHanniRenderWorldEvent) {
        if (!isEnabled()) return

        val color = config.color.toChromaColour()

        val playerLocation = LocationUtils.playerLocation()
        for (orb in souls) {
            if (orb.isDead) continue
            val orbLocation = orb.getLorenzVec()
            event.drawWaypointFilled(orbLocation.add(-0.5, 1.25, -0.5), color, extraSize = -0.15)
            if (orbLocation.distance(playerLocation) < 10) {
                // TODO find way to dynamically change color
                event.drawString(orbLocation.add(y = 2.5), "§bBlazing Soul")
            }
        }
    }

    @HandleEvent
    fun onWorldChange(event: WorldChangeEvent) {
        souls.clear()
    }

    private fun isEnabled() = SkyBlockAPI.isConnected && config.enabled &&
        DamageIndicatorManager.isBossSpawned(BossType.NETHER_ASHFANG)
}

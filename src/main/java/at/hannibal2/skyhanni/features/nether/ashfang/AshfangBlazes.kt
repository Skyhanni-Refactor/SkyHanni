package at.hannibal2.skyhanni.features.nether.ashfang

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.events.LorenzWorldChangeEvent
import at.hannibal2.skyhanni.events.SkyHanniRenderEntityEvent
import at.hannibal2.skyhanni.events.entity.EntityHealthUpdateEvent
import at.hannibal2.skyhanni.events.utils.SecondPassedEvent
import at.hannibal2.skyhanni.features.combat.damageindicator.BossType
import at.hannibal2.skyhanni.features.combat.damageindicator.DamageIndicatorManager
import at.hannibal2.skyhanni.mixins.hooks.RenderLivingEntityHelper
import at.hannibal2.skyhanni.utils.CollectionUtils.editCopy
import at.hannibal2.skyhanni.utils.ColourUtils.withAlpha
import at.hannibal2.skyhanni.utils.EntityUtils.getAllNameTagsWith
import at.hannibal2.skyhanni.utils.LorenzColor
import at.hannibal2.skyhanni.utils.LorenzUtils.isInIsland
import at.hannibal2.skyhanni.utils.mc.McWorld
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.entity.monster.EntityBlaze
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object AshfangBlazes {

    private val config get() = SkyHanniMod.feature.crimsonIsle.ashfang

    private val blazeColor = mutableMapOf<EntityBlaze, LorenzColor>()
    private var blazeArmorStand = mapOf<EntityBlaze, EntityArmorStand>()

    private var nearAshfang = false

    @HandleEvent
    fun onSecondPassed(event: SecondPassedEvent) {
        if (!isEnabled()) return
        checkNearAshfang()

        if (nearAshfang) {
            for (entity in McWorld.getEntitiesOf<EntityBlaze>()
                .filter { it !in blazeColor.keys }) {
                val list = entity.getAllNameTagsWith(2, "Ashfang")
                if (list.size == 1) {
                    val armorStand = list[0]
                    val color = when {
                        armorStand.name.contains("Ashfang Follower") -> LorenzColor.DARK_GRAY
                        armorStand.name.contains("Ashfang Underling") -> LorenzColor.RED
                        armorStand.name.contains("Ashfang Acolyte") -> LorenzColor.BLUE
                        else -> {
                            blazeArmorStand = blazeArmorStand.editCopy {
                                remove(entity)
                            }
                            continue
                        }
                    }
                    blazeArmorStand = blazeArmorStand.editCopy {
                        this[entity] = armorStand
                    }
                    entity setBlazeColor color
                }
            }
        }
    }

    @HandleEvent
    fun onEntityHealthUpdate(event: EntityHealthUpdateEvent) {
        if (!isEnabled()) return

        val entityId = event.entity.entityId
        if (entityId !in blazeArmorStand.keys.map { it.entityId }) return

        if (event.health % 10_000_000 != 0) {
            blazeArmorStand = blazeArmorStand.editCopy {
                keys.removeIf { it.entityId == entityId }
            }
        }
    }

    private fun checkNearAshfang() {
        nearAshfang = McWorld.getEntitiesOf<EntityArmorStand>().any { it.name.contains("Ashfang") }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    fun onRenderLiving(event: SkyHanniRenderEntityEvent.Specials.Pre<EntityLivingBase>) {
        if (!isEnabled()) return
        if (!config.hide.fullNames) return

        val entity = event.entity
        if (entity !is EntityArmorStand) return
        if (!entity.hasCustomName()) return
        if (entity.isDead) return
        if (entity in blazeArmorStand.values) {
            event.isCanceled = true
        }
    }

    @SubscribeEvent
    fun onWorldChange(event: LorenzWorldChangeEvent) {
        blazeColor.clear()
        blazeArmorStand = emptyMap()
    }

    private fun isEnabled(): Boolean {
        return IslandType.CRIMSON_ISLE.isInIsland() && DamageIndicatorManager.isBossSpawned(BossType.NETHER_ASHFANG)
    }

    private infix fun EntityBlaze.setBlazeColor(color: LorenzColor) {
        blazeColor[this] = color
        RenderLivingEntityHelper.setEntityColorWithNoHurtTime(
            this,
            color.toColor().withAlpha(40),
        ) { isEnabled() && config.highlightBlazes }
    }
}

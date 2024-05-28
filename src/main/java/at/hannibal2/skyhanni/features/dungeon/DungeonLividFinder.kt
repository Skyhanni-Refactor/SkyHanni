package at.hannibal2.skyhanni.features.dungeon

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.entity.CheckRenderEntityEvent
import at.hannibal2.skyhanni.events.minecraft.ClientTickEvent
import at.hannibal2.skyhanni.events.minecraft.WorldChangeEvent
import at.hannibal2.skyhanni.events.render.world.SkyHanniRenderWorldEvent
import at.hannibal2.skyhanni.mixins.hooks.RenderLivingEntityHelper
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.test.GriffinUtils.drawWaypointFilled
import at.hannibal2.skyhanni.utils.ColourUtils.withAlpha
import at.hannibal2.skyhanni.utils.LocationUtils.distanceSqToPlayer
import at.hannibal2.skyhanni.utils.LorenzColor
import at.hannibal2.skyhanni.utils.LorenzColor.Companion.toLorenzColor
import at.hannibal2.skyhanni.utils.LorenzVec
import at.hannibal2.skyhanni.utils.RenderUtils.draw3DLine
import at.hannibal2.skyhanni.utils.RenderUtils.drawDynamicText
import at.hannibal2.skyhanni.utils.RenderUtils.exactPlayerEyeLocation
import at.hannibal2.skyhanni.utils.getLorenzVec
import at.hannibal2.skyhanni.utils.math.BoundingBox
import at.hannibal2.skyhanni.utils.mc.McWorld
import at.hannibal2.skyhanni.utils.mc.McWorld.getBlockStateAt
import net.minecraft.block.BlockStainedGlass
import net.minecraft.client.Minecraft
import net.minecraft.client.entity.EntityOtherPlayerMP
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.potion.Potion

@SkyHanniModule
object DungeonLividFinder {

    private val config get() = SkyHanniMod.feature.dungeon.lividFinder
    private val blockLocation = LorenzVec(6, 109, 43)

    var lividEntity: EntityOtherPlayerMP? = null
    private var lividArmorStand: EntityArmorStand? = null
    private var gotBlinded = false
    private var color: LorenzColor? = null

    @HandleEvent
    fun onTick(event: ClientTickEvent) {
        if (!inDungeon()) return

        val isCurrentlyBlind = isCurrentlyBlind()
        if (!gotBlinded) {
            gotBlinded = isCurrentlyBlind
            return
        } else if (isCurrentlyBlind) return

        if (!config.enabled) return

        val dyeColor = blockLocation.getBlockStateAt().getValue(BlockStainedGlass.COLOR)
        color = dyeColor.toLorenzColor()

        val color = color ?: return
        val chatColor = color.getChatColor()

        lividArmorStand = McWorld.getEntitiesOf<EntityArmorStand>()
            .firstOrNull { it.name.startsWith("${chatColor}﴾ ${chatColor}§lLivid") }
        val lividArmorStand = lividArmorStand ?: return

        val box = with(lividArmorStand) {
            BoundingBox(
                posX - 0.5,
                posY - 2,
                posZ - 0.5,
                posX + 0.5,
                posY,
                posZ + 0.5
            )
        }

        val newLivid = McWorld.getEntitiesInBox<EntityOtherPlayerMP>(box)
            .takeIf { it.size == 1 }?.firstOrNull() ?: return
        if (!newLivid.name.contains("Livid")) return

        lividEntity = newLivid
        RenderLivingEntityHelper.setEntityColorWithNoHurtTime(
            newLivid,
            color.toColor().withAlpha(30)
        ) { shouldHighlight() }
    }

    private fun shouldHighlight() = getLividAlive() != null && config.enabled

    private fun getLividAlive() = lividEntity?.let {
        if (!it.isDead && it.health > 0.5) it else null
    }

    @HandleEvent
    fun onCheckRender(event: CheckRenderEntityEvent<*>) {
        if (!inDungeon()) return
        if (!config.hideWrong) return
        if (!config.enabled) return

        val entity = event.entity
        if (entity is EntityPlayerSP) return
        val livid = getLividAlive() ?: return

        if (entity != livid && entity != lividArmorStand) {
            if (entity.name.contains("Livid")) {
                event.cancel()
            }
        }
    }

    private fun isCurrentlyBlind() = if (Minecraft.getMinecraft().thePlayer.isPotionActive(Potion.blindness)) {
        Minecraft.getMinecraft().thePlayer.getActivePotionEffect(Potion.blindness).duration > 10
    } else false

    @HandleEvent
    fun onRenderWorld(event: SkyHanniRenderWorldEvent) {
        if (!inDungeon()) return
        if (!config.enabled) return

        val livid = getLividAlive() ?: return
        val location = livid.getLorenzVec().add(-0.5, 0.0, -0.5)

        val lorenzColor = color ?: return

        event.drawDynamicText(location, lorenzColor.getChatColor() + "Livid", 1.5)

        if (location.distanceSqToPlayer() < 50) return

        val color = lorenzColor.toColor()
        event.draw3DLine(event.exactPlayerEyeLocation(), location.add(0.5, 0.0, 0.5), color, 3, true)
        event.drawWaypointFilled(location, color, beacon = false, seeThroughBlocks = true)
    }

    @HandleEvent
    fun onWorldChange(event: WorldChangeEvent) {
        lividEntity = null
        gotBlinded = false
    }

    private fun inDungeon(): Boolean {
        if (!DungeonAPI.inDungeon()) return false
        if (!DungeonAPI.inBossRoom) return false
        if (!DungeonAPI.isOneOf("F5", "M5")) return false

        return true
    }
}

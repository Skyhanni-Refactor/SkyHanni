package at.hannibal2.skyhanni.features.garden.visitor

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.api.skyblock.Gamemode
import at.hannibal2.skyhanni.api.skyblock.SkyBlockAPI
import at.hannibal2.skyhanni.config.features.garden.visitor.VisitorConfig.VisitorBlockBehaviour
import at.hannibal2.skyhanni.data.jsonobjects.repo.GardenJson
import at.hannibal2.skyhanni.data.jsonobjects.repo.GardenVisitor
import at.hannibal2.skyhanni.events.minecraft.packet.SendPacketEvent
import at.hannibal2.skyhanni.events.utils.RepositoryReloadEvent
import at.hannibal2.skyhanni.events.utils.SecondPassedEvent
import at.hannibal2.skyhanni.features.garden.GardenAPI
import at.hannibal2.skyhanni.mixins.hooks.RenderLivingEntityHelper
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.ColourUtils.withAlpha
import at.hannibal2.skyhanni.utils.EntityUtils.getSkinTexture
import at.hannibal2.skyhanni.utils.LorenzColor
import at.hannibal2.skyhanni.utils.mc.McWorld
import at.hannibal2.skyhanni.utils.toLorenzVec
import io.github.moulberry.notenoughupdates.util.SBInfo
import net.minecraft.client.Minecraft
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.network.play.client.C02PacketUseEntity

object HighlightVisitorsOutsideOfGarden {

    private var visitorJson = mapOf<String?, List<GardenVisitor>>()

    private val config get() = GardenAPI.config.visitors

    @HandleEvent
    fun onRepoReload(event: RepositoryReloadEvent) {
        visitorJson = event.getConstant<GardenJson>(
            "Garden", GardenJson::class.java
        ).visitors.values.groupBy {
            it.mode
        }
        for (list in visitorJson.values) {
            for (visitor in list) {
                visitor.skinOrType = visitor.skinOrType?.replace("\\n", "")?.replace("\n", "")
            }
        }
    }

    private fun getSkinOrTypeFor(entity: Entity): String {
        if (entity is EntityPlayer) {
            return entity.getSkinTexture() ?: "no skin"
        }
        return entity.javaClass.simpleName
    }

    private fun isVisitor(entity: Entity): Boolean {
        val mode = SBInfo.getInstance().getLocation()
        val possibleJsons = visitorJson[mode] ?: return false
        val skinOrType = getSkinOrTypeFor(entity)
        return possibleJsons.any {
            (it.position == null || it.position.distance(entity.position.toLorenzVec()) < 1)
                && it.skinOrType == skinOrType
        }
    }

    @HandleEvent
    fun onSecondPassed(event: SecondPassedEvent) {
        if (!config.highlightVisitors) return
        McWorld.getEntitiesOf<EntityLivingBase>()
            .filter { it !is EntityArmorStand && isVisitor(it) }
            .forEach {
                RenderLivingEntityHelper.setEntityColor(
                    it,
                    LorenzColor.DARK_RED.toColor().withAlpha(50)
                ) { config.highlightVisitors }
            }
    }

    private val shouldBlock
        get() = when (config.blockInteracting) {
            VisitorBlockBehaviour.DONT -> false
            VisitorBlockBehaviour.ALWAYS -> true
            VisitorBlockBehaviour.ONLY_ON_BINGO -> SkyBlockAPI.gamemode == Gamemode.BINGO
            null -> false
        }

    private fun isVisitorNearby(entity: Entity) =
        McWorld.getEntitiesNear<EntityLivingBase>(entity, 2.0).any(::isVisitor)

    @HandleEvent
    fun onClickEntity(event: SendPacketEvent) {
        if (!shouldBlock) return
        val world = Minecraft.getMinecraft().theWorld ?: return
        val player = Minecraft.getMinecraft().thePlayer ?: return
        if (player.isSneaking) return
        val packet = event.packet as? C02PacketUseEntity ?: return
        val entity = packet.getEntityFromWorld(world) ?: return
        if (isVisitor(entity) || (entity is EntityArmorStand && isVisitorNearby(entity))) {
            event.cancel()
            if (packet.action == C02PacketUseEntity.Action.INTERACT) {
                ChatUtils.chatAndOpenConfig("Blocked you from interacting with a visitor. Sneak to bypass or click here to change settings.",
                    GardenAPI.config.visitors::blockInteracting
                )
            }
        }
    }
}

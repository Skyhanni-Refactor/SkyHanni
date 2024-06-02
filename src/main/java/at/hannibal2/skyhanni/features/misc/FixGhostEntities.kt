package at.hannibal2.skyhanni.features.misc

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.events.CheckRenderEntityEvent
import at.hannibal2.skyhanni.events.LorenzWorldChangeEvent
import at.hannibal2.skyhanni.events.PacketEvent
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.MobUtils.isDefaultValue
import net.minecraft.entity.item.EntityArmorStand
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.api.skyblock.SkyBlockAPI
import at.hannibal2.skyhanni.events.minecraft.WorldChangeEvent
import at.hannibal2.skyhanni.events.minecraft.packet.ReceivePacketEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import net.minecraft.network.play.server.S0CPacketSpawnPlayer
import net.minecraft.network.play.server.S0FPacketSpawnMob
import net.minecraft.network.play.server.S13PacketDestroyEntities

/**
 * This feature fixes ghost entities sent by hypixel that are not properly deleted in the correct order.
 * This included Diana, Dungeon and Crimson Isle mobs and nametags.
 */
@SkyHanniModule
object FixGhostEntities {

    private val config get() = SkyHanniMod.feature.misc

    private var recentlyRemovedEntities = ArrayDeque<Int>()
    private var recentlySpawnedEntities = ArrayDeque<Int>()

    @HandleEvent
    fun onWorldChange(event: WorldChangeEvent) {
        recentlyRemovedEntities = ArrayDeque()
        recentlySpawnedEntities = ArrayDeque()
    }

    @HandleEvent
    fun onReceiveCurrentShield(event: ReceivePacketEvent) {
        if (!isEnabled()) return

        when (val packet = event.packet) {
            is S0CPacketSpawnPlayer -> {
                if (packet.entityID in recentlyRemovedEntities) {
                    event.cancel()
                }
                recentlySpawnedEntities.addLast(packet.entityID)
            }

            is S0FPacketSpawnMob -> {
                if (packet.entityID in recentlyRemovedEntities) {
                    event.cancel()
                }
                recentlySpawnedEntities.addLast(packet.entityID)
            }

            is S13PacketDestroyEntities -> {
                for (entityID in packet.entityIDs) {
                    // ignore entities that got properly spawned and then removed
                    if (entityID !in recentlySpawnedEntities) {
                        recentlyRemovedEntities.addLast(entityID)
                        if (recentlyRemovedEntities.size == 10) {
                            recentlyRemovedEntities.removeFirst()
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    fun onCheckRender(event: CheckRenderEntityEvent<*>) {
        if (!LorenzUtils.inSkyBlock || !config.hideTemporaryArmorstands) return
        if (event.entity !is EntityArmorStand) return
        with(event.entity) {
            if (ticksExisted < 10 && isDefaultValue() && inventory.all { it == null }) event.cancel()
        }
    }

    fun isEnabled() = SkyBlockAPI.isConnected && config.fixGhostEntities
}

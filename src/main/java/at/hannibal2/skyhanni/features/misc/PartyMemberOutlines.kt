package at.hannibal2.skyhanni.features.misc

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.api.skyblock.SkyBlockAPI
import at.hannibal2.skyhanni.config.enums.OutsideSbFeature
import at.hannibal2.skyhanni.data.PartyAPI
import at.hannibal2.skyhanni.events.render.entity.RenderEntityOutlineEvent
import at.hannibal2.skyhanni.features.dungeon.DungeonAPI
import at.hannibal2.skyhanni.utils.ColourUtils.toChromaColourInt
import net.minecraft.client.entity.EntityOtherPlayerMP
import net.minecraft.entity.Entity

object PartyMemberOutlines {

    private val config get() = SkyHanniMod.feature.misc.highlightPartyMembers

    @HandleEvent
    fun onRenderEntityOutlines(event: RenderEntityOutlineEvent) {
        if (isEnabled() && event.type === RenderEntityOutlineEvent.Type.NO_XRAY) {
            event.queueEntitiesToOutline { entity -> getEntityOutlineColor(entity) }
        }
    }

    fun isEnabled() = config.enabled &&
        (SkyBlockAPI.isConnected || OutsideSbFeature.HIGHLIGHT_PARTY_MEMBERS.isSelected()) && !DungeonAPI.inDungeon()

    private fun getEntityOutlineColor(entity: Entity): Int? {
        if (entity !is EntityOtherPlayerMP || !PartyAPI.partyMembers.contains(entity.name)) return null

        return config.outlineColor.toChromaColourInt()
    }
}

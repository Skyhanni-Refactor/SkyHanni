package at.hannibal2.skyhanni.features.dungeon

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.api.skyblock.IslandType
import at.hannibal2.skyhanni.events.entity.EntityMaxHealthUpdateEvent
import at.hannibal2.skyhanni.mixins.hooks.RenderLivingEntityHelper
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ColourUtils.withAlpha
import at.hannibal2.skyhanni.utils.LorenzColor
import net.minecraft.entity.monster.EntitySilverfish

@SkyHanniModule
object HighlightDungeonDeathmite {

    @HandleEvent(onlyOnIsland = IslandType.CATACOMBS)
    fun onEntityHealthUpdate(event: EntityMaxHealthUpdateEvent) {
        if (!SkyHanniMod.feature.dungeon.highlightDeathmites) return

        val entity = event.entity
        val maxHealth = event.normalizedMaxHealth

        if (entity is EntitySilverfish && maxHealth == 1_000_000_000) {
            RenderLivingEntityHelper.setEntityColorWithNoHurtTime(entity, LorenzColor.DARK_RED.toColor().withAlpha(20))
            { SkyHanniMod.feature.dungeon.highlightDeathmites }
        }
    }
}

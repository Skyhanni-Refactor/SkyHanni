package at.hannibal2.skyhanni.features.misc.teleportpad

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.api.skyblock.IslandType
import at.hannibal2.skyhanni.events.render.entity.SkyHanniRenderEntityEvent
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraft.entity.item.EntityArmorStand

object TeleportPadCompactName {
    private val patternGroup = RepoPattern.group("misc.teleportpad")
    private val namePattern by patternGroup.pattern(
        "name",
        "§.✦ §aWarp To (?<name>.*)"
    )
    private val noNamePattern by patternGroup.pattern(
        "noname",
        "§.✦ §cNo Destination"
    )

    @HandleEvent(
        onlyOnIsland = IslandType.PRIVATE_ISLAND,
        priority = HandleEvent.HIGH,
        generic = EntityArmorStand::class
    )
    fun onRenderLiving(event: SkyHanniRenderEntityEvent.Specials.Pre<EntityArmorStand>) {
        if (!SkyHanniMod.feature.misc.teleportPad.compactName) return
        val entity = event.entity

        val name = entity.name

        noNamePattern.matchMatcher(name) {
            event.cancel()
        }

        namePattern.matchMatcher(name) {
            entity.customNameTag = group("name")
        }
    }
}

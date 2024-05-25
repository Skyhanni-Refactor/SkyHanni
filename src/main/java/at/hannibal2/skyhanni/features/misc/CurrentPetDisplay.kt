package at.hannibal2.skyhanni.features.misc

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.api.skyblock.IslandType
import at.hannibal2.skyhanni.data.PetAPI
import at.hannibal2.skyhanni.events.chat.SkyHanniChatEvent
import at.hannibal2.skyhanni.events.inventory.InventoryFullyOpenedEvent
import at.hannibal2.skyhanni.events.render.gui.GuiOverlayRenderEvent
import at.hannibal2.skyhanni.features.rift.RiftAPI
import at.hannibal2.skyhanni.utils.ItemUtils.getLore
import at.hannibal2.skyhanni.utils.RegexUtils.matchFirst
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.RenderUtils.renderString
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern

object CurrentPetDisplay {

    private val config get() = SkyHanniMod.feature.misc.pets

    private val patternGroup = RepoPattern.group("misc.currentpet")
    private val inventorySelectedPetPattern by patternGroup.pattern(
        "inventory.selected",
        "§7§7Selected pet: (?<pet>.*)"
    )
    private val chatSpawnPattern by patternGroup.pattern(
        "chat.spawn",
        "§aYou summoned your §r(?<pet>.*)§r§a!"
    )
    private val chatDespawnPattern by patternGroup.pattern(
        "chat.despawn",
        "§aYou despawned your §r.*§r§a!"
    )
    private val chatPetRulePattern by patternGroup.pattern(
        "chat.rule",
        "§cAutopet §eequipped your §7\\[Lvl .*] (?<pet>.*)! §a§lVIEW RULE"
    )

    @HandleEvent
    fun onChat(event: SkyHanniChatEvent) {
        findPetInChat(event.message)?.let {
            PetAPI.currentPet = it
            if (config.hideAutopet) {
                event.blockedReason = "pets"
            }
        }
    }

    private fun findPetInChat(message: String): String? {
        chatSpawnPattern.matchMatcher(message) {
            return group("pet")
        }
        if (chatDespawnPattern.matches(message)) {
            return ""
        }
        chatPetRulePattern.matchMatcher(message) {
            return group("pet")
        }

        return null
    }

    @HandleEvent
    fun onInventoryOpen(event: InventoryFullyOpenedEvent) {
        if (!PetAPI.isPetMenu(event.inventoryName)) return

        val lore = event.inventoryItems[4]?.getLore() ?: return
        lore.matchFirst(inventorySelectedPetPattern) {
            val newPet = group("pet")
            PetAPI.currentPet = if (newPet != "§cNone") newPet else ""
        }
    }

    @HandleEvent(onlyOnIsland = IslandType.THE_RIFT)
    fun onRenderOverlay(event: GuiOverlayRenderEvent) {
        if (RiftAPI.inRift()) return

        if (!config.display) return

        config.displayPos.renderString(PetAPI.currentPet, posLabel = "Current Pet")
    }
}

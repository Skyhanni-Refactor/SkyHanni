package at.hannibal2.skyhanni.features.garden.farming

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.data.TitleManager
import at.hannibal2.skyhanni.data.item.SkyhanniItems
import at.hannibal2.skyhanni.events.inventory.InventoryCloseEvent
import at.hannibal2.skyhanni.events.inventory.OwnInventoryItemUpdateEvent
import at.hannibal2.skyhanni.features.garden.GardenAPI
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.ItemBlink
import at.hannibal2.skyhanni.utils.ItemUtils.getInternalName
import at.hannibal2.skyhanni.utils.ItemUtils.name
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.mc.McSound
import at.hannibal2.skyhanni.utils.mc.McSound.play
import kotlin.time.Duration.Companion.seconds

object WildStrawberryDyeNotification {

    private var lastCloseTime = SimpleTimeMark.farPast()

    @HandleEvent
    fun onInventoryClose(event: InventoryCloseEvent) {
        lastCloseTime = SimpleTimeMark.now()
    }

    @HandleEvent
    fun onOwnInventoryItemUpdate(event: OwnInventoryItemUpdateEvent) {
        if (!GardenAPI.inGarden()) return
        if (!GardenAPI.config.wildStrawberryDyeNotification) return
        // Prevent false positives when buying the item in ah or moving it from a storage
        if (lastCloseTime.passedSince() < 1.seconds) return

        val itemStack = event.itemStack

        val internalName = itemStack.getInternalName()
        if (internalName == SkyhanniItems.DYE_WILD_STRAWBERRY()) {
            val name = itemStack.name
            TitleManager.sendTitle(name, 5.seconds)
            ChatUtils.chat("You found a $name§e!")
            McSound.BEEP.play()
            ItemBlink.setBlink(itemStack, 5_000)
        }
    }
}

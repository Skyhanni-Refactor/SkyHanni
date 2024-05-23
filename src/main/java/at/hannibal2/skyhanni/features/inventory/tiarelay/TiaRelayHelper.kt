package at.hannibal2.skyhanni.features.inventory.tiarelay

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.minecraft.PlaySoundEvent
import at.hannibal2.skyhanni.events.render.gui.RenderInventoryItemTipEvent
import at.hannibal2.skyhanni.events.render.gui.SlotClickEvent
import at.hannibal2.skyhanni.events.utils.SecondPassedEvent
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.CollectionUtils.sorted
import at.hannibal2.skyhanni.utils.HypixelCommands
import at.hannibal2.skyhanni.utils.InventoryUtils
import at.hannibal2.skyhanni.utils.ItemUtils.getLore
import at.hannibal2.skyhanni.api.skyblock.SkyBlockAPI
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import kotlin.time.Duration.Companion.minutes

object TiaRelayHelper {

    private val config get() = SkyHanniMod.feature.inventory.helper.tiaRelay
    private var inInventory = false

    private var lastClickSlot = 0
    private var lastClickTime = SimpleTimeMark.farPast()
    private var sounds = mutableMapOf<Int, Sound>()

    private var resultDisplay = mutableMapOf<Int, Int>()

    @HandleEvent
    fun onPlaySound(event: PlaySoundEvent) {
        if (!SkyBlockAPI.isConnected) return
        val soundName = event.soundName

        if (config.tiaRelayMute && soundName == "mob.wolf.whine") {
            event.cancel()
        }

        if (!config.soundHelper) return
        if (!inInventory) return

        val distance = event.distanceToPlayer
        if (distance >= 2) return

        if (lastClickSlot == 0) return
        if (lastClickTime.passedSince() > 1.minutes) return
        if (sounds.contains(lastClickSlot)) return

        sounds[lastClickSlot] = Sound(soundName, event.pitch)

        lastClickSlot = 0

        tryResult()
    }

    // TODO inventory open and close events
    @HandleEvent
    fun onSecondPassed(event: SecondPassedEvent) {
        if (!SkyBlockAPI.isConnected) return
        if (!config.soundHelper) return

        if (InventoryUtils.openInventoryName().contains("Network Relay")) {
            inInventory = true
        } else {
            inInventory = false
            sounds.clear()
            resultDisplay.clear()
        }
    }

    private fun tryResult() {
        if (sounds.size < 4) return

        val name = sounds.values.first().name
        for (sound in sounds.toMutableMap()) {
            if (sound.value.name != name) {
                ChatUtils.userError("Tia Relay Helper error: Too much background noise! Try turning off the music and then try again.")
                ChatUtils.clickableChat("Click here to run /togglemusic", onClick = {
                    HypixelCommands.toggleMusic()
                })
                sounds.clear()
                return
            }
        }

        val pitchMap = mutableMapOf<Int, Float>()
        for (sound in sounds) {
            pitchMap[sound.key] = sound.value.pitch
        }
        sounds.clear()
        resultDisplay.clear()

        var i = 1
        for (entry in pitchMap.sorted()) {
            resultDisplay[entry.key] = i
            i++
        }
    }

    @HandleEvent
    fun onRenderItemTip(event: RenderInventoryItemTipEvent) {
        if (!SkyBlockAPI.isConnected) return
        if (!config.soundHelper) return
        if (!inInventory) return

        val slot = event.slot
        val stack = slot.stack

        val slotNumber = slot.slotNumber

        val position = resultDisplay.getOrDefault(slotNumber, null)
        if (position != null) {
            if (stack.getLore().any { it.contains("Done!") }) {
                resultDisplay.clear()
                return
            }
            event.stackTip = "#$position"
            return
        }

        if (!sounds.contains(slotNumber) && stack.getLore().any { it.contains("Hear!") }) {
            event.stackTip = "Hear!"
            event.offsetX = 5
            event.offsetY = -5
            return
        }
    }

    @HandleEvent
    fun onSlotClick(event: SlotClickEvent) {
        if (!SkyBlockAPI.isConnected) return
        if (!config.soundHelper) return
        if (!inInventory) return

        // only listen to right clicks
        if (event.clickedButton != 1) return

        lastClickSlot = event.slotId
        lastClickTime = SimpleTimeMark.now()
    }

    class Sound(val name: String, val pitch: Float)
}

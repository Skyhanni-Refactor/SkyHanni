package at.hannibal2.skyhanni.features.inventory.chocolatefactory

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.utils.ProfileJoinEvent
import at.hannibal2.skyhanni.events.utils.SecondPassedEvent
import at.hannibal2.skyhanni.features.fame.ReminderUtils
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.HypixelCommands
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.datetime.TimeUtils.minutes
import at.hannibal2.skyhanni.utils.mc.McSound
import at.hannibal2.skyhanni.utils.mc.McSound.play

object ChocolateFactoryUpgradeWarning {

    private val config get() = ChocolateFactoryAPI.config.chocolateUpgradeWarnings
    private val profileStorage get() = ChocolateFactoryAPI.profileStorage

    private var lastUpgradeWarning = SimpleTimeMark.farPast()
    private var lastUpgradeSlot = -1
    private var lastUpgradeLevel = 0

    @HandleEvent
    fun onSecondPassed(event: SecondPassedEvent) {
        if (!LorenzUtils.inSkyBlock) return
        val profileStorage = profileStorage ?: return

        val upgradeAvailableAt = SimpleTimeMark(profileStorage.bestUpgradeAvailableAt)
        if (upgradeAvailableAt.isInPast() && !upgradeAvailableAt.isFarPast()) {
            checkUpgradeWarning()
        }
    }

    private fun checkUpgradeWarning() {
        if (!ChocolateFactoryAPI.isEnabled()) return
        if (!config.upgradeWarning) return
        if (ReminderUtils.isBusy()) return
        if (ChocolateFactoryCustomReminder.isActive()) return
        if (lastUpgradeWarning.passedSince() < config.timeBetweenWarnings.minutes) return
        lastUpgradeWarning = SimpleTimeMark.now()
        if (config.upgradeWarningSound) {
            McSound.BEEP.play()
        }
        if (ChocolateFactoryAPI.inChocolateFactory) return
        ChatUtils.clickableChat(
            "You have a Chocolate factory upgrade available to purchase!",
            onClick = {
                HypixelCommands.chocolateFactory()
            }
        )
    }

    @HandleEvent
    fun onProfileJoin(event: ProfileJoinEvent) {
        lastUpgradeWarning = SimpleTimeMark.farPast()
    }

    fun checkUpgradeChange(slot: Int, level: Int) {
        if (slot != lastUpgradeSlot || level != lastUpgradeLevel) {
            lastUpgradeWarning = SimpleTimeMark.now()
            lastUpgradeSlot = slot
            lastUpgradeLevel = level
        }
    }
}

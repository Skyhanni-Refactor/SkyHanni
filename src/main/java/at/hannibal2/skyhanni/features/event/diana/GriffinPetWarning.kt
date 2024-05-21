package at.hannibal2.skyhanni.features.event.diana

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.data.TitleManager
import at.hannibal2.skyhanni.events.minecraft.ClientTickEvent
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import kotlin.time.Duration.Companion.seconds

object GriffinPetWarning {

    private var lastWarnTime = SimpleTimeMark.farPast()

    @HandleEvent
    fun onTick(event: ClientTickEvent) {
        if (!event.isMod(10)) return
        if (!SkyHanniMod.feature.event.diana.petWarning) return
        if (!DianaAPI.isDoingDiana()) return
        if (!DianaAPI.hasSpadeInHand()) return

        if (!DianaAPI.hasGriffinPet() && lastWarnTime.passedSince() > 30.seconds) {
            lastWarnTime = SimpleTimeMark.now()
            TitleManager.sendTitle("§cGriffin Pet!", 3.seconds)
            ChatUtils.chat("Reminder to use a Griffin pet for Mythological Ritual!")
        }
    }
}

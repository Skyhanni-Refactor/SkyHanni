package at.hannibal2.skyhanni.mixins.kotlin.gui

import at.hannibal2.skyhanni.events.render.gui.TabListLineRenderEvent
import at.hannibal2.skyhanni.kmixin.annotations.InjectionKind
import at.hannibal2.skyhanni.kmixin.annotations.KInject
import at.hannibal2.skyhanni.kmixin.annotations.KMixin
import net.minecraft.client.gui.GuiPlayerTabOverlay
import net.minecraft.client.network.NetworkPlayerInfo
import net.minecraft.scoreboard.ScorePlayerTeam
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
import kotlin.reflect.KProperty

@KMixin(GuiPlayerTabOverlay::class)
object GuiPlayerTabOverlayMixin {

    var tabListGuard by object : ThreadLocal<Boolean>() {
        override fun initialValue(): Boolean {
            return false
        }
    }

    operator fun <T> ThreadLocal<T>.setValue(t: Any?, property: KProperty<*>, any: T) {
        this.set(any)
    }

    operator fun <T> ThreadLocal<T>.getValue(t: Any?, property: KProperty<*>): T {
        return get()
    }

    @KInject(method = "getPlayerName", kind = InjectionKind.HEAD, cancellable = true)
    fun onGetPlayerName(info: NetworkPlayerInfo, cir: CallbackInfoReturnable<String>) {
        val text = if (info.displayName != null) {
            info.displayName.formattedText
        } else {
            ScorePlayerTeam.formatPlayerName(info.playerTeam, info.gameProfile.name)
        }

        if (tabListGuard) return

        val event = TabListLineRenderEvent(text)
        event.post()
        val newText = event.text
        if (text != newText) {
            cir.returnValue = newText
        }
    }
}

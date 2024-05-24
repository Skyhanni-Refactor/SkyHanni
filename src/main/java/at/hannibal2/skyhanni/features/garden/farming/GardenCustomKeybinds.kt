package at.hannibal2.skyhanni.features.garden.farming

import at.hannibal2.skyhanni.features.garden.GardenAPI
import at.hannibal2.skyhanni.mixins.transformers.AccessorKeyBinding
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.KeyboardManager.isKeyHeld
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.mc.McClient
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.inventory.GuiEditSign
import net.minecraft.client.settings.KeyBinding
import org.lwjgl.input.Keyboard
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
import java.util.IdentityHashMap
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

object GardenCustomKeybinds {

    private val config get() = GardenAPI.config.keyBind

    private val map: MutableMap<KeyBinding, () -> Int> = IdentityHashMap()
    private var lastWindowOpenTime = SimpleTimeMark.farPast()
    private var lastDuplicateKeybindsWarnTime = SimpleTimeMark.farPast()

    init {
        map[McClient.options.keyBindAttack] = { config.attack }
        map[McClient.options.keyBindUseItem] = { config.useItem }
        map[McClient.options.keyBindLeft] = { config.left }
        map[McClient.options.keyBindRight] = { config.right }
        map[McClient.options.keyBindForward] = { config.forward }
        map[McClient.options.keyBindBack] = { config.back }
        map[McClient.options.keyBindJump] = { config.jump }
        map[McClient.options.keyBindSneak] = { config.sneak }
    }

    private fun isEnabled() = GardenAPI.inGarden() && config.enabled && !(GardenAPI.onBarnPlot && config.excludeBarn)

    private fun isActive(): Boolean {
        if (!isEnabled()) return false
        if (GardenAPI.toolInHand == null) return false

        if (Minecraft.getMinecraft().currentScreen != null) {
            if (Minecraft.getMinecraft().currentScreen is GuiEditSign) {
                lastWindowOpenTime = SimpleTimeMark.now()
            }
            return false
        }

        // TODO remove workaround
        if (lastWindowOpenTime.passedSince() < 300.milliseconds) return false

        val areDuplicates = map.values
            .map { it() }
            .filter { it != Keyboard.KEY_NONE }
            .let { values -> values.size != values.toSet().size }
        if (areDuplicates) {
            if (lastDuplicateKeybindsWarnTime.passedSince() > 30.seconds) {
                ChatUtils.chatAndOpenConfig(
                    "Duplicate Custom Keybinds aren't allowed!",
                    GardenAPI.config::keyBind
                )
                lastDuplicateKeybindsWarnTime = SimpleTimeMark.now()
            }
            return false
        }

        return true
    }

    @JvmStatic
    fun isKeyDown(keyBinding: KeyBinding, cir: CallbackInfoReturnable<Boolean>) {
        if (!isActive()) return
        val override = map[keyBinding] ?: return
        val keyCode = override()
        cir.returnValue = keyCode.isKeyHeld()
    }

    @JvmStatic
    fun onTick(keyCode: Int, ci: CallbackInfo) {
        if (!isActive()) return
        if (keyCode == 0) return
        val keyBinding = map.entries.firstOrNull { it.value() == keyCode }?.key ?: return
        ci.cancel()
        keyBinding as AccessorKeyBinding
        keyBinding.pressTime_skyhanni++
    }
}

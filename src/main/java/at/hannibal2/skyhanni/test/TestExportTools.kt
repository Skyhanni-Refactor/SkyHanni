package at.hannibal2.skyhanni.test

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.render.gui.GuiKeyPressEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.test.command.CopyItemCommand.copyItemToClipboard
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.KeyboardManager.isKeyHeld
import at.hannibal2.skyhanni.utils.chat.Text
import at.hannibal2.skyhanni.utils.chat.Text.asComponent
import at.hannibal2.skyhanni.utils.chat.Text.hover
import at.hannibal2.skyhanni.utils.chat.Text.onClick
import at.hannibal2.skyhanni.utils.system.OS
import net.minecraft.nbt.CompressedStreamTools
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardOpenOption
import kotlin.io.path.name

@SkyHanniModule
object TestExportTools {

    private val config get() = SkyHanniMod.feature.dev.debug

    @HandleEvent
    fun onKeybind(event: GuiKeyPressEvent) {
        if (config.copyItemData.isKeyHeld()) {
            copyItemToClipboard(event.guiContainer.slotUnderMouse?.stack ?: return)
        }
        if (config.copyItemDataCompressed.isKeyHeld()) {
            val nbt = event.guiContainer.slotUnderMouse?.stack?.serializeNBT() ?: return
            val bytes = ByteArrayOutputStream().also { CompressedStreamTools.writeCompressed(nbt, it) }.toByteArray()
            val path = Files.write(
                File("config/skyhanni/exports/${System.currentTimeMillis()}.nbt").toPath(),
                bytes,
                StandardOpenOption.CREATE, StandardOpenOption.WRITE
            )
            ChatUtils.chat(Text.text("§e[SkyHanni] Compressed item data saved to ${path.name}") {
                onClick { OS.openFile(path.toAbsolutePath().toString()) }
                hover = "Click to open".asComponent()
            })
        }
    }
}

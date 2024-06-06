package at.hannibal2.skyhanni.data

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.minecraft.TitleReceivedEvent
import at.hannibal2.skyhanni.events.minecraft.packet.ReceivePacketEvent
import at.hannibal2.skyhanni.events.render.gui.GuiOverlayRenderEvent
import at.hannibal2.skyhanni.events.utils.ProfileJoinEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.mc.McFont
import at.hannibal2.skyhanni.utils.mc.McScreen
import io.github.notenoughupdates.moulconfig.internal.TextRenderUtils
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.network.play.server.S45PacketTitle
import org.lwjgl.opengl.GL11
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object TitleManager {

    private var originalText = ""
    private var display = ""
    private var endTime = SimpleTimeMark.farPast()
    private var heightModifier = 1.8
    private var fontSizeModifier = 4f

    fun sendTitle(text: String, duration: Duration, height: Double = 1.8, fontSize: Float = 4f) {
        originalText = text
        display = "§f$text"
        endTime = SimpleTimeMark.now() + duration
        heightModifier = height
        fontSizeModifier = fontSize
    }

    fun optionalResetTitle(condition: (String) -> Boolean) {
        if (condition(originalText)) {
            endTime = SimpleTimeMark.farPast()
        }
    }

    fun command(args: Array<String>) {
        if (args.size < 4) {
            ChatUtils.userError("Usage: /shsendtitle <duration> <height> <fontSize> <text ..>")
            return
        }

        val duration = args[0].toInt().seconds
        val height = args[1].toDouble()
        val fontSize = args[2].toFloat()
        val title = "§6" + args.drop(3).joinToString(" ").replace("&", "§")

        sendTitle(title, duration, height, fontSize)
    }

    @HandleEvent
    fun onTitleReceived(event: ReceivePacketEvent) {
        val packet = event.packet

        if (packet !is S45PacketTitle) return
        val message = packet.message ?: return
        val formattedText = message.formattedText
        if (TitleReceivedEvent(formattedText).post()) {
            event.cancel()
        }
    }

    @HandleEvent
    fun onProfileJoin(event: ProfileJoinEvent) {
        endTime = SimpleTimeMark.farPast()
    }

    @HandleEvent
    fun onRenderOverlay(event: GuiOverlayRenderEvent) {
        if (endTime.isInPast()) return

        val width = McScreen.width
        val height = McScreen.height

        GlStateManager.enableBlend()
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO)

        GlStateManager.pushMatrix()
        GlStateManager.translate((width / 2).toFloat(), (height / heightModifier).toFloat(), 3.0f)
        GlStateManager.scale(fontSizeModifier, fontSizeModifier, 1f)
        // TODO dont use neu text method
        TextRenderUtils.drawStringCenteredScaledMaxWidth(display, McFont.font, 0f, 0f, true, 75, 0)
        GlStateManager.popMatrix()
    }
}

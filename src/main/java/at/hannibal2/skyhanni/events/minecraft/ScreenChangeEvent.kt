package at.hannibal2.skyhanni.events.minecraft

import at.hannibal2.skyhanni.api.event.CancellableSkyHanniEvent
import net.minecraft.client.gui.GuiScreen

class ScreenChangeEvent(val screen: GuiScreen?) : CancellableSkyHanniEvent()

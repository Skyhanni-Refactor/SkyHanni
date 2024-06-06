package at.hannibal2.skyhanni.events

import at.hannibal2.skyhanni.api.event.SkyHanniEvent
import at.hannibal2.skyhanni.data.model.TabWidget

/** The events get send on change of the widget and on island switch */
open class WidgetUpdateEvent(
    val widget: TabWidget,
    val lines: List<String>,
) : SkyHanniEvent() {

    fun isWidget(widgetType: TabWidget) = widget == widgetType

    fun isClear() = lines.isEmpty()
}

package at.hannibal2.skyhanni.events.inventory

import at.hannibal2.skyhanni.api.event.SkyHanniEvent
import at.hannibal2.skyhanni.utils.NEUInternalName

class ItemAddInInventoryEvent(val internalName: NEUInternalName, val amount: Int) : SkyHanniEvent()

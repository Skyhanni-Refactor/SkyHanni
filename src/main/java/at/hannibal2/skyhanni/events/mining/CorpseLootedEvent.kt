package at.hannibal2.skyhanni.events.mining

import at.hannibal2.skyhanni.api.event.SkyHanniEvent
import at.hannibal2.skyhanni.features.mining.mineshaft.CorpseType

class CorpseLootedEvent(val corpseType: CorpseType, val loot: List<Pair<String, Int>>) : SkyHanniEvent()

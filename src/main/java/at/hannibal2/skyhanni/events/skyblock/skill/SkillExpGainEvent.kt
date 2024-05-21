package at.hannibal2.skyhanni.events.skyblock.skill

import at.hannibal2.skyhanni.api.event.SkyHanniEvent

// does not know how much exp is there, also gets called multiple times
class SkillExpGainEvent(val skill: String) : SkyHanniEvent()

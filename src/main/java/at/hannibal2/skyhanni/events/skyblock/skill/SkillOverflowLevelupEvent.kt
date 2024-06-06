package at.hannibal2.skyhanni.events.skyblock.skill

import at.hannibal2.skyhanni.api.event.SkyHanniEvent
import at.hannibal2.skyhanni.features.skillprogress.SkillType

class SkillOverflowLevelupEvent(val skill: SkillType, val oldLevel: Int, val newLevel: Int) : SkyHanniEvent()

package at.hannibal2.skyhanni.events.damageindicator

import at.hannibal2.skyhanni.api.event.SkyHanniEvent
import at.hannibal2.skyhanni.features.combat.damageindicator.EntityData

class DamageIndicatorDetectedEvent(val entityData: EntityData) : SkyHanniEvent()

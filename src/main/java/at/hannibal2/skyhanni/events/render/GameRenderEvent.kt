package at.hannibal2.skyhanni.events.render

import at.hannibal2.skyhanni.api.event.SkyHanniEvent

abstract class GameRenderEvent private constructor() : SkyHanniEvent() {

    class Start : GameRenderEvent()

    class End : GameRenderEvent()
}

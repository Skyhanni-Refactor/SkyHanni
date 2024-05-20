package at.hannibal2.skyhanni.api.event

abstract class SkyHanniEvent protected constructor() {

    var isCancelled: Boolean = false
        protected set

    fun post(onError: (Throwable) -> Unit = {}) {
        SkyHanniEvents.getEventHandler(javaClass).post(this, onError)
    }
}

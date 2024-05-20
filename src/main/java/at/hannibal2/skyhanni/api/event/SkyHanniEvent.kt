package at.hannibal2.skyhanni.api.event

abstract class SkyHanniEvent protected constructor() {

    var isCancelled: Boolean = false
        protected set

    fun post(): Boolean {
        return SkyHanniEvents.getEventHandler(javaClass).post(this)
    }

    fun post(onError: (Throwable) -> Unit = {}): Boolean {
        return SkyHanniEvents.getEventHandler(javaClass).post(this, onError)
    }
}

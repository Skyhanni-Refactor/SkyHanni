package at.hannibal2.skyhanni.api.event

abstract class CancellableEvent : SkyHanniEvent() {

    fun cancel() {
        isCancelled = true
    }
}

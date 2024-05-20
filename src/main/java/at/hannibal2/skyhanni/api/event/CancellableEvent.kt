package at.hannibal2.skyhanni.api.event

interface CancellableEvent {

    fun cancel() {
        (this as SkyHanniEvent).isCancelled = true
    }
}

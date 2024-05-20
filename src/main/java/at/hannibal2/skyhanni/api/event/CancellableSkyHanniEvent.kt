package at.hannibal2.skyhanni.api.event

abstract class CancellableSkyHanniEvent : SkyHanniEvent() {

    fun cancel() {
        isCancelled = true
    }
}

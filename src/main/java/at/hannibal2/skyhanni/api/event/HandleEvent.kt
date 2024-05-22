package at.hannibal2.skyhanni.api.event

annotation class HandleEvent(
    /**
     * The priority of when the event will be called, lower priority will be called first.
     * HIGHEST -> -2
     * HIGH -> -1
     * NORMAL -> 0
     * LOW -> 1
     * LOWEST -> 2
     */
    val priority: Int = 0,

    /**
     * If the event is cancelled & receiveCancelled is true, then the method will still invoke.
     */
    val receiveCancelled: Boolean = false,
) {

    companion object {
        const val HIGHEST = -2
        const val HIGH = -1
        const val NORMAL = 0
        const val LOW = 1
        const val LOWEST = 2
    }
}

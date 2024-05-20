package at.hannibal2.skyhanni.api.event

annotation class HandleEvent(
    /**
     * The priority of when the event will be called, lower priority will be called first.
     */
    val priority: Int = 0,

    /**
     * If the event is cancelled & if receiveCancelled is true then the method will still invoke.
     */
    val receiveCancelled: Boolean = false,
)

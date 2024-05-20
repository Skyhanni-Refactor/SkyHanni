package at.hannibal2.skyhanni.api.event

import java.lang.reflect.Method

internal object SkyHanniEvents {

    private val eventHolders: MutableMap<Class<*>, EventHandler<*>> = mutableMapOf()

    fun init(instances: List<Any>) {
        instances.forEach { instance ->
            instance.javaClass.declaredMethods.forEach {
                registerMethod(it, instance)
            }
        }
        eventHolders.values.forEach { it.freeze() }
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : SkyHanniEvent> getEventHandler(event: Class<T>): EventHandler<T> {
        return eventHolders.getOrPut(event) { EventHandler(event) } as EventHandler<T>
    }

    @Suppress("UNCHECKED_CAST")
    private fun registerMethod(method: Method, instance: Any) {
        if (method.parameterCount != 1) return
        val options = method.getAnnotation(HandleEvent::class.java) ?: return
        val event = method.parameterTypes[0]
        if (!SkyHanniEvent::class.java.isAssignableFrom(event)) return
        val handler = getEventHandler(event as Class<SkyHanniEvent>)
        handler.addListener(method, instance, options)
    }
}

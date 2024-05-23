package at.hannibal2.skyhanni.api.event

import at.hannibal2.skyhanni.data.MinecraftData
import at.hannibal2.skyhanni.data.jsonobjects.repo.DisabledEventsJson
import at.hannibal2.skyhanni.events.utils.DebugDataCollectEvent
import at.hannibal2.skyhanni.events.utils.RepositoryReloadEvent
import java.lang.reflect.Method

object SkyHanniEvents {

    private val handlers: MutableMap<Class<*>, EventHandler<*>> = mutableMapOf()
    private var disabledHandlers = emptySet<String>()
    private var disabledHandlerInvokers = emptySet<String>()

    fun init(instances: List<Any>) {
        instances.forEach { instance ->
            instance.javaClass.declaredMethods.forEach {
                registerMethod(it, instance)
            }
        }
        handlers.values.forEach { it.freeze() }
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : SkyHanniEvent> getEventHandler(event: Class<T>): EventHandler<T> {
        return handlers.getOrPut(event) { EventHandler(event) } as EventHandler<T>
    }

    fun isDisabledHandler(handler: String): Boolean {
        return handler in disabledHandlers
    }

    fun isDisabledInvoker(invoker: String): Boolean {
        return invoker in disabledHandlerInvokers
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

    @HandleEvent
    fun onRepoLoad(event: RepositoryReloadEvent) {
        val data = event.getConstant<DisabledEventsJson>("DisabledEvents")
        disabledHandlers = data.disabledHandlers
        disabledHandlerInvokers = data.disabledInvokers
    }

    @HandleEvent
    fun onDebug(event: DebugDataCollectEvent) {
        event.title("Events")
        event.addData {
            handlers.values.toMutableList()
                .filter { it.invokeCount > 0 }
                .sortedWith(compareBy({ -it.invokeCount }, { it.name }))
                .forEach {
                    add("- ${it.name} (${it.invokeCount} ${it.invokeCount / (MinecraftData.totalTicks / 20)}/s)")
                }
        }
    }
}

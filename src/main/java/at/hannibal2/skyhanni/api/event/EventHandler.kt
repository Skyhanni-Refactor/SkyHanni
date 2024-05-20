package at.hannibal2.skyhanni.api.event

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.test.command.ErrorManager
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.chat.Text
import java.lang.reflect.Method

class EventHandler<T : SkyHanniEvent> private constructor(private val name: String) {

    private val listeners: MutableList<Listener<T>> = mutableListOf()

    private var isFrozen = false
    private var canReceiveCancelled = false

    private var invokeCount: Int = 0

    constructor(event: Class<T>) : this(
        (event.name.split(".").lastOrNull() ?: event.name).replace("$", ".")
    )

    fun addListener(method: Method, instance: Any, options: HandleEvent) {
        if (isFrozen) throw IllegalStateException("Cannot add listener to frozen event handler")
        val name = "${method.declaringClass.name}.${method.name}${
            method.parameterTypes.joinTo(
                StringBuilder(),
                prefix = "(",
                postfix = ")",
                separator = ", ",
                transform = Class<*>::getTypeName
            )
        }"
        listeners.add(Listener(name, { e -> method.invoke(instance, e) }, options))
    }

    fun freeze() {
        isFrozen = true
        listeners.sortBy { it.options.priority }
        canReceiveCancelled = listeners.any { it.options.receiveCancelled }
    }

    fun post(event: T, onError: ((Throwable) -> Unit)? = null) {
        invokeCount++
        if (this.listeners.isEmpty()) return
        if (!isFrozen) error("Cannot invoke event on unfrozen event handler")

        if (SkyHanniEvents.isDisabledHandler(name)) return

        var errors = 0

        for (listener in listeners) {
            if (event.isCancelled && !listener.options.receiveCancelled) continue
            if (SkyHanniEvents.isDisabledInvoker(listener.name)) continue
            try {
                listener.invoker(event)
            } catch (throwable: Throwable) {
                errors++
                if (errors <= 3) {
                    val errorName = throwable::class.simpleName ?: "error"
                    val message = "Caught an $errorName in ${listener.name} at $name: ${throwable.message}"
                    ErrorManager.logErrorWithData(throwable, message, ignoreErrorCache = onError == null)
                }
                onError?.invoke(throwable)
            }
            if (event.isCancelled && !canReceiveCancelled) break
        }

        if (errors > 3) {
            val hiddenErrors = errors - 3
            ChatUtils.chat(
                Text.text(
                    "§c[SkyHanni/${SkyHanniMod.version}] $hiddenErrors more errors in $name are hidden!"
                )
            )
        }
    }

    private class Listener<T : SkyHanniEvent>(val name: String, val invoker: (T) -> Unit, val options: HandleEvent)
}

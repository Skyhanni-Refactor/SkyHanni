package at.hannibal2.skyhanni.data

import at.hannibal2.skyhanni.events.entity.EntityDisplayNameEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.TimeLimitedCache
import net.minecraft.entity.Entity
import net.minecraft.util.IChatComponent
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
import kotlin.time.Duration.Companion.milliseconds

@SkyHanniModule
object EntityData {

    private val nametagCache = TimeLimitedCache<Entity, IChatComponent>(50.milliseconds)

    @JvmStatic
    fun getDisplayName(entity: Entity, ci: CallbackInfoReturnable<IChatComponent>) {
        ci.returnValue = postRenderNametag(entity, ci.returnValue)
    }

    private fun postRenderNametag(entity: Entity, chatComponent: IChatComponent) = nametagCache.getOrPut(entity) {
        val event = EntityDisplayNameEvent(entity, chatComponent)
        event.post()
        event.chatComponent
    }
}

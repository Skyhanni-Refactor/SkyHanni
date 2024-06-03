package at.hannibal2.skyhanni.mixins.kotlin

import at.hannibal2.skyhanni.events.entity.EntityDisplayNameEvent
import at.hannibal2.skyhanni.kmixin.annotations.InjectionKind
import at.hannibal2.skyhanni.kmixin.annotations.KInject
import at.hannibal2.skyhanni.kmixin.annotations.KMixin
import at.hannibal2.skyhanni.kmixin.annotations.KSelf
import at.hannibal2.skyhanni.utils.TimeLimitedCache
import net.minecraft.entity.Entity
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.IChatComponent
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
import kotlin.time.Duration.Companion.milliseconds

@KMixin(EntityPlayer::class)
object EntityPlayerMixin {

    private val nametagCache = TimeLimitedCache<Entity, IChatComponent>(50.milliseconds)

    @KInject(method = "getDisplayName", kind = InjectionKind.RETURN, cancellable = true)
    fun getDisplayName(ci: CallbackInfoReturnable<IChatComponent>, @KSelf player: EntityPlayer) {
        ci.returnValue = nametagCache.getOrPut(player) {
            val event = EntityDisplayNameEvent(player, ci.returnValue)
            event.post()
            event.chatComponent
        }
    }
}

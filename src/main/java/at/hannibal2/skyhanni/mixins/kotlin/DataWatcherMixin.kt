package at.hannibal2.skyhanni.mixins.kotlin

import at.hannibal2.skyhanni.events.entity.DataWatcherUpdatedEvent
import at.hannibal2.skyhanni.kmixin.annotations.InjectionKind
import at.hannibal2.skyhanni.kmixin.annotations.KInject
import at.hannibal2.skyhanni.kmixin.annotations.KMixin
import at.hannibal2.skyhanni.kmixin.annotations.KShadow
import at.hannibal2.skyhanni.kmixin.annotations.ShadowKind
import net.minecraft.entity.DataWatcher
import net.minecraft.entity.DataWatcher.WatchableObject
import net.minecraft.entity.Entity
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo

@KMixin(DataWatcher::class)
object DataWatcherMixin {

    @KInject(method = "updateWatchedObjectsFromList", kind = InjectionKind.TAIL)
    fun onWhatever(list: List<WatchableObject>, ci: CallbackInfo, @KShadow(ShadowKind.FINAL_FIELD) owner: Entity) {
        DataWatcherUpdatedEvent(owner, list).post()
    }
}

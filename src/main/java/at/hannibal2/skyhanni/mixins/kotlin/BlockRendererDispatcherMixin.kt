package at.hannibal2.skyhanni.mixins.kotlin

import at.hannibal2.skyhanni.kmixin.annotations.InjectionKind
import at.hannibal2.skyhanni.kmixin.annotations.KInject
import at.hannibal2.skyhanni.kmixin.annotations.KMixin
import at.hannibal2.skyhanni.kmixin.annotations.KSelf
import at.hannibal2.skyhanni.mixins.hooks.modifyGetModelFromBlockState
import net.minecraft.block.state.IBlockState
import net.minecraft.client.renderer.BlockRendererDispatcher
import net.minecraft.client.resources.model.IBakedModel
import net.minecraft.util.BlockPos
import net.minecraft.world.IBlockAccess
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable

@KMixin(BlockRendererDispatcher::class)
object BlockRendererDispatcherMixin {

    @KInject(method = "getModelFromBlockState", kind = InjectionKind.RETURN, cancellable = true)
    fun modifyGetModelFromBlockState(
        state: IBlockState?,
        worldIn: IBlockAccess?,
        pos: BlockPos,
        cir: CallbackInfoReturnable<IBakedModel>,
        @KSelf self: BlockRendererDispatcher
    ) {
        modifyGetModelFromBlockState(self, state, pos, cir)
    }
}

package at.hannibal2.skyhanni.mixins.kotlin.renderer

import at.hannibal2.skyhanni.api.skyblock.SkyBlockAPI
import at.hannibal2.skyhanni.features.mining.MiningCommissionsBlocksColor
import at.hannibal2.skyhanni.kmixin.annotations.InjectionKind
import at.hannibal2.skyhanni.kmixin.annotations.KInject
import at.hannibal2.skyhanni.kmixin.annotations.KMixin
import at.hannibal2.skyhanni.kmixin.annotations.KSelf
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
        pos: BlockPos?,
        cir: CallbackInfoReturnable<IBakedModel>,
        @KSelf self: BlockRendererDispatcher
    ) {
        if (state == null || pos == null) return
        var returnState: IBlockState = state

        if (!SkyBlockAPI.isConnected) return

        if (MiningCommissionsBlocksColor.enabled && MiningCommissionsBlocksColor.active) {
            for (block in MiningCommissionsBlocksColor.MiningBlock.entries) {
                if (block.checkIsland() && block.onCheck(state)) {
                    returnState = block.onColor(state, block.highlight)
                }
            }
        }

        if (returnState !== state) {
            cir.returnValue = self.blockModelShapes.getModelForState(returnState)
        }
    }
}

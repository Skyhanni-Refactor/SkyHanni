package at.hannibal2.skyhanni.mixins.hooks

import at.hannibal2.skyhanni.api.skyblock.SkyBlockAPI
import at.hannibal2.skyhanni.features.mining.MiningCommissionsBlocksColor
import net.minecraft.block.state.IBlockState
import net.minecraft.client.renderer.BlockRendererDispatcher
import net.minecraft.client.resources.model.IBakedModel
import net.minecraft.util.BlockPos
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable


// Taken and modified from Skytils
fun modifyGetModelFromBlockState(
    blockRendererDispatcher: BlockRendererDispatcher,
    state: IBlockState?,
    pos: BlockPos?,
    cir: CallbackInfoReturnable<IBakedModel>,
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
        cir.returnValue = blockRendererDispatcher.blockModelShapes.getModelForState(returnState)
    }
}

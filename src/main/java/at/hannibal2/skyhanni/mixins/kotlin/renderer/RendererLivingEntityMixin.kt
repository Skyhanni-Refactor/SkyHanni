package at.hannibal2.skyhanni.mixins.kotlin.renderer

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.skyblock.SkyBlockAPI
import at.hannibal2.skyhanni.events.render.entity.EntityRenderLayersEvent
import at.hannibal2.skyhanni.features.misc.ContributorManager
import at.hannibal2.skyhanni.kmixin.annotations.InjectionKind
import at.hannibal2.skyhanni.kmixin.annotations.KInject
import at.hannibal2.skyhanni.kmixin.annotations.KInjectAt
import at.hannibal2.skyhanni.kmixin.annotations.KMixin
import at.hannibal2.skyhanni.kmixin.annotations.KRedirectCall
import at.hannibal2.skyhanni.kmixin.annotations.KRedirectField
import at.hannibal2.skyhanni.kmixin.annotations.TargetShift
import at.hannibal2.skyhanni.mixins.hooks.RenderLivingEntityHelper
import at.hannibal2.skyhanni.mixins.hooks.RenderLivingEntityHelper.internalSetColorMultiplier
import at.hannibal2.skyhanni.utils.EntityOutlineRenderer
import at.hannibal2.skyhanni.utils.datetime.DateUtils
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.entity.RendererLivingEntity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable

@KMixin(RendererLivingEntity::class, priority = 1001)
object RendererLivingEntityMixin {

    @KRedirectField(
        method = "setBrightness",
        target = "Lnet/minecraft/entity/EntityLivingBase;hurtTime:I"
    )
    fun changeHurtTime(entity: EntityLivingBase): Int {
        return RenderLivingEntityHelper.internalChangeHurtTime(entity)
    }

    @KInject(
        method = "getColorMultiplier",
        kind = InjectionKind.HEAD,
        cancellable = true
    )
    fun setColorMultiplier(entity: EntityLivingBase, light: Float, partialTicks: Float, cir: CallbackInfoReturnable<Int>) {
        cir.returnValue = internalSetColorMultiplier(entity)
    }

    @KInject(
        method = "renderLayers",
        kind = InjectionKind.HEAD,
        cancellable = true
    )
    fun onRenderLayersPre(entity: EntityLivingBase, p_177093_2_: Float, p_177093_3_: Float, partialTicks: Float, p_177093_5_: Float, p_177093_6_: Float, p_177093_7_: Float, p_177093_8_: Float, ci: CallbackInfo) {
        if (EntityRenderLayersEvent.Pre(entity).post()) {
            ci.cancel()
        }
    }

    @KRedirectCall(
        method = "setScoreTeamColor",
        target = "Lnet/minecraft/client/renderer/GlStateManager;color(FFFF)V"
    )
    fun setOutlineColor(
        red: Float,
        green: Float,
        blue: Float,
        alpha: Float,
        entity: EntityLivingBase
    ) {
        val color = EntityOutlineRenderer.getCustomOutlineColor(entity)

        if (color != null) {
            val colorRed = (color shr 16 and 255).toFloat() / 255.0f
            val colorGreen = (color shr 8 and 255).toFloat() / 255.0f
            val colorBlue = (color and 255).toFloat() / 255.0f
            GlStateManager.color(colorRed, colorGreen, colorBlue, alpha)
        } else {
            GlStateManager.color(red, green, blue, alpha)
        }
    }

    @KInjectAt(
        method = "rotateCorpse",
        target = "Lnet/minecraft/util/EnumChatFormatting;getTextWithoutFormattingCodes(Ljava/lang/String;)Ljava/lang/String;",
        shift = TargetShift.BEFORE,
        cancellable = true
    )
    fun rotateCorpse(entity: EntityLivingBase, p_77043_2_: Float, p_77043_3_: Float, partialTicks: Float, ci: CallbackInfo) {
        if (entity is EntityPlayer) {
            rotatePlayer(entity)
            if (shouldBeUpsideDown(entity.name)) {
                GlStateManager.translate(0.0f, entity.height + 0.1f, 0.0f)
                GlStateManager.rotate(180.0f, 0.0f, 0.0f, 1.0f)

                ci.cancel()
            }
        }
    }

    private val config get() = SkyHanniMod.feature.dev

    private fun shouldBeUpsideDown(userName: String?): Boolean {
        if (!SkyBlockAPI.isConnected) return false
        if (!config.flipContributors && !DateUtils.isAprilFools()) return false
        val name = userName ?: return false
        return ContributorManager.shouldBeUpsideDown(name)
    }

    private fun rotatePlayer(player: EntityPlayer) {
        if (!SkyBlockAPI.isConnected) return
        if (!config.rotateContributors && !DateUtils.isAprilFools()) return
        val name = player.name ?: return
        if (!ContributorManager.shouldSpin(name)) return
        val rotation = ((player.ticksExisted % 90) * 4).toFloat()
        GlStateManager.rotate(rotation, 0f, 1f, 0f)
    }
}

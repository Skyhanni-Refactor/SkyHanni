package at.hannibal2.skyhanni.mixins.hooks

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.skyblock.SkyBlockAPI
import at.hannibal2.skyhanni.features.misc.ContributorManager
import at.hannibal2.skyhanni.utils.datetime.DateUtils
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.player.EntityPlayer

object RendererLivingEntityHook {
    private val config get() = SkyHanniMod.feature.dev

    /**
     * Check if the player is on the cool person list and if they should be flipped.
     */
    @JvmStatic
    fun shouldBeUpsideDown(userName: String?): Boolean {
        if (!SkyBlockAPI.isConnected) return false
        if (!config.flipContributors && !DateUtils.isAprilFools()) return false
        val name = userName ?: return false
        return ContributorManager.shouldBeUpsideDown(name)
    }

    /**
     * Check if the player should spin and rotate them if the option is on.
     */
    @JvmStatic
    fun rotatePlayer(player: EntityPlayer) {
        if (!SkyBlockAPI.isConnected) return
        if (!config.rotateContributors && !DateUtils.isAprilFools()) return
        val name = player.name ?: return
        if (!ContributorManager.shouldSpin(name)) return
        val rotation = ((player.ticksExisted % 90) * 4).toFloat()
        GlStateManager.rotate(rotation, 0f, 1f, 0f)
    }
}

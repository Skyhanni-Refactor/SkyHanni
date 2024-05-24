package at.hannibal2.skyhanni.features.misc

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.features.misc.HideArmorConfig.ModeEntry
import at.hannibal2.skyhanni.events.render.entity.SkyHanniRenderEntityEvent
import at.hannibal2.skyhanni.utils.EntityUtils.getArmorInventory
import at.hannibal2.skyhanni.utils.EntityUtils.hasPotionEffect
import at.hannibal2.skyhanni.utils.EntityUtils.isNPC
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.potion.Potion

object HideArmor {

    private val config get() = SkyHanniMod.feature.misc.hideArmor2
    private var armor = mapOf<Int, ItemStack>()

    private fun shouldHideArmor(entity: EntityPlayer): Boolean {
        if (entity.hasPotionEffect(Potion.invisibility)) return false
        if (entity.isNPC()) return false

        return when (config.mode) {
            ModeEntry.ALL -> true

            ModeEntry.OWN -> entity is EntityPlayerSP
            ModeEntry.OTHERS -> entity !is EntityPlayerSP

            else -> false
        }
    }

    @HandleEvent(onlyOnSkyblock = true, generic = EntityPlayer::class)
    fun onRenderLivingPre(event: SkyHanniRenderEntityEvent.Pre<EntityPlayer>) {
        val entity = event.entity
        if (!shouldHideArmor(entity)) return
        val armorInventory = entity.getArmorInventory() ?: return

        armor = buildMap {
            for ((i, stack) in armorInventory.withIndex()) {
                stack?.let {
                    if (!config.onlyHelmet || i == 3) {
                        this[i] = it.copy()
                        armorInventory[i] = null
                    }
                }
            }
        }
    }

    @HandleEvent(onlyOnSkyblock = true, generic = EntityPlayer::class)
    fun onRenderLivingPost(event: SkyHanniRenderEntityEvent.Post<EntityPlayer>) {
        val entity = event.entity
        if (!shouldHideArmor(entity)) return
        val armorInventory = entity.getArmorInventory() ?: return

        for ((index, stack) in armor) {
            armorInventory[index] = stack
        }
    }
}

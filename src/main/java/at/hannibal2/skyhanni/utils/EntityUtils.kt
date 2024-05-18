package at.hannibal2.skyhanni.utils

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.data.Perk
import at.hannibal2.skyhanni.data.mob.MobFilter.isRealPlayer
import at.hannibal2.skyhanni.events.SkyHanniRenderEntityEvent
import at.hannibal2.skyhanni.features.dungeon.DungeonAPI
import at.hannibal2.skyhanni.utils.ItemUtils.getSkullTexture
import at.hannibal2.skyhanni.utils.LocationUtils.canBeSeen
import at.hannibal2.skyhanni.utils.LorenzUtils.baseMaxHealth
import at.hannibal2.skyhanni.utils.LorenzUtils.derpy
import at.hannibal2.skyhanni.utils.StringUtils.removeColor
import at.hannibal2.skyhanni.utils.math.BoundingBox
import at.hannibal2.skyhanni.utils.mc.McWorld
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.entity.monster.EntityEnderman
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.potion.Potion
import net.minecraftforge.client.event.RenderLivingEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object EntityUtils {

    fun EntityLivingBase.hasNameTagWith(
        y: Int,
        contains: String,
        debugRightEntity: Boolean = false,
        inaccuracy: Double = 1.6,
        debugWrongEntity: Boolean = false,
    ): Boolean {
        return getNameTagWith(y, contains, debugRightEntity, inaccuracy, debugWrongEntity) != null
    }

    fun EntityLivingBase.getAllNameTagsInRadiusWith(
        contains: String,
        radius: Double = 3.0,
    ): List<EntityArmorStand> {
        val center = getLorenzVec().add(y = 3)
        val found = getArmorStandsInRadius(center, radius)
        return found.filter {
            val result = it.name.contains(contains)
            result
        }
    }

    fun EntityLivingBase.getNameTagWith(
        y: Int,
        contains: String,
        debugRightEntity: Boolean = false,
        inaccuracy: Double = 1.6,
        debugWrongEntity: Boolean = false,
    ): EntityArmorStand? = getAllNameTagsWith(y, contains, debugRightEntity, inaccuracy, debugWrongEntity).firstOrNull()

    fun EntityLivingBase.getAllNameTagsWith(
        y: Int,
        contains: String,
        debugRightEntity: Boolean = false,
        inaccuracy: Double = 1.6,
        debugWrongEntity: Boolean = false,
    ): List<EntityArmorStand> {
        val center = getLorenzVec().add(y = y)
        val found = getArmorStandsInRadius(center, inaccuracy)
        return found.filter {
            val result = it.name.contains(contains)
            if (debugWrongEntity && !result) {
                SkyHanniMod.logger.info("wrong entity in aabb: '" + it.name + "'")
            }
            if (debugRightEntity && result) {
                SkyHanniMod.logger.info("mob: " + center.printWithAccuracy(2))
                SkyHanniMod.logger.info("nametag: " + it.getLorenzVec().printWithAccuracy(2))
                SkyHanniMod.logger.info("accuracy: " + (it.getLorenzVec() - center).printWithAccuracy(3))
            }
            result
        }
    }

    private fun getArmorStandsInRadius(center: LorenzVec, radius: Double): List<EntityArmorStand> {
        val a = center.add(-radius, -radius - 3, -radius)
        val b = center.add(radius, radius + 3, radius)
        return McWorld.getEntitiesInBox<EntityArmorStand>(BoundingBox(a, b))
    }

    fun EntityLivingBase.hasBossHealth(health: Int): Boolean = this.hasMaxHealth(health, true)

    // TODO remove baseMaxHealth
    fun EntityLivingBase.hasMaxHealth(health: Int, boss: Boolean = false, maxHealth: Int = baseMaxHealth): Boolean {
        val derpyMultiplier = if (Perk.DOUBLE_MOBS_HP.isActive) 2 else 1
        if (maxHealth == health * derpyMultiplier) return true

        if (!boss && !DungeonAPI.inDungeon()) {
            // Corrupted
            if (maxHealth == health * 3 * derpyMultiplier) return true
            // Runic
            if (maxHealth == health * 4 * derpyMultiplier) return true
            // Corrupted+Runic
            if (maxHealth == health * 12 * derpyMultiplier) return true
        }

        return false
    }

    fun EntityPlayer.getSkinTexture(): String? {
        val gameProfile = gameProfile ?: return null

        return gameProfile.properties.entries()
            .filter { it.key == "textures" }
            .map { it.value }
            .firstOrNull { it.name == "textures" }
            ?.value
    }

    fun EntityLivingBase.isAtFullHealth() = baseMaxHealth == health.toInt()

    fun EntityArmorStand.hasSkullTexture(skin: String): Boolean {
        if (inventory == null) return false
        return inventory.any { it != null && it.getSkullTexture() == skin }
    }

    fun EntityPlayer.isNPC() = !isRealPlayer()

    fun EntityLivingBase.hasPotionEffect(potion: Potion) = getActivePotionEffect(potion) != null

    fun EntityLivingBase.getArmorInventory(): Array<ItemStack?>? =
        if (this is EntityPlayer) inventory.armorInventory else null

    fun EntityEnderman.getBlockInHand(): IBlockState? = heldBlockState

    fun Entity.canBeSeen(radius: Double = 150.0) = getLorenzVec().add(y = 0.5).canBeSeen(radius)

    @SubscribeEvent
    fun onEntityRenderPre(event: RenderLivingEvent.Pre<*>) {
        val shEvent = SkyHanniRenderEntityEvent.Pre(event.entity, event.renderer, event.x, event.y, event.z)
        if (shEvent.postAndCatch()) {
            event.isCanceled = true
        }
    }

    @SubscribeEvent
    fun onEntityRenderPost(event: RenderLivingEvent.Post<*>) {
        SkyHanniRenderEntityEvent.Post(event.entity, event.renderer, event.x, event.y, event.z).postAndCatch()
    }

    @SubscribeEvent
    fun onEntityRenderSpecialsPre(event: RenderLivingEvent.Specials.Pre<*>) {
        val shEvent = SkyHanniRenderEntityEvent.Specials.Pre(event.entity, event.renderer, event.x, event.y, event.z)
        if (shEvent.postAndCatch()) {
            event.isCanceled = true
        }
    }

    @SubscribeEvent
    fun onEntityRenderSpecialsPost(event: RenderLivingEvent.Specials.Post<*>) {
        SkyHanniRenderEntityEvent.Specials.Post(event.entity, event.renderer, event.x, event.y, event.z).postAndCatch()
    }

    fun EntityLivingBase.isCorrupted() = baseMaxHealth == health.toInt().derpy() * 3 || isRunicAndCorrupt()
    fun EntityLivingBase.isRunic() = baseMaxHealth == health.toInt().derpy() * 4 || isRunicAndCorrupt()
    fun EntityLivingBase.isRunicAndCorrupt() = baseMaxHealth == health.toInt().derpy() * 3 * 4

    fun Entity.cleanName() = this.name.removeColor()
}

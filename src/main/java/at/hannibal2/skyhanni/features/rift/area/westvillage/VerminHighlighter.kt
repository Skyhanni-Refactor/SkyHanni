package at.hannibal2.skyhanni.features.rift.area.westvillage

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.api.skyblock.IslandArea
import at.hannibal2.skyhanni.data.item.SkyhanniItems
import at.hannibal2.skyhanni.events.minecraft.ClientTickEvent
import at.hannibal2.skyhanni.events.utils.ConfigLoadEvent
import at.hannibal2.skyhanni.features.rift.RiftAPI
import at.hannibal2.skyhanni.mixins.hooks.RenderLivingEntityHelper
import at.hannibal2.skyhanni.utils.ColourUtils.toChromaColour
import at.hannibal2.skyhanni.utils.ColourUtils.withAlpha
import at.hannibal2.skyhanni.utils.ConditionalUtils
import at.hannibal2.skyhanni.utils.EntityUtils.hasSkullTexture
import at.hannibal2.skyhanni.utils.InventoryUtils
import at.hannibal2.skyhanni.utils.LorenzUtils.baseMaxHealth
import at.hannibal2.skyhanni.utils.TimeLimitedSet
import at.hannibal2.skyhanni.utils.mc.McWorld
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.entity.monster.EntitySilverfish
import kotlin.time.Duration.Companion.minutes

object VerminHighlighter {
    private val config get() = RiftAPI.config.area.westVillage.verminHighlight

    private val checkedEntites = TimeLimitedSet<Int>(1.minutes)

    // TODO repo
    private const val FLY_TEXTURE =
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTMwYWMxZjljNjQ5Yzk5Y2Q2MGU0YmZhNTMzNmNjMTg1MGYyNzNlYWI5ZjViMGI3OTQwZDRkNGQ3ZGM4MjVkYyJ9fX0="
    private const val SPIDER_TEXTURE =
        "ewogICJ0aW1lc3RhbXAiIDogMTY1MDU1NjEzMTkxNywKICAicHJvZmlsZUlkIiA6ICI0ODI5MmJkMjI1OTc0YzUwOTZiMTZhNjEyOGFmMzY3NSIsCiAgInByb2ZpbGVOYW1lIiA6ICJLVVJPVE9ZVEIyOCIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS84ZmRmNjJkNGUwM2NhNTk0YzhjZDIxZGQxNzUzMjdmMWNmNzdjNGJjMDU3YTA5NTk2MDNkODNhNjhiYTI3MDA4IgogICAgfQogIH0KfQ=="

    @HandleEvent
    fun onTick(event: ClientTickEvent) {
        if (!isEnabled()) return

        for (entity in McWorld.getEntitiesOf<EntityLivingBase>()) {
            val id = entity.entityId
            if (id in checkedEntites) continue
            checkedEntites.add(id)

            if (!isVermin(entity)) continue
            val color = config.color.get().toChromaColour().withAlpha(60)
            RenderLivingEntityHelper.setEntityColorWithNoHurtTime(entity, color) { isEnabled() }
        }
    }

    @HandleEvent
    fun onConfigLoad(event: ConfigLoadEvent) {
        ConditionalUtils.onToggle(config.color) {
            // running setEntityColorWithNoHurtTime() again
            checkedEntites.clear()
        }
    }

    private fun isVermin(entity: EntityLivingBase): Boolean = when (entity) {
        is EntityArmorStand -> entity.hasSkullTexture(FLY_TEXTURE) || entity.hasSkullTexture(SPIDER_TEXTURE)
        is EntitySilverfish -> entity.baseMaxHealth == 8

        else -> false
    }

    private fun inArea() = IslandArea.WEST_VILLAGE.isInside() || IslandArea.INFESTED_HOUSE.isInside()

    private fun hasItemInHand() = InventoryUtils.itemInHandId == SkyhanniItems.TURBOMAX_VACUUM()

    fun isEnabled() = RiftAPI.inRift() && inArea() && config.enabled && hasItemInHand()

}

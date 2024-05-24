package at.hannibal2.skyhanni.features.fishing

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.api.skyblock.SkyBlockAPI
import at.hannibal2.skyhanni.events.minecraft.ClientTickEvent
import at.hannibal2.skyhanni.events.render.world.SkyHanniRenderWorldEvent
import at.hannibal2.skyhanni.features.fishing.FishingAPI.isBait
import at.hannibal2.skyhanni.utils.ItemUtils.name
import at.hannibal2.skyhanni.utils.LocationUtils
import at.hannibal2.skyhanni.utils.LorenzVec
import at.hannibal2.skyhanni.utils.RenderUtils.drawString
import at.hannibal2.skyhanni.utils.RenderUtils.exactLocation
import at.hannibal2.skyhanni.utils.StringUtils.removeColor
import at.hannibal2.skyhanni.utils.TimeLimitedCache
import at.hannibal2.skyhanni.utils.mc.McPlayer
import at.hannibal2.skyhanni.utils.mc.McWorld
import net.minecraft.entity.item.EntityItem
import kotlin.time.Duration.Companion.milliseconds

object ShowFishingItemName {

    private val config get() = SkyHanniMod.feature.fishing.fishedItemName
    private var hasRodInHand = false
    private var cache = TimeLimitedCache<EntityItem, Pair<LorenzVec, String>>(750.milliseconds)

    // Taken from Skytils
    private val cheapCoins = setOf(
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTM4MDcxNzIxY2M1YjRjZDQwNmNlNDMxYTEzZjg2MDgzYTg5NzNlMTA2NGQyZjg4OTc4Njk5MzBlZTZlNTIzNyJ9fX0=",
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZGZhMDg3ZWI3NmU3Njg3YTgxZTRlZjgxYTdlNjc3MjY0OTk5MGY2MTY3Y2ViMGY3NTBhNGM1ZGViNmM0ZmJhZCJ9fX0="
    )

    @HandleEvent
    fun onTick(event: ClientTickEvent) {
        if (!isEnabled()) return

        if (event.isMod(10)) {
            hasRodInHand = isFishingRod()
        }
    }

    private fun isFishingRod() = McPlayer.heldItem?.name?.contains("Rod") ?: false

    @HandleEvent
    fun onRenderWorld(event: SkyHanniRenderWorldEvent) {
        if (!isEnabled()) return
        if (hasRodInHand) {
            for (entityItem in McWorld.getEntitiesOf<EntityItem>()) {
                val location = event.exactLocation(entityItem).add(y = 0.8)
                if (location.distance(LocationUtils.playerLocation()) > 15) continue
                val itemStack = entityItem.entityItem
                var name = itemStack.name

                // Hypixel sometimes replaces the bait item mid air with a stone
                if (name.removeColor() == "Stone") continue

                val size = itemStack.stackSize
                val prefix = if (!itemStack.isBait()) {
                    "§a§l+"
                } else {
                    if (!config.showBaits) continue
                    name = "§7" + name.removeColor()
                    "§c§l-"
                }

                itemStack?.tagCompound?.getTag("SkullOwner")?.toString()?.let {
                    for (coin in cheapCoins) {
                        if (it.contains(coin)) {
                            name = "§6Coins"
                        }
                    }
                }

                val sizeText = if (size != 1) "§7x$size §r" else ""
                cache.put(entityItem, location to "$prefix §r$sizeText$name")
            }
        }

        for ((location, text) in cache.values()) {
            event.drawString(location, text)
        }
    }

    fun isEnabled() = SkyBlockAPI.isConnected && config.enabled
}

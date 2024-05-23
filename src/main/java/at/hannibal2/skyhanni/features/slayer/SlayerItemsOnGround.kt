package at.hannibal2.skyhanni.features.slayer

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.data.SlayerAPI
import at.hannibal2.skyhanni.data.item.SkyhanniItems
import at.hannibal2.skyhanni.events.render.world.SkyHanniRenderWorldEvent
import at.hannibal2.skyhanni.utils.ItemUtils.getInternalName
import at.hannibal2.skyhanni.utils.LocationUtils
import at.hannibal2.skyhanni.api.skyblock.SkyBlockAPI
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.LorenzVec
import at.hannibal2.skyhanni.utils.RenderUtils.drawString
import at.hannibal2.skyhanni.utils.RenderUtils.exactLocation
import at.hannibal2.skyhanni.utils.TimeLimitedCache
import at.hannibal2.skyhanni.utils.mc.McWorld
import net.minecraft.entity.item.EntityItem
import net.minecraft.init.Items
import kotlin.time.Duration.Companion.seconds

object SlayerItemsOnGround {

    private val config get() = SkyHanniMod.feature.slayer.itemsOnGround

    private var itemsOnGround = TimeLimitedCache<EntityItem, Pair<LorenzVec, String>>(2.seconds)

    @HandleEvent
    fun onRenderWorld(event: SkyHanniRenderWorldEvent) {
        if (!SkyBlockAPI.isConnected) return
        if (!config.enabled) return
        if (!SlayerAPI.isInCorrectArea) return
        if (!SlayerAPI.hasActiveSlayerQuest()) return

        for (entityItem in McWorld.getEntitiesOf<EntityItem>()) {
            val location = event.exactLocation(entityItem).add(y = 0.8)
            if (location.distance(LocationUtils.playerLocation()) > 15) continue

            val itemStack = entityItem.entityItem
            // happens in spiders den sometimes
            if (itemStack.item == Items.spawn_egg) continue
            if (itemStack.getInternalName().equals("")) continue // TODO remove, should never happen
            if (itemStack.getInternalName() == SkyhanniItems.NONE()) continue

            val (itemName, price) = SlayerAPI.getItemNameAndPrice(itemStack.getInternalName(), itemStack.stackSize)
            if (config.minimumPrice > price) continue

            itemsOnGround.put(entityItem, location to itemName)
        }

        for ((location, text) in itemsOnGround.values()) {
            event.drawString(location, text)
        }
    }
}

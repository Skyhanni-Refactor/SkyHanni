package at.hannibal2.skyhanni.features.slayer

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.api.skyblock.SkyBlockAPI
import at.hannibal2.skyhanni.data.SlayerAPI
import at.hannibal2.skyhanni.data.item.SkyhanniItems
import at.hannibal2.skyhanni.events.minecraft.ClientTickEvent
import at.hannibal2.skyhanni.events.render.world.SkyHanniRenderWorldEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ItemUtils.getInternalName
import at.hannibal2.skyhanni.utils.RenderUtils.drawString
import at.hannibal2.skyhanni.utils.RenderUtils.exactLocation
import at.hannibal2.skyhanni.utils.TimeLimitedCache
import at.hannibal2.skyhanni.utils.mc.McWorld
import net.minecraft.entity.item.EntityItem
import net.minecraft.init.Items
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object SlayerItemsOnGround {

    private val config get() = SkyHanniMod.feature.slayer.itemsOnGround

    private var itemsOnGround = TimeLimitedCache<EntityItem, String>(2.seconds)

    @HandleEvent
    fun onTick(event: ClientTickEvent) {
        if (!isEnabled()) return
        for (entityItem in McWorld.getEntitiesNearPlayer<EntityItem>(15.0)) {
            val itemStack = entityItem.entityItem
            if (itemStack.item == Items.spawn_egg) continue
            if (itemStack.getInternalName() == SkyhanniItems.NONE()) continue
            val (name, price) = SlayerAPI.getItemNameAndPrice(itemStack.getInternalName(), itemStack.stackSize)
            if (config.minimumPrice > price) continue
            itemsOnGround.put(entityItem, name)
        }
    }

    @HandleEvent
    fun onRenderWorld(event: SkyHanniRenderWorldEvent) {
        if (!isEnabled()) return

        for ((item, text) in itemsOnGround) {
            val location = event.exactLocation(item).add(y = 0.8)
            event.drawString(location, text)
        }
    }

    fun isEnabled() = SkyBlockAPI.isConnected && config.enabled &&
        SlayerAPI.isInCorrectArea && SlayerAPI.hasActiveSlayerQuest()
}

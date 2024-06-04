package at.hannibal2.skyhanni.utils.json

import at.hannibal2.skyhanni.api.skyblock.IslandType
import at.hannibal2.skyhanni.features.fishing.trophy.TrophyRarity
import at.hannibal2.skyhanni.utils.LorenzRarity
import at.hannibal2.skyhanni.utils.LorenzVec
import at.hannibal2.skyhanni.utils.NEUInternalName
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.tracker.SkyHanniTracker
import com.google.gson.GsonBuilder
import io.github.notenoughupdates.moulconfig.observer.PropertyTypeAdapterFactory
import net.minecraft.item.ItemStack
import java.util.*

object BaseGsonBuilder {
    fun gson(): GsonBuilder = GsonBuilder().setPrettyPrinting()
        .excludeFieldsWithoutExposeAnnotation()
        .serializeSpecialFloatingPointValues()
        .registerTypeAdapterFactory(PropertyTypeAdapterFactory())
        .registerTypeAdapter(UUID::class.java, SkyHanniTypeAdapters.UUID.nullSafe())
        .registerTypeAdapter(LorenzVec::class.java, SkyHanniTypeAdapters.VEC_STRING.nullSafe())
        .registerTypeAdapter(TrophyRarity::class.java, SkyHanniTypeAdapters.TROPHY_RARITY.nullSafe())
        .registerTypeAdapter(ItemStack::class.java, SkyHanniTypeAdapters.NEU_ITEMSTACK.nullSafe())
        .registerTypeAdapter(NEUInternalName::class.java, SkyHanniTypeAdapters.INTERNAL_NAME.nullSafe())
        .registerTypeAdapter(LorenzRarity::class.java, SkyHanniTypeAdapters.RARITY.nullSafe())
        .registerTypeAdapter(IslandType::class.java, SkyHanniTypeAdapters.ISLAND_TYPE.nullSafe())
        .registerTypeAdapter(
            SkyHanniTracker.DefaultDisplayMode::class.java,
            SkyHanniTypeAdapters.TRACKER_DISPLAY_MODE.nullSafe()
        )
        .registerTypeAdapter(SimpleTimeMark::class.java, SkyHanniTypeAdapters.TIME_MARK.nullSafe())
        .enableComplexMapKeySerialization()

    fun lenientGson(): GsonBuilder = gson().registerTypeAdapterFactory(SkippingTypeAdapterFactory)
}

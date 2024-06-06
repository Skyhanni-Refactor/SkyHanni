package at.hannibal2.skyhanni.data.jsonobjects.repo

import at.hannibal2.skyhanni.features.slayer.SlayerType
import com.google.gson.annotations.Expose

data class SlayerTypeJson(
    @Expose val slayerAreas: Map<String, SlayerType>
)

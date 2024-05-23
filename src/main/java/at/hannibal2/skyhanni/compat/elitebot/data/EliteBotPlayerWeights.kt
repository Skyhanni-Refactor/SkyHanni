package at.hannibal2.skyhanni.compat.elitebot.data

import at.hannibal2.skyhanni.features.garden.CropType
import at.hannibal2.skyhanni.features.garden.pests.PestType
import com.google.gson.annotations.Expose

data class EliteBotPlayerWeights(
    @Expose val selectedProfileId: String,
    @Expose val profiles: List<WeightProfile>,
) {

    data class WeightProfile(
        @Expose val profileId: String,
        @Expose val profileName: String,
        @Expose val totalWeight: Double,
        @Expose val cropWeight: Map<CropType, Double>,
        @Expose val bonusWeight: Map<String, Double>,
        @Expose val uncountedCrops: Map<CropType, Int>,
        @Expose val pests: Map<PestType, Int>,
    )
}

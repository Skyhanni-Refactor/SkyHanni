package at.hannibal2.skyhanni.compat.elitebot.data

import at.hannibal2.skyhanni.features.garden.CropType
import at.hannibal2.skyhanni.features.garden.pests.PestType
import com.google.gson.annotations.Expose

data class EliteBotWeights(
    @Expose val crops: Map<CropType, Double>,
    @Expose val pests: Pests
) {

    data class Pests(
        @Expose val brackets: Map<Int, Int>,
        @Expose val values: Map<PestType, Map<Int, Double>>
    )
}

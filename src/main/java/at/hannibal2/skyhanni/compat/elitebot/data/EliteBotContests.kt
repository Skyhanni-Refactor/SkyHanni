package at.hannibal2.skyhanni.compat.elitebot.data

import at.hannibal2.skyhanni.features.garden.CropType
import com.google.gson.annotations.Expose

data class EliteBotContests(
    @Expose val year: Int,
    @Expose val count: Int = 0,
    @Expose val complete: Boolean,
    @Expose val contests: Map<Long, List<CropType>>
)

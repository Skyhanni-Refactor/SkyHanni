package at.hannibal2.skyhanni.compat.hypixel.data

import com.google.gson.annotations.Expose

data class HypixelMayorData(
    @Expose val mayor: MayorInfo,
    @Expose val current: Election?,
) {

    data class MayorInfo(
        @Expose val key: String,
        @Expose val name: String,
        @Expose val perks: List<Perk>,
        @Expose val election: Election,
    )

    data class Election(
        @Expose val year: Int,
        @Expose val candidates: List<Candidate>,
    ) {

        val winner: Candidate get() = candidates.maxBy { it.votes }
    }

    data class Candidate(
        @Expose val key: String,
        @Expose val name: String,
        @Expose val perks: List<Perk>,
        @Expose val votes: Int,
    )

    data class Perk(
        @Expose val name: String,
        @Expose val description: String,
    )
}

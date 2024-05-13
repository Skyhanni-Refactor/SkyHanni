package at.hannibal2.skyhanni.features.nether.reputationhelper.dailyquest.quest

import at.hannibal2.skyhanni.utils.LorenzVec
import at.hannibal2.skyhanni.utils.NEUInternalName

class DojoQuest(
    val dojoName: String,
    location: LorenzVec?,
    displayItem: NEUInternalName,
    dojoRankGoal: String,
    state: QuestState,
) :
    Quest(
        displayItem,
        location,
        QuestCategory.DOJO,
        "$dojoName Rank $dojoRankGoal",
        state,
        "$dojoName §7(§e" + getPointsForDojoRank(dojoRankGoal) + " points§7)"
    )

fun getPointsForDojoRank(rank: String): Int {
    return when (rank) {
        "S" -> 1000
        "A" -> 800
        "B" -> 600
        "C" -> 400
        "D" -> 200
        "F" -> 0
        else -> 0
    }
}

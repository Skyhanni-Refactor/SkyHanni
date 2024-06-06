package at.hannibal2.skyhanni.features.nether.reputationhelper.dailyquest.quest

import at.hannibal2.skyhanni.data.item.SkyhanniItems

class UnknownQuest(unknownName: String) :
    Quest(SkyhanniItems.MISSING_ITEM(), null, QuestCategory.UNKNOWN, unknownName, QuestState.NOT_ACCEPTED)

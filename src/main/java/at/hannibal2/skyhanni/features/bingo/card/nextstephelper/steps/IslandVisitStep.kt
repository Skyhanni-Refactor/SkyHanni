package at.hannibal2.skyhanni.features.bingo.card.nextstephelper.steps

import at.hannibal2.skyhanni.api.skyblock.IslandType

class IslandVisitStep(val island: IslandType) : NextStep("Visit ${island.displayName}")

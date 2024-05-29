package at.hannibal2.skyhanni.features.gui.customscoreboard

import at.hannibal2.skyhanni.api.BitsAPI
import at.hannibal2.skyhanni.api.HypixelAPI
import at.hannibal2.skyhanni.api.skyblock.Gamemode
import at.hannibal2.skyhanni.api.skyblock.IslandType
import at.hannibal2.skyhanni.api.skyblock.IslandTypeTag
import at.hannibal2.skyhanni.api.skyblock.SkyBlockAPI
import at.hannibal2.skyhanni.config.features.gui.customscoreboard.ArrowConfig.ArrowAmountDisplay
import at.hannibal2.skyhanni.data.MaxwellAPI
import at.hannibal2.skyhanni.data.MayorAPI
import at.hannibal2.skyhanni.data.MiningAPI.getCold
import at.hannibal2.skyhanni.data.PartyAPI
import at.hannibal2.skyhanni.data.PurseAPI
import at.hannibal2.skyhanni.data.QuiverAPI
import at.hannibal2.skyhanni.data.QuiverAPI.NONE_ARROW_TYPE
import at.hannibal2.skyhanni.data.ScoreboardData
import at.hannibal2.skyhanni.data.SlayerAPI
import at.hannibal2.skyhanni.features.dungeon.DungeonAPI
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboardUtils.formatNum
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboardUtils.getGroupFromPattern
import at.hannibal2.skyhanni.test.command.ErrorManager
import at.hannibal2.skyhanni.utils.CollectionUtils.nextAfter
import at.hannibal2.skyhanni.utils.NumberUtil.addSeparators
import at.hannibal2.skyhanni.utils.NumberUtil.percentageColor
import at.hannibal2.skyhanni.utils.RegexUtils.anyMatches
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.RenderUtils.HorizontalAlignment
import at.hannibal2.skyhanni.utils.StringUtils.firstLetterUppercase
import at.hannibal2.skyhanni.utils.StringUtils.pluralize
import at.hannibal2.skyhanni.utils.TabListData
import at.hannibal2.skyhanni.utils.TimeLimitedSet
import at.hannibal2.skyhanni.utils.datetime.SkyBlockTime
import at.hannibal2.skyhanni.utils.datetime.TimeUtils.format
import at.hannibal2.skyhanni.utils.datetime.TimeUtils.formatted
import java.util.function.Supplier
import kotlin.time.Duration.Companion.seconds

internal var confirmedUnknownLines = mutableListOf<String>()
internal var unconfirmedUnknownLines = listOf<String>()
internal var unknownLinesSet = TimeLimitedSet<String>(1.seconds) { onRemoval(it) }

private fun onRemoval(line: String) {
    if (!unconfirmedUnknownLines.contains(line)) return
    unconfirmedUnknownLines = unconfirmedUnknownLines.filterNot { it == line }
    confirmedUnknownLines.add(line)
    if (!CustomScoreboard.config.unknownLinesWarning) return
    val pluralize = pluralize(confirmedUnknownLines.size, "unknown line", withNumber = true)
    val message = "CustomScoreboard detected $pluralize"
    ErrorManager.logErrorWithData(
        CustomScoreboardUtils.UndetectedScoreboardLines(message),
        message,
        "Unknown Lines" to confirmedUnknownLines,
        "Island" to SkyBlockAPI.island,
        "Area" to SkyBlockAPI.area,
        "Full Scoreboard" to ScoreboardData.sidebarLinesFormatted,
        noStackTrace = true,
        betaOnly = true,
    )
}

internal var amountOfUnknownLines = 0

enum class ScoreboardElement(
    private val displayPair: Supplier<List<ScoreboardElementType>>,
    val showWhen: () -> Boolean,
    private val configLine: String,
) {
    TITLE(
        ::getTitleDisplayPair,
        { true },
        "§6§lSKYBLOCK"
    ),
    PROFILE(
        ::getProfileDisplayPair,
        { true },
        "§7♲ Blueberry"
    ),
    PURSE(
        ::getPurseDisplayPair,
        ::getPurseShowWhen,
        "Purse: §652,763,737"
    ),
    MOTES(
        ::getMotesDisplayPair,
        ::getMotesShowWhen,
        "Motes: §d64,647"
    ),
    BANK(
        ::getBankDisplayPair,
        ::getBankShowWhen,
        "Bank: §6249M"
    ),
    BITS(
        ::getBitsDisplayPair,
        ::getBitsShowWhen,
        "Bits: §b59,264"
    ),
    COPPER(
        ::getCopperDisplayPair,
        ::getCopperShowWhen,
        "Copper: §c23,495"
    ),
    GEMS(
        ::getGemsDisplayPair,
        ::getGemsShowWhen,
        "Gems: §a57,873"
    ),
    HEAT(
        ::getHeatDisplayPair,
        ::getHeatShowWhen,
        "Heat: §c♨ 0"
    ),
    COLD(
        ::getColdDisplayPair,
        ::getColdShowWhen,
        "Cold: §b0❄"
    ),
    NORTH_STARS(
        ::getNorthStarsDisplayPair,
        ::getNorthStarsShowWhen,
        "North Stars: §d756"
    ),
    EMPTY_LINE(
        ::getEmptyLineDisplayPair,
        { true }, ""
    ),
    ISLAND(
        ::getIslandDisplayPair,
        { true },
        "§7㋖ §aHub"
    ),
    LOCATION(
        ::getLocationDisplayPair,
        { true },
        "§7⏣ §bVillage"
    ),
    PLAYER_AMOUNT(
        ::getPlayerAmountDisplayPair,
        { true },
        "§7Players: §a69§7/§a80"
    ),
    VISITING(
        ::getVisitDisplayPair,
        ::getVisitShowWhen,
        " §a✌ §7(§a1§7/6)"
    ),
    DATE(
        ::getDateDisplayPair,
        { true },
        "Late Summer 11th"
    ),
    TIME(
        ::getTimeDisplayPair,
        { true },
        "§710:40pm §b☽"
    ),
    LOBBY_CODE(
        ::getLobbyDisplayPair,
        { true },
        "§8mega77CK"
    ),
    POWER(
        ::getPowerDisplayPair,
        ::getPowerShowWhen,
        "Power: §aSighted §7(§61.263§7)"
    ),
    TUNING(
        ::getTuningDisplayPair,
        ::getPowerShowWhen,
        "Tuning: §c❁34§7, §e⚔20§7, and §9☣7"
    ),
    COOKIE(
        ::getCookieDisplayPair,
        ::getCookieShowWhen,
        "§dCookie Buff§f: 3d 17h"
    ),
    EMPTY_LINE2(
        ::getEmptyLineDisplayPair,
        { true }, ""
    ),
    OBJECTIVE(
        ::getObjectiveDisplayPair,
        ::getObjectiveShowWhen,
        "Objective:\n§eStar SkyHanni on Github"
    ),
    SLAYER(
        ::getSlayerDisplayPair,
        ::getSlayerShowWhen,
        "Slayer Quest\n §7- §cVoidgloom Seraph III\n §7- §e12§7/§c120 §7Kills"
    ),
    EMPTY_LINE3(
        ::getEmptyLineDisplayPair,
        { true },
        ""
    ),
    QUIVER(
        ::getQuiverDisplayPair,
        ::getQuiverShowWhen,
        "Flint Arrow: §f1,234"
    ),
    POWDER(
        ::getPowderDisplayPair,
        ::getPowderShowWhen,
        "§9§lPowder\n §7- §fMithril: §254,646\n §7- §fGemstone: §d51,234"
    ),
    EVENTS(
        ::getEventsDisplayPair,
        ::getEventsShowWhen,
        "§7Wide Range of Events\n§7(too much to show all)"
    ),
    MAYOR(
        ::getMayorDisplayPair,
        ::getMayorShowWhen,
        "§2Diana:\n §7- §eLucky!\n §7- §eMythological Ritual\n §7- §ePet XP Buff"
    ),
    PARTY(
        ::getPartyDisplayPair,
        ::getPartyShowWhen,
        "§9§lParty (4):\n §7- §fhannibal2\n §7- §fMoulberry\n §7- §fVahvl\n §7- §fSkirtwearer"
    ),
    FOOTER(
        ::getFooterDisplayPair,
        { true },
        "§ewww.hypixel.net"
    ),
    EXTRA(
        ::getExtraDisplayPair,
        ::getExtraShowWhen,
        "§cUnknown lines the mod is not detecting"
    ),
    EMPTY_LINE4(
        ::getEmptyLineDisplayPair,
        { true },
        ""
    ),
    EMPTY_LINE5(
        ::getEmptyLineDisplayPair,
        { true },
        ""
    ),
    EMPTY_LINE6(
        ::getEmptyLineDisplayPair,
        { true },
        ""
    ),
    EMPTY_LINE7(
        ::getEmptyLineDisplayPair,
        { true },
        ""
    ),
    EMPTY_LINE8(
        ::getEmptyLineDisplayPair,
        { true },
        ""
    ),
    EMPTY_LINE9(
        ::getEmptyLineDisplayPair,
        { true },
        ""
    ),
    EMPTY_LINE10(
        ::getEmptyLineDisplayPair,
        { true },
        ""
    ),
    ;

    override fun toString() = configLine

    fun getVisiblePair() = if (isVisible()) getPair() else listOf("<hidden>" to HorizontalAlignment.LEFT)

    private fun getPair(): List<ScoreboardElementType> {
        return try {
            displayPair.get()
        } catch (e: NoSuchElementException) {
            listOf("<hidden>" to HorizontalAlignment.LEFT)
        }
    }

    private fun isVisible(): Boolean {
        if (!CustomScoreboard.informationFilteringConfig.hideIrrelevantLines) return true
        return showWhen()
    }

    companion object {
        // I don't know why, but this field is needed for it to work
        @JvmField
        val defaultOption = listOf(
            TITLE,
            PROFILE,
            PURSE,
            BANK,
            MOTES,
            BITS,
            COPPER,
            NORTH_STARS,
            HEAT,
            COLD,
            EMPTY_LINE,
            ISLAND,
            LOCATION,
            LOBBY_CODE,
            PLAYER_AMOUNT,
            VISITING,
            EMPTY_LINE2,
            DATE,
            TIME,
            EVENTS,
            OBJECTIVE,
            COOKIE,
            EMPTY_LINE3,
            QUIVER,
            POWER,
            TUNING,
            EMPTY_LINE4,
            POWDER,
            MAYOR,
            PARTY,
            FOOTER,
            EXTRA
        )
    }
}

private fun getTitleDisplayPair(): List<ScoreboardElementType> =
    if (CustomScoreboard.displayConfig.titleAndFooter.useHypixelTitleAnimation) {
        listOf(ScoreboardData.objectiveTitle to CustomScoreboard.displayConfig.titleAndFooter.alignTitleAndFooter)
    } else {
        listOf(
            CustomScoreboard.displayConfig.titleAndFooter.customTitle.get().toString()
            .replace("&", "§")
            .split("\\n")
                .map { it to CustomScoreboard.displayConfig.titleAndFooter.alignTitleAndFooter }
        ).flatten()
    }

private fun getProfileDisplayPair() =
    listOf(CustomScoreboardUtils.getProfileTypeSymbol() + SkyBlockAPI.profileName?.firstLetterUppercase() to HorizontalAlignment.LEFT)

private fun getPurseDisplayPair(): List<ScoreboardElementType> {
    var purse = PurseAPI.currentPurse.formatNum()

    val earned = getGroupFromPattern(ScoreboardData.sidebarLinesFormatted, PurseAPI.coinsPattern, "earned")

    if (earned != "0") {
        purse += " §7(§e+$earned§7)§6"
    }

    return listOf(
        when {
            CustomScoreboard.informationFilteringConfig.hideEmptyLines && purse == "0" -> "<hidden>"
            CustomScoreboard.displayConfig.displayNumbersFirst -> "§6$purse Purse"
            else -> "Purse: §6$purse"
        } to HorizontalAlignment.LEFT
    )
}

private fun getPurseShowWhen() = !IslandType.THE_RIFT.isInIsland()

private fun getMotesDisplayPair(): List<ScoreboardElementType> {
    val motes = getGroupFromPattern(ScoreboardData.sidebarLinesFormatted, ScoreboardPattern.motesPattern, "motes")
        .formatNum()

    return listOf(
        when {
            CustomScoreboard.informationFilteringConfig.hideEmptyLines && motes == "0" -> "<hidden>"
            CustomScoreboard.displayConfig.displayNumbersFirst -> "§d$motes Motes"
            else -> "Motes: §d$motes"
        } to HorizontalAlignment.LEFT
    )
}

private fun getMotesShowWhen() = IslandType.THE_RIFT.isInIsland()

private fun getBankDisplayPair(): List<ScoreboardElementType> {
    val bank = getGroupFromPattern(TabListData.getTabList(), ScoreboardPattern.bankPattern, "bank")

    return listOf(
        when {
            CustomScoreboard.informationFilteringConfig.hideEmptyLines && (bank == "0" || bank == "0§7 / §60") -> "<hidden>"
            CustomScoreboard.displayConfig.displayNumbersFirst -> "§6$bank Bank"
            else -> "Bank: §6$bank"
        } to HorizontalAlignment.LEFT
    )
}

private fun getBankShowWhen() = !IslandType.THE_RIFT.isInIsland()

private fun getBitsDisplayPair(): List<ScoreboardElementType> {
    val bits = BitsAPI.bits.coerceAtLeast(0).formatNum()
    val bitsToClaim = if (BitsAPI.bitsAvailable == -1) {
        "§cOpen Sbmenu§b"
    } else {
        BitsAPI.bitsAvailable.coerceAtLeast(0).formatNum()
    }

    return listOf(
        when {
            CustomScoreboard.informationFilteringConfig.hideEmptyLines && bits == "0" && bitsToClaim == "0" -> "<hidden>"
            CustomScoreboard.displayConfig.displayNumbersFirst -> {
                if (CustomScoreboard.displayConfig.showUnclaimedBits) {
                    "§b$bits§7/${if (bitsToClaim == "0") "§30" else "§b${bitsToClaim}"} §bBits"
                } else {
                    "§b$bits Bits"
                }
            }

            else -> {
                if (CustomScoreboard.displayConfig.showUnclaimedBits) {
                    "Bits: §b$bits§7/${if (bitsToClaim == "0") "§30" else "§b${bitsToClaim}"}"
                } else {
                    "Bits: §b$bits"
                }
            }
        } to HorizontalAlignment.LEFT
    )
}

private fun getBitsShowWhen() =
    SkyBlockAPI.gamemode != Gamemode.BINGO && !IslandTypeTag.BITS_NOT_SHOWN.inAny()

private fun getCopperDisplayPair(): List<ScoreboardElementType> {
    val copper = getGroupFromPattern(ScoreboardData.sidebarLinesFormatted, ScoreboardPattern.copperPattern, "copper")
        .formatNum()

    return listOf(
        when {
            CustomScoreboard.informationFilteringConfig.hideEmptyLines && copper == "0" -> "<hidden>"
            CustomScoreboard.displayConfig.displayNumbersFirst -> "§c$copper Copper"
            else -> "Copper: §c$copper"
        } to HorizontalAlignment.LEFT
    )
}

private fun getCopperShowWhen() = IslandType.GARDEN.isInIsland()

private fun getGemsDisplayPair(): List<ScoreboardElementType> {
    val gems = getGroupFromPattern(TabListData.getTabList(), ScoreboardPattern.gemsPattern, "gems")

    return listOf(
        when {
            CustomScoreboard.informationFilteringConfig.hideEmptyLines && gems == "0" -> "<hidden>"
            CustomScoreboard.displayConfig.displayNumbersFirst -> "§a$gems Gems"
            else -> "Gems: §a$gems"
        } to HorizontalAlignment.LEFT
    )
}

private fun getGemsShowWhen() = !IslandTypeTag.GEMS_NOT_SHOWN.inAny()

private fun getHeatDisplayPair(): List<ScoreboardElementType> {
    val heat = getGroupFromPattern(ScoreboardData.sidebarLinesFormatted, ScoreboardPattern.heatPattern, "heat")

    return listOf(
        when {
            CustomScoreboard.informationFilteringConfig.hideEmptyLines && heat == "§c♨ 0" -> "<hidden>"
            CustomScoreboard.displayConfig.displayNumbersFirst/* && heat != "§6IMMUNE" */ -> if (heat == "0") "§c♨ 0 Heat" else "$heat Heat"
            else -> if (heat == "0") "Heat: §c♨ 0" else "Heat: $heat"
        } to HorizontalAlignment.LEFT
    )
}

private fun getHeatShowWhen() = IslandType.CRYSTAL_HOLLOWS.isInIsland()
    && ScoreboardData.sidebarLinesFormatted.any { ScoreboardPattern.heatPattern.matches(it) }

private fun getColdDisplayPair(): List<ScoreboardElementType> {
    val cold = -getCold()

    return listOf(
        when {
            CustomScoreboard.informationFilteringConfig.hideEmptyLines && cold == 0 -> "<hidden>"
            CustomScoreboard.displayConfig.displayNumbersFirst -> "§b$cold❄ Cold"
            else -> "Cold: §b$cold❄"
        } to HorizontalAlignment.LEFT
    )
}

private fun getColdShowWhen() = IslandTypeTag.IS_COLD.inAny()
    && ScoreboardData.sidebarLinesFormatted.any { ScoreboardPattern.coldPattern.matches(it) }

private fun getNorthStarsDisplayPair(): List<ScoreboardElementType> {
    val northStars =
        getGroupFromPattern(ScoreboardData.sidebarLinesFormatted, ScoreboardPattern.northstarsPattern, "northstars")
            .formatNum()

    return listOf(
        when {
            CustomScoreboard.informationFilteringConfig.hideEmptyLines && northStars == "0" -> "<hidden>"
            CustomScoreboard.displayConfig.displayNumbersFirst -> "§d$northStars North Stars"
            else -> "North Stars: §d$northStars"
        } to HorizontalAlignment.LEFT
    )
}

private fun getNorthStarsShowWhen() = IslandType.WINTER.isInIsland()

private fun getEmptyLineDisplayPair() = listOf("<empty>" to HorizontalAlignment.LEFT)

private fun getIslandDisplayPair() =
    listOf("§7㋖ §a" + SkyBlockAPI.island.displayName to HorizontalAlignment.LEFT)

private fun getLocationDisplayPair() = buildList {
    SkyBlockAPI.areaWithSymbol?.let { add(it to HorizontalAlignment.LEFT) }

    ScoreboardData.sidebarLinesFormatted.firstOrNull { ScoreboardPattern.plotPattern.matches(it) }
        ?.let { add(it to HorizontalAlignment.LEFT) }
}

fun getPlayerAmountDisplayPair() = buildList {
    val max = if (CustomScoreboard.displayConfig.showMaxIslandPlayers) {
        "§7/§a${SkyBlockAPI.maxPlayers}"
    } else {
        ""
    }
    if (CustomScoreboard.displayConfig.displayNumbersFirst) {
        add("§a${SkyBlockAPI.players}$max Players" to HorizontalAlignment.LEFT)
    } else {
        add("§7Players: §a${SkyBlockAPI.players}$max" to HorizontalAlignment.LEFT)
    }
}

private fun getVisitDisplayPair() =
    listOf(
        ScoreboardData.sidebarLinesFormatted.first { ScoreboardPattern.visitingPattern.matches(it) } to HorizontalAlignment.LEFT
    )

private fun getVisitShowWhen() =
    ScoreboardData.sidebarLinesFormatted.any { ScoreboardPattern.visitingPattern.matches(it) }

private fun getDateDisplayPair() =
    listOf(
        SkyBlockTime.now().formatted(yearElement = false, hoursAndMinutesElement = false) to HorizontalAlignment.LEFT
    )

private fun getTimeDisplayPair(): List<ScoreboardElementType> {
    var symbol = getGroupFromPattern(ScoreboardData.sidebarLinesFormatted, ScoreboardPattern.timePattern, "symbol")
    if (symbol == "0") symbol = ""
    return listOf(
        "§7" + SkyBlockTime.now()
            .formatted(dayAndMonthElement = false, yearElement = false) + " $symbol" to HorizontalAlignment.LEFT
    )
}

private fun getLobbyDisplayPair(): List<ScoreboardElementType> {
    val roomId = DungeonAPI.getRoomID()?.let { "§8$it" } ?: ""
    val lobbyDisplay = HypixelAPI.server?.let { "§8$it $roomId" } ?: "<hidden>"
    return listOf(lobbyDisplay to HorizontalAlignment.LEFT)
}

private fun getPowerDisplayPair() = listOf(
    (MaxwellAPI.currentPower?.let {
        val mp =
            if (CustomScoreboard.maxwellConfig.showMagicalPower) "§7(§6${MaxwellAPI.magicalPower?.addSeparators()}§7)" else ""
        if (CustomScoreboard.displayConfig.displayNumbersFirst) {
            "§a${it.replace(" Power", "")} Power $mp"
        } else {
            "Power: §a$it $mp"
        }
    }
        ?: "§cOpen \"Your Bags\"!") to HorizontalAlignment.LEFT
)

private fun getTuningDisplayPair(): List<Pair<String, HorizontalAlignment>> {
    val tunings = MaxwellAPI.tunings ?: return listOf("§cTalk to \"Maxwell\"!" to HorizontalAlignment.LEFT)
    if (tunings.isEmpty()) return listOf("§cNo Maxwell Tunings :(" to HorizontalAlignment.LEFT)

    val title = pluralize(tunings.size, "Tuning", "Tunings")
    return if (CustomScoreboard.maxwellConfig.compactTuning) {
        val tuning = tunings
            .take(3)
            .joinToString("§7, ") { tuning ->
                with(tuning) {
                    if (CustomScoreboard.displayConfig.displayNumbersFirst) {
                        "$color$value$icon"
                    } else {
                        "$color$icon$value"
                    }
                }

            }
        listOf(
            if (CustomScoreboard.displayConfig.displayNumbersFirst) {
                "$tuning §f$title"
            } else {
                "$title: $tuning"
            } to HorizontalAlignment.LEFT
        )
    } else {
        val tuning = tunings
            .take(CustomScoreboard.maxwellConfig.tuningAmount.coerceAtLeast(1))
            .map { tuning ->
                with(tuning) {
                    " §7- §f" + if (CustomScoreboard.displayConfig.displayNumbersFirst) {
                        "$color$value $icon $name"
                    } else {
                        "$name: $color$value$icon"
                    }
                }

            }.toTypedArray()
        listOf("$title:", *tuning).map { it to HorizontalAlignment.LEFT }
    }
}

private fun getPowerShowWhen() = !IslandType.THE_RIFT.isInIsland()

private fun getCookieDisplayPair() = listOf(
    "§dCookie Buff§f: " + (BitsAPI.cookieBuffTime?.let {
        if (!BitsAPI.hasCookieBuff()) "§cNot Active" else it.timeUntil().format(maxUnits = 2)
    }
        ?: "§cOpen SbMenu!") to HorizontalAlignment.LEFT
)

private fun getCookieShowWhen(): Boolean {
    if (SkyBlockAPI.gamemode == Gamemode.BINGO) return false
    return CustomScoreboard.informationFilteringConfig.hideEmptyLines && BitsAPI.hasCookieBuff()
}

private fun getObjectiveDisplayPair() = buildList {
    val objective =
        ScoreboardData.sidebarLinesFormatted.first { ScoreboardPattern.objectivePattern.matches(it) }

    add(objective to HorizontalAlignment.LEFT)
    add((ScoreboardData.sidebarLinesFormatted.nextAfter(objective) ?: "<hidden>") to HorizontalAlignment.LEFT)

    if (ScoreboardData.sidebarLinesFormatted.any { ScoreboardPattern.thirdObjectiveLinePattern.matches(it) }) {
        add(
            (ScoreboardData.sidebarLinesFormatted.nextAfter(objective, 2)
                ?: "Second objective here") to HorizontalAlignment.LEFT
        )
    }
}

private fun getObjectiveShowWhen(): Boolean =
    ScoreboardPattern.objectivePattern.anyMatches(ScoreboardData.sidebarLinesFormatted)

private fun getSlayerDisplayPair(): List<ScoreboardElementType> = buildList {
    add((if (SlayerAPI.hasActiveSlayerQuest()) "Slayer Quest" else "<hidden>") to HorizontalAlignment.LEFT)
    add(" §7- §e${SlayerAPI.latestSlayerCategory.trim()}" to HorizontalAlignment.LEFT)
    add(" §7- §e${SlayerAPI.latestSlayerProgress.trim()}" to HorizontalAlignment.LEFT)
}

private fun getSlayerShowWhen() =
    if (CustomScoreboard.informationFilteringConfig.hideIrrelevantLines) SlayerAPI.isInCorrectArea else true

private fun getQuiverDisplayPair(): List<ScoreboardElementType> {
    if (QuiverAPI.currentArrow == null)
        return listOf("§cChange your Arrow once" to HorizontalAlignment.LEFT)
    if (QuiverAPI.currentArrow == NONE_ARROW_TYPE)
        return listOf("No Arrows selected" to HorizontalAlignment.LEFT)

    val amountString = (if (CustomScoreboard.arrowConfig.colorArrowAmount) {
        percentageColor(
            QuiverAPI.currentAmount.toLong(),
            QuiverAPI.MAX_ARROW_AMOUNT.toLong()
        ).getChatColor()
    } else {
        ""
    }) + if (QuiverAPI.wearingSkeletonMasterChestplate) {
        "∞"
    } else {
        when (CustomScoreboard.arrowConfig.arrowAmountDisplay) {
            ArrowAmountDisplay.NUMBER -> QuiverAPI.currentAmount.addSeparators()
            ArrowAmountDisplay.PERCENTAGE -> "${QuiverAPI.asArrowPercentage(QuiverAPI.currentAmount)}%"
            else -> QuiverAPI.currentAmount.addSeparators()
        }
    }

    return listOf(
        if (CustomScoreboard.displayConfig.displayNumbersFirst) {
            "$amountString ${QuiverAPI.currentArrow?.arrow}s"
        } else {
            "Arrows: $amountString ${QuiverAPI.currentArrow?.arrow?.replace(" Arrow", "")}"
        } to HorizontalAlignment.LEFT
    )
}

private fun getQuiverShowWhen(): Boolean {
    if (CustomScoreboard.informationFilteringConfig.hideIrrelevantLines && !QuiverAPI.hasBowInInventory()) return false
    return !IslandType.THE_RIFT.isInIsland()
}

private fun getPowderDisplayPair() = buildList {
    val powderTypes: List<Triple<String, String, String>> = listOf(
        Triple(
            "Mithril", "§2", getGroupFromPattern(
                TabListData.getTabList(),
                ScoreboardPattern.mithrilPowderPattern,
                "mithrilpowder"
            ).formatNum()
        ),
        Triple(
            "Gemstone", "§d", getGroupFromPattern(
                TabListData.getTabList(),
                ScoreboardPattern.gemstonePowderPattern,
                "gemstonepowder"
            ).formatNum()
        ),
        Triple(
            "Glacite", "§b", getGroupFromPattern(
                TabListData.getTabList(),
                ScoreboardPattern.glacitePowderPattern,
                "glacitepowder"
            ).formatNum()
        )
    )

    if (CustomScoreboard.informationFilteringConfig.hideEmptyLines && powderTypes.all { it.third == "0" }) {
        add("<hidden>" to HorizontalAlignment.LEFT)
    } else {
        add("§9§lPowder" to HorizontalAlignment.LEFT)

        if (CustomScoreboard.displayConfig.displayNumbersFirst) {
            for ((type, color, value) in powderTypes) {
                if (value != "0") {
                    add(" §7- $color$value $type" to HorizontalAlignment.LEFT)
                }
            }
        } else {
            for ((type, color, value) in powderTypes) {
                if (value != "0") {
                    add(" §7- §f$type: $color$value" to HorizontalAlignment.LEFT)
                }
            }
        }
    }
}

private fun getPowderShowWhen() = IslandTypeTag.ADVANCED_MINING.inAny()

private fun getEventsDisplayPair(): List<ScoreboardElementType> {
    return ScoreboardEvents.getEvent()
        .filterNotNull()
        .flatMap { it.getLines().map { i -> i to HorizontalAlignment.LEFT } }
        .takeIf { it.isNotEmpty() } ?: listOf("<hidden>" to HorizontalAlignment.LEFT)
}

private fun getEventsShowWhen() = ScoreboardEvents.getEvent().isNotEmpty()

private fun getMayorDisplayPair() = buildList {
    add(
        ((MayorAPI.currentMayor?.mayorName?.let { MayorAPI.mayorNameWithColorCode(it) }
            ?: "<hidden>") +
            (if (CustomScoreboard.mayorConfig.showTimeTillNextMayor) {
                "§7 (§e${MayorAPI.timeTillNextMayor.format(maxUnits = 2)}§7)"
            } else {
                ""
            })) to HorizontalAlignment.LEFT
    )
    if (CustomScoreboard.mayorConfig.showMayorPerks) {
        MayorAPI.currentMayor?.activePerks?.forEach {
            add(" §7- §e${it.perkName}" to HorizontalAlignment.LEFT)
        }
    }
}

private fun getMayorShowWhen() =
    !IslandType.THE_RIFT.isInIsland() && MayorAPI.currentMayor != null

private fun getPartyDisplayPair() =
    if (PartyAPI.partyMembers.isEmpty() && CustomScoreboard.informationFilteringConfig.hideEmptyLines) {
        listOf("<hidden>" to HorizontalAlignment.LEFT)
    } else {
        val title =
            if (PartyAPI.partyMembers.isEmpty()) "§9§lParty" else "§9§lParty (${PartyAPI.partyMembers.size})"
        val partyList = PartyAPI.partyMembers
            .take(CustomScoreboard.partyConfig.maxPartyList.get())
            .map {
                " §7- §f$it"
            }
            .toTypedArray()
        listOf(title, *partyList).map { it to HorizontalAlignment.LEFT }
    }

private fun getPartyShowWhen() = if (DungeonAPI.inDungeon()) {
    false // Hidden bc the scoreboard lines already exist
} else {
    CustomScoreboard.partyConfig.showPartyEverywhere || IslandTypeTag.SHOW_PARTY.inAny()
}

private fun getFooterDisplayPair(): List<ScoreboardElementType> =
    listOf(
        CustomScoreboard.displayConfig.titleAndFooter.customFooter.get().toString()
        .replace("&", "§")
        .split("\\n")
            .map { it to CustomScoreboard.displayConfig.titleAndFooter.alignTitleAndFooter }
    ).flatten()

private fun getExtraDisplayPair(): List<ScoreboardElementType> {
    if (confirmedUnknownLines.isEmpty()) return listOf("<hidden>" to HorizontalAlignment.LEFT)
    amountOfUnknownLines = confirmedUnknownLines.size

    return listOf("§cUndetected Lines:" to HorizontalAlignment.LEFT) + confirmedUnknownLines.map { it to HorizontalAlignment.LEFT }
}

private fun getExtraShowWhen(): Boolean {
    if (confirmedUnknownLines.isEmpty()) {
        amountOfUnknownLines = 0
    }
    return confirmedUnknownLines.isNotEmpty()
}

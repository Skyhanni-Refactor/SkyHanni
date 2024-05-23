package at.hannibal2.skyhanni.data

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.minecraft.ClientTickEvent
import at.hannibal2.skyhanni.events.minecraft.RawScoreboardUpdateEvent
import at.hannibal2.skyhanni.events.minecraft.ScoreboardUpdateEvent
import at.hannibal2.skyhanni.events.minecraft.packet.ReceivePacketEvent
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.mc.McWorld
import net.minecraft.network.play.server.S3CPacketUpdateScore
import net.minecraft.network.play.server.S3EPacketTeams
import net.minecraft.scoreboard.Score
import net.minecraft.scoreboard.ScorePlayerTeam

object ScoreboardData {

    private val minecraftColorCodesPattern = "(?i)[0-9a-fkmolnr]".toPattern()

    var sidebarLinesFormatted: List<String> = emptyList()

    private var sidebarLines: List<String> = emptyList() // TODO rename to raw
    var sidebarLinesRaw: List<String> = emptyList() // TODO delete
    var objectiveTitle = ""

    var dirty = false

    // TODO USE SH-REPO
    private val splitIcons = listOf(
        "\uD83C\uDF6B",
        "\uD83D\uDCA3",
        "\uD83D\uDC7D",
        "\uD83D\uDD2E",
        "\uD83D\uDC0D",
        "\uD83D\uDC7E",
        "\uD83C\uDF20",
        "\uD83C\uDF6D",
        "⚽",
        "\uD83C\uDFC0",
        "\uD83D\uDC79",
        "\uD83C\uDF81",
        "\uD83C\uDF89",
        "\uD83C\uDF82",
        "\uD83D\uDD2B",
    )

    fun formatLines(rawList: List<String>): List<String> {
        val list = mutableListOf<String>()
        for (line in rawList) {
            val separator = splitIcons.find { line.contains(it) } ?: continue
            val split = line.split(separator)
            val start = split[0]
            var end = split[1]
            // get last color code in start
            val lastColorIndex = start.lastIndexOf('§')
            val lastColor = if (lastColorIndex != -1
                && lastColorIndex + 1 < start.length
                && (minecraftColorCodesPattern.matches(start[lastColorIndex + 1].toString()))
            ) start.substring(lastColorIndex, lastColorIndex + 2)
            else ""

            // remove first color code from end, when it is the same as the last color code in start
            end = end.removePrefix(lastColor)

            list.add(start + end)
        }

        return list
    }

    @HandleEvent(receiveCancelled = true)
    fun onPacketReceive(event: ReceivePacketEvent) {
        if (event.packet is S3CPacketUpdateScore) {
            if (event.packet.objectiveName == "update") {
                dirty = true
            }
        }
        if (event.packet is S3EPacketTeams) {
            if (event.packet.name.startsWith("team_")) {
                dirty = true
            }
        }
    }

    @HandleEvent(priority = HandleEvent.HIGHEST)
    fun onTick(event: ClientTickEvent) {
        if (!dirty) return
        dirty = false

        val list = fetchScoreboardLines().reversed()
        val semiFormatted = list.map { cleanSB(it) }
        if (semiFormatted != sidebarLines) {
            RawScoreboardUpdateEvent(semiFormatted).post()
            sidebarLines = semiFormatted
        }

        sidebarLinesRaw = list
        val new = formatLines(list)
        if (new != sidebarLinesFormatted) {
            ScoreboardUpdateEvent(new).post()
            sidebarLinesFormatted = new
        }
    }

    private fun cleanSB(scoreboard: String): String {
        return scoreboard.toCharArray().filter { it.code in 21..126 || it.code == 167 }.joinToString(separator = "")
    }

    private fun fetchScoreboardLines(): List<String> {
        val scoreboard = McWorld.getScoreboard() ?: return emptyList()
        val objective = scoreboard.getObjectiveInDisplaySlot(1) ?: return emptyList()
        objectiveTitle = objective.displayName
        var scores = scoreboard.getSortedScores(objective)
        val list = scores.filter { input: Score? ->
            input != null && input.playerName != null && !input.playerName.startsWith("#")
        }
        scores = if (list.size > 15) {
            list.drop(15)
        } else {
            list
        }
        return scores.map {
            ScorePlayerTeam.formatPlayerName(scoreboard.getPlayersTeam(it.playerName), it.playerName)
        }
    }
}

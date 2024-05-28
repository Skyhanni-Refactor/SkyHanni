package at.hannibal2.skyhanni.features.mining.eventtracker

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.api.skyblock.IslandType
import at.hannibal2.skyhanni.api.skyblock.IslandTypeTag
import at.hannibal2.skyhanni.api.skyblock.SkyBlockAPI
import at.hannibal2.skyhanni.compat.soopy.data.MiningEventData
import at.hannibal2.skyhanni.compat.soopy.data.RunningEventType
import at.hannibal2.skyhanni.config.features.mining.MiningEventConfig
import at.hannibal2.skyhanni.events.render.gui.GuiOverlayRenderEvent
import at.hannibal2.skyhanni.events.utils.SecondPassedEvent
import at.hannibal2.skyhanni.features.fame.ReminderUtils
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.RenderUtils.renderStrings
import at.hannibal2.skyhanni.utils.SimpleTimeMark.Companion.asTimeMark

@SkyHanniModule
object MiningEventDisplay {
    private val config get() = SkyHanniMod.feature.mining.miningEvent
    private var display = listOf<String>()

    private val islandEventData: MutableMap<IslandType, MiningIslandEventInfo> = mutableMapOf()

    @HandleEvent
    fun onSecondPassed(event: SecondPassedEvent) {
        updateDisplay()
    }

    @HandleEvent
    fun onRenderOverlay(event: GuiOverlayRenderEvent) {
        if (!shouldDisplay()) return
        config.position.renderStrings(display, posLabel = "Upcoming Events Display")
    }

    private fun updateDisplay() {
        display = emptyList()
        updateEvents()
    }

    private fun updateEvents() {
        val list = mutableListOf<String>()

        if (MiningEventTracker.apiError) {
            val count = MiningEventTracker.apiErrorCount
            list.add("§cMining Event API Error! ($count)")
            list.add("§cSwap servers to try again!")
        }

        islandEventData.forEach { (islandType, eventDetails) ->
            val shouldShow = when (config.showType) {
                MiningEventConfig.ShowType.DWARVEN -> islandType == IslandType.DWARVEN_MINES
                MiningEventConfig.ShowType.CRYSTAL -> islandType == IslandType.CRYSTAL_HOLLOWS
                MiningEventConfig.ShowType.MINESHAFT -> islandType == IslandType.MINESHAFT
                MiningEventConfig.ShowType.CURRENT -> islandType.isInIsland()
                else -> true
            }

            eventDetails.islandEvents.firstOrNull()?.let { firstEvent ->
                if (firstEvent.endsAt.asTimeMark().isInPast()) {
                    eventDetails.lastEvent = firstEvent.event
                }
            }

            if (shouldShow) {
                val upcomingEvents = formatUpcomingEvents(eventDetails.islandEvents, eventDetails.lastEvent)
                list.add("§a${islandType.displayName}§8: $upcomingEvents")
            }
        }
        display = list
    }

    private fun formatUpcomingEvents(events: List<RunningEventType>, lastEvent: MiningEventType?): String {
        val upcoming = events.filter { !it.endsAt.asTimeMark().isInPast() }
            .map { if (it.isDoubleEvent) "${it.event} §8-> ${it.event}" else it.event.toString() }.toMutableList()

        if (upcoming.isEmpty()) upcoming.add("§7???")
        if (config.passedEvents && upcoming.size < 4) lastEvent?.let { upcoming.add(0, it.toPastString()) }
        return upcoming.joinToString(" §8-> ")
    }

    fun updateData(eventData: MiningEventData) {
        eventData.runningEvents.forEach { (islandType, events) ->
            val sorted = events.filter { islandType == IslandType.DWARVEN_MINES || !it.event.dwarvenSpecific }
                .sortedBy { it.endsAt - it.event.defaultLength.inWholeMilliseconds }

            val oldData = islandEventData[islandType]
            if (oldData == null) {
                //todo remove once mineshaft is on main server
                if (sorted.isNotEmpty() || islandType != IslandType.MINESHAFT) {
                    islandEventData[islandType] = MiningIslandEventInfo(sorted)
                }
            } else {
                oldData.islandEvents = sorted
            }
        }
    }

    private fun shouldDisplay() =
        SkyBlockAPI.isConnected && config.enabled && !ReminderUtils.isBusy() && !(!config.outsideMining && !IslandTypeTag.ADVANCED_MINING.inAny())
}

private class MiningIslandEventInfo(var islandEvents: List<RunningEventType>, var lastEvent: MiningEventType? = null)

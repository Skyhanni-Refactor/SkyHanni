package at.hannibal2.skyhanni.features.mining.eventtracker

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.api.skyblock.IslandType
import at.hannibal2.skyhanni.api.skyblock.IslandTypeTag
import at.hannibal2.skyhanni.api.skyblock.SkyBlockAPI
import at.hannibal2.skyhanni.compat.soopy.data.MiningEventData
import at.hannibal2.skyhanni.compat.soopy.data.RunningEventType
import at.hannibal2.skyhanni.config.features.mining.MiningEventConfig
import at.hannibal2.skyhanni.data.item.SkyhanniItems
import at.hannibal2.skyhanni.events.render.gui.GuiOverlayRenderEvent
import at.hannibal2.skyhanni.events.utils.ConfigFixEvent
import at.hannibal2.skyhanni.events.utils.SecondPassedEvent
import at.hannibal2.skyhanni.features.mining.eventtracker.MiningEventType.Companion.CompressFormat
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ConfigUtils
import at.hannibal2.skyhanni.utils.NEUItems.getItemStack
import at.hannibal2.skyhanni.utils.RenderUtils.renderRenderables
import at.hannibal2.skyhanni.utils.SimpleTimeMark.Companion.asTimeMark
import at.hannibal2.skyhanni.utils.renderables.Renderable
import net.minecraft.init.Blocks
import net.minecraft.item.ItemStack

@SkyHanniModule
object MiningEventDisplay {
    private val config get() = SkyHanniMod.feature.mining.miningEvent
    private var display = listOf<Renderable>()

    private val islandEventData: MutableMap<IslandType, MiningIslandEventInfo> = mutableMapOf()

    @HandleEvent
    fun onSecondPassed(event: SecondPassedEvent) {
        updateDisplay()
    }

    @HandleEvent
    fun onRenderOverlay(event: GuiOverlayRenderEvent) {
        if (!shouldDisplay()) return
        config.position.renderRenderables(display, posLabel = "Upcoming Events Display")
    }

    private fun updateDisplay() {
        display = emptyList()
        updateEvents()
    }

    private fun updateEvents() {
        val list = mutableListOf<Renderable>()

        if (MiningEventTracker.apiError) {
            val count = MiningEventTracker.apiErrorCount
            list.add(Renderable.string("§cMining Event API Error! ($count)"))
            list.add(Renderable.string("§cSwap servers to try again!"))
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
                val island =
                    if (!config.islandAsIcon) Renderable.string("§a${islandType.displayName}§8:") else
                        Renderable.horizontalContainer(
                            listOf(
                                when (islandType) {
                                    IslandType.DWARVEN_MINES -> Renderable.itemStack(
                                        SkyhanniItems.MITHRIL_ORE().getItemStack()
                                    )

                                    IslandType.CRYSTAL_HOLLOWS -> Renderable.itemStack(
                                        SkyhanniItems.PERFECT_RUBY_GEM().getItemStack()
                                    )

                                    IslandType.MINESHAFT -> Renderable.itemStack(ItemStack(Blocks.packed_ice))
                                    else -> unknownDisplay
                                },
                                Renderable.string("§8:")
                            )
                        )
                list.add(
                    Renderable.horizontalContainer(
                        listOf(
                            island,
                            *upcomingEvents
                        ), 3
                    )
                )
            }
        }
        display = list
    }

    private val unknownDisplay = Renderable.string("§7???")
    private val transitionDisplay = Renderable.string("§8->")

    private fun formatUpcomingEvents(events: List<RunningEventType>, lastEvent: MiningEventType?): Array<Renderable> {
        val upcoming = events.filter { !it.endsAt.asTimeMark().isInPast() }
            .flatMap {
                if (it.isDoubleEvent) listOf(it.event, it.event) else listOf(it.event)
                /* if (it.isDoubleEvent) "${it.event} §8-> ${it.event}" else it.event.toString() */
            }.map { it.getRenderable() }.toMutableList()

        if (upcoming.isEmpty()) upcoming.add(unknownDisplay)
        if (config.passedEvents && upcoming.size < 4) lastEvent?.let { upcoming.add(0, it.getRenderableAsPast()) }
        return upcoming.flatMap { listOf(it, transitionDisplay) }.dropLast(1).toTypedArray()
        /* return upcoming.joinToString(" §8-> ") */
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
        SkyBlockAPI.isConnected && config.enabled && !(!config.outsideMining && !IslandTypeTag.ADVANCED_MINING.inAny())

    @HandleEvent
    fun onConfigFix(event: ConfigFixEvent) {
        event.transform(46, "mining.miningEvent.compressedFormat") {
            ConfigUtils.migrateBooleanToEnum(it, CompressFormat.COMPACT_TEXT, CompressFormat.DEFAULT)
        }
    }
}

private class MiningIslandEventInfo(var islandEvents: List<RunningEventType>, var lastEvent: MiningEventType? = null)

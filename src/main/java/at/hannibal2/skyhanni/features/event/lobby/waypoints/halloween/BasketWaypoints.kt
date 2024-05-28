package at.hannibal2.skyhanni.features.event.lobby.waypoints.halloween

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.HypixelAPI
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.api.skyblock.SkyBlockAPI
import at.hannibal2.skyhanni.data.ScoreboardData
import at.hannibal2.skyhanni.events.chat.SkyHanniChatEvent
import at.hannibal2.skyhanni.events.render.world.SkyHanniRenderWorldEvent
import at.hannibal2.skyhanni.events.utils.SecondPassedEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.test.GriffinUtils.drawWaypointFilled
import at.hannibal2.skyhanni.utils.LocationUtils.distanceSqToPlayer
import at.hannibal2.skyhanni.utils.LorenzColor
import at.hannibal2.skyhanni.utils.RenderUtils.drawDynamicText

@SkyHanniModule
object BasketWaypoints {

    private val config get() = SkyHanniMod.feature.event.lobbyWaypoints.halloweenBasket
    private var closest: Basket? = null
    private var isHalloween: Boolean = false

    @HandleEvent
    fun onChat(event: SkyHanniChatEvent) {
        if (!config.allWaypoints && !config.allEntranceWaypoints) return
        if (!isHalloween) return

        if (!isEnabled()) return

        val message = event.message
        if (message.startsWith("§a§lYou found a Candy Basket! §r") || message == "§cYou already found this Candy Basket!") {
            val basket = Basket.entries.minByOrNull { it.waypoint.distanceSqToPlayer() }!!
            basket.found = true
            if (closest == basket) {
                closest = null
            }
        }
    }

    @HandleEvent
    fun onSecondPassed(event: SecondPassedEvent) {
        if (!config.allWaypoints && !config.allEntranceWaypoints) return
        if (!isEnabled()) return

        isHalloween = checkScoreboardHalloweenSpecific()

        if (isHalloween) {
            if (config.onlyClosest) {
                if (closest == null) {
                    val notFoundBaskets = Basket.entries.filter { !it.found }
                    if (notFoundBaskets.isEmpty()) return
                    closest = notFoundBaskets.minByOrNull { it.waypoint.distanceSqToPlayer() }!!
                }
            }
        }
    }

    @HandleEvent
    fun onRenderWorld(event: SkyHanniRenderWorldEvent) {
        if (!isEnabled()) return
        if (!isHalloween) return

        if (config.allWaypoints) {
            for (basket in Basket.entries) {
                if (!basket.shouldShow()) continue
                event.drawWaypointFilled(basket.waypoint, LorenzColor.GOLD.toColor())
                event.drawDynamicText(basket.waypoint, "§6" + basket.basketName, 1.5)
            }
        }

        if (config.allEntranceWaypoints) {
            for (basketEntrance in BasketEntrances.entries) {
                if (!basketEntrance.basket.any { it.shouldShow() }) continue
                event.drawWaypointFilled(basketEntrance.waypoint, LorenzColor.YELLOW.toColor())
                event.drawDynamicText(basketEntrance.waypoint, "§e" + basketEntrance.basketEntranceName, 1.5)
            }
            return
        }
    }

    private fun Basket.shouldShow(): Boolean {
        if (found) {
            return false
        }

        return if (config.onlyClosest) closest == this else true
    }

    // TODO use regex with the help of knowing the original lore. Will most likely need to wait until next halloween event
    private fun checkScoreboardHalloweenSpecific(): Boolean {
        val a = ScoreboardData.sidebarLinesFormatted.any { it.contains("Hypixel Level") }
        val b = ScoreboardData.sidebarLinesFormatted.any { it.contains("Halloween") }
        val c = ScoreboardData.sidebarLinesFormatted.any { it.contains("Baskets") }
        return a && b && c
    }

    private fun isEnabled() = HypixelAPI.onHypixel && !SkyBlockAPI.isConnected
}

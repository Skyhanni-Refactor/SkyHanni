package at.hannibal2.skyhanni.features.garden.composter

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.enums.OutsideSbFeature
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.data.TitleManager
import at.hannibal2.skyhanni.data.item.SkyhanniItems
import at.hannibal2.skyhanni.events.minecraft.TabListUpdateEvent
import at.hannibal2.skyhanni.events.render.gui.GuiRenderEvent
import at.hannibal2.skyhanni.features.fame.ReminderUtils
import at.hannibal2.skyhanni.features.garden.GardenAPI
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.CollectionUtils.addAsSingletonList
import at.hannibal2.skyhanni.utils.HypixelCommands
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.LorenzUtils.isInIsland
import at.hannibal2.skyhanni.utils.NEUInternalName
import at.hannibal2.skyhanni.utils.NEUItems.getItemStack
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.RenderUtils.renderStringsAndItems
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.datetime.TimeUtils.format
import java.util.Collections
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

object ComposterDisplay {

    private val config get() = GardenAPI.config.composters
    private val storage get() = GardenAPI.storage
    private var display = emptyList<List<Any>>()
    private var composterEmptyTime: Duration? = null

    private val bucket by lazy { SkyhanniItems.BUCKET().getItemStack() }
    private var tabListData by ComposterAPI::tabListData

    @HandleEvent
    fun onTabListUpdate(event: TabListUpdateEvent) {
        if (!(config.displayEnabled && GardenAPI.inGarden())) return

        readData(event.tabList)

        if (tabListData.isNotEmpty()) {
            composterEmptyTime = ComposterAPI.estimateEmptyTimeFromTab()
            updateDisplay()
            sendNotify()
        }
    }

    private fun updateDisplay() {
        val newDisplay = mutableListOf<List<Any>>()
        newDisplay.addAsSingletonList("§bComposter")


        newDisplay.add(DataType.TIME_LEFT.addToList(tabListData))

        val list = mutableListOf<Any>()
        list.addAll(DataType.ORGANIC_MATTER.addToList(tabListData))
        list.add(" ")
        list.addAll(DataType.FUEL.addToList(tabListData))
        newDisplay.add(list)

        newDisplay.add(DataType.STORED_COMPOST.addToList(tabListData))
        newDisplay.add(addComposterEmptyTime(composterEmptyTime))

        display = newDisplay
    }

    private fun addComposterEmptyTime(emptyTime: Duration?): List<Any> {
        return if (emptyTime != null) {
            GardenAPI.storage?.composterEmptyTime = (SimpleTimeMark.now() + emptyTime).toMillis()
            val format = emptyTime.format()
            listOf(bucket, "§b$format")
        } else {
            listOf("§cOpen Composter Upgrades!")
        }
    }

    private fun readData(tabList: List<String>) {
        var next = false
        val newData = mutableMapOf<DataType, String>()

        for (line in tabList) {
            if (line == "§b§lComposter:") {
                next = true
                continue
            }
            if (next) {
                if (line == "") break
                for (type in DataType.entries) {
                    type.pattern.matchMatcher(line) {
                        newData[type] = group(1)
                    }
                }
            }
        }

        for (type in DataType.entries) {
            if (!newData.containsKey(type)) {
                tabListData = emptyMap()
                return
            }
        }

        tabListData = newData
    }

    private fun sendNotify() {
        if (!config.notifyLow.enabled) return
        if (ReminderUtils.isBusy()) return

        val storage = storage ?: return

        if (ComposterAPI.getOrganicMatter() <= config.notifyLow.organicMatter && System.currentTimeMillis() >= storage.informedAboutLowMatter) {
            if (config.notifyLow.title) {
                TitleManager.sendTitle("§cYour Organic Matter is low", 4.seconds)
            }
            ChatUtils.chat("§cYour Organic Matter is low!")
            storage.informedAboutLowMatter = System.currentTimeMillis() + 60_000 * 5
        }

        if (ComposterAPI.getFuel() <= config.notifyLow.fuel &&
            System.currentTimeMillis() >= storage.informedAboutLowFuel
        ) {
            if (config.notifyLow.title) {
                TitleManager.sendTitle("§cYour Fuel is low", 4.seconds)
            }
            ChatUtils.chat("§cYour Fuel is low!")
            storage.informedAboutLowFuel = System.currentTimeMillis() + 60_000 * 5
        }
    }

    @HandleEvent
    fun onRenderOverlay(event: GuiRenderEvent.GuiOverlayRenderEvent) {
        if (!LorenzUtils.inSkyBlock && !OutsideSbFeature.COMPOSTER_TIME.isSelected()) return

        if (GardenAPI.inGarden() && config.displayEnabled) {
            config.displayPos.renderStringsAndItems(display, posLabel = "Composter Display")
        }

        checkWarningsAndOutsideGarden()
    }

    private fun checkWarningsAndOutsideGarden() {
        val format = GardenAPI.storage?.let {
            val composterEmpty = SimpleTimeMark(it.composterEmptyTime)
            if (!composterEmpty.isFarPast()) {
                val timeUntilEmpty = composterEmpty.timeUntil()
                if (timeUntilEmpty.isPositive()) {
                    if (timeUntilEmpty < 20.minutes) {
                        warn("Your composter in the garden is almost empty!")
                    }
                    timeUntilEmpty.format(maxUnits = 3)
                } else {
                    warn("Your composter is empty!")
                    "§cComposter is empty!"
                }
            } else "?"
        } ?: "§cJoin SkyBlock to show composter timer."

        val inSb = LorenzUtils.inSkyBlock && config.displayOutsideGarden
        val outsideSb = !LorenzUtils.inSkyBlock && OutsideSbFeature.COMPOSTER_TIME.isSelected()
        if (!GardenAPI.inGarden() && (inSb || outsideSb)) {
            val list = Collections.singletonList(listOf(bucket, "§b$format"))
            config.outsideGardenPos.renderStringsAndItems(list, posLabel = "Composter Outside Garden Display")
        }
    }

    private fun warn(warningMessage: String) {
        if (!config.warnAlmostClose) return
        val storage = GardenAPI.storage ?: return

        if (ReminderUtils.isBusy()) return

        if (System.currentTimeMillis() < storage.lastComposterEmptyWarningTime + 1000 * 60 * 2) return
        storage.lastComposterEmptyWarningTime = System.currentTimeMillis()
        if (IslandType.GARDEN.isInIsland()) {
            ChatUtils.chat(warningMessage)
        } else {
            ChatUtils.clickableChat(warningMessage, onClick = {
                HypixelCommands.warp("garden")
            })
        }
        TitleManager.sendTitle("§eComposter Warning!", 3.seconds)
    }

    enum class DataType(rawPattern: String, val icon: NEUInternalName) {
        ORGANIC_MATTER(" Organic Matter: §r(.*)", SkyhanniItems.WHEAT()),
        FUEL(" Fuel: §r(.*)", SkyhanniItems.OIL_BARREL()),
        TIME_LEFT(" Time Left: §r(.*)", SkyhanniItems.WATCH()),
        STORED_COMPOST(" Stored Compost: §r(.*)", SkyhanniItems.COMPOST());

        val displayItem by lazy { icon.getItemStack() }

        val pattern by lazy { rawPattern.toPattern() }

        fun addToList(map: Map<DataType, String>): List<Any> {
            return listOf(displayItem, map[this]!!)
        }
    }
}

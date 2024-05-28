package at.hannibal2.skyhanni.features.fishing.tracker

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.api.skyblock.SkyBlockAPI
import at.hannibal2.skyhanni.events.fishing.FishingBobberCastEvent
import at.hannibal2.skyhanni.events.fishing.SeaCreatureFishEvent
import at.hannibal2.skyhanni.events.minecraft.ClientTickEvent
import at.hannibal2.skyhanni.events.render.gui.GuiRenderEvent
import at.hannibal2.skyhanni.events.utils.ConfigLoadEvent
import at.hannibal2.skyhanni.features.fishing.FishingAPI
import at.hannibal2.skyhanni.features.fishing.SeaCreatureManager
import at.hannibal2.skyhanni.features.nether.kuudra.KuudraAPI
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.test.command.ErrorManager
import at.hannibal2.skyhanni.utils.CollectionUtils.addAsSingletonList
import at.hannibal2.skyhanni.utils.CollectionUtils.addOrPut
import at.hannibal2.skyhanni.utils.CollectionUtils.sumAllValues
import at.hannibal2.skyhanni.utils.ConditionalUtils
import at.hannibal2.skyhanni.utils.ItemUtils.getInternalName
import at.hannibal2.skyhanni.utils.LorenzUtils.addButton
import at.hannibal2.skyhanni.utils.NumberUtil.addSeparators
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.StringUtils.allLettersFirstUppercase
import at.hannibal2.skyhanni.utils.StringUtils.formatPercentage
import at.hannibal2.skyhanni.utils.mc.McPlayer
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import at.hannibal2.skyhanni.utils.tracker.SkyHanniTracker
import at.hannibal2.skyhanni.utils.tracker.TrackerData
import com.google.gson.annotations.Expose
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object SeaCreatureTracker {

    private val config get() = SkyHanniMod.feature.fishing.seaCreatureTracker

    private val trophyArmorNames by RepoPattern.pattern(
        "fishing.trophyfishing.armor",
        "(BRONZE|SILVER|GOLD|DIAMOND)_HUNTER_(HELMET|CHESTPLATE|LEGGINGS|BOOTS)"
    )

    private val tracker = SkyHanniTracker("Sea Creature Tracker", { Data() }, { it.fishing.seaCreatureTracker })
    { drawDisplay(it) }
    private var lastArmorCheck = SimpleTimeMark.farPast()
    private var isTrophyFishing = false

    class Data : TrackerData() {

        override fun reset() {
            amount.clear()
        }

        @Expose
        var amount: MutableMap<String, Int> = mutableMapOf()
    }

    @HandleEvent
    fun onSeaCreatureFish(event: SeaCreatureFishEvent) {
        if (!isEnabled()) return

        tracker.modify {
            val amount = if (event.doubleHook && config.countDouble) 2 else 1
            it.amount.addOrPut(event.seaCreature.name, amount)
        }

        if (config.hideChat) {
            event.chatEvent.blockedReason = "sea_creature_tracker"
        }
    }

    private const val NAME_ALL: String = "All"
    private var currentCategory: String = NAME_ALL

    private fun getCurrentCategories(data: Data): Map<String, Int> {
        val map = mutableMapOf<String, Int>()
        map[NAME_ALL] = data.amount.size
        for ((category, names) in SeaCreatureManager.allVariants) {
            val amount = names.count { it in data.amount }
            if (amount > 0) {
                map[category] = amount
            }
        }

        return map
    }

    private fun drawDisplay(data: Data): List<List<Any>> = buildList {
        addAsSingletonList("§7Sea Creature Tracker:")

        val filter: (String) -> Boolean = addCategories(data)
        val realAmount = data.amount.filter { filter(it.key) }

        val total = realAmount.sumAllValues()
        realAmount.entries.sortedByDescending { it.value }.forEach { (name, amount) ->
            val displayName = SeaCreatureManager.allFishingMobs[name]?.displayName ?: run {
                ErrorManager.logErrorStateWithData(
                    "Sea Creature Tracker can not display a name correctly",
                    "Could not find sea creature by name",
                    "SeaCreatureManager.allFishingMobs.keys" to SeaCreatureManager.allFishingMobs.keys,
                    "name" to name
                )
                name
            }

            val percentageSuffix = if (config.showPercentage.get()) {
                val percentage = (amount.toDouble() / total).formatPercentage()
                " §7$percentage"
            } else ""

            addAsSingletonList(" §7- §e${amount.addSeparators()} $displayName$percentageSuffix")
        }
        addAsSingletonList(" §7- §e${total.addSeparators()} §7Total Sea Creatures")
    }

    private fun MutableList<List<Any>>.addCategories(data: Data): (String) -> Boolean {
        val amounts = getCurrentCategories(data)
        val list = amounts.keys.toList()
        if (currentCategory !in list) {
            currentCategory = NAME_ALL
        }

        if (tracker.isInventoryOpen()) {
            addButton(
                prefix = "§7Category: ",
                getName = currentCategory.allLettersFirstUppercase() + " §7(" + amounts[currentCategory] + ")",
                onChange = {
                    val id = list.indexOf(currentCategory)
                    currentCategory = list[(id + 1) % list.size]
                    tracker.update()
                }
            )
        }

        return if (currentCategory == NAME_ALL) {
            { true }
        } else filterCurrentCategory()
    }

    private fun filterCurrentCategory(): (String) -> Boolean {
        val items = SeaCreatureManager.allVariants[currentCategory] ?: run {
            ErrorManager.logErrorStateWithData(
                "Sea Creature Tracker can not find all sea creature variants",
                "Sea creature variant is not found",
                "SeaCreatureManager.allVariants.keys" to SeaCreatureManager.allVariants.keys,
                "currentCategory" to currentCategory,
            )
            return { true }
        }
        return { it in items }
    }

    @HandleEvent
    fun onConfigLoad(event: ConfigLoadEvent) {
        ConditionalUtils.onToggle(config.showPercentage) {
            tracker.update()
        }
    }

    @HandleEvent
    fun onBobberThrow(event: FishingBobberCastEvent) {
        tracker.firstUpdate()
    }

    @HandleEvent
    fun onRenderOverlay(event: GuiRenderEvent) {
        if (!isEnabled()) return
        if (!FishingAPI.isFishing(checkRodInHand = false)) return

        tracker.renderDisplay(config.position)
    }

    fun resetCommand() {
        tracker.resetCommand()
    }

    private fun isEnabled() = SkyBlockAPI.isConnected && config.enabled && !isTrophyFishing && !KuudraAPI.inKuudra

    private fun isWearingTrophyArmor(): Boolean = McPlayer.armor.all {
        trophyArmorNames.matches(it?.getInternalName()?.asString())
    }

    @HandleEvent
    fun onTick(event: ClientTickEvent) {
        if (lastArmorCheck.passedSince() < 3.seconds) return
        lastArmorCheck = SimpleTimeMark.now()
        isTrophyFishing = isWearingTrophyArmor()
    }
}

package at.hannibal2.skyhanni.features.slayer

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.data.ClickType
import at.hannibal2.skyhanni.data.SlayerAPI
import at.hannibal2.skyhanni.data.TitleManager
import at.hannibal2.skyhanni.data.item.SkyhanniItems
import at.hannibal2.skyhanni.events.entity.EntityHealthUpdateEvent
import at.hannibal2.skyhanni.events.minecraft.ScoreboardUpdateEvent
import at.hannibal2.skyhanni.events.minecraft.click.ItemClickEvent
import at.hannibal2.skyhanni.features.event.diana.DianaAPI
import at.hannibal2.skyhanni.features.rift.RiftAPI
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.CollectionUtils.nextAfter
import at.hannibal2.skyhanni.utils.DelayedRun
import at.hannibal2.skyhanni.utils.ItemUtils.getInternalNameOrNull
import at.hannibal2.skyhanni.utils.LocationUtils.distanceToPlayer
import at.hannibal2.skyhanni.api.skyblock.SkyBlockAPI
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.StringUtils.removeColor
import at.hannibal2.skyhanni.utils.getLorenzVec
import net.minecraft.entity.EntityLivingBase
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

object SlayerQuestWarning {

    private val config get() = SkyHanniMod.feature.slayer

    private var lastWeaponUse = SimpleTimeMark.farPast()
    private val voidItem = SkyhanniItems.ASPECT_OF_THE_VOID()
    private val endItem = SkyhanniItems.ASPECT_OF_THE_END()

    private val outsideRiftData = SlayerData()
    private val insideRiftData = SlayerData()

    class SlayerData {
        var currentSlayerState: String? = null
        var lastSlayerType: SlayerType? = null
    }

    @HandleEvent
    fun onScoreboardUpdate(event: ScoreboardUpdateEvent) {
        val slayerType = event.scoreboard.nextAfter("Slayer Quest")
        val slayerProgress = event.scoreboard.nextAfter("Slayer Quest", skip = 2) ?: "no slayer"
        val new = slayerProgress.removeColor()
        val slayerData = getSlayerData()

        if (slayerData.currentSlayerState == new) return

        slayerData.currentSlayerState?.let {
            change(it, new)
        }
        slayerData.currentSlayerState = new
        slayerType?.let {
            slayerData.lastSlayerType = SlayerType.getByName(it)
        }
    }

    private fun getSlayerData() = if (RiftAPI.inRift()) outsideRiftData else insideRiftData

    private fun change(old: String, new: String) {
        if (new.contains("Combat")) {
            if (!old.contains("Combat")) {
                needSlayerQuest = false
            }
        }
        if (new == "no slayer") {
            if (old == "Slay the boss!") {
                needNewQuest("The old slayer quest has failed!")
            }
        }
        if (new == "Boss slain!") {
            DelayedRun.runDelayed(2.seconds) {
                if (getSlayerData().currentSlayerState == "Boss slain!") {
                    needNewQuest("You have no Auto-Slayer active!")
                }
            }
        }
    }

    private var needSlayerQuest = false
    private var lastWarning = SimpleTimeMark.farPast()
    private var currentReason = ""

    private fun needNewQuest(reason: String) {
        currentReason = reason
        needSlayerQuest = true
    }

    private fun tryWarn() {
        if (!needSlayerQuest) return
        warn("New Slayer Quest!", "Start a new slayer quest! $currentReason")
    }

    private fun warn(titleMessage: String, chatMessage: String) {
        if (!config.questWarning) return
        if (lastWarning.passedSince() < 10.seconds) return

        if (DianaAPI.isDoingDiana()) return
        // prevent warnings when mobs are hit by other players
        if (lastWeaponUse.passedSince() > 500.milliseconds) return

        lastWarning = SimpleTimeMark.now()
        ChatUtils.chat(chatMessage)

        if (config.questWarningTitle) {
            TitleManager.sendTitle("§e$titleMessage", 2.seconds)
        }
    }

    @HandleEvent(onlyOnSkyblock = true)
    fun onEntityHealthUpdate(event: EntityHealthUpdateEvent) {
        val entity = event.entity
        if (entity.getLorenzVec().distanceToPlayer() < 6 && isSlayerMob(entity)) {
            tryWarn()
        }
    }

    private fun isSlayerMob(entity: EntityLivingBase): Boolean {
        val slayerType = SlayerAPI.getSlayerTypeForCurrentArea() ?: return false

        val activeSlayer = SlayerAPI.getActiveSlayer()

        if (activeSlayer != null) {
            if (slayerType != activeSlayer) {
                val activeSlayerName = activeSlayer.displayName
                val slayerName = slayerType.displayName
                SlayerAPI.latestWrongAreaWarning = SimpleTimeMark.now()
                warn(
                    "Wrong Slayer!",
                    "Wrong slayer selected! You have $activeSlayerName selected and you are in an $slayerName area!"
                )
            }
        }
        return (getSlayerData().lastSlayerType == slayerType) && slayerType.clazz.isInstance(entity)
    }

    @HandleEvent
    fun onItemClick(event: ItemClickEvent) {
        val internalName = event.itemInHand?.getInternalNameOrNull()

        if (event.clickType == ClickType.RIGHT_CLICK) {
            if (internalName == voidItem || internalName == endItem) {
                // ignore harmless teleportation
                return
            }
            if (internalName == null) {
                // ignore harmless right click
                return
            }
        }
        lastWeaponUse = SimpleTimeMark.now()
    }
}

package at.hannibal2.skyhanni.features.garden.farming

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.compat.elitebot.EliteBotAPI
import at.hannibal2.skyhanni.compat.elitebot.data.EliteBotLeaderboardRank
import at.hannibal2.skyhanni.config.enums.OutsideSbFeature
import at.hannibal2.skyhanni.data.HypixelData
import at.hannibal2.skyhanni.data.ProfileStorageData
import at.hannibal2.skyhanni.events.garden.farming.GardenToolChangeEvent
import at.hannibal2.skyhanni.events.minecraft.ClientTickEvent
import at.hannibal2.skyhanni.events.minecraft.WorldChangeEvent
import at.hannibal2.skyhanni.events.render.gui.GuiRenderEvent
import at.hannibal2.skyhanni.events.utils.ConfigFixEvent
import at.hannibal2.skyhanni.events.utils.ProfileJoinEvent
import at.hannibal2.skyhanni.features.garden.CropType
import at.hannibal2.skyhanni.features.garden.GardenAPI
import at.hannibal2.skyhanni.features.garden.farming.GardenCropSpeed.getSpeed
import at.hannibal2.skyhanni.test.command.ErrorManager
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.NumberUtil.addSeparators
import at.hannibal2.skyhanni.utils.NumberUtil.roundTo
import at.hannibal2.skyhanni.utils.RenderUtils.renderRenderables
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.StringUtils
import at.hannibal2.skyhanni.utils.StringUtils.toDashlessUUID
import at.hannibal2.skyhanni.utils.datetime.TimeUtils.format
import at.hannibal2.skyhanni.utils.mc.McPlayer
import at.hannibal2.skyhanni.utils.renderables.Renderable
import at.hannibal2.skyhanni.utils.system.OS
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

object FarmingWeightDisplay {

    @HandleEvent
    fun onRenderOverlay(event: GuiRenderEvent) {
        if (GardenAPI.hideExtraGuis()) return
        val shouldShow = apiError || (config.ignoreLow || weight >= 200)
        if (isEnabled() && shouldShow) {
            config.pos.renderRenderables(display, posLabel = "Farming Weight Display")
        }
    }

    @HandleEvent
    fun onGardenToolChange(event: GardenToolChangeEvent) {
        // Reset speed
        weightPerSecond = -1.0
    }

    @HandleEvent
    fun onWorldChange(event: WorldChangeEvent) {
        // We want to try to connect to the api again after a world switch.
        resetData()
    }

    @HandleEvent
    fun onProfileJoin(event: ProfileJoinEvent) {
        display = emptyList()
        profileId = ""
        weight = -1.0

        nextPlayers.clear()
        rankGoal = -1
    }

    @HandleEvent
    fun onTick(event: ClientTickEvent) {
        if (!isEnabled()) return
        if (!event.isMod(5)) return
        update()

        SkyHanniMod.coroutineScope.launch {
            getCropWeights()
        }
    }


    private val config get() = GardenAPI.config.eliteFarmingWeights
    private val localCounter = mutableMapOf<CropType, Long>()

    private var display = emptyList<Renderable>()
    private var profileId = ""
    private var lastLeaderboardUpdate = SimpleTimeMark.farPast()
    private var apiError = false
    private var leaderboardPosition = -1
    private var weight = -1.0
    private var localWeight = 0.0
    private var weightPerSecond = -1.0
    private var weightNeedsRecalculating = false
    private var isLoadingWeight = false
    private var isLoadingLeaderboard = false
    private var rankGoal = -1

    private var nextPlayers = mutableListOf<EliteBotLeaderboardRank.UpcomingPlayer>()
    private val nextPlayer get() = nextPlayers.firstOrNull()

    private val recalculate by lazy {
        ({
            resetData()
        })
    }

    private val errorMessage by lazy {
        listOf(
            Renderable.clickAndHover(
                "§cFarming Weight error: Cannot load",
                listOf("§eClick here to reload the data right now!"),
                onClick = recalculate
            ), Renderable.clickAndHover(
                "§cdata from Elite Farmers!",
                listOf("§eClick here to reload the data right now!"),
                onClick = recalculate
            ), Renderable.clickAndHover(
                "§eRejoin the garden or",
                listOf("§eClick here to reload the data right now!"),
                onClick = recalculate
            ), Renderable.clickAndHover(
                "§eclick here to fix it.",
                listOf("§eClick here to reload the data right now!"),
                onClick = recalculate
            )
        )
    }

    private var lastOpenWebsite = SimpleTimeMark.farPast()

    private fun update() {
        if (!isEnabled()) return
        if (apiError) {
            display = errorMessage
            return
        }

        if (weight == -1.0) {
            if (!isLoadingWeight) {
                val localProfile = HypixelData.profileName

                isLoadingWeight = true
                if (display.isEmpty()) {
                    display = Renderable.singeltonString("§6Farming Weight§7: §eLoading..")
                }
                SkyHanniMod.coroutineScope.launch {
                    loadWeight(localProfile)
                    isLoadingWeight = false
                }
            }
            return
        }

        val weight = getWeight()

        if (rankGoal == -1) rankGoal = getRankGoal()
        val leaderboard = getLeaderboard()

        val list = mutableListOf<Renderable>()
        list.add(
            Renderable.clickAndHover(
                "§6Farming Weight§7: $weight$leaderboard",
                listOf("§eClick to open your Farming Profile."),
                onClick = { openWebsite(McPlayer.name) }
            )
        )

        if (isEtaEnabled() && (weightPerSecond != -1.0 || config.overtakeETAAlways)) {
            getETA()?.let {
                list.add(it)
            }
        }
        display = list
    }

    private fun getLeaderboard(): String {
        if (!config.leaderboard) return ""

        // Fetching new leaderboard position every 10.5 minutes
        if (lastLeaderboardUpdate.passedSince() > 10.5.minutes) {
            loadLeaderboardIfAble()
        }

        return if (leaderboardPosition != -1) {
            val format = leaderboardPosition.addSeparators()
            " §7[§b#$format§7]"
        } else {
            if (isLoadingLeaderboard) " §7[§b#?§7]" else ""
        }
    }

    private fun getWeight(): String {
        if (weightNeedsRecalculating) {
            val values = calculateCollectionWeight().values
            if (values.isNotEmpty()) {
                localWeight = values.sum()
                weightNeedsRecalculating = false
            }
        }

        val totalWeight = (localWeight + weight)
        return "§e" + totalWeight.roundTo(2).addSeparators()
    }

    private fun getRankGoal(): Int {
        val value = config.etaGoalRank
        var goal = 10000

        // Check that the provided string is valid
        val parsed = value.toIntOrNull() ?: 0
        if (parsed < 1 || parsed > goal) {
            ChatUtils.chatAndOpenConfig(
                "Invalid Farming Weight Overtake Goal! Click here to edit the Overtake Goal config value " +
                    "to a valid number [1-10000] to use this feature!",
                GardenAPI.config.eliteFarmingWeights::etaGoalRank
            )
            config.etaGoalRank = goal.toString()
        } else {
            goal = parsed
        }

        // Fetch the positions again if the goal was changed
        if (rankGoal != goal) {
            loadLeaderboardIfAble()
        }

        return goal
    }

    private fun getETA(): Renderable? {
        if (weight < 0) return null

        val nextPlayer = nextPlayer ?: return Renderable.clickAndHover(
            "§cWaiting for leaderboard update...",
            listOf("§eClick here to load new data right now!"),
            onClick = recalculate
        )
        val showRankGoal = leaderboardPosition == -1 || leaderboardPosition > rankGoal
        var nextName =
            if (showRankGoal) "#$rankGoal" else nextPlayer.ign

        val totalWeight = (localWeight + weight)
        var weightUntilOvertake = nextPlayer.amount - totalWeight

        if (weightUntilOvertake < 0) {
            if (weightPerSecond > 0) {
                farmingChatMessage("You passed §b$nextName §ein the Farming Weight Leaderboard!")
            }

            // Lower leaderboard position
            if (leaderboardPosition == -1) {
                leaderboardPosition = 10000
            } else {
                leaderboardPosition--
            }
            GardenAPI.storage?.farmingWeight?.lastFarmingWeightLeaderboard =
                leaderboardPosition

            // Remove passed player to present the next one
            nextPlayers.removeFirst()

            // Display waiting message if nextPlayers list is empty
            // Update values to next player
            nextName = nextPlayer.ign
            weightUntilOvertake = nextPlayer.amount - totalWeight
        }

        if (nextPlayer.amount == 0.0) {
            return Renderable.clickAndHover(
                "§cRejoin the garden to show ETA!",
                listOf("Click here to calculate the data right now!"),
                onClick = recalculate
            )
        }

        val timeFormat = if (weightPerSecond != -1.0) {
            val timeTillOvertake = (weightUntilOvertake / weightPerSecond).minutes
            val format = timeTillOvertake.format()
            " §7(§b$format§7)"
        } else ""

        val weightFormat = weightUntilOvertake.roundTo(2).addSeparators()
        val text = "§e$weightFormat$timeFormat §7behind §b$nextName"
        return if (showRankGoal) {
            Renderable.string(text)
        } else {
            Renderable.clickAndHover(
                text,
                listOf("§eClick to open the Farming Profile of §b$nextName."),
                onClick = { openWebsite(nextName) }
            )
        }
    }

    private fun resetData() {
        apiError = false
        // We ask both api endpoints after every world switch
        weight = -1.0
        weightPerSecond = -1.0

        leaderboardPosition = -1
        weightNeedsRecalculating = true
        lastLeaderboardUpdate = SimpleTimeMark.farPast()

        nextPlayers.clear()
        rankGoal = -1

        localCounter.clear()
    }

    private fun farmingChatMessage(message: String) {
        ChatUtils.hoverableChat(
            message,
            listOf(
                "§eClick to open your Farming Weight",
                "§eprofile on §celitebot.dev",
            ),
            "shfarmingprofile ${McPlayer.name}"
        )
    }

    private fun isEnabled() = ((OutsideSbFeature.FARMING_WEIGHT.isSelected() && !LorenzUtils.inSkyBlock) ||
        (LorenzUtils.inSkyBlock && (GardenAPI.inGarden() || config.showOutsideGarden))) && config.display

    private fun isEtaEnabled() = config.overtakeETA

    fun addCrop(crop: CropType, addedCounter: Int) {
        val before = getExactWeight()
        localCounter[crop] = crop.getLocalCounter() + addedCounter
        val after = getExactWeight()

        updateWeightPerSecond(crop, before, after, addedCounter)

        weightNeedsRecalculating = true
    }

    private fun updateWeightPerSecond(crop: CropType, before: Double, after: Double, diff: Int) {
        val speed = crop.getSpeed() ?: return
        val weightDiff = (after - before) * 1000
        weightPerSecond = weightDiff / diff * speed / 1000
    }

    private fun getExactWeight(): Double {
        val values = calculateCollectionWeight().values
        return if (values.isNotEmpty()) {
            values.sum()
        } else 0.0
    }

    private fun loadLeaderboardIfAble() {
        if (isLoadingLeaderboard) return
        isLoadingLeaderboard = true

        SkyHanniMod.coroutineScope.launch {
            val wasNotLoaded = leaderboardPosition == -1
            leaderboardPosition = loadLeaderboardPosition()
            if (wasNotLoaded && config.showLbChange) {
                checkOffScreenLeaderboardChanges()
            }
            GardenAPI.storage?.farmingWeight?.lastFarmingWeightLeaderboard =
                leaderboardPosition
            lastLeaderboardUpdate = SimpleTimeMark.now()
            isLoadingLeaderboard = false
        }
    }

    private fun checkOffScreenLeaderboardChanges() {
        val profileSpecific = ProfileStorageData.profileSpecific ?: return
        val oldPosition = profileSpecific.garden.farmingWeight.lastFarmingWeightLeaderboard

        if (oldPosition <= 0) return
        if (leaderboardPosition <= 0) return

        val diff = leaderboardPosition - oldPosition
        if (diff == 0) return

        if (diff > 0) {
            showLbChange("§cdropped ${StringUtils.pluralize(diff, "place", withNumber = true)}", oldPosition)
        } else {
            showLbChange("§arisen ${StringUtils.pluralize(-diff, "place", withNumber = true)}", oldPosition)
        }
    }

    private fun showLbChange(direction: String, oldPosition: Int) {
        farmingChatMessage(
            "§7Since your last visit to the §aGarden§7, " +
                "you have $direction §7on the §dFarming Leaderboard§7. " +
                "§7(§e#${oldPosition.addSeparators()} §7-> §e#${leaderboardPosition.addSeparators()}§7)"
        )
    }

    private suspend fun loadLeaderboardPosition(): Int {
        val includeUpcoming = isEtaEnabled()
        val goalRank = getRankGoal() + 1 // API returns upcoming players as if you were at this rank already

        return EliteBotAPI.getLeaderboard(
            "farmingweight",
            McPlayer.uuid.toDashlessUUID(),
            profileId,
            includeUpcoming = includeUpcoming,
            atRank = if (includeUpcoming && goalRank != 10001) goalRank else -1
        ).fold(
            onSuccess = {
                if (includeUpcoming) {
                    nextPlayers.clear()
                    it.upcomingPlayers.forEach { nextPlayers.add(it) }
                }
                return@fold it.rank
            },
            onFailure = {
                ErrorManager.logErrorWithData(
                    it, "Error getting weight leaderboard position",
                    "error" to it
                )
                return@fold -1
            }
        )
    }

    private suspend fun loadWeight(localProfile: String) {
        if (localProfile == "") {
            return ErrorManager.logErrorStateWithData(
                "User has no local profile",
                "User has no local profile",
            )
        }
        EliteBotAPI.getPlayerWeights(McPlayer.uuid.toDashlessUUID()).fold(
            { data ->
                val entry = data.profiles.find { it.profileId == data.selectedProfileId }
                    ?.takeIf { it.profileName.equals(localProfile, ignoreCase = true) }
                    ?: data.profiles.find { it.profileName.lowercase() == localProfile }

                if (entry != null) {
                    profileId = entry.profileId
                    weight = entry.totalWeight

                    localCounter.clear()
                    weightNeedsRecalculating = true
                    return
                }

                apiError = true
                ErrorManager.logErrorWithData(
                    IllegalStateException("Error loading user farming weight"),
                    "Error loading user farming weight\n" +
                        "§eLoading the farming weight data from elitebot.dev failed!\n" +
                        "§eYou can re-enter the garden to try to fix the problem.\n" +
                        "§cIf this message repeats, please report it on Discord!\n",
                    "apiResponse" to data,
                    "localProfile" to localProfile
                )
            },
            {
                apiError = true
                ErrorManager.logErrorWithData(
                    it,
                    "Error loading user farming weight\n" +
                        "§eLoading the farming weight data from elitebot.dev failed!\n" +
                        "§eYou can re-enter the garden to try to fix the problem.\n" +
                        "§cIf this message repeats, please report it on Discord!\n",
                    "error" to it,
                    "localProfile" to localProfile
                )
            }
        )
    }

    private fun calculateCollectionWeight(): MutableMap<CropType, Double> {
        val weightPerCrop = mutableMapOf<CropType, Double>()
        var totalWeight = 0.0
        for (crop in CropType.entries) {
            val weight = crop.getLocalCounter() / crop.getFactor()
            weightPerCrop[crop] = weight
            totalWeight += weight
        }
        if (totalWeight > 0) {
            weightPerCrop[CropType.MUSHROOM] = specialMushroomWeight(weightPerCrop, totalWeight)
        }
        return weightPerCrop
    }

    private fun specialMushroomWeight(weightPerCrop: MutableMap<CropType, Double>, totalWeight: Double): Double {
        val cactusWeight = weightPerCrop[CropType.CACTUS]!!
        val sugarCaneWeight = weightPerCrop[CropType.SUGAR_CANE]!!
        val doubleBreakRatio = (cactusWeight + sugarCaneWeight) / totalWeight
        val normalRatio = (totalWeight - cactusWeight - sugarCaneWeight) / totalWeight

        val mushroomFactor = CropType.MUSHROOM.getFactor()
        val mushroomCollection = CropType.MUSHROOM.getLocalCounter()
        return doubleBreakRatio * (mushroomCollection / (2 * mushroomFactor)) + normalRatio * (mushroomCollection / mushroomFactor)
    }

    private fun CropType.getLocalCounter() = localCounter[this] ?: 0L

    private fun CropType.getFactor(): Double {
        return cropWeight[this] ?: backupCropWeights[this] ?: error("Crop $this not in backupFactors!")
    }

    fun lookUpCommand(it: Array<String>) {
        val name = if (it.size == 1) it[0] else McPlayer.name
        openWebsite(name, ignoreCooldown = true)
    }

    private var lastName = ""

    private fun openWebsite(name: String, ignoreCooldown: Boolean = false) {
        if (!ignoreCooldown && lastOpenWebsite.passedSince() < 5.seconds && name == lastName) return
        lastOpenWebsite = SimpleTimeMark.now()
        lastName = name

        OS.openUrl("https://elitebot.dev/@$name/")
        ChatUtils.chat("Opening Farming Profile of player §b$name")
    }

    private val cropWeight = mutableMapOf<CropType, Double>()
    private var attemptingCropWeightFetch = false
    private var hasFetchedCropWeights = false

    private suspend fun getCropWeights() {
        if (attemptingCropWeightFetch || hasFetchedCropWeights) return
        attemptingCropWeightFetch = true

        EliteBotAPI.getWeights().fold(
            onSuccess = {
                for ((crop, weight) in it.crops) {
                    cropWeight[crop] = weight
                }
                hasFetchedCropWeights = true
            },
            onFailure = {
                ErrorManager.logErrorWithData(
                    it, "Error getting crop weights from elitebot.dev",
                    "error" to it
                )
            }
        )
    }

    // still needed when first joining garden and if they cant make https requests
    private val backupCropWeights by lazy {
        mapOf(
            CropType.WHEAT to 100_000.0,
            CropType.CARROT to 302_061.86,
            CropType.POTATO to 300_000.0,
            CropType.SUGAR_CANE to 200_000.0,
            CropType.NETHER_WART to 250_000.0,
            CropType.PUMPKIN to 98_284.71,
            CropType.MELON to 485_308.47,
            CropType.MUSHROOM to 90_178.06,
            CropType.COCOA_BEANS to 267_174.04,
            CropType.CACTUS to 177_254.45,
        )
    }

    @HandleEvent
    fun onConfigFix(event: ConfigFixEvent) {
        event.move(34, "garden.eliteFarmingWeights.ETAGoalRank", "garden.eliteFarmingWeights.etaGoalRank")
    }
}

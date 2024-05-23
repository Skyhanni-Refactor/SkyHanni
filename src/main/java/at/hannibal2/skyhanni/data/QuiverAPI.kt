package at.hannibal2.skyhanni.data

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.api.skyblock.SkyBlockAPI
import at.hannibal2.skyhanni.data.item.SkyhanniItems
import at.hannibal2.skyhanni.data.jsonobjects.repo.ArrowTypeJson
import at.hannibal2.skyhanni.events.chat.SkyHanniChatEvent
import at.hannibal2.skyhanni.events.inventory.InventoryFullyOpenedEvent
import at.hannibal2.skyhanni.events.inventory.OwnInventoryItemUpdateEvent
import at.hannibal2.skyhanni.events.skyblock.QuiverUpdateEvent
import at.hannibal2.skyhanni.events.utils.RepositoryReloadEvent
import at.hannibal2.skyhanni.events.utils.SecondPassedEvent
import at.hannibal2.skyhanni.test.command.ErrorManager
import at.hannibal2.skyhanni.utils.ItemCategory
import at.hannibal2.skyhanni.utils.ItemUtils.getInternalName
import at.hannibal2.skyhanni.utils.ItemUtils.getInternalNameOrNull
import at.hannibal2.skyhanni.utils.ItemUtils.getItemCategoryOrNull
import at.hannibal2.skyhanni.utils.ItemUtils.getLore
import at.hannibal2.skyhanni.utils.NEUInternalName
import at.hannibal2.skyhanni.utils.NEUInternalName.Companion.asInternalName
import at.hannibal2.skyhanni.utils.NumberUtil.formatInt
import at.hannibal2.skyhanni.utils.NumberUtil.roundTo
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.SkyBlockItemModifierUtils.getExtraAttributes
import at.hannibal2.skyhanni.utils.StringUtils.removeResets
import at.hannibal2.skyhanni.utils.StringUtils.trimWhiteSpace
import at.hannibal2.skyhanni.utils.mc.McPlayer
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraft.item.ItemBow

private var infinityQuiverLevelMultiplier = 0.03f

object QuiverAPI {
    private val storage get() = ProfileStorageData.profileSpecific
    var currentArrow: ArrowType?
        get() = storage?.arrows?.currentArrow?.asInternalName()?.let { getArrowByNameOrNull(it) } ?: NONE_ARROW_TYPE
        set(value) {
            storage?.arrows?.currentArrow = value?.toString() ?: return
        }
    var arrowAmount: MutableMap<NEUInternalName, Int>
        get() = storage?.arrows?.arrowAmount ?: mutableMapOf()
        set(value) {
            storage?.arrows?.arrowAmount = value
        }
    var currentAmount: Int
        get() = currentArrow?.amount ?: 0
        set(value) {
            currentArrow?.amount = value
        }
    var ArrowType.amount: Int
        get() = arrowAmount[this.internalName] ?: 0
        set(value) {
            arrowAmount[this.internalName] = value
        }

    private var arrows: List<ArrowType> = listOf()

    var wearingSkeletonMasterChestplate = false
        private set
    private var hasBow = false

    const val MAX_ARROW_AMOUNT = 2880

    var NONE_ARROW_TYPE: ArrowType? = null
    private var FLINT_ARROW_TYPE: ArrowType? = null

    private val group = RepoPattern.group("data.quiver")
    private val chatGroup = group.group("chat")
    private val selectPattern by chatGroup.pattern("select", "§aYou set your selected arrow type to §.(?<arrow>.*)§a!")
    private val fillUpJaxPattern by chatGroup.pattern(
        "fillupjax",
        "(?:§.)*Jax forged (?:§.)*(?<type>.*?)(?:§.)* x(?<amount>[\\d,]+)(?: (?:§.)*for (?:§.)*(?<coins>[\\d,]+) Coins)?(?:§.)*!"
    )
    private val fillUpPattern by chatGroup.pattern(
        "fillup",
        "§aYou filled your quiver with §f(?<flintAmount>.*) §aextra arrows!"
    )
    private val clearedPattern by chatGroup.pattern(
        "cleared",
        "§aCleared your quiver!|§c§lYour quiver is now completely empty!"
    )
    private val arrowRanOutPattern by chatGroup.pattern(
        "ranout",
        "§c§lQUIVER! §cYou have run out of §f(?<type>.*)s§c!"
    )
    private val arrowResetPattern by chatGroup.pattern("arrowreset", "§cYour favorite arrow has been reset!")
    private val addedToQuiverPattern by chatGroup.pattern(
        "addedtoquiver",
        "(?:§.)*You've added (?:§.)*(?<type>.*) x(?<amount>.*) (?:§.)*to your quiver!"
    )

    // Bows that don't use the players arrows, checked using the SkyBlock ID
    private val fakeBowsPattern by group.pattern("fakebows", "^(BOSS_SPIRIT_BOW|CRYPT_BOW)$")
    private val quiverInventoryNamePattern by group.pattern("quivername", "^Quiver$")
    private val quiverInventoryPattern by group.pattern(
        "quiver.inventory",
        "§7Active Arrow: §.(?<type>.*) §7\\(§e(?<amount>.*)§7\\)"
    )

    @HandleEvent
    fun onChat(event: SkyHanniChatEvent) {
        if (!isEnabled()) return
        val message = event.message.trimWhiteSpace().removeResets()

        selectPattern.matchMatcher(message) {
            val type = group("arrow")
            currentArrow = getArrowByNameOrNull(type)
                ?: return ErrorManager.logErrorWithData(
                    UnknownArrowType("Unknown arrow type: $type"),
                    "Unknown arrow type: $type",
                    "message" to message,
                )
            postUpdateEvent()
            return
        }

        arrowRanOutPattern.matchMatcher(message) {
            val type = group("type")
            val ranOutType = getArrowByNameOrNull(type)
                ?: return ErrorManager.logErrorWithData(
                    UnknownArrowType("Unknown arrow type: $type"),
                    "Unknown arrow type: $type",
                    "message" to message,
                )
            ranOutType.amount = 0
            postUpdateEvent(ranOutType)
        }

        fillUpJaxPattern.matchMatcher(message) {
            val type = group("type")
            val amount = group("amount").formatInt()
            val filledUpType = getArrowByNameOrNull(type)
                ?: return ErrorManager.logErrorWithData(
                    UnknownArrowType("Unknown arrow type: $type"),
                    "Unknown arrow type: $type",
                    "message" to message,
                )

            filledUpType.amount += amount
            if (filledUpType == currentArrow) {
                postUpdateEvent()
            }
            return
        }

        fillUpPattern.matchMatcher(message) {
            val flintAmount = group("flintAmount").formatInt()

            FLINT_ARROW_TYPE?.let { it.amount += flintAmount }
            if (currentArrow == FLINT_ARROW_TYPE) {
                postUpdateEvent()
            }
            return
        }

        addedToQuiverPattern.matchMatcher(message) {
            val type = group("type")
            val amount = group("amount").formatInt()
            val filledUpType = getArrowByNameOrNull(type)
                ?: return ErrorManager.logErrorWithData(
                    UnknownArrowType("Unknown arrow type: $type"),
                    "Unknown arrow type: $type",
                    "message" to message,
                )

            filledUpType.amount += amount
            if (filledUpType == currentArrow) {
                postUpdateEvent()
            }
            return
        }

        clearedPattern.matchMatcher(message) {
            currentAmount = 0
            arrowAmount.clear()

            postUpdateEvent()
            return
        }

        arrowResetPattern.matchMatcher(message) {
            currentArrow = NONE_ARROW_TYPE
            currentAmount = 0

            postUpdateEvent()
            return
        }
    }

    @HandleEvent
    fun onInventoryFullyLoaded(event: InventoryFullyOpenedEvent) {
        if (!isEnabled()) return
        if (!quiverInventoryNamePattern.matches(event.inventoryName)) return

        // clear to prevent duplicates
        currentAmount = 0
        arrowAmount.clear()

        val stacks = event.inventoryItems
        for (stack in stacks.values) {
            if (stack.getItemCategoryOrNull() != ItemCategory.ARROW) continue
            val arrow = stack.getInternalNameOrNull() ?: continue
            val arrowType = getArrowByNameOrNull(arrow) ?: continue
            arrowType.amount += stack.stackSize
        }
    }

    @HandleEvent
    fun onInventoryUpdate(event: OwnInventoryItemUpdateEvent) {
        if (!isEnabled() && event.slot != 44) return
        val stack = event.itemStack
        if (stack.getExtraAttributes()?.hasKey("quiver_arrow") == true) {
            for (line in stack.getLore()) {
                quiverInventoryPattern.matchMatcher(line) {
                    val type = group("type")
                    val amount = group("amount").formatInt()
                    val currentArrowType = getArrowByNameOrNull(type)
                        ?: return ErrorManager.logErrorWithData(
                            UnknownArrowType("Unknown arrow type: $type"),
                            "Unknown arrow type: $type",
                            "line" to line,
                        )
                    if (currentArrowType != currentArrow || amount != currentAmount) {
                        currentArrow = currentArrowType
                        currentAmount = amount
                        postUpdateEvent()
                    }
                }
            }
        }
    }

    fun asArrowPercentage(value: Int) = ((value.toFloat() / MAX_ARROW_AMOUNT) * 100).roundTo(1)

    fun hasBowInInventory() = hasBow

    fun isHoldingBow(): Boolean {
        McPlayer.heldItem?.let {
            return it.item is ItemBow && !fakeBowsPattern.matches(it.getInternalName().asString())
        } ?: return false
    }

    fun getArrowByNameOrNull(name: String): ArrowType? {
        return arrows.firstOrNull { it.arrow == name }
    }

    fun getArrowByNameOrNull(internalName: NEUInternalName): ArrowType? {
        return arrows.firstOrNull { it.internalName == internalName }
    }

    fun isEnabled() = SkyBlockAPI.isConnected && storage != null

    private fun checkBowInventory() {
        hasBow = McPlayer.inventory.any {
            it.item is ItemBow && !fakeBowsPattern.matches(it.getInternalName().asString())
        }
    }

    private fun checkChestplate() {
        val wasWearing = wearingSkeletonMasterChestplate
        wearingSkeletonMasterChestplate =
            McPlayer.chestplate?.getInternalName() == SkyhanniItems.SKELETON_MASTER_CHESTPLATE()
        if (wasWearing != wearingSkeletonMasterChestplate) {
            postUpdateEvent()
        }
    }

    private fun postUpdateEvent(arrowType: ArrowType? = currentArrow) {
        QuiverUpdateEvent(arrowType, currentAmount).post()
    }

    @HandleEvent
    fun onSecondPassed(event: SecondPassedEvent) {
        if (!isEnabled()) return
        if (event.repeatSeconds(2)) {
            checkChestplate()
            checkBowInventory()
        }
    }

    @HandleEvent
    fun onRepoReload(event: RepositoryReloadEvent) {
        val arrowData = event.getConstant<ArrowTypeJson>("ArrowTypes")
        arrows = arrowData.arrows.map { ArrowType(it.value.arrow, it.key.asInternalName()) }

        NONE_ARROW_TYPE = getArrowByNameOrNull(SkyhanniItems.NONE())
        FLINT_ARROW_TYPE = getArrowByNameOrNull(SkyhanniItems.ARROW())
    }

    class UnknownArrowType(message: String) : Exception(message)
}

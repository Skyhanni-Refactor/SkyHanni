package at.hannibal2.skyhanni.data

import at.hannibal2.skyhanni.data.item.SkyhanniItems
import at.hannibal2.skyhanni.events.InventoryCloseEvent
import at.hannibal2.skyhanni.events.InventoryOpenEvent
import at.hannibal2.skyhanni.events.ItemAddEvent
import at.hannibal2.skyhanni.events.LorenzChatEvent
import at.hannibal2.skyhanni.events.SackChangeEvent
import at.hannibal2.skyhanni.events.entity.ItemAddInInventoryEvent
import at.hannibal2.skyhanni.features.inventory.SuperCraftFeatures.craftedPattern
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.NEUInternalName
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.StringUtils.matches
import at.hannibal2.skyhanni.utils.TimeLimitedSet
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

object ItemAddManager {
    private var lastDiceRoll = SimpleTimeMark.farPast()
    private var superCraftedItems = TimeLimitedSet<NEUInternalName>(30.seconds)

    private val diceRollChatPattern by RepoPattern.pattern(
        "data.itemmanager.diceroll",
        "§eYour §r§(5|6High Class )Archfiend Dice §r§erolled a §r§.(?<number>.)§r§e! Bonus: §r§.(?<hearts>.*)❤"
    )

    private var inSackInventory = false
    private var lastSackInventoryLeave = SimpleTimeMark.farPast()

    @SubscribeEvent
    fun onInventoryOpen(event: InventoryOpenEvent) {
        if (event.inventoryName.contains("Sack")) {
            inSackInventory = true
        }
    }

    @SubscribeEvent
    fun onInventoryClose(event: InventoryCloseEvent) {
        if (inSackInventory) {
            inSackInventory = false
            lastSackInventoryLeave = SimpleTimeMark.now()
        }
    }

    @SubscribeEvent
    fun onSackChange(event: SackChangeEvent) {
        if (!LorenzUtils.inSkyBlock) return

        if (inSackInventory || lastSackInventoryLeave.passedSince() < 10.seconds) return

        for (sackChange in event.sackChanges) {
            val change = sackChange.delta
            val internalName = sackChange.internalName
            if (change > 0 && internalName !in superCraftedItems) {
                Source.SACKS.addItem(internalName, change)
            }
        }
        superCraftedItems.clear()
    }

    @SubscribeEvent
    fun onItemAdd(event: ItemAddInInventoryEvent) {
        if (!LorenzUtils.inSkyBlock) return

        val internalName = event.internalName
        if (internalName == SkyhanniItems.ARCHFIEND_DICE() || internalName == SkyhanniItems.HIGH_CLASS_ARCHFIEND_DICE()) {
            if (lastDiceRoll.passedSince() < 500.milliseconds) {
                return
            }
        }

        Source.ITEM_ADD.addItem(internalName, event.amount)
    }

    private fun Source.addItem(internalName: NEUInternalName, amount: Int) {
        ItemAddEvent(internalName, amount, this).postAndCatch()
    }

    @SubscribeEvent
    fun onChat(event: LorenzChatEvent) {
        if (diceRollChatPattern.matches(event.message)) {
            lastDiceRoll = SimpleTimeMark.now()
        }
        craftedPattern.matchMatcher(event.message) {
            val internalName = NEUInternalName.fromItemName(group("item"))
            if (!SackAPI.sackListInternalNames.contains(internalName.asString())) return@matchMatcher
            superCraftedItems.add(internalName)
        }
    }

    enum class Source {
        ITEM_ADD,
        SACKS,
        ;
    }
}

package at.hannibal2.skyhanni.api

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.api.skyblock.SkyBlockAPI
import at.hannibal2.skyhanni.compat.neu.NEUCompat
import at.hannibal2.skyhanni.data.SackAPI
import at.hannibal2.skyhanni.events.chat.MessageSendToServerEvent
import at.hannibal2.skyhanni.events.chat.SkyHanniChatEvent
import at.hannibal2.skyhanni.events.inventory.InventoryCloseEvent
import at.hannibal2.skyhanni.events.item.SkyHanniToolTipEvent
import at.hannibal2.skyhanni.events.minecraft.ClientTickEvent
import at.hannibal2.skyhanni.events.render.gui.SlotClickEvent
import at.hannibal2.skyhanni.features.commands.tabcomplete.GetFromSacksTabComplete
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.test.command.ErrorManager
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.ChatUtils.isCommand
import at.hannibal2.skyhanni.utils.ChatUtils.senderIsSkyhanni
import at.hannibal2.skyhanni.utils.HypixelCommands
import at.hannibal2.skyhanni.utils.NEUInternalName
import at.hannibal2.skyhanni.utils.NEUInternalName.Companion.asInternalName
import at.hannibal2.skyhanni.utils.NumberUtil.isDouble
import at.hannibal2.skyhanni.utils.PrimitiveItemStack
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.StringUtils.removeColor
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import java.util.Deque
import java.util.LinkedList
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object GetFromSackAPI {
    private val config get() = SkyHanniMod.feature.inventory.gfs

    val commands = arrayOf("gfs", "getfromsacks")
    private val commandsWithSlash = commands.map { "/$it" }

    private val patternGroup = RepoPattern.group("gfs.chat")
    private val fromSacksChatPattern by patternGroup.pattern(
        "from",
        "§aMoved §r§e(?<amount>\\d+) (?<item>.+)§r§a from your Sacks to your inventory."
    )
    private val missingChatPattern by patternGroup.pattern(
        "missing",
        "§cYou have no (?<item>.+) in your Sacks!"
    )

    private fun getFromSack(item: PrimitiveItemStack) = getFromSack(listOf(item))

    private fun getFromSack(items: List<PrimitiveItemStack>) = addToQueue(items)

    fun getFromChatMessageSackItems(
        item: PrimitiveItemStack,
        text: String = "§lCLICK HERE§r§e to grab §ax${item.amount} §9${item.itemName}§e from sacks!",
    ) =
        ChatUtils.clickableChat(text, onClick = {
            HypixelCommands.getFromSacks(item.internalName.asString(), item.amount)
        })

    private val minimumDelay = 1.65.seconds

    private val queue: Deque<PrimitiveItemStack> = LinkedList()
    private val inventoryMap = mutableMapOf<Int, List<PrimitiveItemStack>>()

    private var lastTimeOfCommand = SimpleTimeMark.farPast()

    private var lastItemStack: PrimitiveItemStack? = null

    private fun addToQueue(items: List<PrimitiveItemStack>) = queue.addAll(items)

    @HandleEvent(onlyOnSkyblock = true)
    fun onTick(event: ClientTickEvent) {
        if (queue.isNotEmpty() && lastTimeOfCommand.passedSince() >= minimumDelay) {
            val item = queue.poll()
            HypixelCommands.getFromSacks(item.internalName.asString().replace('-', ':'), item.amount)
            lastTimeOfCommand = ChatUtils.getTimeWhenNewlyQueuedMessageGetsExecuted()
        }
    }

    @HandleEvent
    fun onInventoryClose(event: InventoryCloseEvent) {
        inventoryMap.clear()
    }

    @HandleEvent(onlyOnSkyblock = true)
    fun onSlotClicked(event: SlotClickEvent) {
        if (event.clickedButton != 1) return // filter none right clicks
        addToQueue(inventoryMap[event.slotId] ?: return)
        inventoryMap.remove(event.slotId)
        event.cancel()
    }

    @HandleEvent(onlyOnSkyblock = true)
    fun onTooltip(event: SkyHanniToolTipEvent) {
        val list = inventoryMap[event.slot.slotIndex] ?: return
        event.toolTip.let { tip ->
            tip.add("")
            tip.add("§ePress right click to get from sack:")
            tip.addAll(list.map { "§ex" + it.amount.toString() + " " + it.internalName.asString() })
        }
    }

    @HandleEvent(onlyOnSkyblock = true)
    fun onMessageToServer(event: MessageSendToServerEvent) {
        if (!config.queuedGFS && !config.bazaarGFS) return
        if (!event.isCommand(commandsWithSlash)) return
        val replacedEvent = GetFromSacksTabComplete.handleUnderlineReplace(event)
        queuedHandler(replacedEvent)
        bazaarHandler(replacedEvent)
        if (replacedEvent.isCancelled) {
            event.cancel()
            return
        }
        if (replacedEvent !== event) {
            event.cancel()
            ChatUtils.sendMessageToServer(replacedEvent.message)
        }
    }

    private fun queuedHandler(event: MessageSendToServerEvent) {
        if (!config.queuedGFS) return
        if (event.senderIsSkyhanni()) return

        val (result, stack) = commandValidator(event.splitMessage.drop(1))

        when (result) {
            CommandResult.VALID -> getFromSack(stack ?: return)
            CommandResult.WRONG_ARGUMENT -> ChatUtils.userError("Missing arguments! Usage: /getfromsacks <name/id> <amount>")
            CommandResult.WRONG_IDENTIFIER -> ChatUtils.userError("Couldn't find an item with this name or identifier!")
            CommandResult.WRONG_AMOUNT -> ChatUtils.userError("Invalid amount!")
            CommandResult.INTERNAL_ERROR -> {}
        }
        event.cancel()
    }

    private fun bazaarHandler(event: MessageSendToServerEvent) {
        if (event.isCancelled) return
        if (!config.bazaarGFS || SkyBlockAPI.gamemode.noTrade) return
        lastItemStack = commandValidator(event.splitMessage.drop(1)).second
    }

    private fun bazaarMessage(item: String, amount: Int, isRemaining: Boolean = false) = ChatUtils.clickableChat(
        "§lCLICK §r§eto get the ${if (isRemaining) "remaining " else ""}§ax${amount} §9$item §efrom bazaar",
        onClick = { HypixelCommands.bazaar(item.removeColor()) }
    )

    private fun commandValidator(args: List<String>): Pair<CommandResult, PrimitiveItemStack?> {
        if (args.size <= 1) {
            return CommandResult.WRONG_ARGUMENT to null
        }

        var amountString = args.last()
        amountString = NEUCompat.calculate(amountString)?.toString() ?: amountString

        if (!amountString.isDouble()) return CommandResult.WRONG_AMOUNT to null

        val itemString = args.dropLast(1).joinToString(" ").uppercase().replace(':', '-')

        val item = when {
            SackAPI.sackListInternalNames.contains(itemString) -> itemString.asInternalName()
            SackAPI.sackListNames.contains(itemString) -> NEUInternalName.fromItemNameOrNull(itemString) ?: run {
                ErrorManager.logErrorStateWithData(
                    "Couldn't resolve item name",
                    "Query failed",
                    "itemName" to itemString
                )
                return CommandResult.INTERNAL_ERROR to null
            }

            else -> return CommandResult.WRONG_IDENTIFIER to null
        }

        return CommandResult.VALID to PrimitiveItemStack(item, amountString.toDouble().toInt())
    }

    @HandleEvent(onlyOnSkyblock = true)
    fun onChat(event: SkyHanniChatEvent) {
        if (!config.bazaarGFS || SkyBlockAPI.gamemode.noTrade) return
        val stack = lastItemStack ?: return
        val message = event.message
        fromSacksChatPattern.matchMatcher(message) {
            val diff = stack.amount - group("amount").toInt()
            lastItemStack = null
            if (diff <= 0) return
            bazaarMessage(stack.itemName, diff, true)
            return
        }
        missingChatPattern.matchMatcher(message) {
            bazaarMessage(stack.itemName, stack.amount)
            lastItemStack = null
            return
        }
    }

    private enum class CommandResult {
        VALID,
        WRONG_ARGUMENT,
        WRONG_IDENTIFIER,
        WRONG_AMOUNT,
        INTERNAL_ERROR
    }
}

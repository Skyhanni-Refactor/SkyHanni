package at.hannibal2.skyhanni.features.event

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.api.skyblock.Gamemode
import at.hannibal2.skyhanni.api.skyblock.SkyBlockAPI
import at.hannibal2.skyhanni.data.ProfileStorageData
import at.hannibal2.skyhanni.events.chat.SkyHanniChatEvent
import at.hannibal2.skyhanni.events.entity.EntityCustomNameUpdateEvent
import at.hannibal2.skyhanni.events.entity.EntityEnterWorldEvent
import at.hannibal2.skyhanni.events.minecraft.ClientTickEvent
import at.hannibal2.skyhanni.events.minecraft.WorldChangeEvent
import at.hannibal2.skyhanni.features.event.winter.UniqueGiftCounter
import at.hannibal2.skyhanni.mixins.hooks.RenderLivingEntityHelper
import at.hannibal2.skyhanni.utils.ColourUtils.withAlpha
import at.hannibal2.skyhanni.utils.EntityUtils.isNPC
import at.hannibal2.skyhanni.utils.InventoryUtils
import at.hannibal2.skyhanni.utils.LorenzColor
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.datetime.DateUtils
import at.hannibal2.skyhanni.utils.getLorenzVec
import at.hannibal2.skyhanni.utils.mc.McWorld
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraft.client.entity.EntityOtherPlayerMP
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.entity.player.EntityPlayer

object UniqueGiftingOpportunitiesFeatures {

    private val playerList: MutableSet<String>?
        get() = ProfileStorageData.playerSpecific?.winter?.playersThatHaveBeenGifted

    private val patternGroup = RepoPattern.group("event.winter.uniquegifts")
    private val giftedPattern by patternGroup.pattern(
        "gifted",
        "§6\\+1 Unique Gift given! To ([^§]+)§r§6!"
    )
    private val giftNamePattern by patternGroup.pattern(
        "giftname",
        "(?:WHITE|RED|GREEN)_GIFT\$"
    )

    private var holdingGift = false

    private fun hasGiftedPlayer(player: EntityPlayer) = playerList?.contains(player.name) == true

    private fun addGiftedPlayer(playerName: String) {
        playerList?.add(playerName)
    }

    private val config get() = SkyHanniMod.feature.event.winter.giftingOpportunities

    private fun isEnabled() = holdingGift

    private val hasNotGiftedNametag = "§a§lꤥ"
    private val hasGiftedNametag = "§c§lꤥ"

    private fun analyzeArmorStand(entity: EntityArmorStand) {
        if (!config.useArmorStandDetection) return
        if (entity.name != hasGiftedNametag) return

        val matchedPlayer = McWorld.getEntitiesNear<EntityPlayer>(entity.getLorenzVec(), 2.0)
            .singleOrNull { !it.isNPC() } ?: return
        addGiftedPlayer(matchedPlayer.name)
    }

    @HandleEvent
    fun onEntityChangeName(event: EntityCustomNameUpdateEvent) {
        val entity = event.entity as? EntityArmorStand ?: return
        analyzeArmorStand(entity)
    }

    @HandleEvent
    fun onEntityJoinWorld(event: EntityEnterWorldEvent) {
        playerColor(event)
        val entity = event.entity as? EntityArmorStand ?: return
        analyzeArmorStand(entity)
    }

    private fun playerColor(event: EntityEnterWorldEvent) {
        if (event.entity is EntityOtherPlayerMP) {
            val entity = event.entity
            if (entity.isNPC() || isIronman(entity) || isBingo(entity)) return

            RenderLivingEntityHelper.setEntityColor(
                entity,
                LorenzColor.DARK_GREEN.toColor().withAlpha(127)
            ) { isEnabled() && !hasGiftedPlayer(entity) }
        }
    }

    private fun isBingo(entity: EntityLivingBase) =
        SkyBlockAPI.gamemode != Gamemode.BINGO && entity.displayName.formattedText.endsWith("Ⓑ§r")

    private fun isIronman(entity: EntityLivingBase) =
        !SkyBlockAPI.gamemode.noTrade && entity.displayName.formattedText.endsWith("♲§r")

    @HandleEvent
    fun onChat(event: SkyHanniChatEvent) {
        giftedPattern.matchMatcher(event.message) {
            addGiftedPlayer(group(1))
            UniqueGiftCounter.addUniqueGift()
        }
    }

    @HandleEvent
    fun onTick(event: ClientTickEvent) {
        holdingGift = false

        if (!SkyBlockAPI.isConnected) return
        if (!config.enabled) return
        if (!DateUtils.isDecember()) return

        holdingGift = !config.highlighWithGiftOnly || giftNamePattern.matches(InventoryUtils.itemInHandId.asString())
    }

    @HandleEvent
    fun onWorldChange(event: WorldChangeEvent) {
        holdingGift = false
    }
}

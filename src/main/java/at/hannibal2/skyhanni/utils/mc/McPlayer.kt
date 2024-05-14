package at.hannibal2.skyhanni.utils.mc

import at.hannibal2.skyhanni.data.SackAPI.getAmountInSacks
import at.hannibal2.skyhanni.utils.ItemCategory
import at.hannibal2.skyhanni.utils.ItemUtils.getInternalNameOrNull
import at.hannibal2.skyhanni.utils.ItemUtils.getItemCategoryOrNull
import at.hannibal2.skyhanni.utils.LorenzVec
import at.hannibal2.skyhanni.utils.NEUInternalName
import at.hannibal2.skyhanni.utils.toLorenzVec
import net.minecraft.client.Minecraft
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import java.util.UUID

typealias Player = EntityPlayer

object McPlayer {

    val player: Player? get() = Minecraft.getMinecraft().thePlayer
    val name: String get() = player?.name ?: McClient.profileName
    val uuid: UUID get() = player?.uniqueID ?: McClient.profileUUID

    val isSneaking: Boolean get() = player?.isSneaking ?: false
    val onGround: Boolean get() = player?.onGround ?: false

    val pos: LorenzVec get() = LorenzVec(player?.posX ?: 0.0, player?.posY ?: 0.0, player?.posZ ?: 0.0)
    val eyePos: LorenzVec get() = pos.up(player?.eyeHeight?.toDouble() ?: 0.0)
    val posBelow: LorenzVec get() = pos.roundLocationToBlock().up(-1.0)
    val blockOn: BlockState get() = McWorld.getBlockState(posBelow)

    val blockLookingAt: LorenzVec?
        get() = player?.let {
            McWorld.world?.rayTraceBlocks(
                eyePos.toVec3(),
                (eyePos + (it.lookVec.toLorenzVec().normalize() * 10.0)).toVec3()
            )?.blockPos?.toLorenzVec()
        }

    private val mainInventory: Array<ItemStack?> get() = player?.inventory?.mainInventory ?: arrayOfNulls(36)

    val heldItem: ItemStack? get() = player?.inventory?.getCurrentItem()
    val armor: Array<ItemStack?> get() = player?.inventory?.armorInventory ?: arrayOfNulls(4)
    val inventory: List<ItemStack> get() = mainInventory.filterNotNull()
    val hotbar: List<ItemStack> get() = mainInventory.sliceArray(0..8).filterNotNull()
    // TODO use this instead of inventory for many cases, e.g. vermin tracker, diana spade, etc

    val helmet: ItemStack? get() = armor[3]
    val chestplate: ItemStack? get() = armor[2]
    val leggings: ItemStack? get() = armor[1]
    val boots: ItemStack? get() = armor[0]

    fun countItems(predicate: (ItemStack) -> Boolean) = inventory.filter(predicate).sumOf { it.stackSize }
    fun countItems(item: NEUInternalName, checkSacks: Boolean = false) =
        countItems { it.getInternalNameOrNull() == item } + if (checkSacks) item.getAmountInSacks() else 0

    fun has(item: NEUInternalName, onlyHotBar: Boolean = false) =
        (if (onlyHotBar) hotbar else inventory).any { it.getInternalNameOrNull() == item }

    fun has(category: ItemCategory, onlyHotBar: Boolean = false) =
        (if (onlyHotBar) hotbar else inventory).any { it.getItemCategoryOrNull() == category }

    @JvmStatic
    fun closeContainer() = player?.closeScreen()
}

package at.hannibal2.skyhanni.utils.mc

import at.hannibal2.skyhanni.utils.LorenzVec
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

    val blockLookingAt: LorenzVec? get() = player?.let {
        McWorld.world?.rayTraceBlocks(
            eyePos.toVec3(),
            (eyePos + (it.lookVec.toLorenzVec().normalize() * 10.0)).toVec3()
        )?.blockPos?.toLorenzVec()
    }

    val heldItem: ItemStack? get() = player?.inventory?.getCurrentItem()
    val armor: Array<ItemStack?> get() = player?.inventory?.armorInventory ?: arrayOfNulls(4)

    val helmet: ItemStack? get() = armor[3]
    val chestplate: ItemStack? get() = armor[2]
    val leggings: ItemStack? get() = armor[1]
    val boots: ItemStack? get() = armor[0]

    @JvmStatic
    fun closeContainer() = player?.closeScreen()
}

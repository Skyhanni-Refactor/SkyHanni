package at.hannibal2.skyhanni.utils.mc

import at.hannibal2.skyhanni.utils.LorenzVec
import at.hannibal2.skyhanni.utils.toLorenzVec
import net.minecraft.client.Minecraft
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import java.util.UUID

typealias Player = EntityPlayer

object McPlayer {

    val hasPlayer: Boolean get() = Minecraft.getMinecraft().thePlayer != null
    val player: Player get() = Minecraft.getMinecraft().thePlayer
    val name: String get() = player.name
    val uuid: UUID get() = player.uniqueID

    val isSneaking: Boolean get() = hasPlayer && player.isSneaking
    val onGround: Boolean get() = hasPlayer && player.onGround

    val pos: LorenzVec get() = LorenzVec(player.posX, player.posY, player.posZ)
    val eyePos: LorenzVec get() = pos.up(player.eyeHeight.toDouble())
    val posBelow: LorenzVec get() = pos.roundLocationToBlock().up(-1.0)
    val blockOn: BlockState get() = McWorld.getBlockState(posBelow)

    val blockLookingAt: LorenzVec? get() = McWorld.world?.rayTraceBlocks(
        eyePos.toVec3(),
        (eyePos + (player.lookVec.toLorenzVec().normalize() * 10.0)).toVec3()
    )?.blockPos?.toLorenzVec()

    val heldItem: ItemStack? get() = player.inventory.getStackInSlot(player.inventory.currentItem)

    @JvmStatic
    fun closeContainer() = player.closeScreen()
}

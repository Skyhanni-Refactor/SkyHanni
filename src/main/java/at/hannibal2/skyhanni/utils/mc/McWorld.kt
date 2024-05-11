package at.hannibal2.skyhanni.utils.mc

import at.hannibal2.skyhanni.data.mob.MobFilter.isRealPlayer
import at.hannibal2.skyhanni.utils.EntityUtils.isNPC
import at.hannibal2.skyhanni.utils.LocationUtils.distanceTo
import at.hannibal2.skyhanni.utils.LorenzVec
import net.minecraft.block.Block
import net.minecraft.block.properties.IProperty
import net.minecraft.block.state.IBlockState
import net.minecraft.client.Minecraft
import net.minecraft.client.entity.EntityOtherPlayerMP
import net.minecraft.client.multiplayer.WorldClient
import net.minecraft.tileentity.TileEntity
import java.util.UUID

typealias BlockState = IBlockState
typealias BlockEntity = TileEntity
typealias Entity = net.minecraft.entity.Entity

object McWorld {

    val hasWorld: Boolean get() = Minecraft.getMinecraft().theWorld != null
    val world: WorldClient? get() = Minecraft.getMinecraft().theWorld

    // Block Related Functions

    fun getBlock(pos: LorenzVec): Block = getBlockState(pos).block
    fun getBlockState(pos: LorenzVec): BlockState = world!!.getBlockState(pos.toBlockPos())
    fun getBlockEntity(pos: LorenzVec): BlockEntity? = world?.getTileEntity(pos.toBlockPos())

    fun LorenzVec.getBlockAt(): Block = getBlock(this)
    fun LorenzVec.getBlockStateAt(): BlockState = getBlockState(this)
    fun LorenzVec.getBlockEntityAt(): BlockEntity? = getBlockEntity(this)

    inline fun <T : Comparable<T>, reified P : IProperty<T>> BlockState.checkProperty(name: String, value: (T) -> Boolean): Boolean {
        val property = block.blockState.properties.find { it.name == name } ?: return false
        return if (property is P) value(getValue(property)) else false
    }

    // Player Entity Related Functions

    val players: Iterable<Player> get() =
        world?.playerEntities?.filter { it.isRealPlayer() && it is EntityOtherPlayerMP } ?: emptyList()

    fun getPlayer(id: UUID): Player? = world?.getPlayerEntityByUUID(id)

    // Entity Related Functions

    val entities: List<Entity> get() = world?.loadedEntityList?.let {
        if (McClient.isCalledFromMainThread) it else it.toMutableList()
    }?.filterNotNull() ?: emptyList()

    fun getEntity(id: Int): Entity? = world?.getEntityByID(id)

    inline fun <reified R : Entity> getEntitiesOf(): Sequence<R> =
        entities.asSequence().filterIsInstance<R>()

    inline fun <reified T : Entity> getEntitiesNear(entity: Entity, radius: Double): Sequence<T> =
        getEntitiesOf<T>().filter { it.distanceTo(entity) < radius }

    inline fun <reified T : Entity> getEntitiesNear(pos: LorenzVec, radius: Double): Sequence<T> =
        getEntitiesOf<T>().filter { it.distanceTo(pos) < radius }

    inline fun <reified T : Entity> getEntitiesNearPlayer(radius: Double): Sequence<T> =
        getEntitiesOf<T>().filter { it.distanceTo(McPlayer.pos) < radius }

    // Scoreboard Related Functions

    fun getScoreboard() = world?.scoreboard

    fun getObjective(slot: Int) = getScoreboard()?.getObjectiveInDisplaySlot(slot)
}

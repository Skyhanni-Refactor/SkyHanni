package at.hannibal2.skyhanni.test

import at.hannibal2.skyhanni.utils.ItemUtils.isEnchanted
import at.hannibal2.skyhanni.utils.SkyBlockItemModifierUtils.getDungeonStarCount
import at.hannibal2.skyhanni.utils.SkyBlockItemModifierUtils.getEnchantments
import at.hannibal2.skyhanni.utils.SkyBlockItemModifierUtils.getHotPotatoCount
import at.hannibal2.skyhanni.utils.SkyBlockItemModifierUtils.getItemUuid
import at.hannibal2.skyhanni.utils.SkyBlockItemModifierUtils.getReforgeName
import at.hannibal2.skyhanni.utils.SkyBlockItemModifierUtils.hasArtOfWar
import at.hannibal2.skyhanni.utils.SkyBlockItemModifierUtils.isRecombobulated
import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompressedStreamTools
import org.junit.jupiter.api.Test

class ItemModifierTest {

    private fun getTestItem(id: String): ItemStack {
        val stream = javaClass.getResourceAsStream("/testdata/item/$id.nbt")
        require(stream != null) { "Test item $id not found" }
        return ItemStack.loadItemStackFromNBT(CompressedStreamTools.readCompressed(stream))
    }

    @Test
    fun testUpgradeLevelMasterStars() {
        val itemStack = getTestItem("shinyheroichyperion")
        assert(itemStack.isRecombobulated())
        assert(itemStack.getReforgeName() == "heroic")
        assert(itemStack.getItemUuid() == "2c28ffde-739b-4de0-8abd-91bbf9f13dc8")
        assert(itemStack.isEnchanted())
        assert(itemStack.getHotPotatoCount() == 15)
        assert(itemStack.getEnchantments()?.size == 21)
        assert(itemStack.hasArtOfWar())
        assert(itemStack.getDungeonStarCount() == 5)
    }
}

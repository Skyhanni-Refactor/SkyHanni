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
import java.util.Base64

class ItemModifierTest {

    companion object {

        private const val SHINY_HEROIC_HYPERION = "H4sIAAAAAAAAAHVV3WobRxgdRbItyUnTlkAplDIpcbGwJbSS9ZdSii3LkUhiG8t2MCUso92RNGh3R92dtaPLvkB704tSkqteGPoYfhQ/SOmZHclyAhX27vyc72e+Od/ZPCFZ8kC45IkvAu6EbKiei1AGdnQlQzdFVtoyDlQqT9KKjXIkwwNnTPQvRdbPgkHI2YQNPJ5Kk1xXuPzAY6MIu//myZoroqnHZjB6JUOexep3hN5cN15wFtK+g7XnmLqWVavg3dysVKxygWwCsc98Nkp2na1qVe/yza1quZDAturNSqnaLJAfgOyrkAcjNTbYSq18H1v/eav2Fu/W5lZtYVyr7JTKtQJ5BuN2KBS9H6tR3jAoq1YvWRsFUgFqTwZxRHeVYs6E9qecu3PwhnHd2Ji7tsqlGmw2YNPlzDNJMeSiT3k//3qpVSDPAesFinueGKGo3IBr9brxalVqhU/srG2ruVOyqgXyDWwPeCgdoWbzIFWD2cHRvk2OfnM9uH3/G0Zv51Pn9q8/9BT38CMc31x75nnmKeEzxekbEXF6jujbOoX2mPlTIQPau1vxOLsEgtT0BLUTDvPo+XI/HojIX3rYD9kI9l3wh4e0p0/c6gQuxn3QAq+laecdd2J1L/qBCDndjabcUbTXM7ZYixTFjYsJp707aM+fMk8EI41brL0MpDMZ6AuDbRkLr6RUCebO7FWM3WUGfQdnC0Y6UXPAvi90Qkufp2zAIkfeD3M6jvV5PHQKkKSpl4RiAX0pPO+jA57rYoZJeZZrPJC+BLfOcSXfa9qDNRHdwlXVEiI23ISbVEncjRrzMCoRSzdQyAIVJSS09PP2w69zGmsjFrgJJXTbaAqQIt73maZBU2QHyzYDq6U/0M4aHr/kXgm5FNBDJ2I0VkXHE6gS4segxkzGIXU8FkWUDYQH7j0FGNVt3Fzv3H74Wzs84b/EuDrtj52OOV0GOPCk1BXpkS+w15b+1OMK/IJRify0IOSE3VzrzJb07Hd7hxf09cVpt9em+2eHLzpHh7T/5uhkny6NyFdg/821X/z0l7RKn+vb0I3SuMAZ+L4M+F7surOkV/fiGYUOBPJKI+rWdrlZ3i6Xk3/qSBFoPfsSQHA3oiLQKF5taRV8glF7USIANV2fZknmkPlcN9kw+XP7YxHMaBcNKxzanaH2uq8Q6/b9P//3JHnyuPNOhQzCE4oBmiNKk8chQ9vN7Hg6CpnLdWKpFMmKyI50CIjw52Op7KlUTEnb0coNyOM8yYy4H2VJtn30em/31C6TtePOyUGnfZojn8WBh2bhrh15UkVaph+QfH/3+LjbO+kAemeUJQ8XQxv+SObkbO8i+xF24ZYgExYqWw7tKxbeZZJCCr50xVDwkKyOk4KkyaP5ceyEgICtmC9StnsBd72jwzx5qD87IL3PQfw0TjzveYDTaZLx0MsYrmLHmeuSma46iWZpn2myEumWxngtTdY8IwiYZdLaPdoYBdCyZCxz0UIQDCQ3WUiKLhDml4uONvj1IWhvs4QCJqs1bkTNBH/kJmpojxM11LVA2KFWNDtKFM2EyV7ORcFYrSkjOsbjulrqjYn6KJ6Lt30F8TY2D5WWIHuSSNCiDIk0m/2sM1f25CMOtsYxiv2s4lSaw6HLi41qa1DccXm52GQDt9iyBoNha2hVXaeZITmE45GCAxj/fvFn7mtNl1WjPnD4H72LfN5PCAAA"
    }

    private fun getTestItem(base64: String): ItemStack {
        val bytes = Base64.getDecoder().decode(base64)
        val stream = bytes.inputStream()
        return ItemStack.loadItemStackFromNBT(CompressedStreamTools.readCompressed(stream))
    }

    @Test
    fun testUpgradeLevelMasterStars() {
        val itemStack = getTestItem(SHINY_HEROIC_HYPERION)
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

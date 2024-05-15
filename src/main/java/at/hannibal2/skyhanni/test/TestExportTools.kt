package at.hannibal2.skyhanni.test

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.events.GuiKeyPressEvent
import at.hannibal2.skyhanni.test.command.CopyItemCommand.copyItemToClipboard
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.KSerializable
import at.hannibal2.skyhanni.utils.KeyboardManager.isKeyHeld
import at.hannibal2.skyhanni.utils.KotlinTypeAdapterFactory
import at.hannibal2.skyhanni.utils.json.fromJson
import at.hannibal2.skyhanni.utils.json.SkyHanniTypeAdapters
import at.hannibal2.skyhanni.utils.system.OS
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import net.minecraft.item.ItemStack
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.io.InputStreamReader
import java.io.Reader

object TestExportTools {

    private val config get() = SkyHanniMod.feature.dev.debug

    val gson: Gson = GsonBuilder()
        .registerTypeAdapterFactory(KotlinTypeAdapterFactory())
        .registerTypeAdapter(ItemStack::class.java, SkyHanniTypeAdapters.GZIP_BASE_64_ITEMSTACK)
        .create()

    class Key<T> internal constructor(val name: String)

    val Item = Key<ItemStack>("Item")

    @KSerializable
    data class TestValue(
        val type: String,
        val data: JsonElement,
    )

    private fun <T> toJson(key: Key<T>, value: T): String {
        return gson.toJson(TestValue(key.name, gson.toJsonTree(value)))
    }

    inline fun <reified T> fromJson(key: Key<T>, reader: Reader): T {
        val serializable = gson.fromJson<TestValue>(reader)
        require(key.name == serializable.type)
        return gson.fromJson(serializable.data)
    }

    @SubscribeEvent
    fun onKeybind(event: GuiKeyPressEvent) {
        if (!config.copyItemDataCompressed.isKeyHeld() && !config.copyItemData.isKeyHeld()) return
        val stack = event.guiContainer.slotUnderMouse?.stack ?: return
        if (config.copyItemData.isKeyHeld()) {
            copyItemToClipboard(stack)
            return
        }
        val json = toJson(Item, stack)
        OS.copyToClipboard(json)
        ChatUtils.chat("Compressed item info copied into the clipboard!")
    }

    inline fun <reified T> getTestData(category: Key<T>, name: String): T {
        val reader = InputStreamReader(javaClass.getResourceAsStream("/testdata/${category.name}/$name.json")!!)
        return fromJson(category, reader)
    }
}

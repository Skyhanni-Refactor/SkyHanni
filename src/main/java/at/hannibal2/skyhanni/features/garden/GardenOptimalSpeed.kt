package at.hannibal2.skyhanni.features.garden

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.data.TitleManager
import at.hannibal2.skyhanni.events.garden.farming.GardenToolChangeEvent
import at.hannibal2.skyhanni.events.minecraft.ClientTickEvent
import at.hannibal2.skyhanni.events.render.gui.GuiRenderEvent
import at.hannibal2.skyhanni.events.utils.ConfigLoadEvent
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.ConditionalUtils
import at.hannibal2.skyhanni.utils.RenderUtils.renderString
import at.hannibal2.skyhanni.utils.RenderUtils.renderStringsAndItems
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.StringUtils.removeColor
import at.hannibal2.skyhanni.utils.mc.McScreen.setTextIntoSign
import at.hannibal2.skyhanni.utils.mc.McScreen.text
import at.hannibal2.skyhanni.utils.renderables.Renderable
import io.github.notenoughupdates.moulconfig.observer.Property
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.inventory.GuiEditSign
import net.minecraftforge.client.event.GuiOpenEvent
import net.minecraftforge.client.event.GuiScreenEvent.DrawScreenEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

object GardenOptimalSpeed {

    private val config get() = GardenAPI.config.optimalSpeeds

    private val configCustomSpeed get() = config.customSpeed
    private var sneakingTime = 0.seconds
    private val sneaking get() = Minecraft.getMinecraft().thePlayer.isSneaking
    private val sneakingPersistent get() = sneakingTime > 5.seconds

    /**
     * This speed value represents the walking speed, not the speed stat.
     * blocks per second = 4.317 * speed / 100
     *
     * It has an absolute speed cap of 500, and items that normally increase the cap do not apply here:
     * (Black Cat pet, Cactus knife, Racing Helmet or Young Dragon Armor)
     *
     * If this information ever gets abstracted away and made available outside this class,
     * and some features need the actual value of the Speed stat instead,
     * we can always just have two separate variables, like walkSpeed and speedStat.
     * But since this change is confined to Garden-specific code, it's fine the way it is for now.
     */
    private var currentSpeed = 100

    private var optimalSpeed = -1
    private var lastWarnTime = 0L
    private var cropInHand: CropType? = null
    private var rancherOverlayList: List<List<Any?>> = emptyList()
    private var lastToolSwitch = SimpleTimeMark.farPast()

    @HandleEvent
    fun onTick(event: ClientTickEvent) {
        currentSpeed = (Minecraft.getMinecraft().thePlayer.capabilities.walkSpeed * 1000).toInt()

        if (sneaking) {
            currentSpeed = (currentSpeed * 0.3).toInt()
            sneakingTime += 50.milliseconds
        } else {
            sneakingTime = 0.seconds
        }
    }

    @SubscribeEvent
    fun onGuiOpen(event: GuiOpenEvent) {
        if (!isRancherOverlayEnabled()) return
        val gui = event.gui
        if (gui !is GuiEditSign) return
        if (!isRancherSign(gui)) return
        rancherOverlayList = CropType.entries.map { crop ->
            listOf(crop.icon, Renderable.link("${crop.cropName} - ${crop.getOptimalSpeed()}") {
                gui.setTextIntoSign("${crop.getOptimalSpeed()}")
            })
        }
    }

    @SubscribeEvent
    fun onGuiRender(event: DrawScreenEvent.Post) {
        if (!isRancherOverlayEnabled()) return
        val gui = event.gui
        if (gui !is GuiEditSign) return
        if (!isRancherSign(gui)) return
        config.signPosition.renderStringsAndItems(
            rancherOverlayList,
            posLabel = "Optimal Speed Rancher Overlay"
        )
    }

    @HandleEvent
    fun onGardenToolChange(event: GardenToolChangeEvent) {
        lastToolSwitch = SimpleTimeMark.now()
        cropInHand = event.crop
        optimalSpeed = cropInHand?.getOptimalSpeed() ?: -1
    }

    @HandleEvent
    fun onConfigLoad(event: ConfigLoadEvent) {
        for (value in CropType.entries) {
            ConditionalUtils.onToggle(value.getConfig()) {
                if (value == cropInHand) {
                    optimalSpeed = value.getOptimalSpeed()
                }
            }
        }
    }

    private fun CropType.getOptimalSpeed() = getConfig().get().toInt()

    private fun CropType.getConfig(): Property<Float> = with(configCustomSpeed) {
        when (this@getConfig) {
            CropType.WHEAT -> wheat
            CropType.CARROT -> carrot
            CropType.POTATO -> potato
            CropType.NETHER_WART -> netherWart
            CropType.PUMPKIN -> pumpkin
            CropType.MELON -> melon
            CropType.COCOA_BEANS -> cocoaBeans
            CropType.SUGAR_CANE -> sugarCane
            CropType.CACTUS -> cactus
            CropType.MUSHROOM -> mushroom
        }
    }

    @HandleEvent
    fun onRenderOverlay(event: GuiRenderEvent.GuiOverlayRenderEvent) {
        if (!GardenAPI.inGarden()) return

        if (optimalSpeed == -1) return

        if (GardenAPI.hideExtraGuis()) return

        var text = "Optimal Speed: §f$optimalSpeed"
        if (optimalSpeed != currentSpeed) {
            text += " (§eCurrent: §f$currentSpeed"
            if (sneaking) text += " §7[Sneaking]"
            text += "§f)"
        }

        val recentlySwitchedTool = lastToolSwitch.passedSince() < 1.5.seconds
        val recentlyStartedSneaking = sneaking && !sneakingPersistent

        val colorCode =
            if (recentlySwitchedTool || recentlyStartedSneaking) "7" else if (optimalSpeed != currentSpeed) "c" else "a"

        if (config.showOnHUD) config.pos.renderString("§$colorCode$text", posLabel = "Garden Optimal Speed")
        if (optimalSpeed != currentSpeed && !recentlySwitchedTool && !recentlyStartedSneaking) warn()
    }

    private fun warn() {
        if (!config.warning) return
        if (!Minecraft.getMinecraft().thePlayer.onGround) return
        if (GardenAPI.onBarnPlot) return
        if (System.currentTimeMillis() < lastWarnTime + 20_000) return

        lastWarnTime = System.currentTimeMillis()
        TitleManager.sendTitle("§cWrong speed!", 3.seconds)
        cropInHand?.let {
            var text = "Wrong speed for ${it.cropName}: §f$currentSpeed"
            if (sneaking) text += " §7[Sneaking]"
            text += " §e(§f$optimalSpeed §eis optimal)"

            ChatUtils.chat(text)
        }
    }

    fun isRancherSign(sign: GuiEditSign): Boolean {
        val text = sign.text ?: return false
        return text[1].removeColor() == "^^^^^^" && text[2].removeColor() == "Set your" && text[3].removeColor() == "speed cap!"
    }

    private fun isRancherOverlayEnabled() = GardenAPI.inGarden() && config.signEnabled
}

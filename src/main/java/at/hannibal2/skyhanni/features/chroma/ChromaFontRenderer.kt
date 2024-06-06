package at.hannibal2.skyhanni.features.chroma

import at.hannibal2.skyhanni.utils.shader.ShaderHelper
import net.minecraft.client.renderer.GlStateManager
import org.lwjgl.opengl.GL11
import java.awt.Color

/**
 * Class to handle chroma font rendering
 *
 * Modified class from SkyblockAddons
 *
 * Credit: [DrawStateFontRenderer.java](https://github.com/BiscuitDevelopment/SkyblockAddons/blob/main/src/main/java/codes/biscuit/skyblockaddons/utils/draw/DrawStateFontRenderer.java)
 */
class ChromaFontRenderer(private val baseColor: Color) {

    private var chromaOn = false

    fun startChroma() {
        chromaOn = true
    }

    fun endChroma() {
        chromaOn = false
    }

    fun loadChromaEnv() {
        if (chromaOn) {
            newChromaEnv()
        }
    }

    fun restoreChromaEnv() {
        if (ShaderHelper.areShadersSupported() && !chromaOn) ChromaShaderManager.end()
    }

    fun newChromaEnv(): ChromaFontRenderer {
        if (ShaderHelper.areShadersSupported()) {
            ChromaShaderManager.begin(ChromaType.TEXTURED)
            GlStateManager.shadeModel(GL11.GL_SMOOTH)
        }
        return this
    }

    fun bindActualColor(alpha: Float): ChromaFontRenderer {
        GlStateManager.color(
            baseColor.red / 255f,
            baseColor.green / 255f,
            baseColor.blue / 255f,
            alpha
        )
        return this
    }

    fun endChromaEnv(): ChromaFontRenderer {
        if (ShaderHelper.areShadersSupported()) {
            ChromaShaderManager.end()
            GlStateManager.shadeModel(GL11.GL_FLAT)
        }
        return this
    }

    fun getChromaState() = chromaOn
}

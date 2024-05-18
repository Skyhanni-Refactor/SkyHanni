package at.hannibal2.skyhanni.compat.neu

import at.hannibal2.skyhanni.test.command.ErrorManager
import at.hannibal2.skyhanni.utils.RecalculatingValue
import io.github.moulberry.notenoughupdates.NotEnoughUpdates
import io.github.moulberry.notenoughupdates.util.Calculator
import java.math.BigDecimal
import kotlin.time.Duration.Companion.seconds

object NEUCompat {

    val isNeuStorageEnabled = RecalculatingValue(10.seconds) {
        try {
            val config = NotEnoughUpdates.INSTANCE.config

            val storageField = config.javaClass.getDeclaredField("storageGUI")
            val storage = storageField.get(config)

            val booleanField = storage.javaClass.getDeclaredField("enableStorageGUI3")
            booleanField.get(storage) as Boolean
        } catch (e: Throwable) {
            ErrorManager.logErrorWithData(e, "Could not read NEU config to determine if the neu storage is enabled.")
            false
        }
    }

    fun calculate(input: String): BigDecimal? = runCatching { Calculator.calculate(input) }.getOrNull()


}

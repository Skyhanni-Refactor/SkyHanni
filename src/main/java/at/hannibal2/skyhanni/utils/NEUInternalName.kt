package at.hannibal2.skyhanni.utils

import at.hannibal2.skyhanni.test.command.ErrorManager

class NEUInternalName private constructor(private val internalName: String) {

    companion object {

        private val map = mutableMapOf<String, NEUInternalName>()

        fun String.asInternalName(): NEUInternalName {
            val internalName = uppercase().replace(" ", "_")
            return map.getOrPut(internalName) { NEUInternalName(internalName) }
        }

        fun fromItemNameOrNull(itemName: String): NEUInternalName? =
            ItemNameResolver.getInternalNameOrNull(itemName.removeSuffix(" Pet"))

        fun fromItemName(itemName: String): NEUInternalName =
            fromItemNameOrNull(itemName) ?: ErrorManager.skyHanniError(
                "NEUInternalName is null for item name: '$itemName'",
                "inventoryName" to InventoryUtils.openInventoryName()
            )
    }

    fun asString() = internalName

    override fun equals(other: Any?): Boolean {
        if (other is NEUInternalName) {
            return internalName == other.internalName
        }
        return super.equals(other)
    }

    override fun toString(): String = "internalName:$internalName"

    override fun hashCode(): Int = internalName.hashCode()

    fun equals(other: String) = internalName == other

    fun contains(other: String) = internalName.contains(other)

    fun startsWith(other: String) = internalName.startsWith(other)

    fun endsWith(other: String) = internalName.endsWith(other)

    fun replace(oldValue: String, newValue: String) =
        internalName.replace(oldValue.uppercase(), newValue.uppercase()).asInternalName()
}

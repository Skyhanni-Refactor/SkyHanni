/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */
package at.hannibal2.skyhanni.utils.network

import io.netty.buffer.ByteBuf

/**
 * Utility class for creating a nice human readable dump of binary data.
 *
 *
 * It might look something like this:<BR></BR>
 *
 * <PRE>
 * 00 01 02 03 04 05 06 07 08 09 0A 0B 0C 0D 0E 0F    ................
 * 69 68 67 66 65 64 63 62 61 61 6A 6B 6C 6D 6E 00    ihgfedcbaajklmn.
 * 41 00                                              A.
 * Length: 34
</PRE> *
 */
object HexDumper {
    fun dump(data: ByteBuf): String {
        val current = data.readerIndex()
        data.readerIndex(0)
        val inst = Instance(current, data.readableBytes())
        data.forEachByte { b: Byte ->
            inst.add(b)
            true
        }
        data.readerIndex(current)
        return inst.finish()
    }

    @JvmOverloads
    fun dump(data: ByteArray, marker: Int = -1): String {
        val inst = Instance(marker, data.size)
        for (x in data.indices) inst.add(data[x])
        return inst.finish()
    }

    private class Instance(private val marked: Int, size: Int) {
        private val buf: StringBuilder
        private val ascii = CharArray(16)
        private var index = 0

        init {
            val lines = ((size + 15) / 16)
            this.buf = StringBuilder(
                (size * 3) //Hex
                        + size // ASCII
                        + (lines * 2) // \t and \n per line
                        + (if (marked == -1) 0 else lines)
            ) // ' ' or < at the start of each line

            for (x in ascii.indices) ascii[x] = ' '
        }

        fun add(data: Byte) {
            if (index == 0 && marked != -1) buf.append(if (index == marked) '<' else ' ')

            if (index != 0 && index % 16 == 0) {
                buf.append('\t')
                for (x in 0..15) {
                    buf.append(ascii[x])
                    ascii[x] = ' '
                }
                buf.append('\n')
                if (marked != -1) buf.append(if (index == marked) '<' else ' ')
            }
            ascii[index % 16] = if (data < ' '.code.toByte() || data > '~'.code.toByte()) '.' else Char(data.toUShort())
            buf.append(HEX[data.toInt() and 0xF0 shr 4])
            buf.append(HEX[data.toInt() and 0x0F])
            if (index + 1 == marked) buf.append(if (marked % 16 == 0) ' ' else '<')
            else buf.append(if (marked == index) '>' else ' ')

            index++
        }

        fun finish(): String {
            val padding = 16 - (index % 16)
            if (padding > 0) {
                for (x in 0 until padding * 3) buf.append(' ')
                buf.append('\t')
                buf.append(ascii)
            }
            buf.append('\n')
            buf.append("Length: ").append(index)
            if (marked != -1) buf.append(" Mark: ").append(marked)
            return buf.toString()
        }

        companion object {
            private const val HEX = "0123456789ABCDEF"
        }
    }
}

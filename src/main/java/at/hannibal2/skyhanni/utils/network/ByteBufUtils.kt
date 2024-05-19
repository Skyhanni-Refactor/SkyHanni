package at.hannibal2.skyhanni.utils.network

import io.netty.buffer.ByteBuf
import io.netty.handler.codec.DecoderException
import io.netty.util.CharsetUtil

typealias ByteBufReader<T> = ByteBuf.() -> T

object ByteBufUtils {

    private const val MAX_STRING_LENGTH = 32767

    fun ByteBuf.readVarInt(): Int {
        var i = 0
        var j = 0

        var b: Byte
        do {
            b = this.readByte()
            i = i or ((b.toInt() and 127) shl (j++ * 7))
            if (j > 5) {
                throw RuntimeException("VarInt too big")
            }
        } while ((b.toInt() and 128) == 128)

        return i
    }

    fun ByteBuf.readString(): String {
        val maxBytes: Int = MAX_STRING_LENGTH * 4
        val length: Int = this.readVarInt()
        if (length > maxBytes) {
            throw DecoderException("String too long (length $length, max $maxBytes)")
        }

        if (length < 0) {
            throw DecoderException("String length is less than zero!")
        }

        val readableBytes: Int = this.readableBytes()
        if (length > readableBytes) {
            throw DecoderException("Not enough readable bytes - bytes: $readableBytes length: $length")
        }

        val string: String = this.toString(this.readerIndex(), length, CharsetUtil.UTF_8)
        this.readerIndex(this.readerIndex() + length)
        if (string.length > MAX_STRING_LENGTH) {
            throw DecoderException("String length " + string.length + " is longer than maximum allowed " + MAX_STRING_LENGTH)
        }

        return string
    }

    fun <T> ByteBuf.readNullable(reader: ByteBufReader<T>): T? {
        return if (this.readBoolean()) reader() else null
    }
}

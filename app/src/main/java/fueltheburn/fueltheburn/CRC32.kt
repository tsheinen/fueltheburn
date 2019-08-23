package fueltheburn.fueltheburn

import java.lang.reflect.Field
import java.security.InvalidParameterException

class CRC32 {

    private val CRCPOLY = 79764919
    private val INITIAL_VALUE = -1
    private var mValue = -1

    fun getValue(): Int {
        return this.mValue
    }

    fun reset() {
        this.mValue = -1
    }

    @Throws(InvalidParameterException::class)
    fun update(array: ByteArray) {
        if (array.size % 4 != 0) {
            throw InvalidParameterException("Length of data must be a multiple of 4")
        }
        for (i in array.indices) {
            this.mValue = this.mValue xor (array[i xor 0x3].toInt() shl 24)
            for (j in 0..7) {
                if (Integer.MIN_VALUE and this.mValue != 0x0) {
                    this.mValue = 0x4C11DB7 xor (this.mValue shl 1)
                } else {
                    this.mValue = this.mValue shl 1
                }
            }
        }
    }
}
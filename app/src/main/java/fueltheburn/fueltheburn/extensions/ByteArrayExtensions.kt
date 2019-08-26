package fueltheburn.fueltheburn.extensions

fun ByteArray.toHexString() = "0x${joinToString(" 0x") { "%02x".format(it) }}"
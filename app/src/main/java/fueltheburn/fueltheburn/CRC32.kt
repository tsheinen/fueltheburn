package fueltheburn.fueltheburn

import java.util.zip.CRC32
import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.superclasses
class CRC32 : CRC32 {

    // you might ask, why on earth did you do this abomination?  now thats a really good question and I have no answer
    constructor() : super() {
        val crc: KMutableProperty<*>? = this::class.superclasses.first().memberProperties.filterIsInstance<KMutableProperty<*>>().find { it.name == "crc" }
        crc!!.setter.call(this, -1)
    }
}
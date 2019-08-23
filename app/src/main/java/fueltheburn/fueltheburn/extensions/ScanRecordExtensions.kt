package fueltheburn.fueltheburn.extensions

import android.bluetooth.le.ScanRecord
import android.os.Bundle
import android.os.ParcelUuid
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.*


fun ScanRecord.getAdvertisementData(): Bundle {
    val parsed: Bundle = Bundle()
    val buffer: ByteBuffer = ByteBuffer.wrap(this.bytes)
    while (buffer.remaining() > 0) {
        val size = buffer.get()
        val identifier = buffer.get()
        if( size == 0.toByte() || size > buffer.remaining() )
            break;
        val data = ByteArray(size - 1)
        buffer.get(data)

        when (identifier) {
            (-1).toByte() -> {
                parsed.putByteArray("COMPANYCODE", Arrays.copyOfRange(data, 0, 2))
                parsed.putByteArray("MANUDATA", Arrays.copyOfRange(data, 2, data.size))
            }
            (6).toByte(), (7).toByte() -> parsed.putParcelableArrayList("SERVICES", parseUuids(data))
            (9).toByte() -> parsed.putString("LOCALNAME", String(data))
            else -> {
            }
        }
    }
    return parsed
}


// Helper functions

private fun parseUuids(paramArrayOfByte: ByteArray): ArrayList<ParcelUuid> {
    val localByteBuffer = ByteBuffer.wrap(paramArrayOfByte)
    localByteBuffer.order(ByteOrder.LITTLE_ENDIAN)
    val localArrayList: ArrayList<ParcelUuid> = arrayListOf()
    while (localByteBuffer.remaining() >= 16) {
        val l = localByteBuffer.long
        localArrayList.add(ParcelUuid(UUID(localByteBuffer
                .long, l)))
    }
    return localArrayList
}
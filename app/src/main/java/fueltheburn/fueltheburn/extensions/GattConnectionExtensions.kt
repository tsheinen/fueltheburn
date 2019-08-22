package fueltheburn.fueltheburn.extensions

import android.bluetooth.BluetoothGattService
import android.util.Log
import com.beepiz.bluetooth.gattcoroutines.GattConnection
import fueltheburn.fueltheburn.CRC32
import kotlinx.coroutines.cancel
import kotlinx.coroutines.runBlocking
import java.lang.NullPointerException
import java.nio.ByteBuffer
import java.util.*

private val COPPERHEAD_COMMAND_UUID = UUID.fromString("c7d25540-31dd-11e2-81c1-0800200c9a66")
private val COPPERHEAD_RESPONSE_UUID = UUID.fromString("d36f33f0-31dd-11e2-81c1-0800200c9a66")
private val COPPERHEAD_SERVICE_UUID = UUID.fromString("83cdc410-31dd-11e2-81c1-0800200c9a66")

fun GattConnection.authenticate() {
    try {
        val copperheadService: BluetoothGattService = this.getService(COPPERHEAD_SERVICE_UUID)!!
        val responseChannel = copperheadService.getCharacteristic(COPPERHEAD_RESPONSE_UUID)

        runBlocking {
            try {
                val auth: ByteArray = byteArrayOf(0x90.toByte(), 0x01.toByte(), 0x01.toByte(), *ByteArray(16) { 0x00.toByte() })
                this@authenticate.connect()
                this@authenticate.discoverServices()
                val nonceCommand = copperheadService.getCharacteristic(COPPERHEAD_COMMAND_UUID)
                nonceCommand.value = auth
                this@authenticate.writeCharacteristic(nonceCommand)
                val responseCharacteristic = this@authenticate.readCharacteristic(responseChannel)
                val responseBytes: ByteArray = responseCharacteristic.value
                val responseConstantBytes = byteArrayOf(0xc0.toByte(), 0x11.toByte(), 0x41.toByte()).toList()

                // sanity check on the response
                if (responseBytes.take(3) != responseConstantBytes) {
                    cancel()
                }
                val nonce = responseBytes.takeLast(16).toByteArray()

                val auth_token = ByteArray(16) { 0xFF.toByte() }

                val crc: CRC32 = CRC32()
                crc.update(nonce)
                crc.update(auth_token)

                val challengeResponse: Short = (0xFFFF and crc.getValue().toInt() xor (0xFFFF and crc.getValue().ushr(16).toInt())) as Short;
                val challengeResponseBuffer: ByteBuffer = ByteBuffer.allocate(18)
                challengeResponseBuffer.put(byteArrayOf(0xB0.toByte(), 0x03.toByte(), 0x02.toByte()))
                challengeResponseBuffer.putShort(challengeResponse)
                val authCommand = copperheadService.getCharacteristic(COPPERHEAD_COMMAND_UUID)
                authCommand.value = challengeResponseBuffer.array()
                this@authenticate.writeCharacteristic(authCommand)
            } finally {
                this@authenticate.disconnect()
            }
        }
    } catch (e: NullPointerException) {
        Log.e("FuelTheBurn", "Some copperhead service doesn't exist, shits fucked")
    }
}
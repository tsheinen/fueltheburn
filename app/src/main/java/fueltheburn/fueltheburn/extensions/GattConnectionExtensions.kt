package fueltheburn.fueltheburn.extensions

import android.bluetooth.BluetoothGattService
import android.util.Log
import com.beepiz.blegattcoroutines.genericaccess.GenericAccess
import com.beepiz.bluetooth.gattcoroutines.BGC
import com.beepiz.bluetooth.gattcoroutines.GattConnection
import fueltheburn.fueltheburn.CRC32
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.lang.Exception
import java.lang.NullPointerException
import java.nio.ByteBuffer
import java.util.*

private val COPPERHEAD_COMMAND_UUID = UUID.fromString("c7d25540-31dd-11e2-81c1-0800200c9a66")
private val COPPERHEAD_RESPONSE_UUID = UUID.fromString("d36f33f0-31dd-11e2-81c1-0800200c9a66")
private val COPPERHEAD_SERVICE_UUID = UUID.fromString("83cdc410-31dd-11e2-81c1-0800200c9a66")

fun GattConnection.authenticate() {
    try {

        GlobalScope.launch {
            try {
                with(GenericAccess) {
                    this@authenticate.connect()
                    val t = this@authenticate.discoverServices()


                    val copperheadService: BluetoothGattService = this@authenticate.getService(COPPERHEAD_SERVICE_UUID)!!
                    val responseCharacteristic = copperheadService.getCharacteristic(COPPERHEAD_RESPONSE_UUID)
                    val responseChannel = this@authenticate.openNotificationSubscription(responseCharacteristic)
                    this@authenticate.setCharacteristicNotificationsEnabledOnRemoteDevice(responseCharacteristic, true)

                    val auth: ByteArray = byteArrayOf(0x90.toByte(), 0x01.toByte(), 0x01.toByte(), *ByteArray(16) { 0x00.toByte() })

                    val nonceCommand = copperheadService.getCharacteristic(COPPERHEAD_COMMAND_UUID)
                    nonceCommand.value = auth
                    this@authenticate.writeCharacteristic(nonceCommand)
                    val nonceResponseCharacteristic = responseChannel.receive()
                    val nonceResponseBytes: ByteArray = nonceResponseCharacteristic.value
                    val nonceResponseConstantBytes = byteArrayOf(0xc0.toByte(), 0x11.toByte(), 0x41.toByte()).toList()
                    println(nonceResponseBytes.contentToString())

                    // sanity check on the response
                    if (nonceResponseBytes.take(3) != nonceResponseConstantBytes) {
                        cancel()
                    }
                    val nonce = nonceResponseBytes.takeLast(16).toByteArray()

                    val auth_token = ByteArray(16) { 0xFF.toByte() }

                    val crc: CRC32 = CRC32()
                    crc.update(nonce)
                    crc.update(auth_token)

                    val challengeResponse: Short = (0xFFFF and crc.getValue().toInt() xor (0xFFFF and crc.getValue().ushr(16).toInt())).toShort();
                    val challengeResponseBuffer: ByteBuffer = ByteBuffer.allocate(18)
                    challengeResponseBuffer.put(byteArrayOf(0xB0.toByte(), 0x03.toByte(), 0x02.toByte()))
                    challengeResponseBuffer.putShort(challengeResponse)
                    val authCommand = copperheadService.getCharacteristic(COPPERHEAD_COMMAND_UUID)
                    authCommand.value = challengeResponseBuffer.array()


                    // THIS THROWS
                    println(responseChannel.receive().value.contentToString())
                }
            } catch (e: Exception) {
                println(e.message)
            } finally {
                this@authenticate.close()
            }
        }
    } catch (e: NullPointerException) {
        Log.e("FuelTheBurn", "Some copperhead service doesn't exist, shits fucked")
    }
}
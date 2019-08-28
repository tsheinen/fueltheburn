package fueltheburn.fueltheburn.extensions

import java.util.*

private val COPPERHEAD_COMMAND_UUID = UUID.fromString("c7d25540-31dd-11e2-81c1-0800200c9a66")
private val COPPERHEAD_RESPONSE_UUID = UUID.fromString("d36f33f0-31dd-11e2-81c1-0800200c9a66")
private val COPPERHEAD_SERVICE_UUID = UUID.fromString("83cdc410-31dd-11e2-81c1-0800200c9a66")

//suspend fun GattConnection.sendByteArray(serviceUUID: UUID, characteristicUUID: UUID, data: ByteArray): ByteArray {
//    println("Sending byte array ${data.toHexString()}")
//    val service = this.getService(serviceUUID)!!
//    val responseCharacteristic = service.getCharacteristic(COPPERHEAD_RESPONSE_UUID)
//    val responseChannel = this.openNotificationSubscription(responseCharacteristic)
//    val characteristic: BluetoothGattCharacteristic = service.getCharacteristic(characteristicUUID)
//    characteristic.value = data
//    this.writeCharacteristic(characteristic)
//    println("Sent byte array ${data.toHexString()}")
//    sleep(3000)
//    println("orange")
//    val response = responseChannel.receive().value
//    println("banana")
//    responseChannel.cancel()
//    return response
//}
//
//fun GattConnection.authenticate() {
//    try {
//        GlobalScope.launch {
//            try {
//                with(GenericAccess) {
//                    this@authenticate.connect()
//                    val t = this@authenticate.discoverServices()
//
//                    val connection = this@authenticate
//                    val copperheadService: BluetoothGattService = this@authenticate.getService(COPPERHEAD_SERVICE_UUID)!!
//                    val responseCharacteristic = copperheadService.getCharacteristic(COPPERHEAD_RESPONSE_UUID)
//                    this@authenticate.setCharacteristicNotificationsEnabledOnRemoteDevice(responseCharacteristic, true)
//
//
//                    val auth: ByteArray = byteArrayOf(0x90.toByte(), 0x01.toByte(), 0x01.toByte(), *ByteArray(16) { 0x00.toByte() })
//
//                    val nonceResponseBytes = this@authenticate.sendByteArray(COPPERHEAD_SERVICE_UUID, COPPERHEAD_COMMAND_UUID, auth)
//                    println(nonceResponseBytes.toHexString())
//                    val nonce = nonceResponseBytes.takeLast(16).toByteArray()
//
//                    val auth_token = ByteArray(16) { 0xFF.toByte() }
//
//                    val crc: CRC32 = CRC32()
//                    crc.update(nonce)
//                    crc.update(auth_token)
//
//                    val challengeResponse: Short = (0xFFFF and crc.getValue().toInt() xor (0xFFFF and crc.getValue().ushr(16).toInt())).toShort();
//                    val challengeResponseBuffer: ByteBuffer = ByteBuffer.allocate(19)
//                    challengeResponseBuffer.put(byteArrayOf(0xB0.toByte(), 0x03.toByte(), 0x02.toByte()))
//                    challengeResponseBuffer.putShort(challengeResponse)
//                    val authResponseBytes = this@authenticate.sendByteArray(COPPERHEAD_SERVICE_UUID, COPPERHEAD_COMMAND_UUID, challengeResponseBuffer.array())
//                    println("Printing final auth response: ")
//                    println(authResponseBytes.toHexString())
//                    println("Printed final auth response: ")
//                }
//            } catch (e: Exception) {
//                println("catch")
//                println(e.message)
//            } finally {
//                println("finally")
//                this@authenticate.close()
//            }
//        }
//    } catch (e: NullPointerException) {
//        Log.e("FuelTheBurn", "Some copperhead service doesn't exist, shits fucked")
//    }
//
//
//}
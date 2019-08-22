package fueltheburn.fueltheburn

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGattService
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.os.Bundle
import android.util.Log
import com.beepiz.bluetooth.gattcoroutines.GattConnection
import fueltheburn.fueltheburn.extensions.getAdvertisementData
import kotlinx.coroutines.cancel
import kotlinx.coroutines.runBlocking
import java.lang.NullPointerException
import java.util.*

object LeScanCallback : ScanCallback() {

    private val COPPERHEAD_COMMAND_UUID = UUID.fromString("c7d25540-31dd-11e2-81c1-0800200c9a66")
    private val COPPERHEAD_RESPONSE_UUID = UUID.fromString("d36f33f0-31dd-11e2-81c1-0800200c9a66")
    private val COPPERHEAD_SERVICE_UUID = UUID.fromString("83cdc410-31dd-11e2-81c1-0800200c9a66")
    private val NIKE_COMPANY_IDENTIFIER = byteArrayOf(0, 120) // 0x0078
    private const val LOG_TAG = "FuelTheBurn"

    override fun onScanResult(callbackType: Int, result: ScanResult) {

        val advertisementData: Bundle = result.scanRecord.getAdvertisementData()
        val companyCode: ByteArray = advertisementData.getByteArray("COMPANYCODE") ?: byteArrayOf(0, 0)
        if (companyCode contentEquals NIKE_COMPANY_IDENTIFIER) {
            val nonce: ByteArray = authenticate(result.device)

        }

    }

    private fun authenticate(device: BluetoothDevice): ByteArray {
        try {
            val deviceConnection: GattConnection = GattConnection(device)
            val copperheadService: BluetoothGattService = deviceConnection.getService(COPPERHEAD_SERVICE_UUID)!!
            val responseChannel = copperheadService.getCharacteristic(COPPERHEAD_RESPONSE_UUID)
            var nonce = byteArrayOf()

            runBlocking {
                try {
                    val auth: ByteArray = byteArrayOf(0x90.toByte(), 0x01.toByte(), 0x01.toByte(), *ByteArray(16) { 0x00.toByte() })
                    deviceConnection.connect()
                    deviceConnection.discoverServices()
                    val commandChannel = copperheadService.getCharacteristic(COPPERHEAD_COMMAND_UUID)
                    commandChannel.value = auth
                    deviceConnection.writeCharacteristic(commandChannel)
                    val responseCharacteristic = deviceConnection.readCharacteristic(responseChannel)
                    val responseBytes: ByteArray = responseCharacteristic.value
                    val responseConstantBytes = byteArrayOf(0xc0.toByte(), 0x11.toByte(), 0x41.toByte()).toList()

                    // sanity check on the response
                    if (responseBytes.take(3) != responseConstantBytes) {
                        cancel()
                    }
                    nonce = responseBytes.takeLast(16).toByteArray()
                } finally {
                    deviceConnection.close()
                }
            }
        } catch (e: NullPointerException) {
            Log.e(LOG_TAG, "Some copperhead service doesn't exist, shits fucked")
            return byteArrayOf()
        }
        return byteArrayOf()
    }
}

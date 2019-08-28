package fueltheburn.fueltheburn

import android.bluetooth.*
import android.bluetooth.BluetoothAdapter.STATE_CONNECTED
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.os.Bundle
import android.util.Log
import fueltheburn.fueltheburn.extensions.getAdvertisementData
import fueltheburn.fueltheburn.extensions.toHexString
import java.util.*


object LeScanCallback : ScanCallback() {

    private val TAG = "fueltheburn"
    private val COPPERHEAD_COMMAND_UUID = UUID.fromString("c7d25540-31dd-11e2-81c1-0800200c9a66")
    private val COPPERHEAD_RESPONSE_UUID = UUID.fromString("d36f33f0-31dd-11e2-81c1-0800200c9a66")
    private val COPPERHEAD_SERVICE_UUID = UUID.fromString("83cdc410-31dd-11e2-81c1-0800200c9a66")
    private val CHARACTERISTIC_UPDATE_NOTIFICATION_DESCRIPTOR_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")

    private val NIKE_COMPANY_IDENTIFIER = byteArrayOf(0, 120) // 0x0078
    private val connectedDevices: MutableList<String> = mutableListOf()

    override fun onScanResult(callbackType: Int, result: ScanResult) {

        val advertisementData: Bundle = result.scanRecord.getAdvertisementData()
        val companyCode: ByteArray = advertisementData.getByteArray("COMPANYCODE") ?: byteArrayOf(0, 0)
        if (companyCode contentEquals NIKE_COMPANY_IDENTIFIER && !connectedDevices.contains(result.device.address)) {
            connectedDevices.add(result.device.address)
            var bluetoothGatt: BluetoothGatt? = null
            bluetoothGatt = result.device.connectGatt(MainActivity.context, false, object : BluetoothGattCallback() {
                override fun onConnectionStateChange(
                        gatt: BluetoothGatt,
                        status: Int,
                        newState: Int
                ) {
                    when (newState) {
                        BluetoothProfile.STATE_CONNECTED -> {
                            Log.i(TAG, "Connected to GATT server.")
                            Log.i(TAG, "Attempting to start service discovery: " +
                                    gatt?.discoverServices())

                        }
                        BluetoothProfile.STATE_DISCONNECTED -> {
                            Log.i(TAG, "Disconnected from GATT server.")
                        }
                    }
                }

                override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
                    val service = gatt?.getService(COPPERHEAD_SERVICE_UUID)
                    val cmd = service?.getCharacteristic(COPPERHEAD_COMMAND_UUID)
                    val resp = service?.getCharacteristic(COPPERHEAD_RESPONSE_UUID)
                    gatt?.setCharacteristicNotification(resp, true)
                    val descriptor = resp?.getDescriptor(CHARACTERISTIC_UPDATE_NOTIFICATION_DESCRIPTOR_UUID);
                    descriptor?.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
                    gatt?.writeDescriptor(descriptor);
                    cmd?.value = byteArrayOf(0x90.toByte(), 0x01.toByte(), 0x01.toByte(), *ByteArray(16) { 0x00.toByte() })
                    gatt?.writeCharacteristic(cmd)
                    println("written characteristics")
                }

                override fun onCharacteristicChanged(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?) {
                    println("response: ${characteristic?.value?.toHexString() ?: "no response"}")
                }

            })


        }

    }


}

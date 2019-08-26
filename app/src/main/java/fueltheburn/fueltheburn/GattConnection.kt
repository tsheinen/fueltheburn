package fueltheburn.fueltheburn

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import fueltheburn.fueltheburn.gatt.GattCallback

/**
 * Blocking wrappers around GATT connection methods
 */
class GattConnection(device: BluetoothDevice) {

    private var gatt: BluetoothGatt? = null

    init {
        gatt = device.connectGatt(MainActivity.context, false, GattCallback)
    }

}
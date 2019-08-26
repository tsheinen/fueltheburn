package fueltheburn.fueltheburn.gatt

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor

object GattCallback : BluetoothGattCallback() {
    override fun onPhyUpdate(gatt: BluetoothGatt, txPhy: Int, rxPhy: Int, status: Int) {

    }

    override fun onPhyRead(gatt: BluetoothGatt, txPhy: Int, rxPhy: Int, status: Int) {

    }

    override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {

    }

    override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {

    }

    override fun onCharacteristicRead(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int) {

    }

    override fun onCharacteristicWrite(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int) {

    }

    override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {

    }

    override fun onDescriptorRead(gatt: BluetoothGatt, descriptor: BluetoothGattDescriptor, status: Int) {

    }

    override fun onDescriptorWrite(gatt: BluetoothGatt, descriptor: BluetoothGattDescriptor, status: Int) {

    }

    override fun onReliableWriteCompleted(gatt: BluetoothGatt, status: Int) {

    }

    override fun onReadRemoteRssi(gatt: BluetoothGatt, rssi: Int, status: Int) {

    }

    override fun onMtuChanged(gatt: BluetoothGatt, mtu: Int, status: Int) {

    }

}
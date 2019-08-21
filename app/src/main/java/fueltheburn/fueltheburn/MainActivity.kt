package fueltheburn.fueltheburn

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import java.nio.ByteBuffer
import java.util.*

class MainActivity : AppCompatActivity() {

    private val CLIENT_CHARACTERISTIC_CONFIG_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")

    private val COPPERHEAD_CMD_UUID = UUID.fromString("c7d25540-31dd-11e2-81c1-0800200c9a66")
    private val COPPERHEAD_RSP_UUID = UUID.fromString("d36f33f0-31dd-11e2-81c1-0800200c9a66")
    private val COPPERHEAD_SERVICE_UUID = UUID.fromString("83cdc410-31dd-11e2-81c1-0800200c9a66")

    private val bluetoothAdapter: BluetoothAdapter? by lazy(LazyThreadSafetyMode.NONE) {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    private val BluetoothAdapter.isDisabled: Boolean
        get() = !isEnabled

    override fun onCreate(savedInstanceState: Bundle?) {
        bluetoothAdapter
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        bluetoothAdapter?.takeIf { it.isDisabled }?.apply {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, 1)
        }


    }
}

object Callback : BluetoothAdapter.LeScanCallback {

    fun parseAdvertisementData(scanRecord: ByteArray) {
        val bundle: Bundle = Bundle()
        val buf: ByteBuffer = ByteBuffer.wrap(scanRecord)
        while(buf.remaining() > 0) {

        }
    }

    override fun onLeScan(device: BluetoothDevice?, rssi: Int, scanrecord: ByteArray?) {

    }
}

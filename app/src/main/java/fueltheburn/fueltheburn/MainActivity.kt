package fueltheburn.fueltheburn

import android.Manifest
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import java.nio.ByteBuffer
import java.util.*
import android.widget.Toast
import android.Manifest.permission
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.bluetooth.*
import android.support.v4.app.ActivityCompat
import android.content.pm.PackageManager
import android.os.Build
import android.support.annotation.RequiresApi
import android.support.v4.content.ContextCompat
import android.os.ParcelUuid
import android.util.Log
import com.beepiz.bluetooth.gattcoroutines.GattConnection
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.nio.ByteOrder
import java.nio.ByteOrder.LITTLE_ENDIAN
import java.util.logging.Logger


class MainActivity : AppCompatActivity() {

    private val CLIENT_CHARACTERISTIC_CONFIG_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")


    private val bluetoothAdapter: BluetoothAdapter? by lazy(LazyThreadSafetyMode.NONE) {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    private val BluetoothAdapter.isDisabled: Boolean
        get() = !isEnabled

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {

        val permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                Toast.makeText(this, "The permission to get BLE location data is required", Toast.LENGTH_SHORT).show()
            } else {
                requestPermissions(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION), 1)
            }
        } else {
            Toast.makeText(this, "Location permissions already granted", Toast.LENGTH_SHORT).show()
        }

        bluetoothAdapter
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        bluetoothAdapter?.takeIf { it.isDisabled }?.apply {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, 1)
        }

        bluetoothAdapter?.startLeScan(Callback)

    }
}

object Callback : BluetoothAdapter.LeScanCallback {

    private val COPPERHEAD_CMD_UUID = UUID.fromString("c7d25540-31dd-11e2-81c1-0800200c9a66")
    private val COPPERHEAD_RSP_UUID = UUID.fromString("d36f33f0-31dd-11e2-81c1-0800200c9a66")
    private val COPPERHEAD_SERVICE_UUID = UUID.fromString("83cdc410-31dd-11e2-81c1-0800200c9a66")

    fun parseAdvertisementData(scanRecord: ByteArray): Bundle {
        val parsed: Bundle = Bundle()
        val buffer: ByteBuffer = ByteBuffer.wrap(scanRecord)
        while (true) {
            val i = if (buffer.remaining() > 0) buffer.get() else 0


            if (i == (0).toByte() || i > buffer.remaining())
                break

            val j = buffer.get()
            val arrayOfByte = ByteArray(i - 1)
            buffer.get(arrayOfByte)

            when (j) {
                (-1).toByte() -> {
                    parsed.putByteArray("COMPANYCODE", Arrays.copyOfRange(arrayOfByte, 0, 2))
                    parsed.putByteArray("MANUDATA", Arrays.copyOfRange(arrayOfByte, 2, arrayOfByte.size))
                }
                (6).toByte(), (7).toByte() -> parsed.putParcelableArrayList("SERVICES", parseUuids(arrayOfByte))
                (9).toByte() -> parsed.putString("LOCALNAME", String(arrayOfByte))
                else -> {
                }
            }
        }
        return parsed
    }

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

    override fun onLeScan(device: BluetoothDevice?, rssi: Int, scanrecord: ByteArray?) {

        val companycode: ByteArray = (parseAdvertisementData(scanrecord!!).get("COMPANYCODE")
                ?: byteArrayOf(0, 0)) as ByteArray
        if (companycode[1] == 120.toByte()) {
            GlobalScope.launch {
                val deviceConnection = GattConnection(device!!)
                val _CopperheadService = deviceConnection.getService(COPPERHEAD_SERVICE_UUID)
                if (_CopperheadService == null) {
                    Log.e("fueltheburn", "No Copperhead service found.");
                    cancel()
                }
                val _CommandChannel = _CopperheadService!!.getCharacteristic(COPPERHEAD_CMD_UUID);
                val _ResponseChannel = _CopperheadService.getCharacteristic(COPPERHEAD_RSP_UUID);
                try {
                    deviceConnection.connect()
                    deviceConnection.discoverServices()
                    _CommandChannel.value = byteArrayOf()
                    deviceConnection.writeCharacteristic(_CommandChannel)

                } finally {

                }
            }
        }
    }
}

suspend fun BluetoothDevice.logGattServices(tag: String = "BleGattCoroutines") {
    val deviceConnection = GattConnection(bluetoothDevice = this@logGattServices)
    try {
        deviceConnection.connect() // Suspends until connection is established
        val gattServices = deviceConnection.discoverServices() // Suspends until completed
        gattServices.forEach {
            it.characteristics.forEach {
                try {
                    deviceConnection.readCharacteristic(it) // Suspends until characteristic is read
                } catch (e: Exception) {
                    Log.e(tag, "Couldn't read characteristic with uuid: ${it.uuid}", e)
                }
            }

            Log.d(tag, it.type.toString())
        }
    } finally {
        deviceConnection.close() // Close when no longer used. Also triggers disconnect by default.
    }
}
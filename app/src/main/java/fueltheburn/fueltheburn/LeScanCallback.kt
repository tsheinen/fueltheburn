package fueltheburn.fueltheburn

import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.os.Bundle
import com.beepiz.bluetooth.gattcoroutines.GattConnection
import fueltheburn.fueltheburn.extensions.authenticate
import fueltheburn.fueltheburn.extensions.getAdvertisementData



object LeScanCallback : ScanCallback() {


    private val NIKE_COMPANY_IDENTIFIER = byteArrayOf(0, 120) // 0x0078

    override fun onScanResult(callbackType: Int, result: ScanResult) {

        val advertisementData: Bundle = result.scanRecord.getAdvertisementData()
        val companyCode: ByteArray = advertisementData.getByteArray("COMPANYCODE") ?: byteArrayOf(0, 0)
        if (companyCode contentEquals NIKE_COMPANY_IDENTIFIER) {
            val deviceConnection: GattConnection = GattConnection(result.device)
            deviceConnection.authenticate()


        }

    }


}

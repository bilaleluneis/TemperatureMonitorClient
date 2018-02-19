package com.apps.bilaleluneis.temperaturemonitorclient

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Bundle
import android.util.Log

/**
 * @author Bilal El Uneis
 * @since Feb 2018
 * bilaleluneis@gmail.com
 * a simple BlueTooth client Android app
 * to work with IottemperatureMonitor Android things application
 * @see <a href="https://github.com/bilaleluneis/IoTtempratureMonitor">IoTtempratureMonitor</a>
 */

class TemperatureMonitorClientActivity : Activity() {

    private val logTag = "TemperatureMonitorClientActivity"
    private val blueToothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_temperature_monitor_client)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == RESULT_OK){
            Log.d(logTag, "BlueTooth is now turned on !")
        }else if(resultCode == RESULT_CANCELED){
            Log.d(logTag, "BlueTooth failed to turn on !")
        }
    }

    private fun hasBlueToothSupport() : Boolean {

        blueToothAdapter?.let{
            Log.d(logTag, "BlueTooth is supported on this Android Device !")
            return true
        }

        Log.d(logTag, "BlueTooth is NOT supported on this Android Device !")
        return false

    }

    private fun enableBluetooth() {
        if (hasBlueToothSupport()) {
            Log.d(logTag, "Attempt to turn on BlueTooth on Device !")
            Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE).also{
                startActivityForResult(it, 10)
            }
        }
    }
}

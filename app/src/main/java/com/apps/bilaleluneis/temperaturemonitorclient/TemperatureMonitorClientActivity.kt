package com.apps.bilaleluneis.temperaturemonitorclient

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.content.IntentFilter
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

    private val logTag = "TempMonitorActivity"
    private val blueToothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private val blueToothBroadcastReceiver = BlueToothBroadcastReceiver()
    private val currentlyPairedBlueToothDevices by lazy {getPairedBlueToothDevices()}

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_temperature_monitor_client)

        if(enableBluetooth()) {
            IntentFilter().apply {
                addAction(BluetoothDevice.ACTION_FOUND)
                addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
                addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
            }.also { registerReceiver(blueToothBroadcastReceiver, it) }
            blueToothAdapter?.startDiscovery()
        }

    }

    override fun onDestroy() {

        super.onDestroy()
        unregisterReceiver(blueToothBroadcastReceiver)
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

    private fun enableBluetooth() : Boolean {

        blueToothAdapter?.let {
            Log.d(logTag, "Attempt to turn on BlueTooth on Device !")
            Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE).apply {
                startActivityForResult(this, 10)//TODO: create a constant
            }

            return blueToothAdapter.isEnabled
        }

        return false
    }

    private fun getPairedBlueToothDevices() : Set<BluetoothDevice> {

        blueToothAdapter?.let{
            for(blueToothDevice in blueToothAdapter.bondedDevices){
                Log.d(logTag, "Found Device with name ${blueToothDevice.name}")
                Log.d(logTag,"${blueToothDevice.name} address is ${blueToothDevice.address}")
            }
        }

        return blueToothAdapter?.bondedDevices ?: emptySet()//emptySet()
    }

}

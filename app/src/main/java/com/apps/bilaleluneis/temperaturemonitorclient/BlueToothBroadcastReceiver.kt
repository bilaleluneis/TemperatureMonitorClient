package com.apps.bilaleluneis.temperaturemonitorclient

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * @author Bilal El Uneis
 * @since Feb 2018
 * bilaleluneis@gmail.com
 */

/**
 * This is very cool way to do a broadcast receiver in separate class while still
 * be able to expose the properties from activity.. basically the constructor takes
 * a function and calls it... so that function could come from another class instance
 * and will have access to that class instance properties without having to expose them
 * here.
 * see [TemperatureMonitorClientActivity.blueToothBroadcastReceiver]
 * see [TemperatureMonitorClientActivity.processBlueToothDeviceFoundIntent]
 * see [TemperatureMonitorClientActivity.onCreate] line 64 where receiver is registered
 */
class BlueToothBroadcastReceiver(private val intentProcessor: (intent: Intent) -> Unit) : BroadcastReceiver() {

    private val logTag = "BroadcastReceiver"

    override fun onReceive(context: Context?, intent: Intent?) {

        when(intent?.action){
            BluetoothAdapter.ACTION_DISCOVERY_STARTED -> Log.d(logTag,"Discovery Started !")
            BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> Log.d(logTag,"Discovery Finished !")
            BluetoothDevice.ACTION_FOUND -> intentProcessor(intent)
            else -> Log.d(logTag, "unknown action received !")
        }

    }

}

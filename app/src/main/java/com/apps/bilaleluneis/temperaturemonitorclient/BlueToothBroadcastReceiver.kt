package com.apps.bilaleluneis.temperaturemonitorclient

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

class BlueToothBroadcastReceiver : BroadcastReceiver() {

    private val logTag = "BroadcastReceiver"

    override fun onReceive(context: Context?, intent: Intent?) {

        when(intent?.action){

            BluetoothDevice.ACTION_FOUND -> processBlueToothDeviceFoundIntent(intent)
            else -> Log.d(logTag, "No action received !")
        }

    }

    private fun processBlueToothDeviceFoundIntent(intent: Intent){

        intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE).apply{
            Log.d(logTag, "Bluetooth name is: $name")
            Log.d(logTag, "Bluetooth address is $address")
        }

    }

}

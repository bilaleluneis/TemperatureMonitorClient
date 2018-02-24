package com.apps.bilaleluneis.temperaturemonitorclient

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import java.io.InputStream
import java.nio.charset.Charset
import java.util.*

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
    private val currentlyPairedBlueToothDevices by lazy {getPairedBlueToothDevices()}
    private val uuid = UUID.fromString("4e5d48e0-75df-11e3-981f-0800200c9a66")
    private var messageFromIotReader: InputStream? = null
    private var bluetoothSocket: BluetoothSocket? = null
    private var iotBluetoothDevice: BluetoothDevice? = null

    //TODO: there has to be better way to do this!!
    private val blueToothBroadcastReceiver = object: BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent?) {

            when(intent?.action){
                BluetoothAdapter.ACTION_DISCOVERY_STARTED -> Log.d(logTag,"Discovery Started !")
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> Log.d(logTag,"Discovery Finished !")
                BluetoothDevice.ACTION_FOUND -> processBlueToothDeviceFoundIntent(intent)
                else -> Log.d(logTag, "unknown action received !")
            }

        }
    }

    private fun processBlueToothDeviceFoundIntent(intent: Intent){

        Log.d(logTag, "Bluetooth device found !")
        intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE).apply{
            Log.d(logTag, "Bluetooth name is: $name")
            Log.d(logTag, "Bluetooth address is $address")
            if(name.equals("Temperature Monitor", true)){
                Log.d(logTag, "assigning $name as device to connect to!")
                iotBluetoothDevice = this
            }
        }

    }

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
            //TODO: need to clean this mess up!
            val launcher = launch{
                while(iotBluetoothDevice == null){
                    Log.d(logTag, "bluetooth still discovering !")
                    delay(3000L)
                }
                Log.d(logTag, "bluetooth was discovered: ${iotBluetoothDevice?.name}")
                bluetoothSocket = iotBluetoothDevice?.createRfcommSocketToServiceRecord(uuid)
                bluetoothSocket?.connect()
                Log.d(logTag, "bluetooth is now connected to temp sensor IoT !")
                if(bluetoothSocket?.isConnected!!){
                    Log.d(logTag, "bluetooth is connected check pass!")
                }
                messageFromIotReader = bluetoothSocket?.inputStream

                Log.d(logTag, "attempting to read from IoT output stream!")
                if (messageFromIotReader != null) {
                    var bytesReadCount = 0
                    do {
                        val bytes = ByteArray(1024)
                        bytesReadCount = messageFromIotReader?.read(bytes) ?: -1
                        val messageFromServer = bytes.copyOfRange(0, bytesReadCount - 1)
                        Log.d(logTag, "message read from IoT is ${messageFromServer.toString(Charset.defaultCharset())}")
                    }while(bytesReadCount > 0)
                }
                Log.d(logTag, "done pass to read from server!")

            }

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

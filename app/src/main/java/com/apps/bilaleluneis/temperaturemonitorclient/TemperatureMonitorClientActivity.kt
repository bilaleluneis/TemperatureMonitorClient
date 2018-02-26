package com.apps.bilaleluneis.temperaturemonitorclient

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
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
    private val uuid = UUID.fromString("4e5d48e0-75df-11e3-981f-0800200c9a66")
    private val serverBluetoothName = "Temperature Monitor IoT"
    private lateinit var display: TextView
    private val blueToothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private val blueToothBroadcastReceiver = BlueToothBroadcastReceiver(::assignBluetoothServer)
    private var iotBluetoothDevice: BluetoothDevice? = null
    private var bluetoothSocket: BluetoothSocket? = null


    private fun assignBluetoothServer(intent: Intent){

        Log.d(logTag, "Bluetooth device found !")
        intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE).apply{
            Log.d(logTag, "Bluetooth name is: $name")
            Log.d(logTag, "Bluetooth address is $address")
            if(name.equals(serverBluetoothName, true)){
                Log.d(logTag, "assigning $name as device to connect to!")
                iotBluetoothDevice = this
            }
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_temperature_monitor_client)
        display = findViewById(R.id.textView)

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
                if(bluetoothSocket?.isConnected!!){
                    bluetoothSocket?.close()
                }
                bluetoothSocket?.connect()
                Log.d(logTag, "bluetooth is now connected to temp sensor IoT !")
                val messageFromIotReader = bluetoothSocket?.inputStream
                Log.d(logTag, "attempting to read from IoT output stream!")
                if (messageFromIotReader != null) {
                    var bytesReadCount: Int
                    do {
                        delay(2000)
                        val bytes = ByteArray(1024)
                        bytesReadCount = messageFromIotReader.read(bytes)
                        bytes.copyOfRange(0, bytesReadCount).apply{
                            val messageFromServer = toString(Charset.defaultCharset())
                            Log.d(logTag, "message read from IoT is $messageFromServer")
                            val uiUpdateJob = launch(UI){display.text =  messageFromServer}
                            uiUpdateJob.join()
                        }
                    }while(bytesReadCount > 0)
                    messageFromIotReader?.close()
                }
                Log.d(logTag, "done pass to read from server!")

            }

        }

    }

    override fun onDestroy() {

        super.onDestroy()
        unregisterReceiver(blueToothBroadcastReceiver)
        bluetoothSocket?.close()
        blueToothAdapter?.disable()

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

        blueToothAdapter?.let{
            Log.d(logTag, "Attempt to turn on BlueTooth on Device !")
            Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE).apply {
                startActivityForResult(this, 10)//TODO: create a constant
            }

            return blueToothAdapter.isEnabled
        }

        return false
    }

}

package com.example.project

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID

class Pairing : AppCompatActivity() {

    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var arrayAdapter: ArrayAdapter<String>
    private val deviceList = mutableListOf<String>()
    private val discoveredDevices = mutableSetOf<String>() // 중복 방지를 위한 Set
    private var bluetoothSocket: BluetoothSocket? = null
    private val uuid: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")  // SPP UUID
    private var isCommunicating = false

    companion object {
        var selectedDeviceAddress: String? = null
    }

    private val bluetoothReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action: String = intent.action!!
            when (action) {
                BluetoothDevice.ACTION_FOUND -> {
                    val device: BluetoothDevice? =
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    device?.let {
                        val deviceName: String = if (ContextCompat.checkSelfPermission(
                                this@Pairing,
                                Manifest.permission.BLUETOOTH_CONNECT
                            ) == PackageManager.PERMISSION_GRANTED) {
                            it.name ?: "Unknown Device"
                        } else {
                            "Permission Required"
                        }
                        val deviceAddress = it.address // MAC address
                        val deviceInfo = "$deviceName - $deviceAddress"
                        if (discoveredDevices.add(deviceInfo)) {
                            deviceList.add(deviceInfo)
                            arrayAdapter.notifyDataSetChanged()
                            Log.d("Pairing", "Device found: $deviceInfo")
                        }
                    }
                }
                BluetoothDevice.ACTION_PAIRING_REQUEST -> {
                    val device: BluetoothDevice? =
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    device?.let {
                        try {
                            val method = it::class.java.getMethod("setPairingConfirmation", Boolean::class.javaPrimitiveType)
                            method.invoke(it, true)
                            Log.d("Pairing", "Pairing confirmed for device: ${it.address}")
                            connectToDevice(it)
                        } catch (e: Exception) {
                            Log.e("Pairing", "Error during pairing confirmation", e)
                        }
                    }
                }
                BluetoothDevice.ACTION_BOND_STATE_CHANGED -> {
                    val device: BluetoothDevice? =
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    device?.let {
                        when (it.bondState) {
                            BluetoothDevice.BOND_BONDED -> {
                                Log.d("Pairing", "Paired with device: ${it.address}")
                                Toast.makeText(this@Pairing, "Paired with device: ${it.address}", Toast.LENGTH_SHORT).show()
                                connectToDevice(it)
                            }
                            BluetoothDevice.BOND_NONE -> {
                                Log.d("Pairing", "Pairing failed or unpaired from device: ${it.address}")
                                Toast.makeText(this@Pairing, "Pairing failed or unpaired from device: ${it.address}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_pairing)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val searchBtn: Button = findViewById(R.id.searchBtn)
        val deviceListView: ListView = findViewById(R.id.deviceList)

        arrayAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, deviceList)
        deviceListView.adapter = arrayAdapter

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        if (bluetoothAdapter == null) {
            Log.e("Pairing", "Device doesn't support Bluetooth")
            Toast.makeText(this, "Device doesn't support Bluetooth", Toast.LENGTH_SHORT).show()
            return
        }

        val permissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val granted = permissions.entries.all { it.value }
            if (granted) {
                startDiscovery()
            } else {
                Log.e("Pairing", "Permissions denied")
                Toast.makeText(this, "Permissions denied. Please enable Bluetooth permissions.", Toast.LENGTH_SHORT).show()
            }
        }

        searchBtn.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN)
                != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                    this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                permissionLauncher.launch(arrayOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.BLUETOOTH_CONNECT))
            } else {
                startDiscovery()
            }
        }

        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        filter.addAction(BluetoothDevice.ACTION_PAIRING_REQUEST)
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED) // 추가된 부분
        registerReceiver(bluetoothReceiver, filter)

        deviceListView.setOnItemClickListener { _, _, position, _ ->
            val selectedDevice = deviceList[position]
            val deviceAddress = selectedDevice.split(" - ").last()
            selectedDeviceAddress = deviceAddress
            Toast.makeText(this, "SET : $deviceAddress", Toast.LENGTH_SHORT).show()
            attemptPairing(deviceAddress)
        }
    }

    private fun startDiscovery() {
        deviceList.clear()
        discoveredDevices.clear()
        arrayAdapter.notifyDataSetChanged()
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_SCAN
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e("Pairing", "BLUETOOTH_SCAN permission not granted")
            Toast.makeText(this, "BLUETOOTH_SCAN permission not granted", Toast.LENGTH_SHORT).show()
            return
        }
        Log.d("Pairing", "Starting discovery")
        bluetoothAdapter.startDiscovery()
    }

    private fun attemptPairing(deviceAddress: String) {
        val device = bluetoothAdapter.getRemoteDevice(deviceAddress)
        try {
            val method = device::class.java.getMethod("createBond")
            val result = method.invoke(device)
            Log.d("Pairing", "Pairing started: $result")
            Toast.makeText(this, "Pairing started with: $deviceAddress", Toast.LENGTH_SHORT).show()
            // 연결은 페어링 완료 후 ACTION_BOND_STATE_CHANGED에서 처리
        } catch (e: Exception) {
            Log.e("Pairing", "Pairing failed", e)
            Toast.makeText(this, "Pairing failed with: $deviceAddress", Toast.LENGTH_SHORT).show()
        }
    }

    private fun connectToDevice(device: BluetoothDevice) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            Log.e("Pairing", "BLUETOOTH_CONNECT permission not granted")
            Toast.makeText(this, "BLUETOOTH_CONNECT permission not granted", Toast.LENGTH_SHORT).show()
            return
        }

        Thread {
            bluetoothSocket = device.createRfcommSocketToServiceRecord(uuid)
            bluetoothAdapter.cancelDiscovery()
            try {
                bluetoothSocket?.connect()
                Log.d("Pairing", "Connection established with: ${device.address}")
                manageConnectedSocket(bluetoothSocket)
            } catch (e: IOException) {
                Log.e("Pairing", "Connection failed", e)
                runOnUiThread {
                    Toast.makeText(this, "Connection failed with: ${device.address}", Toast.LENGTH_SHORT).show()
                }
                try {
                    bluetoothSocket?.close()
                } catch (closeException: IOException) {
                    Log.e("Pairing", "Could not close the client socket", closeException)
                }
            }
        }.start()
    }

    private fun manageConnectedSocket(socket: BluetoothSocket?) {
        socket?.let {
            // You can start a thread to manage the connection and perform transmissions.
            val inputStream = it.inputStream
            val outputStream = it.outputStream

            // Example of reading and writing data
            val buffer = ByteArray(1024) // buffer store for the stream
            var numBytes: Int // bytes returned from read()

            while (true) {
                // Read from the InputStream
                try {
                    numBytes = inputStream.read(buffer)
                    // Send the obtained bytes to the UI activity.
                    val readMessage = String(buffer, 0, numBytes)
                    Log.d("Pairing", "Message received: $readMessage")
                } catch (e: IOException) {
                    Log.e("Pairing", "Input stream was disconnected", e)
                    break
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(bluetoothReceiver)
        bluetoothSocket?.close()
    }
}

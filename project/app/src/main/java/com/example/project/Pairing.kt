package com.example.project

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
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

class Pairing : AppCompatActivity() {

    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var arrayAdapter: ArrayAdapter<String>
    private val deviceList = mutableListOf<String>()
    private val discoveredDevices = mutableSetOf<String>()

    companion object {
        var selectedDeviceAddress: String? = null
    }

    private val bluetoothReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                BluetoothDevice.ACTION_FOUND -> {
                    val device: BluetoothDevice? = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    device?.let {
                        if (ContextCompat.checkSelfPermission(
                                this@Pairing,
                                Manifest.permission.BLUETOOTH_CONNECT
                            ) == PackageManager.PERMISSION_GRANTED) {
                            val deviceName = it.name
                            if (deviceName != null) {
                                val deviceAddress = it.address
                                val deviceInfo = "$deviceName - $deviceAddress"
                                if (discoveredDevices.add(deviceInfo)) {
                                    deviceList.add(deviceInfo)
                                    arrayAdapter.notifyDataSetChanged()
                                    Log.d("Pairing", "Device found: $deviceInfo")
                                }
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

        deviceListView.setOnItemClickListener { _, _, position, _ ->
            val selectedDevice = deviceList[position]
            val deviceAddress = selectedDevice.split(" - ").last()
            val deviceName = selectedDevice.split(" - ").first()
            selectedDeviceAddress = deviceAddress
            Toast.makeText(this, "Connecting to: $deviceName", Toast.LENGTH_SHORT).show()
            connectToDevice(deviceAddress)
        }

        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        registerReceiver(bluetoothReceiver, filter)
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

    private fun connectToDevice(deviceAddress: String) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            Log.e("Pairing", "BLUETOOTH_CONNECT permission not granted")
            Toast.makeText(this, "BLUETOOTH_CONNECT permission not granted", Toast.LENGTH_SHORT).show()
            return
        }

        val device = bluetoothAdapter.getRemoteDevice(deviceAddress)
        val deviceName = device.name ?: "Unknown Device"
        Thread {
            val socket = BluetoothManager.connectToDevice(this, device)
            if (socket != null) {
                BluetoothManager.setBluetoothSocket(socket)
                runOnUiThread {
                    Toast.makeText(this, "Connected to: $deviceName", Toast.LENGTH_SHORT).show()
                }
            } else {
                runOnUiThread {
                    Toast.makeText(this, "Connection failed with: $deviceName", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(bluetoothReceiver)
    }
}

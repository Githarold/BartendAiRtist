package com.example.project

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.content.ContextCompat
import java.io.IOException
import java.util.UUID

object BluetoothManager {
    private val SERVER_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    private var bluetoothSocket: BluetoothSocket? = null

    @Synchronized
    fun connectToDevice(context: Context, device: BluetoothDevice): BluetoothSocket? {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            Log.e("BluetoothManager", "BLUETOOTH_CONNECT permission not granted")
            return null
        }

        if (bluetoothSocket != null && bluetoothSocket!!.isConnected) {
            return bluetoothSocket
        }

        try {
            bluetoothSocket = device.createRfcommSocketToServiceRecord(SERVER_UUID)
            bluetoothSocket?.connect()
        } catch (e: IOException) {
            Log.e("BluetoothManager", "Connection failed", e)
            try {
                bluetoothSocket?.close()
            } catch (closeException: IOException) {
                Log.e("BluetoothManager", "Could not close the client socket", closeException)
            }
            bluetoothSocket = null
        }

        return bluetoothSocket
    }

    @Synchronized
    fun setBluetoothSocket(socket: BluetoothSocket?) {
        bluetoothSocket = socket
    }

    @Synchronized
    fun getBluetoothSocket(): BluetoothSocket? {
        return bluetoothSocket
    }

    @Synchronized
    fun closeConnection() {
        try {
            bluetoothSocket?.close()
        } catch (e: IOException) {
            Log.e("BluetoothManager", "Could not close the client socket", e)
        } finally {
            bluetoothSocket = null
        }
    }
}

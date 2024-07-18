package com.example.blutooth_def.bluetooth

import android.bluetooth.BluetoothAdapter

class BluetoothController(private val adapter: BluetoothAdapter)  {
    private var connectThread: ConnectThread? = null
    fun connect(mac: String) {
        if (adapter.isEnabled && mac.isNotEmpty()) {
            val device = adapter.getRemoteDevice(mac)
            connectThread = ConnectThread(device)
            connectThread?.start()
        }
    }
}
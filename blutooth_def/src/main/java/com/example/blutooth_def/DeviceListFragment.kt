package com.example.blutooth_def

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.R
import androidx.core.graphics.drawable.DrawableCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.blutooth_def.databinding.FragmentListBinding
import com.google.android.material.snackbar.Snackbar


class DeviceListFragment : Fragment(), ItemAdapter.Listener {
    private var preferences: SharedPreferences? = null
    private lateinit var itemAdapter: ItemAdapter
    private lateinit var binding: FragmentListBinding
    private var bthAdapter: BluetoothAdapter? = null
    private lateinit var bthLauncher: ActivityResultLauncher<Intent>
    private lateinit var pLauncher: ActivityResultLauncher<Array<String>>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        preferences =
            activity?.getSharedPreferences(BluetoothConstants.PREFERENCES, Context.MODE_PRIVATE)
        binding.imageBTNOn.setOnClickListener {
            bthLauncher.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
        }
        checkPermission()
        initRcView()
        registerBthLauncher()
        initAdapter()
        bluetoothState()
    }

    private fun initRcView() = with(binding) {
        recyclerViewConnected.layoutManager = LinearLayoutManager(requireContext())
        itemAdapter = ItemAdapter(this@DeviceListFragment)
        recyclerViewConnected.adapter = itemAdapter

    }

    private fun getParedDevices() {
        try {
            val list = ArrayList<ListItem>()
            for (i in 1..5) {
                list.add(
                    ListItem("Name $i", "address $i", false)
                )
            }
            val deviceList = bthAdapter?.bondedDevices as Set<BluetoothDevice>
            deviceList.forEach {
                list.add(
                    ListItem(
                        it.name,
                        it.address,
                        preferences?.getString(BluetoothConstants.MAC, "") == it.address
                    )
                )
            }
            binding.emptyConnected.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
            itemAdapter.submitList(list)
        } catch (e: SecurityException) {

        }
    }

    private fun bluetoothState() {
        if (bthAdapter?.isEnabled == true) {
            changeButtonColor(binding.imageBTNOn, Color.GREEN)
        }
    }

    private fun initAdapter() {
        val bthManager = activity?.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bthAdapter = bthManager.adapter
    }

    private fun registerBthLauncher() {
        bthLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            if (it.resultCode == Activity.RESULT_OK) {
                changeButtonColor(binding.imageBTNOn, Color.GREEN)
                getParedDevices()
                Snackbar.make(binding.root, "Блютуз включен", Snackbar.LENGTH_LONG).show()
            } else {
                changeButtonColor(binding.imageBTNOn, Color.RED)
                Snackbar.make(binding.root, "Блютуз отключен", Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun registerPermissionListener() {
        pLauncher =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {

            }
    }

    private fun checkPermission() {
        if (!checkBtPermission()) {
            registerPermissionListener()
            launchBtPermission()
        }
    }

    private fun launchBtPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            pLauncher.launch(
                arrayOf(
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            )
        } else {
            pLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            )
        }
    }

    private fun saveMac(mac: String) {
        val editor = preferences?.edit()
        editor?.putString(BluetoothConstants.MAC, mac)
        editor?.apply()
    }

    override fun onClick(device: ListItem) {
        saveMac(device.mac)
    }
}
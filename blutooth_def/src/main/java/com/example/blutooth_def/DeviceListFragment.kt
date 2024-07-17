package com.example.blutooth_def

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat.registerReceiver
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.blutooth_def.databinding.FragmentListBinding
import com.google.android.material.snackbar.Snackbar


class DeviceListFragment : Fragment(), ItemAdapter.Listener {
    private var preferences: SharedPreferences? = null
    private lateinit var itemAdapter: ItemAdapter
    private lateinit var discoveryAdapter: ItemAdapter
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
        binding.imageBTSearch.setOnClickListener {
            Toast.makeText(context, "kfldfkdfkfdf ${bthAdapter?.isEnabled == true}", Toast.LENGTH_LONG).show()
            try {
                if (bthAdapter?.isEnabled == true) {
                    it.visibility = View.GONE
                    binding.pbSearch.visibility = View.VISIBLE
                    bthAdapter?.startDiscovery()
                }
            } catch (e: SecurityException) {
            }
        }
        intentFilters()
        checkPermission()
        initRcView()
        registerBthLauncher()
        initAdapter()
        bluetoothState()
    }


    private fun initRcView() = with(binding) {
        recyclerViewConnected.layoutManager = LinearLayoutManager(requireContext())
        itemAdapter = ItemAdapter(this@DeviceListFragment, false)
        recyclerViewConnected.adapter = itemAdapter

        recyclerViewSearch.layoutManager = LinearLayoutManager(requireContext())
        discoveryAdapter = ItemAdapter(this@DeviceListFragment, true)
        recyclerViewSearch.adapter = discoveryAdapter

    }


    private fun getParedDevices() {
        try {
            val list = ArrayList<ListItem>()
            val deviceList = bthAdapter?.bondedDevices as Set<BluetoothDevice>
            deviceList.forEach {
                list.add(
                    ListItem(
                        it,
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
        saveMac(device.device.address)
    }

    private val bReceiver = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, intent: Intent?) {
            Toast.makeText(context, "kfldfkdfkfdf2", Toast.LENGTH_LONG).show()

            if (intent?.action == BluetoothDevice.ACTION_FOUND) {
                val device =
                    intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                val list = mutableSetOf<ListItem>()
                list.addAll(discoveryAdapter.currentList)
                if (device != null) list.add(ListItem(device, false))
                discoveryAdapter.submitList(list.toList())
                try {
                    Log.d("MyLog", "Device: ${device?.name}")
                } catch (e: SecurityException) {

                }
            } else if (intent?.action == BluetoothDevice.ACTION_BOND_STATE_CHANGED) {
                getParedDevices()
            } else if (intent?.action == BluetoothAdapter.ACTION_DISCOVERY_FINISHED) {
                binding.imageBTSearch.visibility = View.VISIBLE
                binding.pbSearch.visibility = View.GONE
            }
        }
    }

    private fun intentFilters() {
//        Toast.makeText(context, "kfldfkdfkfdf3 ${bReceiver.resultCode}", Toast.LENGTH_LONG).show()

        val f1 = IntentFilter(BluetoothDevice.ACTION_FOUND)
        val f2 = IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
        val f3 = IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        activity?.registerReceiver(bReceiver, f1)
        activity?.registerReceiver(bReceiver, f2)
        activity?.registerReceiver(bReceiver, f3)


    }
}
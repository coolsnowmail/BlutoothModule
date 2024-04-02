package com.example.blutooth_def

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.blutooth_def.databinding.ActivityBaseBinding

class BaseActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBaseBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityBaseBinding.inflate(layoutInflater)
        setContentView(R.layout.activity_base)
//        initRcView()
        supportFragmentManager.beginTransaction()
            .replace(R.id.placeHolder, DeviceListFragment()).commit()
    }

    private fun initRcView() {
        val rcView = findViewById<RecyclerView>(R.id.recyclerViewConnected)
        rcView.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        val adapter = ItemAdapter()
        rcView.adapter = adapter
        adapter.submitList(createDeviceList())
    }

    private fun createDeviceList(): List<ListItem> {
        val list = ArrayList<ListItem>()
        for (i in 0 until 5) {
            list.add(
                ListItem(
                    "Device $i",
                    "34:56:89:56"
                )
            )
        }
        return list
    }
}
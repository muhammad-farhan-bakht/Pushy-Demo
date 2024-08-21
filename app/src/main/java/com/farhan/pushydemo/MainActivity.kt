package com.farhan.pushydemo

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.pushy.sdk.Pushy

class MainActivity : AppCompatActivity() {

    private lateinit var tvDeviceToken: TextView
    private lateinit var btnWhiteListPermission: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initViews()
        initPushy()
    }

    private fun initViews() {
        tvDeviceToken = findViewById(R.id.tv_device_token)
        btnWhiteListPermission = findViewById(R.id.btn_ask_whitelisted_permissions)

        setUpClickListeners()
    }

    private fun setUpClickListeners() {
        btnWhiteListPermission.setOnClickListener {
            askWhiteListPermission()
        }
    }

    private fun updateDeviceToken(deviceToken: String) {
        tvDeviceToken.text = deviceToken
    }

    private fun initPushy() {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                val deviceToken = Pushy.register(this@MainActivity)

                withContext(Dispatchers.Main) {
                   // Pushy.toggleForegroundService(true, this@MainActivity)

                    Log.d("Pushy", "Pushy device token: $deviceToken")
                    updateDeviceToken(deviceToken)

                    Pushy.listen(this@MainActivity)
                }
            }
        }
    }

    private fun askWhiteListPermission() {

        // Get power manager instance
        val powerManager = getSystemService(POWER_SERVICE) as PowerManager

        // Check if app isn't already whitelisted from battery optimizations
        if (!powerManager.isIgnoringBatteryOptimizations(packageName)) {
            // Instruct user to whitelist app from battery optimizations
            AlertDialog.Builder(this)
                .setTitle("Disable battery optimizations")
                .setMessage("To receive notifications in the background, please set \"Battery\" to \"Unrestricted\" in the next screen.")
                .setPositiveButton("OK")
                { dialogInterface, i -> // Open settings screen for this app
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)

                    // Set package to current package
                    intent.setData(Uri.fromParts("package", packageName, null))

                    // Start settings activity
                    startActivity(intent)
                }
                .setNegativeButton("Cancel", null).show()
                .show()
        }
    }
}
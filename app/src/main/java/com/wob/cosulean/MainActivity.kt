package com.wob.cosulean

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {

    private lateinit var dpm: DevicePolicyManager
    private lateinit var pm: PackageManager
    private lateinit var adminName: ComponentName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initMembers()

        setupLockButton()

        disableLockActivity()
    }

    private fun initMembers() {
        dpm = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        // Retrieve DeviceAdminReceiver ComponentName so we can make device management api calls later
        adminName = DeviceAdminReceiver.getComponentName(this)
        // Retrieve Package Manager so that we can enable and disable LockedActivity
        pm = this.packageManager
    }

    private fun disableLockActivity() {
        // Check to see if started by LockActivity and disable LockActivity if so
        val intent = intent

        if (intent.getIntExtra(Locked.LOCK_ACTIVITY_KEY, 0) ==
            Locked.FROM_LOCK_ACTIVITY
        ) {
            dpm.clearPackagePersistentPreferredActivities(
                adminName, packageName
            )
            pm.setComponentEnabledSetting(
                ComponentName(applicationContext, Locked::class.java),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP
            )
        }
    }

    private fun setupLockButton() {
        val lockButton = findViewById<Button>(R.id.btn_start_lock)
        lockButton.setOnClickListener {
            if (dpm.isDeviceOwnerApp(applicationContext.packageName)) {
                val lockIntent = Intent(applicationContext, Locked::class.java)
                pm.setComponentEnabledSetting(
                    ComponentName(
                        applicationContext,
                        Locked::class.java
                    ),
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP
                )
                startActivity(lockIntent)
                finish()
            } else {
                Toast.makeText(
                    applicationContext,
                    R.string.not_lock_whitelisted, Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}

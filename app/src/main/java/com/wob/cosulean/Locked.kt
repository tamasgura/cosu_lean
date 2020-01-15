package com.wob.cosulean

import android.app.ActivityManager
import android.app.admin.DevicePolicyManager
import android.app.admin.SystemUpdatePolicy
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Bundle
import android.os.UserManager
import android.provider.Settings
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity


class Locked : AppCompatActivity() {

    private lateinit var mAdminComponentName: ComponentName

    private lateinit var dpm: DevicePolicyManager
    private lateinit var am: ActivityManager

    companion object {
        val LOCK_ACTIVITY_KEY = "lock_activity"
        val FROM_LOCK_ACTIVITY = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_locked)

        dpm = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        am = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        mAdminComponentName = DeviceAdminReceiver.getComponentName(this)

        initUnlockButton()

        setDefaultCosuPolicy()
    }

    override fun onStart() {
        super.onStart()

        if (dpm.isLockTaskPermitted(packageName) && am.lockTaskModeState == ActivityManager.LOCK_TASK_MODE_NONE) startLockTask()
    }

    private fun setDefaultCosuPolicy() {
        dpm = getSystemService(
            Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        if(dpm.isDeviceOwnerApp(packageName)){
            setCosuPolicy(true)
        }
        else {
            Toast.makeText(applicationContext,
                R.string.not_device_owner, Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun setCosuPolicy(active: Boolean) {
        // Set user restrictions
        setUserRestriction(UserManager.DISALLOW_SAFE_BOOT, active)
        setUserRestriction(UserManager.DISALLOW_FACTORY_RESET, active)
        setUserRestriction(UserManager.DISALLOW_ADD_USER, active)
        setUserRestriction(UserManager.DISALLOW_MOUNT_PHYSICAL_MEDIA, active)
        setUserRestriction(UserManager.DISALLOW_ADJUST_VOLUME, active)
        // Disable keyguard and status bar
        dpm.setKeyguardDisabled(mAdminComponentName, active)
        dpm.setStatusBarDisabled(mAdminComponentName, active)
        // Enable STAY_ON_WHILE_PLUGGED_IN
        enableStayOnWhilePluggedIn(active)
        // Set system update policy
        if (active) {
            dpm.setSystemUpdatePolicy(
                mAdminComponentName,
                SystemUpdatePolicy.createWindowedInstallPolicy(60, 120)
            )
        } else {
            dpm.setSystemUpdatePolicy(
                mAdminComponentName,
                null
            )
        }
        // set this Activity as a lock task package
        dpm.setLockTaskPackages(
            mAdminComponentName,
            if (active) arrayOf(packageName) else arrayOf()
        )
        val intentFilter = IntentFilter(Intent.ACTION_MAIN)
        intentFilter.addCategory(Intent.CATEGORY_HOME)
        intentFilter.addCategory(Intent.CATEGORY_DEFAULT)
        if (active) {
            // set Cosu activity as home intent receiver so that it is started on reboot
            dpm.addPersistentPreferredActivity(
                mAdminComponentName, intentFilter, ComponentName(
                    packageName, Locked::class.java.name
                )
            )
        } else {
            dpm.clearPackagePersistentPreferredActivities(
                mAdminComponentName, packageName
            )
        }
    }

    private fun setUserRestriction(
        restriction: String,
        disallow: Boolean
    ) {
        if (disallow) {
            dpm.addUserRestriction(
                mAdminComponentName,
                restriction
            )
        } else {
            dpm.clearUserRestriction(
                mAdminComponentName,
                restriction
            )
        }
    }

    private fun enableStayOnWhilePluggedIn(enabled: Boolean) {
        if (enabled) {
            dpm.setGlobalSetting(
                mAdminComponentName,
                Settings.Global.STAY_ON_WHILE_PLUGGED_IN,
                (BatteryManager.BATTERY_PLUGGED_AC
                        or BatteryManager.BATTERY_PLUGGED_USB
                        or BatteryManager.BATTERY_PLUGGED_WIRELESS).toString()
            )
        } else {
            dpm.setGlobalSetting(
                mAdminComponentName,
                Settings.Global.STAY_ON_WHILE_PLUGGED_IN,
                "0"
            )
        }
    }

    private fun initUnlockButton() {
        val unlockBtn = findViewById<Button>(R.id.btn_exit_lock)
        unlockBtn.setOnClickListener {
            if (am.lockTaskModeState ==
                ActivityManager.LOCK_TASK_MODE_LOCKED) {
                stopLockTask()
            }
            setCosuPolicy(false)
            intent = Intent(
                applicationContext, MainActivity::class.java)

            intent.putExtra(LOCK_ACTIVITY_KEY, FROM_LOCK_ACTIVITY)
            startActivity(intent)
            finish()
        }
    }
}

package com.iktwo.das

import android.app.role.RoleManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.widget.Toast

/**
 * Helpers to check whether the host app is configured as the device's digital
 * assistant and to guide the user into configuring it.
 */
object DasAssistant {

    /**
     * Returns true when the host app is the currently configured default
     * digital assistant.
     *
     * Uses [RoleManager] on API 29+ and falls back to reading the legacy
     * `assistant` secure setting.
     */
    fun isDefaultAssistant(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = context.getSystemService(RoleManager::class.java)
            if (roleManager != null && roleManager.isRoleAvailable(RoleManager.ROLE_ASSISTANT)) {
                return roleManager.isRoleHeld(RoleManager.ROLE_ASSISTANT)
            }
        }

        val assistant = Settings.Secure.getString(context.contentResolver, "assistant")
            ?: return false
        val componentName = ComponentName.unflattenFromString(assistant)
        return componentName?.packageName == context.packageName
    }

    /**
     * Returns true when the device supports requesting the assistant role
     * through a system dialog via [createRoleRequestIntent].
     */
    fun canRequestRole(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return false
        val roleManager = context.getSystemService(RoleManager::class.java) ?: return false
        return roleManager.isRoleAvailable(RoleManager.ROLE_ASSISTANT)
    }

    /**
     * Intent that shows the system dialog asking the user to make the host app
     * the default digital assistant. Returns null when the role cannot be
     * requested on this device (use [openAssistantSettings] instead).
     *
     * Launch with [android.app.Activity.startActivityForResult] and check
     * [android.app.Activity.RESULT_OK] to know if the user accepted.
     */
    fun createRoleRequestIntent(context: Context): Intent? {
        if (!canRequestRole(context)) return null
        return context.getSystemService(RoleManager::class.java)
            .createRequestRoleIntent(RoleManager.ROLE_ASSISTANT)
    }

    /**
     * Opens the system screen where the default digital assistant app is
     * configured.
     */
    fun openAssistantSettings(context: Context) {
        try {
            context.startActivity(Intent(Settings.ACTION_VOICE_INPUT_SETTINGS))
        } catch (e: Exception) {
            Toast.makeText(context, "Could not open settings: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}

package com.innovatelabs3.projectI2.utils

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.core.content.ContextCompat

object CallUtils {
    fun makePhoneCall(context: Context, phoneNumber: String, onPermissionNeeded: () -> Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            initiateCall(context, phoneNumber)
        } else {
            onPermissionNeeded()
        }
    }

    private fun initiateCall(context: Context, phoneNumber: String) {
        try {
            val intent = Intent(Intent.ACTION_CALL).apply {
                data = Uri.parse("tel:$phoneNumber")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Error making call: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    fun isValidPhoneNumber(number: String): Boolean {
        return number.replace(Regex("[^0-9+]"), "").length >= 10
    }
} 
package com.innovatelabs3.projectI2.utils

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast

object PaymentUtils {

    fun openPhonePe(
        context: Context,
        recipientUpiId: String,
        recipientName: String,
        amount: String
    ) {
        try {
            // Build the UPI payment URI
            val upiUri = Uri.parse(
                "upi://pay?" +
                        "pa=$recipientUpiId" +
                        "&pn=${Uri.encode(recipientName)}" +
                        "&am=$amount" +
                        "&cu=INR"
            )

            // Create intent for PhonePe
            val intent = Intent(Intent.ACTION_VIEW, upiUri).apply {
                setPackage("com.phonepe.app")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            if (isPhonePeInstalled(context)) {
                context.startActivity(intent)
            } else {
                // If PhonePe not installed, show Play Store
                openPlayStore(context)
            }
        } catch (e: Exception) {
            Toast.makeText(
                context,
                "Error launching PhonePe: ${e.localizedMessage}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun isPhonePeInstalled(context: Context): Boolean {
        return try {
            context.packageManager.getPackageInfo("com.phonepe.app", 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    private fun openPlayStore(context: Context, packageName: String = "com.phonepe.app") {
        try {
            context.startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("market://details?id=$packageName")
                ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        } catch (e: ActivityNotFoundException) {
            // If Play Store not available, open browser
            context.startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
                ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }
    }
} 
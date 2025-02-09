package com.innovatelabs3.projectI2.utils

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

object EmailUtils {
    fun sendEmail(context: Context, to: String, subject: String, body: String, isHtml: Boolean = false) {
        try {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:") // only email apps should handle this
                putExtra(Intent.EXTRA_EMAIL, arrayOf(to))
                putExtra(Intent.EXTRA_SUBJECT, subject)
                putExtra(Intent.EXTRA_TEXT, body)
                if (isHtml) {
                    putExtra(Intent.EXTRA_HTML_TEXT, body)
                }
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(Intent.createChooser(intent, "Send email using").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            } else {
                // Try Gmail specific intent
                val gmailIntent = Intent(Intent.ACTION_VIEW).apply {
                    setPackage("com.google.android.gm")
                    data = Uri.parse("mailto:$to?subject=${Uri.encode(subject)}&body=${Uri.encode(body)}")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                try {
                    context.startActivity(gmailIntent)
                } catch (e: ActivityNotFoundException) {
                    Toast.makeText(context, "No email app found", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            Toast.makeText(
                context,
                "Error launching email: ${e.localizedMessage}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
} 
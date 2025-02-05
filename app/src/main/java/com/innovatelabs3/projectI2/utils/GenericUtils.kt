package com.innovatelabs3.projectI2.utils

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarDuration
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.innovatelabs3.projectI2.R
import com.innovatelabs3.projectI2.domain.SystemQueries
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import android.app.NotificationManager

class GenericUtils {

    companion object {
        fun showToast(context: Context, message: String) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }

        fun showSnackbar(
            scope: CoroutineScope,
            snackbarHostState: SnackbarHostState,
            message: String
        ) {
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = message,
                    duration = SnackbarDuration.Short
                )
            }
        }

        @SuppressLint("MissingPermission")
        fun showNotification(context: Context, content: SystemQueries.NotificationContent) {
            val channelId = "project_i_channel"
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            
            // Create notification channel for Android O and above
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = android.app.NotificationChannel(
                    channelId,
                    "Project I Notifications",
                    NotificationManager.IMPORTANCE_DEFAULT
                )
                notificationManager.createNotificationChannel(channel)
            }

            val notification = NotificationCompat.Builder(context, channelId)
                .setContentTitle(content.title)
                .setContentText(content.message)
                .setSmallIcon(R.drawable.ic_project_i)
                .setPriority(when (content.priority) {
                    SystemQueries.NotificationPriority.HIGH -> NotificationCompat.PRIORITY_HIGH
                    SystemQueries.NotificationPriority.LOW -> NotificationCompat.PRIORITY_LOW
                    else -> NotificationCompat.PRIORITY_DEFAULT
                })
                .setAutoCancel(true)
                .build()

            val notificationManagerCompat = NotificationManagerCompat.from(context)
            notificationManagerCompat.notify(System.currentTimeMillis().toInt(), notification)
        }

        fun checkAndRequestNotificationPermission(
            context: Context,
            requestPermissionLauncher: ActivityResultLauncher<String>
        ): Boolean {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val permission = Manifest.permission.POST_NOTIFICATIONS
                if (context.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                    // Launch the permission request
                    requestPermissionLauncher.launch(permission)
                    return false // Permission is not granted
                }
            }
            return true // Permission is granted (or not needed)
        }
    }
} 
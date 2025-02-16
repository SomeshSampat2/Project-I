package com.innovatelabs3.projectI2

import android.Manifest
import android.app.AlertDialog
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.innovatelabs3.projectI2.ui.screen.MainScreen
import com.innovatelabs3.projectI2.ui.theme.AppTheme
import com.innovatelabs3.projectI2.ui.viewmodel.UserViewModel
import com.innovatelabs3.projectI2.utils.GenericUtils
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val viewModel: UserViewModel by viewModels()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        if (allGranted) {
            viewModel.retryLastOperation()
        } else {
            showPermissionRationaleDialog(
                getRequiredPermissions()
            )
        }
    }

    private val callPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        viewModel.onPermissionResult("call", isGranted)
    }

    private val contactsPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {}

    private val micPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            GenericUtils.showToast(this, "Voice input is now available")
        } else {
            GenericUtils.showToast(this, "Voice input requires microphone permission")
        }
    }

    private fun getRequiredPermissions(): Array<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) { // Android 14
            arrayOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO,
                Manifest.permission.READ_MEDIA_AUDIO,
                Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
            )
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13
            arrayOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO,
                Manifest.permission.READ_MEDIA_AUDIO
            )
        } else {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen(viewModel)
                }
            }
        }

        // Fixed lifecycle scope implementation
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.requestPermission.collect { permission ->
                    permission?.let {
                        when (permission) {
                            "storage" -> {
                                requestPermissionLauncher.launch(getRequiredPermissions())
                            }

                            "notification" -> {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    requestPermissionLauncher.launch(
                                        arrayOf(Manifest.permission.POST_NOTIFICATIONS)
                                    )
                                }
                            }

                            "call" -> {
                                callPermissionLauncher.launch(Manifest.permission.CALL_PHONE)
                            }

                            "contacts" -> {
                                // ... existing contacts permission handling ...
                                contactsPermissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.READ_CONTACTS,
                                        Manifest.permission.WRITE_CONTACTS
                                    )
                                )
                            }

                            "microphone" -> {
                                micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            }
                        }
                        viewModel.clearPermissionRequest()
                    }
                }
            }
        }
    }

    private fun showPermissionRationaleDialog(
        permissions: Array<String>,
        message: String = "These permissions are required to access your files. Would you like to grant them?"
    ) {
        AlertDialog.Builder(this)
            .setTitle("Permission Required")
            .setMessage(message)
            .setPositiveButton("Grant") { _, _ ->
                requestPermissionLauncher.launch(permissions)
            }
            .setNegativeButton("Cancel") { _, _ ->
                GenericUtils.showToast(
                    this,
                    "Permission denied. Cannot proceed with the operation."
                )
            }
            .setCancelable(false)
            .show()
    }
}
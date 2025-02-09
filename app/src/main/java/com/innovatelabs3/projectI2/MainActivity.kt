package com.innovatelabs3.projectI2

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.innovatelabs3.projectI2.ui.theme.AppTheme
import com.innovatelabs3.projectI2.ui.viewmodel.UserViewModel
import androidx.compose.runtime.getValue
import com.innovatelabs3.projectI2.ui.screen.UserScreen
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationManagerCompat
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.innovatelabs3.projectI2.utils.GenericUtils
import androidx.compose.runtime.rememberCoroutineScope
import android.app.AlertDialog
import kotlinx.coroutines.launch
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.os.Environment
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle

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
                getRequiredPermissions(),
                "These permissions are required to access your files. Would you like to grant them?"
            )
        }
    }

    private val manageAllFilesLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (isManageExternalStoragePermissionGranted()) {
            viewModel.retryLastOperation()
        } else {
            GenericUtils.showToast(this, "All files access permission denied. Some features may be limited.")
        }
    }

    private val callPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        viewModel.onPermissionResult("call", isGranted)
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

    private fun isManageExternalStoragePermissionGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            true
        }
    }

    private fun requestManageAllFilesPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION).apply {
                    data = Uri.parse("package:${applicationContext.packageName}")
                }
                manageAllFilesLauncher.launch(intent)
            } catch (e: Exception) {
                manageAllFilesLauncher.launch(
                    Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                )
            }
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
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R &&
                                    !isManageExternalStoragePermissionGranted()) {
                                    showPermissionRationaleDialog(
                                        getRequiredPermissions(),
                                        "To access all files, this app needs special permission. Would you like to grant it?"
                                    )
                                } else {
                                    requestPermissionLauncher.launch(getRequiredPermissions())
                                }
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
        message: String
    ) {
        AlertDialog.Builder(this)
            .setTitle("Permission Required")
            .setMessage(message)
            .setPositiveButton("Grant") { _, _ ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R &&
                    permissions.contains(Manifest.permission.MANAGE_EXTERNAL_STORAGE)) {
                    requestManageAllFilesPermission()
                } else {
                    requestPermissionLauncher.launch(permissions)
                }
            }
            .setNegativeButton("Cancel") { _, _ ->
                GenericUtils.showToast(this, "Permission denied. Cannot proceed with the operation.")
            }
            .setCancelable(false)
            .show()
    }
}

@Composable
fun MainScreen(viewModel: UserViewModel) {
    val context = LocalContext.current
    val showToast by viewModel.showToast.collectAsState()
    val showSnackbar by viewModel.showSnackbar.collectAsState()
    val showNotification by viewModel.showNotification.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()

    // Permission request launcher
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permission is granted, you can proceed with showing notifications
            GenericUtils.showToast(context, "Notification permission granted")
        } else {
            // Explain that the notification permission is not available
            GenericUtils.showToast(context, "Notification permission denied")
        }
    }

    // Request notification permission on Android 13+
    LaunchedEffect(key1 = lifecycleOwner.lifecycle, key2 = context) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val permission = Manifest.permission.POST_NOTIFICATIONS
                if (context.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                    // Launch the permission request
                    requestPermissionLauncher.launch(permission)
                }
            }
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    actionColor = Color(0xFF4CAF50), // Success Green
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            // Show toast when message is available
            LaunchedEffect(showToast) {
                showToast?.let { message ->
                    GenericUtils.showToast(context, message)
                    viewModel.clearToast()
                }
            }

            // Show snackbar when message is available
            LaunchedEffect(showSnackbar) {
                showSnackbar?.let { message ->
                    GenericUtils.showSnackbar(scope, snackbarHostState, message)
                    viewModel.clearSnackbar()
                }
            }

            // Show notification when content is available
            LaunchedEffect(showNotification) {
                if (GenericUtils.checkAndRequestNotificationPermission(context, requestPermissionLauncher)) {
                    showNotification?.let { content ->
                        GenericUtils.showNotification(context, content)
                        viewModel.clearNotification()
                    }
                }
            }

            UserScreen(viewModel)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    AppTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            val viewModel = viewModel<UserViewModel>()
            MainScreen(viewModel)
        }
    }
} 
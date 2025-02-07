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

class MainActivity : ComponentActivity() {
    private val viewModel: UserViewModel by viewModels()
    
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        if (allGranted) {
            viewModel.retryLastOperation()
        } else {
            // Show message if permission was denied
            GenericUtils.showToast(this, "Contact permissions are required to save contacts")
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
                    val viewModel = viewModel<UserViewModel>()
                    MainScreen(viewModel)
                }
            }

            // Observe permission requests
            LaunchedEffect(Unit) {
                viewModel.requestPermission.collect { permission ->
                    when (permission) {
                        "contacts" -> {
                            if (shouldShowRequestPermissionRationale(Manifest.permission.READ_CONTACTS) ||
                                shouldShowRequestPermissionRationale(Manifest.permission.WRITE_CONTACTS)) {
                                // Show in-app rationale dialog and then request permission
                                showPermissionRationaleDialog(
                                    onConfirm = {
                                        requestPermissionLauncher.launch(
                                            arrayOf(
                                                Manifest.permission.READ_CONTACTS,
                                                Manifest.permission.WRITE_CONTACTS
                                            )
                                        )
                                    },
                                    onDismiss = {
                                        GenericUtils.showToast(this@MainActivity, 
                                            "Contact permissions are needed to save contacts")
                                    }
                                )
                            } else {
                                // Directly request permission
                                requestPermissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.READ_CONTACTS,
                                        Manifest.permission.WRITE_CONTACTS
                                    )
                                )
                            }
                            viewModel.clearPermissionRequest()
                        }
                    }
                }
            }
        }
    }

    private fun showPermissionRationaleDialog(
        onConfirm: () -> Unit,
        onDismiss: () -> Unit
    ) {
        AlertDialog.Builder(this)
            .setTitle("Permission Required")
            .setMessage("Contact permission is needed to save contacts. Would you like to grant this permission?")
            .setPositiveButton("Yes") { _, _ -> onConfirm() }
            .setNegativeButton("No") { _, _ -> onDismiss() }
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
    val notificationManager = remember { NotificationManagerCompat.from(context) }
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
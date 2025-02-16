package com.innovatelabs3.projectI2.ui.screen

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.innovatelabs3.projectI2.ui.navigation.BottomNavItem
import com.innovatelabs3.projectI2.ui.viewmodel.PhotoEditorViewModel
import com.innovatelabs3.projectI2.ui.viewmodel.UserViewModel
import com.innovatelabs3.projectI2.utils.GenericUtils

@Composable
fun MainScreen(
    userViewModel: UserViewModel,
    photoEditorViewModel: PhotoEditorViewModel
) {
    val context = LocalContext.current
    val showToast by userViewModel.showToast.collectAsState()
    val showSnackbar by userViewModel.showSnackbar.collectAsState()
    val showNotification by userViewModel.showNotification.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    val navController = rememberNavController()

    // Permission request launcher
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {}

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
        },
        bottomBar = { BottomNavigationBar(navController = navController) }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            // Show toast when message is available
            LaunchedEffect(showToast) {
                showToast?.let { message ->
                    GenericUtils.showToast(context, message)
                    userViewModel.clearToast()
                }
            }

            // Show snackbar when message is available
            LaunchedEffect(showSnackbar) {
                showSnackbar?.let { message ->
                    GenericUtils.showSnackbar(scope, snackbarHostState, message)
                    userViewModel.clearSnackbar()
                }
            }

            // Show notification when content is available
            LaunchedEffect(showNotification) {
                if (GenericUtils.checkAndRequestNotificationPermission(
                        context,
                        requestPermissionLauncher
                    )
                ) {
                    showNotification?.let { content ->
                        GenericUtils.showNotification(context, content)
                        userViewModel.clearNotification()
                    }
                }
            }

            NavigationGraph(
                navController = navController,
                userViewModel = userViewModel,
                photoEditorViewModel = photoEditorViewModel
            )
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val items = listOf(
        BottomNavItem.Chat,
        BottomNavItem.PhotoEditor
    )

    NavigationBar(
        modifier = Modifier.height(48.dp),
        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.97f),
        tonalElevation = 10.dp
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        items.forEach { item ->
            NavigationBarItem(
                icon = {
                    Icon(
                        painter = painterResource(id = item.iconResId),
                        contentDescription = item.title,
                        modifier = Modifier.size(22.dp)
                    )
                },
                selected = currentRoute == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        navController.graph.startDestinationRoute?.let { route ->
                            popUpTo(route) {
                                saveState = true
                            }
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                ),
                modifier = Modifier.padding(4.dp)
            )
        }
    }
}

@Composable
fun NavigationGraph(
    navController: NavHostController,
    userViewModel: UserViewModel,
    photoEditorViewModel: PhotoEditorViewModel
) {
    NavHost(
        navController = navController,
        startDestination = BottomNavItem.Chat.route
    ) {
        composable(BottomNavItem.Chat.route) {
            UserScreen(viewModel = userViewModel)
        }
        composable(BottomNavItem.PhotoEditor.route) {
            PhotoEditorScreen(photoEditorViewModel)
        }
    }
}
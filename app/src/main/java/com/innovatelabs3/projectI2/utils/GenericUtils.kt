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
import android.content.Intent
import android.net.Uri
import android.content.pm.PackageManager.NameNotFoundException
import android.content.ActivityNotFoundException

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
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

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
                .setPriority(
                    when (content.priority) {
                        SystemQueries.NotificationPriority.HIGH -> NotificationCompat.PRIORITY_HIGH
                        SystemQueries.NotificationPriority.LOW -> NotificationCompat.PRIORITY_LOW
                        else -> NotificationCompat.PRIORITY_DEFAULT
                    }
                )
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

        fun openWhatsApp(context: Context) {
            val whatsappPackage = "com.whatsapp"

            try {
                // First check if WhatsApp is installed
                context.packageManager.getPackageInfo(whatsappPackage, 0)

                // If we get here, WhatsApp is installed
                val launchIntent = context.packageManager.getLaunchIntentForPackage(whatsappPackage)
                if (launchIntent != null) {
                    launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(launchIntent)
                    showToast(context, "Opening WhatsApp...")
                } else {
                    // This shouldn't happen if the package exists, but just in case
                    openPlayStore(context, whatsappPackage)
                }
            } catch (e: NameNotFoundException) {
                // WhatsApp is not installed
                openPlayStore(context, whatsappPackage)
            }
        }

        private fun openPlayStore(context: Context, packageName: String) {
            showToast(context, "WhatsApp is not installed. Opening Play Store...")
            try {
                // Try opening in Play Store app
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse("market://details?id=$packageName")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                // If Play Store app is not available, open in browser
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            }
        }

        fun openWhatsAppChat(context: Context, phoneNumber: String, message: String) {
            try {
                // First check if WhatsApp is installed
                context.packageManager.getPackageInfo("com.whatsapp", PackageManager.GET_ACTIVITIES)

                // Build the URL using WhatsApp API
                val url =
                    "https://api.whatsapp.com/send?phone=$phoneNumber&text=${Uri.encode(message)}"

                // Create and start the intent
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse(url)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
                showToast(context, "Opening WhatsApp chat...")
            } catch (e: NameNotFoundException) {
                // WhatsApp is not installed
                openPlayStore(context, "com.whatsapp")
            } catch (e: Exception) {
                showToast(context, "Couldn't open WhatsApp chat. Please try again.")
            }
        }

        fun openGoogleMaps(context: Context, destination: String) {
            try {
                // First check if Google Maps is installed
                context.packageManager.getPackageInfo("com.google.android.apps.maps", 0)

                // Create the maps intent
                val encodedDest = Uri.encode(destination)
                val mapsUri = Uri.parse("google.navigation:q=$encodedDest")
                val intent = Intent(Intent.ACTION_VIEW, mapsUri).apply {
                    setPackage("com.google.android.apps.maps")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }

                context.startActivity(intent)
                showToast(context, "Opening Google Maps...")
            } catch (e: NameNotFoundException) {
                // Google Maps is not installed
                showToast(context, "Google Maps is not installed. Opening Play Store...")
                openPlayStore(context, "com.google.android.apps.maps")
            } catch (e: ActivityNotFoundException) {
                showToast(context, "Couldn't open Google Maps. Please try again.")
            }
        }

        fun openYouTubeSearch(context: Context, query: String) {
            try {
                // First check if YouTube is installed
                context.packageManager.getPackageInfo("com.google.android.youtube", 0)

                // Create the YouTube search intent with the correct URL scheme
                val youtubeIntent = Intent(Intent.ACTION_SEARCH).apply {
                    setPackage("com.google.android.youtube")
                    putExtra("query", query)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }

                context.startActivity(youtubeIntent)
                showToast(context, "Opening YouTube search...")
            } catch (e: NameNotFoundException) {
                // YouTube is not installed
                showToast(context, "YouTube is not installed. Opening Play Store...")
                openPlayStore(context, "com.google.android.youtube")
            } catch (e: ActivityNotFoundException) {
                // Fallback to browser if app intent fails
                try {
                    val browserIntent = Intent(Intent.ACTION_VIEW).apply {
                        data = Uri.parse(
                            "https://www.youtube.com/results?search_query=${
                                Uri.encode(query)
                            }"
                        )
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(browserIntent)
                    showToast(context, "Opening YouTube in browser...")
                } catch (e: Exception) {
                    showToast(context, "Couldn't open YouTube. Please try again.")
                }
            }
        }

        fun openInstagramProfile(context: Context, username: String) {
            try {
                // First check if Instagram is installed
                context.packageManager.getPackageInfo("com.instagram.android", 0)

                // Try to open in Instagram app first
                val instaIntent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse("instagram://user?username=$username")
                    setPackage("com.instagram.android")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(instaIntent)
                showToast(context, "Opening Instagram profile...")
            } catch (e: NameNotFoundException) {
                // Instagram is not installed
                showToast(context, "Instagram is not installed. Opening Play Store...")
                openPlayStore(context, "com.instagram.android")
            } catch (e: ActivityNotFoundException) {
                // Fallback to browser
                try {
                    val browserIntent = Intent(Intent.ACTION_VIEW).apply {
                        data = Uri.parse("https://www.instagram.com/$username")
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(browserIntent)
                    showToast(context, "Opening Instagram profile in browser...")
                } catch (e: Exception) {
                    showToast(context, "Couldn't open Instagram profile. Please try again.")
                }
            }
        }

        fun joinGoogleMeet(context: Context, meetingCode: String) {
            try {
                // First check if Google Meet is installed
                context.packageManager.getPackageInfo("com.google.android.apps.meetings", 0)

                // Clean the meeting code (remove spaces, hyphens, etc)
                val cleanCode = meetingCode.replace(Regex("[^a-zA-Z0-9]"), "")

                // Create the Meet intent
                val meetIntent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse("https://meet.google.com/$cleanCode")
                    setPackage("com.google.android.apps.meetings")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }

                context.startActivity(meetIntent)
                showToast(context, "Opening Google Meet...")
            } catch (e: NameNotFoundException) {
                // Google Meet is not installed
                showToast(context, "Google Meet is not installed. Opening Play Store...")
                openPlayStore(context, "com.google.android.apps.meetings")
            } catch (e: ActivityNotFoundException) {
                // Fallback to browser
                try {
                    val browserIntent = Intent(Intent.ACTION_VIEW).apply {
                        data = Uri.parse("https://meet.google.com/$meetingCode")
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(browserIntent)
                    showToast(context, "Opening Google Meet in browser...")
                } catch (e: Exception) {
                    showToast(context, "Couldn't open Google Meet. Please try again.")
                }
            }
        }

        fun searchSpotify(context: Context, query: String, type: String = "track") {
            try {
                // First check if Spotify is installed
                context.packageManager.getPackageInfo("com.spotify.music", 0)

                // Create the Spotify search intent with direct search
                val encodedQuery = Uri.encode(query)
                val spotifyUri = when (type) {
                    "artist" -> "spotify://search/artist/$encodedQuery"
                    "track" -> "spotify://search/track/$encodedQuery"
                    else -> "spotify://search/all/$encodedQuery"
                }

                val spotifyIntent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse(spotifyUri)
                    setPackage("com.spotify.music")
                    // Add these flags to ensure direct search
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    // Add search query as extra
                    putExtra("query", query)
                    putExtra(
                        "android.intent.extra.REFERRER",
                        Uri.parse("android-app://${context.packageName}")
                    )
                }

                context.startActivity(spotifyIntent)
                showToast(context, "Searching Spotify...")
            } catch (e: NameNotFoundException) {
                // Spotify is not installed
                showToast(context, "Spotify is not installed. Opening Play Store...")
                openPlayStore(context, "com.spotify.music")
            } catch (e: ActivityNotFoundException) {
                // Fallback to browser with direct search
                try {
                    val browserIntent = Intent(Intent.ACTION_VIEW).apply {
                        val encodedQuery = Uri.encode(query)
                        // Use the search results URL directly
                        data = Uri.parse("https://open.spotify.com/search/results/$encodedQuery")
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(browserIntent)
                    showToast(context, "Opening Spotify search in browser...")
                } catch (e: Exception) {
                    showToast(context, "Couldn't open Spotify. Please try again.")
                }
            }
        }

        fun bookUberRide(context: Context, destination: String) {
            try {
                // First check if Uber is installed
                context.packageManager.getPackageInfo("com.ubercab", 0)

                // Create the Uber deep link
                val encodedDest = Uri.encode(destination)
                val uberUri =
                    "uber://?action=setPickup&pickup=my_location&dropoff[formatted_address]=$encodedDest"

                val uberIntent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse(uberUri)
                    setPackage("com.ubercab")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }

                context.startActivity(uberIntent)
                showToast(context, "Opening Uber...")
            } catch (e: NameNotFoundException) {
                // Uber is not installed
                showToast(context, "Uber is not installed. Opening Play Store...")
                openPlayStore(context, "com.ubercab")
            } catch (e: ActivityNotFoundException) {
                // Fallback to browser
                try {
                    val browserIntent = Intent(Intent.ACTION_VIEW).apply {
                        val mobileUrl = "https://m.uber.com/ul/?drop=${Uri.encode(destination)}"
                        data = Uri.parse(mobileUrl)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(browserIntent)
                    showToast(context, "Opening Uber in browser...")
                } catch (e: Exception) {
                    showToast(context, "Couldn't open Uber. Please try again.")
                }
            }
        }

        fun searchProduct(context: Context, query: String, platform: String = "flipkart") {
            when (platform.lowercase()) {
                "amazon" -> searchAmazon(context, query)
                else -> searchFlipkart(context, query)
            }
        }

        private fun searchFlipkart(context: Context, query: String) {
            try {
                // First check if Flipkart is installed
                context.packageManager.getPackageInfo("com.flipkart.android", 0)

                // Create the Flipkart search intent with the correct deep link format
                val encodedQuery = Uri.encode(query)
                val flipkartIntent = Intent(Intent.ACTION_VIEW).apply {
                    // Using the correct Flipkart deep link format
                    data = Uri.parse("https://dl.flipkart.com/dl/search?q=$encodedQuery")
                    setPackage("com.flipkart.android")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }

                context.startActivity(flipkartIntent)
                showToast(context, "Searching on Flipkart...")
            } catch (e: NameNotFoundException) {
                // Flipkart is not installed
                try {
                    val browserIntent = Intent(Intent.ACTION_VIEW).apply {
                        data = Uri.parse("https://www.flipkart.com/search?q=${Uri.encode(query)}")
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(browserIntent)
                    showToast(context, "Opening Flipkart in browser...")
                } catch (e: Exception) {
                    showToast(context, "Couldn't open Flipkart. Please try again.")
                }
            }
        }

        private fun searchAmazon(context: Context, query: String) {
            try {
                // First check if Amazon is installed
                context.packageManager.getPackageInfo("com.amazon.mShop.android.shopping", 0)

                // Create the Amazon search intent
                val encodedQuery = Uri.encode(query)
                val amazonIntent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse("amzn://amazon.com/s?k=$encodedQuery")
                    setPackage("com.amazon.mShop.android.shopping")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }

                context.startActivity(amazonIntent)
                showToast(context, "Searching on Amazon...")
            } catch (e: NameNotFoundException) {
                // Amazon is not installed
                try {
                    val browserIntent = Intent(Intent.ACTION_VIEW).apply {
                        data = Uri.parse("https://www.amazon.in/s?k=${Uri.encode(query)}")
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(browserIntent)
                    showToast(context, "Opening Amazon in browser...")
                } catch (e: Exception) {
                    showToast(context, "Couldn't open Amazon. Please try again.")
                }
            }
        }

        fun openLinkedInProfile(context: Context, profileId: String) {
            try {
                // Construct the URLs for the LinkedIn app and the web
                val appUri = Uri.parse("linkedin://in/$profileId")
                val webUri = Uri.parse("https://www.linkedin.com/in/$profileId")

                // Create an intent for the LinkedIn app
                val appIntent = Intent(Intent.ACTION_VIEW, appUri).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                
                // Check if LinkedIn app is installed
                if (appIntent.resolveActivity(context.packageManager) != null) {
                    context.startActivity(appIntent)
                    showToast(context, "Opening LinkedIn profile...")
                } else {
                    // Fallback: open in browser
                    val webIntent = Intent(Intent.ACTION_VIEW, webUri).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(webIntent)
                    showToast(context, "Opening LinkedIn profile in browser...")
                }
            } catch (e: Exception) {
                showToast(context, "Couldn't open LinkedIn profile")
            }
        }
    }
} 
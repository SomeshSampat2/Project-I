package com.innovatelabs3.projectI2.ui.screen

import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.innovatelabs3.projectI2.R
import com.innovatelabs3.projectI2.utils.GenericUtils
import androidx.compose.foundation.background
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.ui.draw.shadow
import android.provider.MediaStore
import android.widget.Toast
import androidx.compose.ui.graphics.asImageBitmap
import com.innovatelabs3.projectI2.ui.viewmodel.PhotoEditorViewModel
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.clickable
import androidx.compose.runtime.LaunchedEffect
import com.innovatelabs3.projectI2.ui.components.ProcessingOverlay
import com.innovatelabs3.projectI2.utils.ImageEditorUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.Brush

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoEditorScreen(
    viewModel: PhotoEditorViewModel
) {
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var showImageOptions by remember { mutableStateOf(selectedImageUri == null) }
    var textState by remember { mutableStateOf(TextFieldValue("")) }
    val context = LocalContext.current
    
    val currentImage by viewModel.currentImage.collectAsState()
    val isProcessing by viewModel.isProcessing.collectAsState()
    val showToast by viewModel.showToast.collectAsState()

    val isImageEditingSupported = true
    
    // Handle toast messages
    LaunchedEffect(showToast) {
        showToast?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearToast()
        }
    }

    // Image picker launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            showImageOptions = false
            // Convert Uri to Bitmap
            val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
            viewModel.updateImage(bitmap)
        }
    }
    
    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            selectedImageUri?.let { uri ->
                showImageOptions = false
                // Convert Uri to Bitmap
                val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                viewModel.updateImage(bitmap)
            }
        }
    }

    if (isImageEditingSupported) {

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                if (!showImageOptions) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextField(
                            value = textState,
                            onValueChange = { textState = it },
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 8.dp),
                            placeholder = { Text("Type your editing command...") },
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                            keyboardActions = KeyboardActions(
                                onSend = {
                                    if (textState.text.isNotBlank()) {
                                        currentImage?.let { bitmap ->
                                            viewModel.processEditCommand(textState.text, bitmap)
                                            textState = TextFieldValue("")
                                        }
                                    }
                                }
                            ),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                disabledContainerColor = MaterialTheme.colorScheme.surface,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                            ),
                            shape = RoundedCornerShape(24.dp),
                            enabled = !isProcessing
                        )

                        IconButton(
                            onClick = {
                                if (textState.text.isNotBlank()) {
                                    currentImage?.let { bitmap ->
                                        viewModel.processEditCommand(textState.text, bitmap)
                                        textState = TextFieldValue("")
                                    }
                                }
                            },
                            enabled = !isProcessing
                        ) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = "Send",
                                tint = if (isProcessing)
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                                else
                                    MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)),
                contentAlignment = Alignment.Center
            ) {
                if (showImageOptions) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(24.dp)
                        ) {
                            Text(
                                text = "Choose an Option",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                ),
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(24.dp)
                            ) {
                                // Gallery button
                                PhotoSelectionButton(
                                    icon = R.drawable.ic_gallery,
                                    text = "Choose from Gallery",
                                    onClick = { galleryLauncher.launch("image/*") }
                                )

                                // Camera button
                                PhotoSelectionButton(
                                    icon = R.drawable.ic_camera,
                                    text = "Take Photo",
                                    onClick = {
                                        val uri = GenericUtils.createImageUri(context)
                                        selectedImageUri = uri
                                        cameraLauncher.launch(uri)
                                    }
                                )
                            }
                        }
                    }
                } else {
                    // Image Display and Processing UI
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            currentImage?.let { bitmap ->
                                Box {
                                    Image(
                                        bitmap = bitmap.asImageBitmap(),
                                        contentDescription = "Selected Image",
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(400.dp)
                                            .padding(16.dp)
                                            .clip(RoundedCornerShape(12.dp)),
                                        contentScale = ContentScale.Fit
                                    )

                                    // Show processing overlay when processing
                                    if (isProcessing) {
                                        ProcessingOverlay(
                                            modifier = Modifier
                                                .matchParentSize()
                                                .padding(16.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                        )
                                    }
                                }

                                // Rotation controls with revert and download
                                val canUndo by viewModel.canUndo.collectAsState()
                                ImageRotationControls(
                                    onRotate = { degrees ->
                                        viewModel.processEditCommand("rotate image by $degrees degrees", bitmap)
                                    },
                                    onRevert = { viewModel.undoLastEdit() },
                                    onDownload = {
                                        if (ImageEditorUtils.saveImageToGallery(context, bitmap)) {
                                            Toast.makeText(context, "Image saved to gallery", Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(context, "Failed to save image", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    onChangeImage = {
                                        showImageOptions = true
                                    },
                                    canUndo = canUndo
                                )

                                // Effects row
                                EffectsRow(
                                    onEffectSelected = { effectName ->
                                        val command = when(effectName) {
                                            "Black & White" -> "convert to black and white"
                                            "Sepia" -> "apply sepia effect"
                                            "Blur" -> "apply blur effect"
                                            "Sharpen" -> "sharpen image"
                                            "Vignette" -> "add vignette effect"
                                            "High Contrast" -> "increase contrast"
                                            "Saturate" -> "increase saturation"
                                            "Warm" -> "make image warmer"
                                            "Cool" -> "make image cooler"
                                            "Grayscale" -> "convert to grayscale"
                                            "Invert" -> "invert colors"
                                            "Vintage" -> "apply vintage effect"
                                            "Pixelate" -> "pixelate image"
                                            "Sketch" -> "convert to sketch"
                                            "Cinematic" -> "apply cinematic effect"
                                            "Vibrant" -> "make colors vibrant"
                                            "Natural" -> "apply natural effect"
                                            "Dramatic" -> "apply dramatic effect"
                                            "Matte" -> "apply matte effect"
                                            "Film" -> "apply film effect"
                                            else -> return@EffectsRow
                                        }
                                        viewModel.processEditCommand(command, bitmap)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    } else {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Image Editing Coming Soon....",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.Black,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ImageRotationControls(
    onRotate: (Float) -> Unit,
    onRevert: () -> Unit,
    onDownload: () -> Unit,
    onChangeImage: () -> Unit,
    canUndo: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Change Image button - updated to match other rotation buttons
        IconButton(
            onClick = onChangeImage,
            modifier = Modifier
                .size(48.dp)
                .background(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    shape = CircleShape
                )
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_add_photo),
                contentDescription = "Change Image",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        }

        // Revert button
        RevertButton(
            onRevert = onRevert,
            enabled = canUndo
        )

        // Rotate Left (-90 degrees)
        IconButton(
            onClick = { onRotate(-90f) },
            modifier = Modifier
                .size(48.dp)
                .background(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    shape = CircleShape
                )
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_rotate_left),
                contentDescription = "Rotate Left",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        }

        // Rotate 180 degrees
        IconButton(
            onClick = { onRotate(180f) },
            modifier = Modifier
                .size(48.dp)
                .background(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    shape = CircleShape
                )
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_rotate_180),
                contentDescription = "Rotate 180°",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        }

        // Rotate Right (90 degrees)
        IconButton(
            onClick = { onRotate(90f) },
            modifier = Modifier
                .size(48.dp)
                .background(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    shape = CircleShape
                )
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_rotate_right),
                contentDescription = "Rotate Right",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        }

        // Download button
        DownloadButton(
            onClick = onDownload
        )
    }
}

@Composable
private fun EffectsRow(
    onEffectSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    // Create a static sample image once
    val sampleImage = remember {
        // Create a simple gradient image for preview
        val width = 100
        val height = 100
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        
        // Create a gradient background
        val gradient = LinearGradient(
            0f, 0f, width.toFloat(), height.toFloat(),
            intArrayOf(
                Color(0xFF2196F3).toArgb(), // Blue
                Color(0xFF4CAF50).toArgb(), // Green
                Color(0xFFFFC107).toArgb()  // Yellow
            ),
            null,
            Shader.TileMode.CLAMP
        )
        
        val paint = Paint().apply {
            shader = gradient
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
        
        // Add some shapes for better effect preview
        paint.shader = null
        paint.color = Color.White.copy(alpha = 0.5f).toArgb()
        canvas.drawCircle(width * 0.3f, height * 0.3f, width * 0.2f, paint)
        canvas.drawRect(width * 0.6f, height * 0.6f, width * 0.8f, height * 0.8f, paint)
        
        bitmap
    }

    val effects = listOf(
        // New cinematic and color effects first
        "Cinematic" to { bitmap: Bitmap -> ImageEditorUtils.applyCinematic(bitmap) },
        "Vibrant" to { bitmap: Bitmap -> ImageEditorUtils.applyVibrant(bitmap) },
        "Natural" to { bitmap: Bitmap -> ImageEditorUtils.applyNatural(bitmap) },
        "Dramatic" to { bitmap: Bitmap -> ImageEditorUtils.applyDramatic(bitmap) },
        "Matte" to { bitmap: Bitmap -> ImageEditorUtils.applyMatte(bitmap) },
        "Film" to { bitmap: Bitmap -> ImageEditorUtils.applyFilm(bitmap) },
        
        // Existing effects
        "Black & White" to { bitmap: Bitmap -> ImageEditorUtils.applyBlackAndWhite(bitmap) },
        "Sepia" to { bitmap: Bitmap -> ImageEditorUtils.applySepia(bitmap) },
        "Blur" to { bitmap: Bitmap -> ImageEditorUtils.applyBlur(context, bitmap, 5f) },
        "Sharpen" to { bitmap: Bitmap -> ImageEditorUtils.applySharpen(bitmap, 50f) },
        "Vignette" to { bitmap: Bitmap -> ImageEditorUtils.applyVignette(bitmap, 50f) },
        "High Contrast" to { bitmap: Bitmap -> ImageEditorUtils.adjustContrast(bitmap, 50f) },
        "Saturate" to { bitmap: Bitmap -> ImageEditorUtils.adjustSaturation(bitmap, 50f) },
        "Warm" to { bitmap: Bitmap -> ImageEditorUtils.adjustTemperature(bitmap, 30f) },
        "Cool" to { bitmap: Bitmap -> ImageEditorUtils.adjustTemperature(bitmap, -30f) },
        "Grayscale" to { bitmap: Bitmap -> ImageEditorUtils.applyGrayscale(bitmap) },
        "Invert" to { bitmap: Bitmap -> ImageEditorUtils.invertColors(bitmap) },
        "Vintage" to { bitmap: Bitmap -> ImageEditorUtils.applyVintage(bitmap) },
        "Pixelate" to { bitmap: Bitmap -> ImageEditorUtils.applyPixelate(bitmap, 20f) },
        "Sketch" to { bitmap: Bitmap -> ImageEditorUtils.applySketch(bitmap) }
    )

    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        items(effects.size) { index ->
            val (effectName, effectFunction) = effects[index]
            
            var previewBitmap by remember {
                mutableStateOf<Bitmap?>(null)
            }

            // Generate preview once using the sample image
            LaunchedEffect(Unit) {
                withContext(Dispatchers.Default) {
                    previewBitmap = effectFunction(sampleImage)
                }
            }

            Card(
                modifier = Modifier
                    .size(80.dp)
                    .clickable { onEffectSelected(effectName) },
                shape = RoundedCornerShape(8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    previewBitmap?.let { bitmap ->
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = effectName,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    
                    // Effect name overlay
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .background(Color.Black.copy(alpha = 0.6f))
                            .padding(4.dp)
                    ) {
                        Text(
                            text = effectName,
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

// Create a new composable for the revert button
@Composable
private fun RevertButton(
    onRevert: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .size(48.dp),
        shape = CircleShape,
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (enabled) 4.dp else 0.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (enabled)
                MaterialTheme.colorScheme.surface
            else
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        IconButton(
            onClick = onRevert,
            enabled = enabled,
            modifier = Modifier.fillMaxSize()
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_undo),
                contentDescription = "Revert last edit",
                tint = if (enabled)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

// Add this composable
@Composable
private fun DownloadButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
            .size(48.dp)
            .background(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                shape = CircleShape
            )
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_download),
            contentDescription = "Download image",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
    }
}

// Add this composable for the photo selection buttons
@Composable
private fun PhotoSelectionButton(
    icon: Int,
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(160.dp)
            .height(180.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = when(text) {
                "Choose from Gallery" -> Color(0xFFF3E5F5)  // Light purple
                else -> Color(0xFFE3F2FD)  // Light blue
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(
                        brush = Brush.linearGradient(
                            colors = when(text) {
                                "Choose from Gallery" -> listOf(
                                    Color(0xFF9C27B0).copy(alpha = 0.2f),  // Purple
                                    Color(0xFFE91E63).copy(alpha = 0.2f)   // Pink
                                )
                                else -> listOf(
                                    Color(0xFF2196F3).copy(alpha = 0.2f),  // Blue
                                    Color(0xFF03A9F4).copy(alpha = 0.2f)   // Light Blue
                                )
                            }
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = icon),
                    contentDescription = text,
                    tint = when(text) {
                        "Choose from Gallery" -> Color(0xFF9C27B0)  // Purple
                        else -> Color(0xFF2196F3)  // Blue
                    },
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = when(text) {
                    "Choose from Gallery" -> Color(0xFF6A1B9A)  // Dark Purple
                    else -> Color(0xFF1565C0)  // Dark Blue
                },
                textAlign = TextAlign.Center
            )
        }
    }
}
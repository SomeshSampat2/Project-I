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
                            colors = TextFieldDefaults.textFieldColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
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
                    Column(
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .clip(RoundedCornerShape(28.dp))
                            .background(
                                MaterialTheme.colorScheme.surface
                                    .copy(alpha = 0.9f)
                            )
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(28.dp)
                            )
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            "Select Image",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Gallery Button
                        OutlinedButton(
                            onClick = { galleryLauncher.launch("image/*") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp)
                                .shadow(
                                    elevation = 0.dp,
                                    shape = RoundedCornerShape(16.dp)
                                ),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                                contentColor = MaterialTheme.colorScheme.primary
                            ),
                            border = BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                            )
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_gallery),
                                    contentDescription = "Gallery",
                                    modifier = Modifier.size(24.dp),
                                    tint = Color.Black
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    "Choose from Gallery",
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = FontWeight.Medium
                                    ),
                                    color = Color.Black
                                )
                            }
                        }

                        // Camera Button
                        Button(
                            onClick = {
                                GenericUtils.createImageUri(context)?.let { uri ->
                                    selectedImageUri = uri
                                    cameraLauncher.launch(uri)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp)
                                .shadow(
                                    elevation = 4.dp,
                                    shape = RoundedCornerShape(16.dp),
                                    spotColor = Color(0xFF9C27B0).copy(alpha = 0.2f)
                                ),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Black,
                                contentColor = Color.White
                            ),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = 0.dp,
                                pressedElevation = 2.dp,
                                hoveredElevation = 4.dp
                            )
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_camera),
                                    contentDescription = "Camera",
                                    modifier = Modifier.size(24.dp),
                                    tint = Color.White
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    "Take Photo",
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = FontWeight.Medium,
                                        color = Color.White
                                    )
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
                                Box {  // Wrap Image in Box to properly position the overlay and revert button
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

                                    // Add revert button
                                    val canUndo by viewModel.canUndo.collectAsState()
                                    RevertButton(
                                        onRevert = { viewModel.undoLastEdit() },
                                        enabled = canUndo,
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(24.dp)
                                    )
                                }

                                // Rotation controls
                                ImageRotationControls(
                                    onRotate = { degrees ->
                                        viewModel.processEditCommand("rotate image by $degrees degrees", bitmap)
                                    }
                                )

                                // Effects row
                                EffectsRow(
                                    currentImage = bitmap,
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
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
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
    }
}

@Composable
private fun EffectsRow(
    currentImage: Bitmap,
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
        "Black & White" to { bitmap: Bitmap -> ImageEditorUtils.applyBlackAndWhite(bitmap) },
        "Sepia" to { bitmap: Bitmap -> ImageEditorUtils.applySepia(bitmap) },
        "Blur" to { bitmap: Bitmap -> ImageEditorUtils.applyBlur(context, bitmap, 5f) },
        "Sharpen" to { bitmap: Bitmap -> ImageEditorUtils.applySharpen(bitmap, 50f) },
        "Vignette" to { bitmap: Bitmap -> ImageEditorUtils.applyVignette(bitmap, 50f) },
        "High Contrast" to { bitmap: Bitmap -> ImageEditorUtils.adjustContrast(bitmap, 50f) },
        "Saturate" to { bitmap: Bitmap -> ImageEditorUtils.adjustSaturation(bitmap, 50f) },
        "Warm" to { bitmap: Bitmap -> ImageEditorUtils.adjustTemperature(bitmap, 30f) },
        "Cool" to { bitmap: Bitmap -> ImageEditorUtils.adjustTemperature(bitmap, -30f) }
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
    IconButton(
        onClick = onRevert,
        enabled = enabled,
        modifier = modifier
            .size(48.dp)
            .background(
                color = if (enabled) 
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                else 
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
                shape = CircleShape
            )
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_undo),
            contentDescription = "Revert last edit",
            tint = if (enabled) 
                MaterialTheme.colorScheme.primary 
            else 
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
            modifier = Modifier.size(24.dp)
        )
    }
}
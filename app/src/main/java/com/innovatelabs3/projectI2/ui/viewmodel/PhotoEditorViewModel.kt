package com.innovatelabs3.projectI2.ui.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.innovatelabs3.projectI2.domain.SystemQueriesForImageEditor
import com.innovatelabs3.projectI2.domain.SystemQueriesForImageEditor.EditType
import com.innovatelabs3.projectI2.domain.SystemQueriesForImageEditor.ImageEditResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.innovatelabs3.projectI2.utils.ImageEditorUtils
import android.app.Application
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PhotoEditorViewModel(application: Application) : AndroidViewModel(application) {

    private val _currentImage = MutableStateFlow<Bitmap?>(null)
    val currentImage: StateFlow<Bitmap?> = _currentImage

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing

    private val _editResponse = MutableStateFlow<ImageEditResponse?>(null)
    val editResponse: StateFlow<ImageEditResponse?> = _editResponse

    private val _showToast = MutableStateFlow<String?>(null)
    val showToast: StateFlow<String?> = _showToast

    fun processEditCommand(command: String, image: Bitmap) {
        viewModelScope.launch {
            try {
                _isProcessing.value = true
                _showToast.value = "Processing your request..."

                val editResponse = SystemQueriesForImageEditor.analyzeEditCommand(command)
                _editResponse.value = editResponse
                
                when (editResponse.editType) {
                    EditType.BLACK_AND_WHITE -> applyBlackAndWhite(image)
                    EditType.BRIGHTNESS -> applyBrightness(image, editResponse.parameters["value"] as Float)
                    EditType.CONTRAST -> applyContrast(image, editResponse.parameters["value"] as Float)
                    EditType.SATURATION -> applySaturation(image, editResponse.parameters["value"] as Float)
                    EditType.BLUR -> applyBlur(image, editResponse.parameters["radius"] as Float)
                    EditType.ROTATE -> {
                        val degrees = editResponse.parameters["degrees"] as Float
                        val result = ImageEditorUtils.rotateImage(image, degrees)
                        _currentImage.value = result
                    }
                    EditType.SHARPEN -> applySharpen(image, editResponse.parameters["value"] as Float)
                    EditType.HUE -> applyHue(image, editResponse.parameters["value"] as Float)
                    EditType.SEPIA -> applySepia(image)
                    EditType.VIGNETTE -> applyVignette(image, editResponse.parameters["value"] as Float)
                    EditType.TEMPERATURE -> applyTemperature(image, editResponse.parameters["value"] as Float)
                    EditType.NOISE_REDUCTION -> applyNoiseReduction(image, editResponse.parameters["value"] as Float)
                    EditType.UNKNOWN -> {
                        _showToast.value = "Sorry, I couldn't understand that edit command"
                    }
                    else -> {
                        _showToast.value = "This edit type is not supported yet"
                    }
                }

                _showToast.value = editResponse.description
                
            } catch (e: Exception) {
                _showToast.value = "Error processing edit command: ${e.message}"
            } finally {
                _isProcessing.value = false
            }
        }
    }

    private fun applyBlackAndWhite(image: Bitmap) {
        val result = ImageEditorUtils.applyBlackAndWhite(image)
        _currentImage.value = result
    }

    private fun applyBrightness(image: Bitmap, value: Float) {
        val result = ImageEditorUtils.adjustBrightness(image, value)
        _currentImage.value = result
    }

    private fun applyContrast(image: Bitmap, value: Float) {
        val result = ImageEditorUtils.adjustContrast(image, value)
        _currentImage.value = result
    }

    private fun applySaturation(image: Bitmap, value: Float) {
        val result = ImageEditorUtils.adjustSaturation(image, value)
        _currentImage.value = result
    }

    private fun applyBlur(image: Bitmap, radius: Float) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val context = getApplication<Application>().applicationContext
                val result = ImageEditorUtils.applyBlur(context, image, radius)
                _currentImage.value = result
            }
        }
    }

    private fun applyRotation(image: Bitmap, degrees: Float) {
        val result = ImageEditorUtils.rotateImage(image, degrees)
        _currentImage.value = result
    }

    private fun applySharpen(image: Bitmap, value: Float) {
        val result = ImageEditorUtils.applySharpen(image, value)
        _currentImage.value = result
    }

    private fun applyHue(image: Bitmap, value: Float) {
        val result = ImageEditorUtils.adjustHue(image, value)
        _currentImage.value = result
    }

    private fun applySepia(image: Bitmap) {
        val result = ImageEditorUtils.applySepia(image)
        _currentImage.value = result
    }

    private fun applyVignette(image: Bitmap, value: Float) {
        val result = ImageEditorUtils.applyVignette(image, value)
        _currentImage.value = result
    }

    private fun applyTemperature(image: Bitmap, value: Float) {
        val result = ImageEditorUtils.adjustTemperature(image, value)
        _currentImage.value = result
    }

    private fun applyNoiseReduction(image: Bitmap, value: Float) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val context = getApplication<Application>().applicationContext
                val result = ImageEditorUtils.applyNoiseReduction(context, image, value)
                _currentImage.value = result
            }
        }
    }

    fun clearToast() {
        _showToast.value = null
    }

    fun updateImage(bitmap: Bitmap) {
        _currentImage.value = bitmap
    }
} 
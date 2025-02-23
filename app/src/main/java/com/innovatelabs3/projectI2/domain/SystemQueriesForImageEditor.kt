package com.innovatelabs3.projectI2.domain

import android.graphics.Bitmap
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.GenerateContentResponse
import com.google.ai.client.generativeai.type.SafetySetting
import com.google.ai.client.generativeai.type.HarmCategory
import com.google.ai.client.generativeai.type.BlockThreshold
import com.innovatelabs3.projectI2.BuildConfig

sealed class SystemQueriesForImageEditor {
    data class ImageEditRequest(
        val command: String,
        val currentImage: Bitmap
    )

    data class ImageEditResponse(
        val editType: EditType,
        val parameters: Map<String, Any> = emptyMap(),
        val description: String = ""
    )

    enum class EditType {
        BLACK_AND_WHITE,
        BRIGHTNESS,
        CONTRAST,
        SATURATION,
        BLUR,
        ROTATE,
        FLIP,
        CROP,
        RESIZE,
        FILTER,
        SHARPEN,
        HUE,
        SEPIA,
        VIGNETTE,
        TEMPERATURE,
        TINT,
        HDR,
        NOISE_REDUCTION,
        GRAYSCALE,
        INVERT,
        VINTAGE,
        PIXELATE,
        SKETCH,
        CINEMATIC,
        VIBRANT,
        NATURAL,
        DRAMATIC,
        MATTE,
        FILM,
        WARM_VINTAGE,
        COOL_TONE,
        UNKNOWN
    }

    companion object {
        private val analyzerModel = GenerativeModel(
            modelName = "gemini-1.5-flash-8b",
            apiKey = BuildConfig.GEMINI_API_KEY,
            safetySettings = listOf(
                SafetySetting(harmCategory = HarmCategory.HARASSMENT, threshold = BlockThreshold.NONE),
                SafetySetting(harmCategory = HarmCategory.HATE_SPEECH, threshold = BlockThreshold.NONE),
                SafetySetting(harmCategory = HarmCategory.SEXUALLY_EXPLICIT, threshold = BlockThreshold.NONE),
                SafetySetting(harmCategory = HarmCategory.DANGEROUS_CONTENT, threshold = BlockThreshold.NONE)
            )
        )

        private val analyzerChat = analyzerModel.startChat()

        suspend fun analyzeEditCommand(command: String): ImageEditResponse {
            val analysisPrompt = """
                Analyze this image editing command and respond with the edit type and parameters.
                Respond with only the edit type and any numerical parameters if present.
                
                Available edit types:
                BLACK_AND_WHITE - for grayscale or black and white conversion
                BRIGHTNESS - for adjusting image brightness (value between -100 to 100)
                CONTRAST - for adjusting image contrast (value between -100 to 100)
                SATURATION - for adjusting color saturation (value between -100 to 100)
                BLUR - for adding blur effect (radius between 1 to 25)
                ROTATE - for rotating image (degrees: 90, 180, 270)
                FLIP - for flipping image (horizontal or vertical)
                CROP - for cropping image
                RESIZE - for resizing image
                FILTER - for applying preset filters
                SHARPEN - for sharpening image (value between 0 to 100)
                HUE - for adjusting color hue (value between -180 to 180)
                SEPIA - for applying sepia tone
                VIGNETTE - for adding vignette effect (value between 0 to 100)
                TEMPERATURE - for adjusting color temperature (value between -100 to 100)
                TINT - for adding color tint
                HDR - for HDR effect
                NOISE_REDUCTION - for reducing image noise (value between 0 to 100)
                GRAYSCALE - for converting image to grayscale
                INVERT - for inverting image colors
                VINTAGE - for applying vintage effect
                PIXELATE - for pixelating image (value between 1 to 100)
                SKETCH - for converting image to sketch
                CINEMATIC - for adding cinematic effect
                VIBRANT - for adding vibrant effect
                NATURAL - for adding natural effect
                DRAMATIC - for adding dramatic effect
                MATTE - for adding matte effect
                FILM - for adding film effect
                WARM_VINTAGE - for adding warm vintage effect
                COOL_TONE - for adding cool tone effect
                
                Examples:
                "Make the image black and white" -> BLACK_AND_WHITE
                "Increase brightness by 50" -> BRIGHTNESS:50
                "Decrease contrast by 30" -> CONTRAST:-30
                "Add blur effect" -> BLUR:5
                "Rotate image by 90 degrees" -> ROTATE:90
                "Make it more saturated" -> SATURATION:30
                "Apply sepia effect" -> SEPIA
                "Reduce noise in the image" -> NOISE_REDUCTION:50
                "Make the image warmer" -> TEMPERATURE:30
                "Add vignette effect" -> VIGNETTE:50
                
                Command: "$command"
            """.trimIndent()

            val response = analyzerChat.sendMessage(analysisPrompt).text?.trim() ?: return ImageEditResponse(EditType.UNKNOWN)
            
            return parseResponse(response)
        }

        private fun parseResponse(response: String): ImageEditResponse {
            val parts = response.split(":")
            val editType = parts[0].trim()
            val value = if (parts.size > 1) parts[1].toFloatOrNull() else null

            return when (editType) {
                "BLACK_AND_WHITE" -> ImageEditResponse(
                    editType = EditType.BLACK_AND_WHITE,
                    description = "Converting image to black and white"
                )
                "BRIGHTNESS" -> ImageEditResponse(
                    editType = EditType.BRIGHTNESS,
                    parameters = mapOf("value" to (value ?: 0f)),
                    description = "Adjusting brightness by ${value ?: 0}"
                )
                "CONTRAST" -> ImageEditResponse(
                    editType = EditType.CONTRAST,
                    parameters = mapOf("value" to (value ?: 0f)),
                    description = "Adjusting contrast by ${value ?: 0}"
                )
                "SATURATION" -> ImageEditResponse(
                    editType = EditType.SATURATION,
                    parameters = mapOf("value" to (value ?: 0f)),
                    description = "Adjusting saturation by ${value ?: 0}"
                )
                "BLUR" -> ImageEditResponse(
                    editType = EditType.BLUR,
                    parameters = mapOf("radius" to (value ?: 5f)),
                    description = "Applying blur with radius ${value ?: 5}"
                )
                "ROTATE" -> ImageEditResponse(
                    editType = EditType.ROTATE,
                    parameters = mapOf("degrees" to (value ?: 90f)),
                    description = "Rotating image by ${value ?: 90} degrees"
                )
                "SHARPEN" -> ImageEditResponse(
                    editType = EditType.SHARPEN,
                    parameters = mapOf("value" to (value ?: 50f)),
                    description = "Sharpening image by ${value ?: 50}"
                )
                "HUE" -> ImageEditResponse(
                    editType = EditType.HUE,
                    parameters = mapOf("value" to (value ?: 0f)),
                    description = "Adjusting hue by ${value ?: 0}"
                )
                "SEPIA" -> ImageEditResponse(
                    editType = EditType.SEPIA,
                    description = "Applying sepia effect"
                )
                "VIGNETTE" -> ImageEditResponse(
                    editType = EditType.VIGNETTE,
                    parameters = mapOf("value" to (value ?: 50f)),
                    description = "Adding vignette effect"
                )
                "TEMPERATURE" -> ImageEditResponse(
                    editType = EditType.TEMPERATURE,
                    parameters = mapOf("value" to (value ?: 0f)),
                    description = "Adjusting color temperature by ${value ?: 0}"
                )
                "NOISE_REDUCTION" -> ImageEditResponse(
                    editType = EditType.NOISE_REDUCTION,
                    parameters = mapOf("value" to (value ?: 50f)),
                    description = "Reducing image noise by ${value ?: 50}"
                )
                "GRAYSCALE" -> ImageEditResponse(
                    editType = EditType.GRAYSCALE,
                    description = "Converting to grayscale"
                )
                "INVERT" -> ImageEditResponse(
                    editType = EditType.INVERT,
                    description = "Inverting colors"
                )
                "VINTAGE" -> ImageEditResponse(
                    editType = EditType.VINTAGE,
                    description = "Applying vintage effect"
                )
                "PIXELATE" -> ImageEditResponse(
                    editType = EditType.PIXELATE,
                    parameters = mapOf("value" to (value ?: 10f)),
                    description = "Pixelating image"
                )
                "SKETCH" -> ImageEditResponse(
                    editType = EditType.SKETCH,
                    description = "Converting to sketch"
                )
                "CINEMATIC" -> ImageEditResponse(
                    editType = EditType.CINEMATIC,
                    description = "Adding cinematic effect"
                )
                "VIBRANT" -> ImageEditResponse(
                    editType = EditType.VIBRANT,
                    description = "Adding vibrant effect"
                )
                "NATURAL" -> ImageEditResponse(
                    editType = EditType.NATURAL,
                    description = "Adding natural effect"
                )
                "DRAMATIC" -> ImageEditResponse(
                    editType = EditType.DRAMATIC,
                    description = "Adding dramatic effect"
                )
                "MATTE" -> ImageEditResponse(
                    editType = EditType.MATTE,
                    description = "Adding matte effect"
                )
                "FILM" -> ImageEditResponse(
                    editType = EditType.FILM,
                    description = "Adding film effect"
                )
                "WARM_VINTAGE" -> ImageEditResponse(
                    editType = EditType.WARM_VINTAGE,
                    description = "Adding warm vintage effect"
                )
                "COOL_TONE" -> ImageEditResponse(
                    editType = EditType.COOL_TONE,
                    description = "Adding cool tone effect"
                )
                else -> ImageEditResponse(EditType.UNKNOWN)
            }
        }
    }
} 
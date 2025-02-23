package com.innovatelabs3.projectI2.utils

import android.content.Context
import android.graphics.*
import android.renderscript.*
import android.content.ContentValues
import android.os.Build
import android.os.Environment
import android.provider.MediaStore

object ImageEditorUtils {

    fun applyBlackAndWhite(source: Bitmap): Bitmap {
        val width = source.width
        val height = source.height
        val result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)

        val paint = Paint().apply {
            colorFilter = ColorMatrixColorFilter(ColorMatrix().apply {
                setSaturation(0f)
            })
        }

        canvas.drawBitmap(source, 0f, 0f, paint)
        return result
    }

    fun adjustBrightness(source: Bitmap, value: Float): Bitmap {
        // Normalize value from -100 to 100 to -255 to 255
        val brightness = (value * 2.55f).toInt()
        
        val width = source.width
        val height = source.height
        val result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        val canvas = Canvas(result)
        val paint = Paint()
        val colorMatrix = ColorMatrix(floatArrayOf(
            1f, 0f, 0f, 0f, brightness.toFloat(),
            0f, 1f, 0f, 0f, brightness.toFloat(),
            0f, 0f, 1f, 0f, brightness.toFloat(),
            0f, 0f, 0f, 1f, 0f
        ))

        paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
        canvas.drawBitmap(source, 0f, 0f, paint)
        return result
    }

    fun adjustContrast(source: Bitmap, value: Float): Bitmap {
        // Normalize value from -100 to 100 to 0.5 to 1.5
        val contrast = (value + 100) / 100f
        
        val width = source.width
        val height = source.height
        val result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        val canvas = Canvas(result)
        val paint = Paint()
        val colorMatrix = ColorMatrix(floatArrayOf(
            contrast, 0f, 0f, 0f, 0f,
            0f, contrast, 0f, 0f, 0f,
            0f, 0f, contrast, 0f, 0f,
            0f, 0f, 0f, 1f, 0f
        ))

        paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
        canvas.drawBitmap(source, 0f, 0f, paint)
        return result
    }

    fun adjustSaturation(source: Bitmap, value: Float): Bitmap {
        // Normalize value from -100 to 100 to 0 to 2
        val saturation = (value + 100) / 100f
        
        val width = source.width
        val height = source.height
        val result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        val canvas = Canvas(result)
        val paint = Paint()
        val colorMatrix = ColorMatrix()
        colorMatrix.setSaturation(saturation)

        paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
        canvas.drawBitmap(source, 0f, 0f, paint)
        return result
    }

    fun applyBlur(context: Context, source: Bitmap, radius: Float): Bitmap {
        try {
            // Create output bitmap
            val output = Bitmap.createBitmap(
                source.width, source.height,
                Bitmap.Config.ARGB_8888
            )

            // Create a RenderScript context
            val rs = RenderScript.create(context)

            // Create an Allocation for input
            val input = Allocation.createFromBitmap(
                rs, source,
                Allocation.MipmapControl.MIPMAP_NONE,
                Allocation.USAGE_SCRIPT
            )

            // Create an Allocation for output
            val output_allocation = Allocation.createFromBitmap(rs, output)

            // Create a ScriptIntrinsicBlur object
            val script = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs))

            // Set blur radius (0 < radius <= 25)
            script.setRadius(radius.coerceIn(0f, 25f))

            // Set input
            script.setInput(input)

            // Process and set output
            script.forEach(output_allocation)

            // Copy the output to the output bitmap
            output_allocation.copyTo(output)

            // Clean up resources
            input.destroy()
            output_allocation.destroy()
            script.destroy()
            rs.destroy()

            return output
        } catch (e: Exception) {
            e.printStackTrace()
            // Return original bitmap if blur fails
            return source
        }
    }

    fun rotateImage(source: Bitmap, degrees: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(degrees)
        return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
    }

    fun applySharpen(source: Bitmap, value: Float): Bitmap {
        // Normalize value from 0 to 100 to 0 to 1
        val sharpness = value / 100f
        
        val width = source.width
        val height = source.height
        val result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        val kernel = floatArrayOf(
            0f, -sharpness, 0f,
            -sharpness, 1 + 4 * sharpness, -sharpness,
            0f, -sharpness, 0f
        )

        val paint = Paint()
        val canvas = Canvas(result)
        
        val convolutionMatrix = ConvolutionMatrix(3)
        convolutionMatrix.setAll(kernel)
        convolutionMatrix.factor = 1f
        convolutionMatrix.offset = 0f
        
        canvas.drawBitmap(applyConvolution(source, convolutionMatrix), 0f, 0f, paint)
        return result
    }

    fun adjustHue(source: Bitmap, value: Float): Bitmap {
        val width = source.width
        val height = source.height
        val result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val pixels = IntArray(width * height)
        
        source.getPixels(pixels, 0, width, 0, 0, width, height)
        
        for (i in pixels.indices) {
            val hsv = FloatArray(3)
            Color.colorToHSV(pixels[i], hsv)
            hsv[0] = (hsv[0] + value) % 360
            pixels[i] = Color.HSVToColor(Color.alpha(pixels[i]), hsv)
        }
        
        result.setPixels(pixels, 0, width, 0, 0, width, height)
        return result
    }

    fun applySepia(source: Bitmap): Bitmap {
        val width = source.width
        val height = source.height
        val result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        val canvas = Canvas(result)
        val paint = Paint()
        val colorMatrix = ColorMatrix(floatArrayOf(
            0.393f, 0.769f, 0.189f, 0f, 0f,
            0.349f, 0.686f, 0.168f, 0f, 0f,
            0.272f, 0.534f, 0.131f, 0f, 0f,
            0f, 0f, 0f, 1f, 0f
        ))

        paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
        canvas.drawBitmap(source, 0f, 0f, paint)
        return result
    }

    fun applyVignette(source: Bitmap, value: Float): Bitmap {
        val width = source.width
        val height = source.height
        val result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        
        // Draw original image
        canvas.drawBitmap(source, 0f, 0f, null)
        
        // Create radial gradient for vignette effect
        val radialGradient = RadialGradient(
            width / 2f,
            height / 2f,
            width.coerceAtLeast(height) / 1.2f,
            intArrayOf(Color.TRANSPARENT, Color.parseColor("#88000000")),
            floatArrayOf(0.6f, 1.0f),
            Shader.TileMode.CLAMP
        )
        
        val paint = Paint().apply {
            shader = radialGradient
            alpha = (value * 2.55f).toInt()
        }
        
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
        return result
    }

    fun adjustTemperature(source: Bitmap, value: Float): Bitmap {
        val width = source.width
        val height = source.height
        val result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        
        // Normalize value from -100 (cool) to 100 (warm)
        val redOffset = value * 2.55f
        val blueOffset = -value * 2.55f
        
        val colorMatrix = ColorMatrix(floatArrayOf(
            1f, 0f, 0f, 0f, redOffset,
            0f, 1f, 0f, 0f, 0f,
            0f, 0f, 1f, 0f, blueOffset,
            0f, 0f, 0f, 1f, 0f
        ))
        
        val paint = Paint().apply {
            colorFilter = ColorMatrixColorFilter(colorMatrix)
        }
        
        Canvas(result).drawBitmap(source, 0f, 0f, paint)
        return result
    }

    fun applyNoiseReduction(context: Context, source: Bitmap, value: Float): Bitmap {
        // Using a simple box blur for noise reduction
        val radius = (value / 20f).coerceIn(0.5f, 5f)  // Convert 0-100 to 0.5-5
        return applyBlur(context, source, radius)
    }

    fun applyGrayscale(source: Bitmap): Bitmap {
        val width = source.width
        val height = source.height
        val result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)

        val paint = Paint()
        val colorMatrix = ColorMatrix(floatArrayOf(
            0.33f, 0.33f, 0.33f, 0f, 0f,
            0.33f, 0.33f, 0.33f, 0f, 0f,
            0.33f, 0.33f, 0.33f, 0f, 0f,
            0f, 0f, 0f, 1f, 0f
        ))

        paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
        canvas.drawBitmap(source, 0f, 0f, paint)
        return result
    }

    fun invertColors(source: Bitmap): Bitmap {
        val width = source.width
        val height = source.height
        val result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)

        val paint = Paint()
        val colorMatrix = ColorMatrix(floatArrayOf(
            -1f, 0f, 0f, 0f, 255f,
            0f, -1f, 0f, 0f, 255f,
            0f, 0f, -1f, 0f, 255f,
            0f, 0f, 0f, 1f, 0f
        ))

        paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
        canvas.drawBitmap(source, 0f, 0f, paint)
        return result
    }

    fun applyVintage(source: Bitmap): Bitmap {
        val width = source.width
        val height = source.height
        val result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)

        val paint = Paint()
        val colorMatrix = ColorMatrix(floatArrayOf(
            0.9f, 0.5f, 0.1f, 0f, 0f,
            0.3f, 0.8f, 0.1f, 0f, 0f,
            0.2f, 0.3f, 0.5f, 0f, 0f,
            0f, 0f, 0f, 1f, 0f
        ))

        paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
        canvas.drawBitmap(source, 0f, 0f, paint)
        return result
    }

    fun applyPixelate(source: Bitmap, pixelSize: Float): Bitmap {
        val width = source.width
        val height = source.height
        val pixelWidth = (width / pixelSize).toInt()
        val pixelHeight = (height / pixelSize).toInt()

        // Scale down
        val scaledBitmap = Bitmap.createScaledBitmap(source, pixelWidth, pixelHeight, false)
        // Scale up
        return Bitmap.createScaledBitmap(scaledBitmap, width, height, false)
    }

    fun applySketch(source: Bitmap): Bitmap {
        val width = source.width
        val height = source.height
        val result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        // First convert to grayscale
        val grayscale = applyGrayscale(source)
        
        // Apply edge detection
        val kernel = floatArrayOf(
            -1f, -1f, -1f,
            -1f, 8f, -1f,
            -1f, -1f, -1f
        )

        val convolutionMatrix = ConvolutionMatrix(3)
        convolutionMatrix.setAll(kernel)
        convolutionMatrix.factor = 1f
        convolutionMatrix.offset = 127f

        val edgeDetected = applyConvolution(grayscale, convolutionMatrix)

        // Invert colors for sketch effect
        return invertColors(edgeDetected)
    }

    private class ConvolutionMatrix(private val size: Int) {
        var matrix: FloatArray = FloatArray(size * size)
        var factor = 1.0f
        var offset = 1.0f

        fun setAll(values: FloatArray) {
            matrix = values
        }
    }

    private fun applyConvolution(source: Bitmap, convMatrix: ConvolutionMatrix): Bitmap {
        val width = source.width
        val height = source.height
        val result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        val pixels = IntArray(width * height)
        source.getPixels(pixels, 0, width, 0, 0, width, height)
        val returnPixels = IntArray(pixels.size)

        // Apply convolution matrix to each pixel
        for (i in 0 until height - 2) {
            for (j in 0 until width - 2) {
                var red = 0f
                var green = 0f
                var blue = 0f
                var alpha = 0f

                // Apply the convolution matrix
                for (k in 0..2) {
                    for (l in 0..2) {
                        val pixel = pixels[(i + k) * width + j + l]
                        val matrixValue = convMatrix.matrix[k * 3 + l]

                        alpha += Color.alpha(pixel) * matrixValue
                        red += Color.red(pixel) * matrixValue
                        green += Color.green(pixel) * matrixValue
                        blue += Color.blue(pixel) * matrixValue
                    }
                }

                // Normalize values
                alpha = (alpha * convMatrix.factor + convMatrix.offset).coerceIn(0f, 255f)
                red = (red * convMatrix.factor + convMatrix.offset).coerceIn(0f, 255f)
                green = (green * convMatrix.factor + convMatrix.offset).coerceIn(0f, 255f)
                blue = (blue * convMatrix.factor + convMatrix.offset).coerceIn(0f, 255f)

                returnPixels[(i + 1) * width + j + 1] = Color.argb(
                    alpha.toInt(),
                    red.toInt(),
                    green.toInt(),
                    blue.toInt()
                )
            }
        }

        result.setPixels(returnPixels, 0, width, 0, 0, width, height)
        return result
    }

    fun applyCinematic(source: Bitmap): Bitmap {
        val width = source.width
        val height = source.height
        val result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)

        // Cinematic look with contrast, teal shadows and orange highlights
        val paint = Paint()
        val colorMatrix = ColorMatrix(floatArrayOf(
            1.3f, -0.1f, 0.1f, 0f, -20f,  // Red channel
            -0.1f, 1.3f, 0.1f, 0f, -10f,  // Green channel
            0.1f, 0.1f, 1.1f, 0f, -30f,   // Blue channel
            0f, 0f, 0f, 1f, 0f
        ))

        paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
        canvas.drawBitmap(source, 0f, 0f, paint)
        return result
    }

    fun applyVibrant(source: Bitmap): Bitmap {
        val width = source.width
        val height = source.height
        val result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)

        // Enhance saturation and contrast for vibrant look
        val paint = Paint()
        val colorMatrix = ColorMatrix()
        colorMatrix.setSaturation(1.5f) // Increase saturation
        
        val contrastMatrix = ColorMatrix(floatArrayOf(
            1.2f, 0f, 0f, 0f, 10f,
            0f, 1.2f, 0f, 0f, 10f,
            0f, 0f, 1.2f, 0f, 10f,
            0f, 0f, 0f, 1f, 0f
        ))
        
        colorMatrix.postConcat(contrastMatrix)
        paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
        canvas.drawBitmap(source, 0f, 0f, paint)
        return result
    }

    fun applyNatural(source: Bitmap): Bitmap {
        val width = source.width
        val height = source.height
        val result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)

        // Subtle enhancement while maintaining natural look
        val paint = Paint()
        val colorMatrix = ColorMatrix(floatArrayOf(
            1.1f, 0f, 0f, 0f, 5f,
            0f, 1.1f, 0f, 0f, 5f,
            0f, 0f, 1.1f, 0f, 5f,
            0f, 0f, 0f, 1f, 0f
        ))

        paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
        canvas.drawBitmap(source, 0f, 0f, paint)
        return result
    }

    fun applyDramatic(source: Bitmap): Bitmap {
        val width = source.width
        val height = source.height
        val result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)

        // High contrast dramatic look
        val paint = Paint()
        val colorMatrix = ColorMatrix(floatArrayOf(
            1.5f, 0f, 0f, 0f, -30f,
            0f, 1.5f, 0f, 0f, -30f,
            0f, 0f, 1.5f, 0f, -30f,
            0f, 0f, 0f, 1f, 0f
        ))

        paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
        canvas.drawBitmap(source, 0f, 0f, paint)
        return result
    }

    fun applyMatte(source: Bitmap): Bitmap {
        val width = source.width
        val height = source.height
        val result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)

        // Matte film look with reduced contrast
        val paint = Paint()
        val colorMatrix = ColorMatrix(floatArrayOf(
            0.9f, 0f, 0f, 0f, 20f,
            0f, 0.9f, 0f, 0f, 20f,
            0f, 0f, 0.9f, 0f, 20f,
            0f, 0f, 0f, 1f, 0f
        ))

        paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
        canvas.drawBitmap(source, 0f, 0f, paint)
        return result
    }

    fun applyFilm(source: Bitmap): Bitmap {
        val width = source.width
        val height = source.height
        val result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)

        // Classic film look
        val paint = Paint()
        val colorMatrix = ColorMatrix(floatArrayOf(
            1.3f, 0f, 0f, 0f, -10f,
            0f, 1.1f, 0f, 0f, -10f,
            0f, 0f, 1.0f, 0f, -10f,
            0f, 0f, 0f, 1f, 0f
        ))

        paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
        canvas.drawBitmap(source, 0f, 0f, paint)
        return result
    }

    fun saveImageToGallery(context: Context, bitmap: Bitmap): Boolean {
        return try {
            val filename = "edited_image_${System.currentTimeMillis()}.jpg"
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                }
            }

            val resolver = context.contentResolver
            val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

            uri?.let {
                resolver.openOutputStream(it)?.use { stream ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    contentValues.clear()
                    contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                    resolver.update(it, contentValues, null, null)
                }
                true
            } ?: false
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
} 
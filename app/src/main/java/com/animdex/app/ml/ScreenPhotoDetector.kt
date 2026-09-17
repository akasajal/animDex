package com.animdex.app.ml

import android.graphics.Bitmap
import android.graphics.Color
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.task.vision.classifier.ImageClassifier
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

enum class SpoofType {
    DIGITAL_SCREEN,
    PRINTED_PHOTO
}

data class LivenessResult(
    val isRealWildlife: Boolean,
    val spoofType: SpoofType? = null,
    val reason: String = ""
)

object ScreenPhotoDetector {

    private val SCREEN_KEYWORDS = listOf(
        "monitor", "screen", "television", "laptop", "notebook",
        "desktop computer", "cellular telephone", "hand-held computer",
        "web site", "computer keyboard", "crt screen", "display"
    )

    private val PRINT_KEYWORDS = listOf(
        "comic book", "book jacket", "menu", "envelope",
        "photocopier", "paper towel", "newspaper", "magazine", "picture frame"
    )

    /**
     * Evaluates whether a captured photo is of real, living wildlife in 3D space,
     * or a spoofed capture from a computer monitor, mobile screen, or physical photo/print.
     */
    fun analyze(bitmap: Bitmap, generalClassifier: ImageClassifier?): LivenessResult {
        // 1. Model-based check using General Classifier (ImageNet-1k)
        if (generalClassifier != null) {
            val modelCheck = checkModelClassification(bitmap, generalClassifier)
            if (!modelCheck.isRealWildlife) {
                return modelCheck
            }
        }

        // 2. Computer Vision: Moiré pattern & high-frequency pixel grid analysis (Digital displays)
        val moireCheck = checkMoireAndPixelGrid(bitmap)
        if (!moireCheck.isRealWildlife) {
            return moireCheck
        }

        // 3. Computer Vision: Planar specular glare & dynamic range clipping
        val glareCheck = checkSpecularReflectionAndBacklight(bitmap)
        if (!glareCheck.isRealWildlife) {
            return glareCheck
        }

        // 4. Perimeter bezel / rectangular photo border detection
        val borderCheck = checkFrameBezelBorders(bitmap)
        if (!borderCheck.isRealWildlife) {
            return borderCheck
        }

        return LivenessResult(isRealWildlife = true)
    }

    private fun checkModelClassification(
        bitmap: Bitmap,
        generalClassifier: ImageClassifier
    ): LivenessResult {
        try {
            val tensorImage = TensorImage.fromBitmap(bitmap)
            val classifications = generalClassifier.classify(tensorImage)

            for (classification in classifications) {
                for (category in classification.categories) {
                    val label = category.label.lowercase().trim()
                    val score = category.score

                    // Check for digital display devices
                    for (keyword in SCREEN_KEYWORDS) {
                        if (label.contains(keyword) && score >= 0.12f) {
                            val deviceName = keyword.replaceFirstChar { it.titlecase() }
                            return LivenessResult(
                                isRealWildlife = false,
                                spoofType = SpoofType.DIGITAL_SCREEN,
                                reason = "Screen detected ($deviceName). AnimDex only registers real animals in the wild, not captures from computer monitors or secondary screens."
                            )
                        }
                    }

                    // Check for physical prints or book jackets
                    for (keyword in PRINT_KEYWORDS) {
                        if (label.contains(keyword) && score >= 0.15f) {
                            return LivenessResult(
                                isRealWildlife = false,
                                spoofType = SpoofType.PRINTED_PHOTO,
                                reason = "Printed photo or media detected. AnimDex requires live, physical wildlife sightings in nature."
                            )
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return LivenessResult(isRealWildlife = true)
    }

    /**
     * Analyzes high-frequency spatial gradients to detect subpixel matrix patterns
     * and optical Moiré fringes that only occur when a camera captures an LCD/OLED panel.
     */
    private fun checkMoireAndPixelGrid(bitmap: Bitmap): LivenessResult {
        // Downscale a center crop to 140x140 for fast sub-millisecond analysis
        val cropSize = 140
        val startX = max(0, (bitmap.width - cropSize) / 2)
        val startY = max(0, (bitmap.height - cropSize) / 2)
        val sampleW = min(cropSize, bitmap.width)
        val sampleH = min(cropSize, bitmap.height)

        if (sampleW < 30 || sampleH < 30) return LivenessResult(isRealWildlife = true)

        val pixels = IntArray(sampleW * sampleH)
        bitmap.getPixels(pixels, 0, sampleW, startX, startY, sampleW, sampleH)

        var rapidAlternations = 0
        var totalTransitions = 0
        var subpixelDispersionCount = 0

        for (y in 2 until sampleH - 2) {
            val rowOffset = y * sampleW
            for (x in 2 until sampleW - 2) {
                val c0 = pixels[rowOffset + x - 1]
                val c1 = pixels[rowOffset + x]
                val c2 = pixels[rowOffset + x + 1]

                val l0 = (Color.red(c0) * 299 + Color.green(c0) * 587 + Color.blue(c0) * 114) / 1000
                val l1 = (Color.red(c1) * 299 + Color.green(c1) * 587 + Color.blue(c1) * 114) / 1000
                val l2 = (Color.red(c2) * 299 + Color.green(c2) * 587 + Color.blue(c2) * 114) / 1000

                val d1 = l1 - l0
                val d2 = l2 - l1

                // Characteristic alternating subpixel zigzag (high-frequency nyquist grating)
                if ((d1 > 10 && d2 < -10) || (d1 < -10 && d2 > 10)) {
                    rapidAlternations++
                }

                // Subpixel RGB color dispersion (adjacent red vs blue oscillations in LCD matrix)
                val rg1 = Color.red(c1) - Color.green(c1)
                val rg2 = Color.red(c2) - Color.green(c2)
                if ((rg1 > 15 && rg2 < -15) || (rg1 < -15 && rg2 > 15)) {
                    subpixelDispersionCount++
                }

                totalTransitions++
            }
        }

        val highFreqRatio = if (totalTransitions > 0) rapidAlternations.toFloat() / totalTransitions else 0f
        val dispersionRatio = if (totalTransitions > 0) subpixelDispersionCount.toFloat() / totalTransitions else 0f

        // LCD / OLED displays produce very high spatial alternation and color dispersion
        if (highFreqRatio > 0.28f && dispersionRatio > 0.16f) {
            return LivenessResult(
                isRealWildlife = false,
                spoofType = SpoofType.DIGITAL_SCREEN,
                reason = "Digital display pixel grid / Moiré pattern detected. Point your camera at a living animal in the real world."
            )
        }

        return LivenessResult(isRealWildlife = true)
    }

    /**
     * Checks for harsh specular light reflections off flat glass/glossy photo paper,
     * and unnatural backlight illumination pedestals.
     */
    private fun checkSpecularReflectionAndBacklight(bitmap: Bitmap): LivenessResult {
        val sampleSize = 100
        val stepX = max(1, bitmap.width / sampleSize)
        val stepY = max(1, bitmap.height / sampleSize)

        var clippedHighlightCount = 0
        var backlightPedestalCount = 0
        var totalSamples = 0

        for (y in 0 until bitmap.height step stepY) {
            for (x in 0 until bitmap.width step stepX) {
                val pixel = bitmap.getPixel(x, y)
                val r = Color.red(pixel)
                val g = Color.green(pixel)
                val b = Color.blue(pixel)

                // Blown out specular reflection on flat screen glass
                if (r > 250 && g > 250 && b > 250) {
                    clippedHighlightCount++
                }

                // LCD backlight glow: elevated black level where dark areas are artificially lit by LED backlight
                val lum = (r * 299 + g * 587 + b * 114) / 1000
                if (lum in 18..45 && abs(r - g) < 6 && abs(g - b) < 6) {
                    backlightPedestalCount++
                }

                totalSamples++
            }
        }

        val glareRatio = if (totalSamples > 0) clippedHighlightCount.toFloat() / totalSamples else 0f
        val pedestalRatio = if (totalSamples > 0) backlightPedestalCount.toFloat() / totalSamples else 0f

        // Unnatural glare hotspot + LCD backlight pedestal
        if (glareRatio > 0.08f && pedestalRatio > 0.35f) {
            return LivenessResult(
                isRealWildlife = false,
                spoofType = SpoofType.DIGITAL_SCREEN,
                reason = "Monitor glass glare & backlight bloom detected. AnimDex requires real outdoor/indoor wildlife."
            )
        }

        return LivenessResult(isRealWildlife = true)
    }

    /**
     * Detects straight border bezels (e.g. laptop/monitor plastic borders or white photo margins).
     */
    private fun checkFrameBezelBorders(bitmap: Bitmap): LivenessResult {
        val w = bitmap.width
        val h = bitmap.height
        if (w < 100 || h < 100) return LivenessResult(isRealWildlife = true)

        // Sample top border (first 4% of image) and center
        val topBand = IntArray(w)
        val bottomBand = IntArray(w)
        bitmap.getPixels(topBand, 0, w, 0, max(0, (h * 0.02f).toInt()), w, 1)
        bitmap.getPixels(bottomBand, 0, w, 0, min(h - 1, (h * 0.98f).toInt()), w, 1)

        var topUniform = true
        var bottomUniform = true
        val topFirst = topBand[0]
        val bottomFirst = bottomBand[0]

        for (i in 1 until w step 5) {
            if (colorDiff(topBand[i], topFirst) > 20) topUniform = false
            if (colorDiff(bottomBand[i], bottomFirst) > 20) bottomUniform = false
        }

        // If top or bottom margin is a perfectly uniform dark plastic bezel or white print margin
        if (topUniform && isBezelColor(topFirst)) {
            return LivenessResult(
                isRealWildlife = false,
                spoofType = SpoofType.DIGITAL_SCREEN,
                reason = "Display bezel / frame detected. Align camera directly on real fauna."
            )
        }

        if (bottomUniform && isBezelColor(bottomFirst)) {
            return LivenessResult(
                isRealWildlife = false,
                spoofType = SpoofType.PRINTED_PHOTO,
                reason = "Photo paper margin detected. Please capture live wildlife."
            )
        }

        return LivenessResult(isRealWildlife = true)
    }

    private fun colorDiff(c1: Int, c2: Int): Int {
        return abs(Color.red(c1) - Color.red(c2)) +
                abs(Color.green(c1) - Color.green(c2)) +
                abs(Color.blue(c1) - Color.blue(c2))
    }

    private fun isBezelColor(pixel: Int): Boolean {
        val r = Color.red(pixel)
        val g = Color.green(pixel)
        val b = Color.blue(pixel)
        val lum = (r * 299 + g * 587 + b * 114) / 1000
        // Near-black monitor bezel or pure paper white margin
        return (lum < 25 && abs(r - g) < 8 && abs(g - b) < 8) || (lum > 245 && abs(r - g) < 5 && abs(g - b) < 5)
    }
}

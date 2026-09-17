package com.animdex.app.ml

import android.graphics.Bitmap
import android.graphics.Color
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.task.vision.classifier.ImageClassifier
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

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
        "monitor", "screen", "television", "tv", "laptop", "notebook",
        "desktop computer", "cellular telephone", "cellphone", "hand-held computer",
        "web site", "website", "computer keyboard", "keypad", "crt screen",
        "display", "touch screen", "flat panel", "computer mouse", "mouse, computer mouse"
    )

    private val PRINT_KEYWORDS = listOf(
        "comic book", "book jacket", "dust cover", "menu", "envelope",
        "photocopier", "paper towel", "newspaper", "magazine", "picture frame",
        "packet", "carton", "poster", "binder"
    )

    /**
     * Evaluates whether a captured photo is of real, living wildlife in 3D space,
     * or a spoofed capture from a computer monitor, mobile screen, or physical photo/print.
     */
    fun analyze(bitmap: Bitmap, livenessClassifier: ImageClassifier?): LivenessResult {
        // 1. High-sensitivity model-based check (ImageNet-1k broad classifier)
        if (livenessClassifier != null) {
            val modelCheck = checkModelClassification(bitmap, livenessClassifier)
            if (!modelCheck.isRealWildlife) {
                return modelCheck
            }
        }

        // Downscale to standardized 256x256 working size for fast sub-frame signal analysis
        val scaledBitmap = if (bitmap.width == 256 && bitmap.height == 256) {
            bitmap
        } else {
            Bitmap.createScaledBitmap(bitmap, 256, 256, true)
        }

        // 2. Web page / digital canvas background detection (flat neutral whites or dark-mode backgrounds)
        val webCanvasCheck = checkWebCanvasBackground(scaledBitmap)
        if (!webCanvasCheck.isRealWildlife) {
            return webCanvasCheck
        }

        // 3. Rolling shutter refresh banding (PWM / 60Hz/120Hz display flicker waves)
        val refreshBandingCheck = checkRollingShutterRefreshBanding(scaledBitmap)
        if (!refreshBandingCheck.isRealWildlife) {
            return refreshBandingCheck
        }

        // 4. Moiré pattern & periodic pixel lattice interference (spatial autocorrelation)
        val moireCheck = checkMoireAutocorrelation(scaledBitmap)
        if (!moireCheck.isRealWildlife) {
            return moireCheck
        }

        // 5. Specular glare off flat screen glass & LCD backlight pedestal
        val glareCheck = checkSpecularReflectionAndBacklight(scaledBitmap)
        if (!glareCheck.isRealWildlife) {
            return glareCheck
        }

        // 6. Straight rectangular image container / monitor bezel borders
        val borderCheck = checkFrameBezelBorders(scaledBitmap)
        if (!borderCheck.isRealWildlife) {
            return borderCheck
        }

        return LivenessResult(isRealWildlife = true)
    }

    /**
     * Queries ImageNet-1k vision model for any display, website, monitor, or print predictions.
     * Uses a sensitive 0.02 (2%) threshold across all top predictions.
     */
    private fun checkModelClassification(
        bitmap: Bitmap,
        classifier: ImageClassifier
    ): LivenessResult {
        try {
            val tensorImage = TensorImage.fromBitmap(bitmap)
            val classifications = classifier.classify(tensorImage)

            for (classification in classifications) {
                for (category in classification.categories) {
                    val label = category.label.lowercase().trim()
                    val score = category.score

                    // Check for digital display & web devices
                    for (keyword in SCREEN_KEYWORDS) {
                        if (label.contains(keyword) && score >= 0.020f) {
                            val deviceName = keyword.replaceFirstChar { it.titlecase() }
                            return LivenessResult(
                                isRealWildlife = false,
                                spoofType = SpoofType.DIGITAL_SCREEN,
                                reason = "Digital screen or web page detected ($deviceName). AnimDex requires real live wildlife in nature, not secondary screens."
                            )
                        }
                    }

                    // Check for physical prints, book jackets, or paper media
                    for (keyword in PRINT_KEYWORDS) {
                        if (label.contains(keyword) && score >= 0.025f) {
                            return LivenessResult(
                                isRealWildlife = false,
                                spoofType = SpoofType.PRINTED_PHOTO,
                                reason = "Printed publication or media detected. AnimDex only registers living animals discovered outdoors."
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
     * Web pages typically have large areas of pure neutral digital white (#FFFFFF / #F8F9FA)
     * or dark-mode backgrounds (#121212) with near-zero local texture variance.
     * Real natural outdoor captures almost never exhibit flat zero-variance pure neutral white.
     */
    private fun checkWebCanvasBackground(bitmap: Bitmap): LivenessResult {
        val w = bitmap.width
        val h = bitmap.height
        val pixels = IntArray(w * h)
        bitmap.getPixels(pixels, 0, w, 0, 0, w, h)

        var pureNeutralWhiteCount = 0
        var darkCanvasCount = 0
        val total = pixels.size

        for (p in pixels) {
            val r = Color.red(p)
            val g = Color.green(p)
            val b = Color.blue(p)

            // Pure neutral digital white (characteristic of web browser windows and web pages)
            if (r >= 238 && g >= 238 && b >= 238 && abs(r - g) <= 3 && abs(g - b) <= 3) {
                pureNeutralWhiteCount++
            }
            // Pure flat digital dark mode canvas
            if (r in 12..35 && g in 12..35 && b in 12..35 && abs(r - g) <= 2 && abs(g - b) <= 2) {
                darkCanvasCount++
            }
        }

        val whiteRatio = pureNeutralWhiteCount.toFloat() / total
        val darkRatio = darkCanvasCount.toFloat() / total

        if (whiteRatio > 0.10f) {
            return LivenessResult(
                isRealWildlife = false,
                spoofType = SpoofType.DIGITAL_SCREEN,
                reason = "Web page canvas detected. Point your camera at animals in the physical world, not on web browsers."
            )
        }

        if (darkRatio > 0.20f) {
            return LivenessResult(
                isRealWildlife = false,
                spoofType = SpoofType.DIGITAL_SCREEN,
                reason = "Digital screen background detected. AnimDex requires natural habitat captures."
            )
        }

        return LivenessResult(isRealWildlife = true)
    }

    /**
     * Analyzes periodic rolling shutter luminance ripple caused by display refresh rates (60Hz/120Hz/144Hz PWM).
     */
    private fun checkRollingShutterRefreshBanding(bitmap: Bitmap): LivenessResult {
        val w = bitmap.width
        val h = bitmap.height
        val rowLum = FloatArray(h)
        val pixels = IntArray(w)

        for (y in 0 until h) {
            bitmap.getPixels(pixels, 0, w, 0, y, w, 1)
            var sum = 0f
            for (p in pixels) {
                sum += (Color.red(p) * 299 + Color.green(p) * 587 + Color.blue(p) * 114) / 1000f
            }
            rowLum[y] = sum / w
        }

        // Detrend with sliding window to isolate high-frequency scanline ripple
        val window = 21
        val halfW = window / 2
        val residual = FloatArray(h)

        for (y in 0 until h) {
            var smoothSum = 0f
            var count = 0
            for (j in -halfW..halfW) {
                val ny = y + j
                if (ny in 0 until h) {
                    smoothSum += rowLum[ny]
                    count++
                }
            }
            residual[y] = rowLum[y] - (smoothSum / count)
        }

        // Measure autocorrelation of residual across lags 6 to 36
        var varSum = 0f
        for (v in residual) varSum += v * v
        val variance = varSum / h

        if (variance > 0.8f) {
            var maxAcf = 0f
            for (lag in 6..36) {
                var cov = 0f
                val validN = h - lag
                for (y in 0 until validN) {
                    cov += residual[y] * residual[y + lag]
                }
                val acf = (cov / validN) / (variance + 1e-5f)
                if (acf > maxAcf) {
                    maxAcf = acf
                }
            }

            // Strong periodic refresh flicker waves across image rows
            if (maxAcf > 0.38f) {
                return LivenessResult(
                    isRealWildlife = false,
                    spoofType = SpoofType.DIGITAL_SCREEN,
                    reason = "Display refresh flicker and scanline modulation detected from monitor panel."
                )
            }
        }

        return LivenessResult(isRealWildlife = true)
    }

    /**
     * Detects Moiré interference patterns and subpixel matrix grid through spatial autocorrelation of differences.
     */
    private fun checkMoireAutocorrelation(bitmap: Bitmap): LivenessResult {
        val w = bitmap.width
        val h = bitmap.height
        val pixels = IntArray(w * h)
        bitmap.getPixels(pixels, 0, w, 0, 0, w, h)

        val lum = FloatArray(w * h)
        for (i in pixels.indices) {
            val p = pixels[i]
            lum[i] = (Color.red(p) * 299 + Color.green(p) * 587 + Color.blue(p) * 114) / 1000f
        }

        // Test horizontal and diagonal differences across image slices
        var periodicHits = 0
        val step = 8

        for (y in 16 until h - 16 step step) {
            val offset = y * w
            val diff = FloatArray(w - 1)
            var dVar = 0f
            for (x in 0 until w - 1) {
                diff[x] = lum[offset + x + 1] - lum[offset + x]
                dVar += diff[x] * diff[x]
            }
            dVar /= (w - 1)

            // If line has noticeable texture
            if (dVar in 2.0f..350.0f) {
                for (lag in 2..14) {
                    var cov = 0f
                    val count = (w - 1) - lag
                    for (x in 0 until count) {
                        cov += diff[x] * diff[x + lag]
                    }
                    val acf = (cov / count) / (dVar + 1e-5f)
                    if (acf > 0.14f) {
                        periodicHits++
                        break
                    }
                }
            }
        }

        // Also test diagonal slices (Moiré interference often runs at 30° to 60° angles)
        var diagonalHits = 0
        for (d in 32 until (w + h - 32) step 16) {
            val diagDiff = mutableListOf<Float>()
            var y = max(0, d - w + 1)
            var x = min(w - 1, d)
            var prevVal: Float? = null

            while (y < h && x >= 0) {
                val curr = lum[y * w + x]
                if (prevVal != null) {
                    diagDiff.add(curr - prevVal)
                }
                prevVal = curr
                y++
                x--
            }

            if (diagDiff.size >= 40) {
                var dVar = 0f
                for (v in diagDiff) dVar += v * v
                dVar /= diagDiff.size

                if (dVar in 2.0f..350.0f) {
                    for (lag in 2..12) {
                        var cov = 0f
                        val count = diagDiff.size - lag
                        for (i in 0 until count) {
                            cov += diagDiff[i] * diagDiff[i + lag]
                        }
                        val acf = (cov / count) / (dVar + 1e-5f)
                        if (acf > 0.15f) {
                            diagonalHits++
                            break
                        }
                    }
                }
            }
        }

        // Multiple consistent periodic wave crests indicate Moiré pattern on digital screen
        if (periodicHits >= 4 || diagonalHits >= 3) {
            return LivenessResult(
                isRealWildlife = false,
                spoofType = SpoofType.DIGITAL_SCREEN,
                reason = "Moiré pattern and subpixel display grid detected. Point camera at physical animals in nature."
            )
        }

        return LivenessResult(isRealWildlife = true)
    }

    /**
     * Checks for harsh specular light reflections off flat glass and LCD backlight pedestal.
     */
    private fun checkSpecularReflectionAndBacklight(bitmap: Bitmap): LivenessResult {
        val w = bitmap.width
        val h = bitmap.height
        val pixels = IntArray(w * h)
        bitmap.getPixels(pixels, 0, w, 0, 0, w, h)

        var clippedHighlightCount = 0
        var backlightPedestalCount = 0
        val total = pixels.size

        for (p in pixels) {
            val r = Color.red(p)
            val g = Color.green(p)
            val b = Color.blue(p)

            if (r > 250 && g > 250 && b > 250) {
                clippedHighlightCount++
            }

            val lum = (r * 299 + g * 587 + b * 114) / 1000
            if (lum in 16..42 && abs(r - g) < 5 && abs(g - b) < 5) {
                backlightPedestalCount++
            }
        }

        val glareRatio = clippedHighlightCount.toFloat() / total
        val pedestalRatio = backlightPedestalCount.toFloat() / total

        if (glareRatio > 0.05f && pedestalRatio > 0.25f) {
            return LivenessResult(
                isRealWildlife = false,
                spoofType = SpoofType.DIGITAL_SCREEN,
                reason = "Monitor glass glare and backlight pedestal detected."
            )
        }

        return LivenessResult(isRealWildlife = true)
    }

    /**
     * Detects collinear straight container edges characteristic of web browser image frames and display bezels.
     */
    private fun checkFrameBezelBorders(bitmap: Bitmap): LivenessResult {
        val w = bitmap.width
        val h = bitmap.height
        val pixels = IntArray(w * h)
        bitmap.getPixels(pixels, 0, w, 0, 0, w, h)

        // Check for long continuous horizontal contrast edges bordering flat areas
        var straightHorizontalEdgeCount = 0
        for (y in 10 until h - 10 step 5) {
            var edgeSpan = 0
            for (x in 10 until w - 10) {
                val topP = pixels[(y - 2) * w + x]
                val botP = pixels[(y + 2) * w + x]
                val topLum = (Color.red(topP) * 299 + Color.green(topP) * 587 + Color.blue(topP) * 114) / 1000
                val botLum = (Color.red(botP) * 299 + Color.green(botP) * 587 + Color.blue(botP) * 114) / 1000

                if (abs(topLum - botLum) > 40) {
                    edgeSpan++
                }
            }
            // If an edge runs horizontally across more than 55% of the frame
            if (edgeSpan > (w * 0.55f).toInt()) {
                straightHorizontalEdgeCount++
            }
        }

        if (straightHorizontalEdgeCount >= 2) {
            return LivenessResult(
                isRealWildlife = false,
                spoofType = SpoofType.DIGITAL_SCREEN,
                reason = "Rectangular web image container or display bezel detected."
            )
        }

        return LivenessResult(isRealWildlife = true)
    }
}

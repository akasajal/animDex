package com.animdex.app.ml

import android.content.Context
import android.graphics.Bitmap
import com.animdex.app.data.AnimalDictionary
import com.animdex.app.data.AnimalGroup
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.task.core.BaseOptions
import org.tensorflow.lite.task.vision.classifier.ImageClassifier

enum class ClassifierMode(
    val label: String,
    val emoji: String,
    val subtitle: String
) {
    AUTO("Auto-Ensemble", "⚡", "Intelligent multi-model cascade"),
    BIRDS("Birds", "🪶", "iNaturalist (965 species)"),
    INSECTS("Insects & Bugs", "🦋", "iNaturalist (1,022 species)"),
    GENERAL("General Wildlife", "🐾", "Mammals, reptiles & fauna")
}

data class RecognitionResult(
    val classIndex: Int,
    val rawLabel: String,
    val displayName: String,
    val scientificName: String = "",
    val group: AnimalGroup,
    val isAnimal: Boolean,
    val confidence: Float,
    val modelSource: String,
    val funFact: String
)

class AnimalClassifier(private val context: Context) {

    private var generalClassifier: ImageClassifier? = null
    private var birdClassifier: ImageClassifier? = null
    private var insectClassifier: ImageClassifier? = null

    init {
        generalClassifier = initClassifier("animal_classifier.tflite", maxResults = 5, threshold = 0.15f)
        birdClassifier = initClassifier("bird_classifier.tflite", maxResults = 5, threshold = 0.10f)
        insectClassifier = initClassifier("insect_classifier.tflite", maxResults = 5, threshold = 0.10f)
    }

    private fun initClassifier(filename: String, maxResults: Int, threshold: Float): ImageClassifier? {
        return try {
            val baseOptions = BaseOptions.builder()
                .setNumThreads(4)
                .build()

            val options = ImageClassifier.ImageClassifierOptions.builder()
                .setBaseOptions(baseOptions)
                .setMaxResults(maxResults)
                .setScoreThreshold(threshold)
                .build()

            ImageClassifier.createFromFileAndOptions(context, filename, options)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun classify(
        bitmap: Bitmap,
        mode: ClassifierMode = ClassifierMode.AUTO
    ): List<RecognitionResult> = withContext(Dispatchers.Default) {
        val tensorImage = TensorImage.fromBitmap(bitmap)

        when (mode) {
            ClassifierMode.BIRDS -> runBirdClassifier(tensorImage)
            ClassifierMode.INSECTS -> runInsectClassifier(tensorImage)
            ClassifierMode.GENERAL -> runGeneralClassifier(tensorImage)
            ClassifierMode.AUTO -> runAutoEnsemble(tensorImage)
        }
    }

    private fun runBirdClassifier(tensorImage: TensorImage): List<RecognitionResult> {
        val instance = birdClassifier ?: return emptyList()
        val results = mutableListOf<RecognitionResult>()
        try {
            val classifications = instance.classify(tensorImage)
            for (classification in classifications) {
                for (cat in classification.categories) {
                    val info = AnimalDictionary.getInfoForSpecializedBird(cat.label)
                    results.add(
                        RecognitionResult(
                            classIndex = cat.index,
                            rawLabel = cat.label,
                            displayName = info.displayName,
                            scientificName = info.scientificName,
                            group = AnimalGroup.BIRD,
                            isAnimal = true,
                            confidence = cat.score,
                            modelSource = "iNaturalist Bird Model (965 species)",
                            funFact = info.funFact
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return results.sortedByDescending { it.confidence }
    }

    private fun runInsectClassifier(tensorImage: TensorImage): List<RecognitionResult> {
        val instance = insectClassifier ?: return emptyList()
        val results = mutableListOf<RecognitionResult>()
        try {
            val classifications = instance.classify(tensorImage)
            for (classification in classifications) {
                for (cat in classification.categories) {
                    val info = AnimalDictionary.getInfoForSpecializedInsect(cat.label)
                    results.add(
                        RecognitionResult(
                            classIndex = cat.index,
                            rawLabel = cat.label,
                            displayName = info.displayName,
                            scientificName = info.scientificName,
                            group = AnimalGroup.INVERTEBRATE,
                            isAnimal = true,
                            confidence = cat.score,
                            modelSource = "iNaturalist Insect Model (1,022 species)",
                            funFact = info.funFact
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return results.sortedByDescending { it.confidence }
    }

    private fun runGeneralClassifier(tensorImage: TensorImage): List<RecognitionResult> {
        val instance = generalClassifier ?: return emptyList()
        val results = mutableListOf<RecognitionResult>()
        try {
            val classifications = instance.classify(tensorImage)
            for (classification in classifications) {
                for (cat in classification.categories) {
                    if (cat.index == 0 || cat.label.equals("background", ignoreCase = true)) continue

                    val info = AnimalDictionary.getInfoForGeneralModel(cat.index, cat.label)
                    results.add(
                        RecognitionResult(
                            classIndex = cat.index,
                            rawLabel = cat.label,
                            displayName = info.displayName,
                            scientificName = info.scientificName,
                            group = info.group,
                            isAnimal = info.isAnimal,
                            confidence = cat.score,
                            modelSource = "General Fauna Model",
                            funFact = info.funFact
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return results.sortedByDescending { it.confidence }
    }

    private fun runAutoEnsemble(tensorImage: TensorImage): List<RecognitionResult> {
        val generalResults = runGeneralClassifier(tensorImage)
        val topGeneral = generalResults.firstOrNull()

        if (topGeneral != null && !topGeneral.isAnimal && topGeneral.confidence >= 0.35f) {
            return generalResults
        }

        if (topGeneral != null && topGeneral.group == AnimalGroup.BIRD) {
            val birdResults = runBirdClassifier(tensorImage)
            if (birdResults.isNotEmpty() && birdResults.first().confidence >= 0.15f) {
                return birdResults
            }
        }

        if (topGeneral != null && topGeneral.group == AnimalGroup.INVERTEBRATE) {
            val insectResults = runInsectClassifier(tensorImage)
            if (insectResults.isNotEmpty() && insectResults.first().confidence >= 0.15f) {
                return insectResults
            }
        }

        val topBird = runBirdClassifier(tensorImage).firstOrNull()
        val topInsect = runInsectClassifier(tensorImage).firstOrNull()

        if (topBird != null && topBird.confidence >= 0.65f && (topGeneral == null || topBird.confidence > topGeneral.confidence)) {
            return runBirdClassifier(tensorImage)
        }

        if (topInsect != null && topInsect.confidence >= 0.65f && (topGeneral == null || topInsect.confidence > topGeneral.confidence)) {
            return runInsectClassifier(tensorImage)
        }

        return generalResults
    }

    fun close() {
        generalClassifier?.close()
        birdClassifier?.close()
        insectClassifier?.close()
        generalClassifier = null
        birdClassifier = null
        insectClassifier = null
    }
}

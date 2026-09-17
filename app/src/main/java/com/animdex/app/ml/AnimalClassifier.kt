package com.animdex.app.ml

import android.content.Context
import android.graphics.Bitmap
import com.animdex.app.data.AnimalDictionary
import com.animdex.app.data.AnimalGroup
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.tensorflow.lite.support.image.TensorImage
import com.animdex.app.R
import org.tensorflow.lite.task.core.BaseOptions
import org.tensorflow.lite.task.vision.classifier.ImageClassifier

enum class ClassifierMode(
    val label: String,
    val iconRes: Int,
    val emoji: String,
    val subtitle: String
) {
    AUTO("Auto-Ensemble", R.drawable.ic_auto_mode, "⚡", "Intelligent multi-model cascade"),
    BIRDS("Birds", R.drawable.ic_bird, "🪶", "iNaturalist (965 species)"),
    INSECTS("Insects & Bugs", R.drawable.ic_butterfly, "🦋", "iNaturalist (1,022 species)"),
    GENERAL("General Wildlife", R.drawable.ic_paw, "🐾", "Mammals, reptiles & fauna")
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
    private var livenessClassifier: ImageClassifier? = null
    private var birdClassifier: ImageClassifier? = null
    private var insectClassifier: ImageClassifier? = null

    init {
        generalClassifier = initClassifier("animal_classifier.tflite", maxResults = 5, threshold = 0.15f)
        livenessClassifier = initClassifier("animal_classifier.tflite", maxResults = 50, threshold = 0.015f)
        birdClassifier = initClassifier("bird_classifier.tflite", maxResults = 5, threshold = 0.10f)
        insectClassifier = initClassifier("insect_classifier.tflite", maxResults = 5, threshold = 0.10f)
    }

    fun getGeneralClassifier(): ImageClassifier? = generalClassifier
    fun getLivenessClassifier(): ImageClassifier? = livenessClassifier

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

        // 1. If an inanimate object is detected with solid confidence, stop and report non-animal
        if (topGeneral != null && !topGeneral.isAnimal && topGeneral.confidence >= 0.40f) {
            val hasAnimalCandidate = generalResults.drop(1).any { it.isAnimal && it.confidence >= 0.25f }
            if (!hasAnimalCandidate) {
                return generalResults
            }
        }

        // 2. If general classifier identifies a MAMMAL, REPTILE, AMPHIBIAN, or FISH with confidence >= 0.25:
        // Strictly protect it from out-of-distribution hallucinations by specialized bird/insect models!
        if (topGeneral != null && topGeneral.isAnimal &&
            (topGeneral.group == AnimalGroup.MAMMAL ||
             topGeneral.group == AnimalGroup.REPTILE ||
             topGeneral.group == AnimalGroup.AMPHIBIAN ||
             topGeneral.group == AnimalGroup.FISH) &&
            topGeneral.confidence >= 0.25f
        ) {
            return generalResults
        }

        // 3. If general classifier indicates an avian species (or any top candidate is a bird)
        val generalSuggestsBird = generalResults.take(3).any { it.group == AnimalGroup.BIRD }
        if (generalSuggestsBird) {
            val birdResults = runBirdClassifier(tensorImage)
            if (birdResults.isNotEmpty() && birdResults.first().confidence >= 0.18f) {
                return birdResults
            }
        }

        // 4. If general classifier indicates an invertebrate/insect
        val generalSuggestsInsect = generalResults.take(3).any { it.group == AnimalGroup.INVERTEBRATE }
        if (generalSuggestsInsect) {
            val insectResults = runInsectClassifier(tensorImage)
            if (insectResults.isNotEmpty() && insectResults.first().confidence >= 0.18f) {
                return insectResults
            }
        }

        // 5. Fallback for species missing from general model (e.g. sparrows, pigeons, dragonflies, moths)
        // Only trigger if specialized model has high confidence (>= 0.40) and clear superiority
        val topBird = runBirdClassifier(tensorImage).firstOrNull()
        val topInsect = runInsectClassifier(tensorImage).firstOrNull()
        val generalConf = topGeneral?.confidence ?: 0f

        if (topBird != null && topBird.confidence >= 0.40f &&
            (topInsect == null || topBird.confidence >= topInsect.confidence) &&
            topBird.confidence > generalConf * 1.15f
        ) {
            return runBirdClassifier(tensorImage)
        }

        if (topInsect != null && topInsect.confidence >= 0.40f &&
            (topBird == null || topInsect.confidence >= topBird.confidence) &&
            topInsect.confidence > generalConf * 1.15f
        ) {
            return runInsectClassifier(tensorImage)
        }

        return generalResults
    }

    fun close() {
        generalClassifier?.close()
        livenessClassifier?.close()
        birdClassifier?.close()
        insectClassifier?.close()
        generalClassifier = null
        livenessClassifier = null
        birdClassifier = null
        insectClassifier = null
    }
}

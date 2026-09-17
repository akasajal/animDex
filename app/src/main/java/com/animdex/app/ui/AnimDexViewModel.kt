package com.animdex.app.ui

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.animdex.app.data.db.AnimDexDatabase
import com.animdex.app.data.db.AnimalEntry
import com.animdex.app.ml.AnimalClassifier
import com.animdex.app.ml.ClassifierMode
import com.animdex.app.ml.RecognitionResult
import com.animdex.app.ui.theme.AccentColors
import com.animdex.app.ui.theme.ThemeMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

data class ScanUiState(
    val isAnalyzing: Boolean = false,
    val scannedBitmap: Bitmap? = null,
    val savedImagePath: String? = null,
    val results: List<RecognitionResult> = emptyList(),
    val showResultSheet: Boolean = false,
    val selectedMode: ClassifierMode = ClassifierMode.AUTO,
    val errorMessage: String? = null
)

class AnimDexViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("animdex_settings", Context.MODE_PRIVATE)

    private val classifier = AnimalClassifier(application)
    private val database = AnimDexDatabase.getDatabase(application)
    private val animalDao = database.animalDao()

    private val _themeMode = MutableStateFlow(
        try {
            ThemeMode.valueOf(prefs.getString("theme_mode", ThemeMode.SYSTEM.name) ?: ThemeMode.SYSTEM.name)
        } catch (e: Exception) {
            ThemeMode.SYSTEM
        }
    )
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    private val _accentColor = MutableStateFlow(
        AccentColors.fromName(prefs.getString("accent_name", "Rose") ?: "Rose")
    )
    val accentColor: StateFlow<Color> = _accentColor.asStateFlow()

    private val _scanState = MutableStateFlow(
        ScanUiState(
            selectedMode = try {
                ClassifierMode.valueOf(prefs.getString("scanner_mode", ClassifierMode.AUTO.name) ?: ClassifierMode.AUTO.name)
            } catch (e: Exception) {
                ClassifierMode.AUTO
            }
        )
    )
    val scanState: StateFlow<ScanUiState> = _scanState.asStateFlow()

    val entries: StateFlow<List<AnimalEntry>> = animalDao.getAllEntries()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalCatches: StateFlow<Int> = animalDao.getEntryCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun setThemeMode(mode: ThemeMode) {
        _themeMode.value = mode
        prefs.edit().putString("theme_mode", mode.name).apply()
    }

    fun setAccentColor(color: Color) {
        _accentColor.value = color
        val name = AccentColors.nameOf(color)
        prefs.edit().putString("accent_name", name).apply()
    }

    fun setModelMode(mode: ClassifierMode) {
        _scanState.value = _scanState.value.copy(selectedMode = mode)
        prefs.edit().putString("scanner_mode", mode.name).apply()
    }

    fun onImageCaptured(bitmap: Bitmap) {
        processBitmap(bitmap)
    }

    fun onGalleryImageSelected(uri: Uri) {
        viewModelScope.launch {
            _scanState.value = _scanState.value.copy(isAnalyzing = true, errorMessage = null)
            try {
                val context = getApplication<Application>()
                val bitmap = withContext(Dispatchers.IO) {
                    context.contentResolver.openInputStream(uri)?.use { stream ->
                        BitmapFactory.decodeStream(stream)
                    }
                }
                if (bitmap != null) {
                    processBitmap(bitmap)
                } else {
                    _scanState.value = _scanState.value.copy(
                        isAnalyzing = false,
                        errorMessage = "Could not load image from gallery."
                    )
                }
            } catch (e: Exception) {
                _scanState.value = _scanState.value.copy(
                    isAnalyzing = false,
                    errorMessage = e.localizedMessage ?: "Failed to read photo."
                )
            }
        }
    }

    private fun processBitmap(bitmap: Bitmap) {
        viewModelScope.launch {
            val mode = _scanState.value.selectedMode
            _scanState.value = _scanState.value.copy(isAnalyzing = true, errorMessage = null)

            val savedPath = withContext(Dispatchers.IO) {
                saveBitmapToInternalStorage(bitmap)
            }

            val recognitionResults = classifier.classify(bitmap, mode)

            _scanState.value = _scanState.value.copy(
                isAnalyzing = false,
                scannedBitmap = bitmap,
                savedImagePath = savedPath,
                results = recognitionResults,
                showResultSheet = true
            )
        }
    }

    fun dismissResultSheet() {
        _scanState.value = _scanState.value.copy(
            showResultSheet = false,
            scannedBitmap = null
        )
    }

    fun saveResultToDex(result: RecognitionResult) {
        viewModelScope.launch(Dispatchers.IO) {
            val imagePath = _scanState.value.savedImagePath ?: ""
            val entry = AnimalEntry(
                speciesName = result.displayName,
                scientificName = result.scientificName,
                modelSource = result.modelSource,
                rawLabel = result.rawLabel,
                groupName = result.group.displayName,
                groupEmoji = result.group.emoji,
                confidence = result.confidence,
                photoUri = imagePath,
                funFact = result.funFact
            )
            animalDao.insertEntry(entry)

            withContext(Dispatchers.Main) {
                dismissResultSheet()
            }
        }
    }

    fun deleteEntry(entry: AnimalEntry) {
        viewModelScope.launch(Dispatchers.IO) {
            animalDao.deleteEntry(entry)
            if (entry.photoUri.isNotEmpty()) {
                val file = File(entry.photoUri)
                if (file.exists()) {
                    file.delete()
                }
            }
        }
    }

    private fun saveBitmapToInternalStorage(bitmap: Bitmap): String {
        val context = getApplication<Application>()
        val filename = "animdex_${System.currentTimeMillis()}.jpg"
        val file = File(context.filesDir, filename)
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
        }
        return file.absolutePath
    }

    override fun onCleared() {
        super.onCleared()
        classifier.close()
    }
}

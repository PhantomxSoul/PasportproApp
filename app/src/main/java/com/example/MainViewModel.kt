package com.example

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import kotlin.ranges.coerceIn

data class ImageItem(
    val uri: Uri,
    val sourceBitmap: Bitmap,
    val croppedBitmap: Bitmap,
    val copies: Int = 4, // default to 4 copies
    // Crop offsets
    val zoomFactor: Float = 1.0f,
    val panX: Float = 0.0f,
    val panY: Float = 0.0f
)

data class UiState(
    val images: List<ImageItem> = emptyList(),
    val appTheme: String = "system", // "system", "light", "dark"
    val widthMm: Float = 35f,
    val heightMm: Float = 45f,
    val spacingMm: Float = 4.0f,
    val borderMm: Float = 0.5f,
    val layoutMode: String = "grid", // "grid" or "line"
    val gridAlignment: String = "normal", // "normal", "right_blank", "left_blank", "lower_right_blank", "lower_left_blank"
    val chosenPreset: PassportPreset = Presets.list.first(), // Default is Indian Passport (35x45)
    
    // UI Flows
    val activeCroppingUri: Uri? = null,
    val isFeedbackModalOpen: Boolean = false,
    val isGeneratingPdf: Boolean = false,
    val generatedPdfFile: File? = null,
    val toastMessage: String? = null
)

class MainViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        // Trigger Credit Protection
        CreditSafetyCheck.performIntegrityCheck()
    }

    fun setTheme(theme: String) {
        _uiState.update { it.copy(appTheme = theme) }
    }

    fun selectPreset(preset: PassportPreset) {
        _uiState.update { 
            it.copy(
                chosenPreset = preset,
                widthMm = preset.widthMm,
                heightMm = preset.heightMm
            )
        }
        showToast("Preset updated: ${preset.country} - ${preset.name}")
    }

    fun addPhotos(context: Context, uris: List<Uri>) {
        viewModelScope.launch {
            val contentResolver = context.contentResolver
            val currentImages = _uiState.value.images.toMutableList()

            // Limit uploading up to 10 photos
            val maxAllowed = 10
            val spacesLeft = (maxAllowed - currentImages.size).coerceAtLeast(0)
            if (spacesLeft == 0) {
                showToast("Maximum photo limit reached (Max: $maxAllowed)")
                return@launch
            }

            val toAdd = uris.take(spacesLeft)
            for (uri in toAdd) {
                try {
                    contentResolver.openInputStream(uri)?.use { inputStream ->
                        val options = BitmapFactory.Options().apply {
                            inJustDecodeBounds = false
                            // Sample large pictures to prevent out-of-memory crashes
                            inSampleSize = 2 
                        }
                        val srcBitmap = BitmapFactory.decodeStream(inputStream, null, options)
                        if (srcBitmap != null) {
                            // Initial cropped bitmap is just the source bitmap
                            val initialCrop = centerCropBitmap(srcBitmap, _uiState.value.widthMm / _uiState.value.heightMm)
                            currentImages.add(
                                ImageItem(
                                    uri = uri,
                                    sourceBitmap = srcBitmap,
                                    croppedBitmap = initialCrop
                                )
                            )
                        }
                    }
                } catch (e: Exception) {
                    showToast("Failed to load image: ${e.localizedMessage}")
                }
            }
            _uiState.update { it.copy(images = currentImages) }
            if (uris.size > spacesLeft) {
                showToast("Added $spacesLeft photos (Max selection is 10)")
            } else {
                showToast("Successfully added ${toAdd.size} photos!")
            }
        }
    }

    fun removePhoto(uri: Uri) {
        _uiState.update { state ->
            val updated = state.images.filter { it.uri != uri }
            state.copy(images = updated)
        }
        showToast("Photo removed")
    }

    fun updateCopies(uri: Uri, copies: Int) {
        _uiState.update { state ->
            val updated = state.images.map {
                if (it.uri == uri) it.copy(copies = copies.coerceIn(1, 20)) else it
            }
            state.copy(images = updated)
        }
    }

    fun setWidthMm(width: Float) {
        _uiState.update { it.copy(widthMm = width.coerceIn(10f, 150f)) }
    }

    fun setHeightMm(height: Float) {
        _uiState.update { it.copy(heightMm = height.coerceIn(10f, 150f)) }
    }

    fun setSpacingMm(spacing: Float) {
        _uiState.update { it.copy(spacingMm = spacing.coerceIn(0f, 30f)) }
    }

    fun setBorderMm(border: Float) {
        _uiState.update { it.copy(borderMm = border.coerceIn(0f, 10f)) }
    }

    fun setLayoutMode(mode: String) {
        _uiState.update { it.copy(layoutMode = mode) }
    }

    fun setGridAlignment(alignment: String) {
        _uiState.update { it.copy(gridAlignment = alignment) }
    }

    fun showToast(message: String?) {
        _uiState.update { it.copy(toastMessage = message) }
    }

    fun clearToast() {
        _uiState.update { it.copy(toastMessage = null) }
    }

    fun startCropping(uri: Uri) {
        _uiState.update { it.copy(activeCroppingUri = uri) }
    }

    fun cancelCropping() {
        _uiState.update { it.copy(activeCroppingUri = null) }
    }

    fun saveCrop(uri: Uri, croppedBitmap: Bitmap) {
        _uiState.update { state ->
            val updated = state.images.map {
                if (it.uri == uri) {
                    it.copy(croppedBitmap = croppedBitmap)
                } else {
                    it
                }
            }
            state.copy(images = updated, activeCroppingUri = null)
        }
        showToast("Image cropped successfully")
    }

    fun setFeedbackModalOpen(isOpen: Boolean) {
        _uiState.update { it.copy(isFeedbackModalOpen = isOpen) }
    }

    fun generatePrintPdf(context: Context) {
        val state = _uiState.value
        val printPhotos = state.images.map {
            PdfGenerator.PrintPhoto(bitmap = it.croppedBitmap, copies = it.copies)
        }

        if (printPhotos.isEmpty()) {
            showToast("No photos added. Drag & drop or select photos first!")
            return
        }

        _uiState.update { it.copy(isGeneratingPdf = true, generatedPdfFile = null) }

        viewModelScope.launch {
            try {
                val outputDir = context.cacheDir
                val file = File(outputDir, "PassportPhotos_${System.currentTimeMillis()}.pdf")
                
                val layoutSettings = PdfGenerator.LayoutSettings(
                    widthMm = state.widthMm,
                    heightMm = state.heightMm,
                    spacingMm = state.spacingMm,
                    borderMm = state.borderMm,
                    layoutMode = state.layoutMode,
                    gridAlignment = state.gridAlignment
                )

                val generatedFile = PdfGenerator.generatePdf(
                    context = context,
                    photos = printPhotos,
                    settings = layoutSettings,
                    outputFile = file
                )

                _uiState.update { it.copy(isGeneratingPdf = false, generatedPdfFile = generatedFile) }
                showToast("A4 Print Sheet Generated successfully! 🎉")
            } catch (e: Exception) {
                _uiState.update { it.copy(isGeneratingPdf = false) }
                showToast("Error generating PDF: ${e.localizedMessage}")
            }
        }
    }

    // Helper to center-crop bitmap based on a given aspect ratio
    private fun centerCropBitmap(src: Bitmap, targetRatio: Float): Bitmap {
        val srcRatio = src.width.toFloat() / src.height.toFloat()
        val cropW: Int
        val cropH: Int
        val startX: Int
        val startY: Int

        if (srcRatio > targetRatio) {
            // Source is wider than target crop region
            cropH = src.height
            cropW = (src.height * targetRatio).toInt().coerceAtMost(src.width)
            startX = (src.width - cropW) / 2
            startY = 0
        } else {
            // Source is taller than target crop region
            cropW = src.width
            cropH = (src.width / targetRatio).toInt().coerceAtMost(src.height)
            startX = 0
            startY = (src.height - cropH) / 2
        }

        return try {
            Bitmap.createBitmap(src, startX, startY, cropW, cropH)
        } catch (e: Exception) {
            src
        }
    }
}

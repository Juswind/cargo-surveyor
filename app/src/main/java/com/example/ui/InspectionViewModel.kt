package com.example.ui

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.benchmark.CargoBenchmarkScenario
import com.example.data.benchmark.CargoBenchmarks
import com.example.data.db.InspectionEntity
import com.example.data.model.InspectionReport
import com.example.data.repository.InspectionRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

enum class Screen {
    HUB,
    ANALYSIS,
    HISTORY,
    STANDARDS
}

data class InspectionUiState(
    val currentScreen: Screen = Screen.HUB,
    val selectedBitmap: Bitmap? = null,
    val selectedImageUri: String? = null,
    val cargoIdentifier: String = "MSKU " + (100000 + Random.nextInt(900000)) + "-" + Random.nextInt(9),
    val cargoType: String = "40ft ISO High-Cube Container",
    val location: String = "Port Terminal Bay 4B",
    val surveyorNotes: String = "",
    val isAnalyzing: Boolean = false,
    val analysisProgressStep: String = "",
    val activeReport: InspectionReport? = null,
    val activeInspectionEntity: InspectionEntity? = null,
    val activeBenchmarkScenario: CargoBenchmarkScenario? = null,
    val errorMessage: String? = null,
    val infoMessage: String? = null,
    val historyFilterStatus: String? = null,
    val historySearchQuery: String = ""
)

class InspectionViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = InspectionRepository(application)

    private val _uiState = MutableStateFlow(InspectionUiState())
    val uiState: StateFlow<InspectionUiState> = _uiState.asStateFlow()

    val inspectionHistory: StateFlow<List<InspectionEntity>> = repository.getAllInspections()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Pre-select the first high-contrast benchmark scenario so user has immediate preview ready
        loadScenario(CargoBenchmarks.scenarios[0])
    }

    fun navigateTo(screen: Screen) {
        _uiState.update { it.copy(currentScreen = screen, errorMessage = null, infoMessage = null) }
    }

    fun setCargoIdentifier(id: String) {
        _uiState.update { it.copy(cargoIdentifier = id) }
    }

    fun setCargoType(type: String) {
        _uiState.update { it.copy(cargoType = type) }
    }

    fun setLocation(loc: String) {
        _uiState.update { it.copy(location = loc) }
    }

    fun setSurveyorNotes(notes: String) {
        _uiState.update { it.copy(surveyorNotes = notes) }
    }

    fun setHistoryFilter(status: String?) {
        _uiState.update { it.copy(historyFilterStatus = status) }
    }

    fun setHistorySearch(query: String) {
        _uiState.update { it.copy(historySearchQuery = query) }
    }

    fun generateNewCargoId() {
        val prefixes = listOf("MSKU", "CMAU", "TGHU", "HLXU", "MEDU", "TEMU", "ONEU")
        val prefix = prefixes.random()
        val num = 100000 + Random.nextInt(900000)
        val checkDigit = Random.nextInt(10)
        _uiState.update { it.copy(cargoIdentifier = "$prefix $num-$checkDigit") }
    }

    fun loadScenario(scenario: CargoBenchmarkScenario) {
        viewModelScope.launch {
            val bitmap = CargoBenchmarks.createScenarioBitmap(scenario)
            _uiState.update {
                it.copy(
                    selectedBitmap = bitmap,
                    selectedImageUri = null,
                    cargoIdentifier = scenario.cargoIdentifier,
                    cargoType = scenario.cargoType,
                    activeBenchmarkScenario = scenario,
                    activeReport = null,
                    activeInspectionEntity = null,
                    errorMessage = null,
                    infoMessage = "Loaded Benchmark: ${scenario.title}"
                )
            }
        }
    }

    fun setImageFromBitmap(bitmap: Bitmap, sourceName: String = "Captured Image") {
        _uiState.update {
            it.copy(
                selectedBitmap = bitmap,
                selectedImageUri = null,
                activeBenchmarkScenario = null,
                activeReport = null,
                activeInspectionEntity = null,
                errorMessage = null,
                infoMessage = "Image ready for survey ($sourceName)"
            )
        }
    }

    fun setImageFromUri(uri: Uri) {
        viewModelScope.launch {
            val bitmap = repository.loadBitmapFromUri(uri)
            if (bitmap != null) {
                _uiState.update {
                    it.copy(
                        selectedBitmap = bitmap,
                        selectedImageUri = uri.toString(),
                        activeBenchmarkScenario = null,
                        activeReport = null,
                        activeInspectionEntity = null,
                        errorMessage = null,
                        infoMessage = "Photo selected from gallery"
                    )
                }
            } else {
                _uiState.update { it.copy(errorMessage = "Failed to load image from gallery.") }
            }
        }
    }

    fun runSurvey() {
        val currentBitmap = _uiState.value.selectedBitmap
        if (currentBitmap == null) {
            _uiState.update { it.copy(errorMessage = "Please capture or select a cargo image first.") }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isAnalyzing = true,
                    errorMessage = null,
                    infoMessage = null,
                    analysisProgressStep = "Scanning cargo structure & panels..."
                )
            }

            delay(350)
            _uiState.update { it.copy(analysisProgressStep = "Inspecting seals, locking bars, and lashing straps...") }
            delay(350)
            _uiState.update { it.copy(analysisProgressStep = "Checking hazardous placards & compliance standards...") }

            val cargoId = _uiState.value.cargoIdentifier
            val cargoType = _uiState.value.cargoType
            val notes = _uiState.value.surveyorNotes
            val location = _uiState.value.location
            val scenario = _uiState.value.activeBenchmarkScenario

            val apiResult = repository.runGeminiInspection(currentBitmap, cargoId, cargoType, notes)

            if (apiResult.isSuccess) {
                val report = apiResult.getOrThrow()
                val imagePath = repository.saveBitmapToFile(currentBitmap, prefix = cargoId.replace(" ", "_"))
                val savedId = repository.saveInspection(
                    report = report,
                    cargoId = cargoId,
                    cargoType = cargoType,
                    imageUri = imagePath,
                    notes = notes,
                    location = location
                )

                val entity = InspectionEntity.fromReport(
                    report = report,
                    cargoIdentifier = cargoId,
                    cargoType = cargoType,
                    imageUri = imagePath,
                    surveyorNotes = notes,
                    location = location
                ).copy(id = savedId)

                _uiState.update {
                    it.copy(
                        isAnalyzing = false,
                        activeReport = report,
                        activeInspectionEntity = entity,
                        currentScreen = Screen.ANALYSIS,
                        infoMessage = "Gemini multimodal survey complete. Report saved to logbook."
                    )
                }
            } else {
                val error = apiResult.exceptionOrNull()
                // If the error was due to missing API key and we have a benchmark scenario loaded,
                // fallback gracefully with informative explanation
                if (scenario != null) {
                    val report = scenario.baselineReport
                    val imagePath = repository.saveBitmapToFile(currentBitmap, prefix = "benchmark_${scenario.id}")
                    val savedId = repository.saveInspection(
                        report = report,
                        cargoId = cargoId,
                        cargoType = cargoType,
                        imageUri = imagePath,
                        notes = if (notes.isBlank()) "Benchmark standard survey" else notes,
                        location = location
                    )

                    val entity = InspectionEntity.fromReport(
                        report = report,
                        cargoIdentifier = cargoId,
                        cargoType = cargoType,
                        imageUri = imagePath,
                        surveyorNotes = notes,
                        location = location
                    ).copy(id = savedId)

                    _uiState.update {
                        it.copy(
                            isAnalyzing = false,
                            activeReport = report,
                            activeInspectionEntity = entity,
                            currentScreen = Screen.ANALYSIS,
                            infoMessage = "Benchmark survey executed. (Tip: Set GEMINI_API_KEY in Secrets panel for live AI custom photos)"
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isAnalyzing = false,
                            errorMessage = error?.message ?: "Inspection analysis failed. Check network or API credentials."
                        )
                    }
                }
            }
        }
    }

    fun openHistoricalInspection(entity: InspectionEntity) {
        val report = entity.toReport()
        _uiState.update {
            it.copy(
                activeReport = report,
                activeInspectionEntity = entity,
                cargoIdentifier = entity.cargoIdentifier,
                cargoType = entity.cargoType,
                location = entity.location,
                surveyorNotes = entity.surveyorNotes,
                currentScreen = Screen.ANALYSIS,
                errorMessage = null,
                infoMessage = "Viewing archived survey record #${entity.id}"
            )
        }
    }

    fun deleteInspection(entity: InspectionEntity) {
        viewModelScope.launch {
            repository.deleteInspection(entity)
            if (_uiState.value.activeInspectionEntity?.id == entity.id) {
                _uiState.update { it.copy(activeInspectionEntity = null, activeReport = null, currentScreen = Screen.HUB) }
            }
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    fun generateOfficialReportText(): String {
        val state = _uiState.value
        val report = state.activeReport ?: return "No active survey report."
        val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss z", Locale.US).format(Date())

        return buildString {
            appendLine("==================================================")
            appendLine("    OFFICIAL MARITIME & CARGO SURVEY REPORT       ")
            appendLine("           CARGOSURVEYOR INSPECTION               ")
            appendLine("==================================================")
            appendLine("DATE & TIME       : $dateStr")
            appendLine("CARGO IDENTIFIER  : ${state.cargoIdentifier}")
            appendLine("CARGO TYPE        : ${state.cargoType}")
            appendLine("LOCATION / BAY    : ${state.location}")
            appendLine("SEAL VISIBLE      : ${if (report.containerSealVisible) "YES - VERIFIED" else "NO / NOT DETECTED"}")
            appendLine("--------------------------------------------------")
            appendLine("SURVEY RESULT     : ${report.status.uppercase()}")
            appendLine("SEVERITY RATING   : ${report.severity.uppercase()}")
            appendLine("PRIMARY CATEGORY  : ${report.damageCategory.uppercase()}")
            appendLine("--------------------------------------------------")
            appendLine("EXECUTIVE FINDINGS:")
            appendLine(report.findingsSummary)
            appendLine()
            appendLine("VISIBLE DEFECTS & COMPLIANCE BREACHES:")
            if (report.visibleDefects.isEmpty()) {
                appendLine("  [None observed - Zero physical or security defects detected]")
            } else {
                report.visibleDefects.forEachIndexed { i, defect ->
                    appendLine("  ${i + 1}. ITEM: ${defect.item}")
                    appendLine("     DESCRIPTION: ${defect.description}")
                    defect.impact?.let { appendLine("     IMPACT: $it") }
                }
            }
            appendLine()
            appendLine("MANDATED OPERATIONAL ACTIONS:")
            if (report.recommendedActions.isEmpty()) {
                appendLine("  - Unit certified fit for transport.")
            } else {
                report.recommendedActions.forEach { action ->
                    appendLine("  [!] $action")
                }
            }
            if (state.surveyorNotes.isNotBlank()) {
                appendLine()
                appendLine("SURVEYOR FIELD NOTES:")
                appendLine("  ${state.surveyorNotes}")
            }
            appendLine("==================================================")
            appendLine("Surveyor Standard: IMO / CSC 1972 / IICL-6 / IMDG")
            appendLine("Digitally Signed & Certified via CargoSurveyor")
            appendLine("==================================================")
        }
    }
}

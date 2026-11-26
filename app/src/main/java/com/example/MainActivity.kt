package com.example

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsBoat
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.example.data.benchmark.CargoBenchmarkScenario
import com.example.data.benchmark.CargoBenchmarks
import com.example.data.network.GeminiSurveyorService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File

@Serializable
data class InspectionResult(
    val status: String = "Pass",
    val severity: String = "None",
    val damage_category: String = "None",
    val findings_summary: String = "",
    val container_seal_visible: Boolean = false,
    val angle_breakdown: List<AngleView> = emptyList(),
    val visible_defects: List<Defect> = emptyList(),
    val recommended_actions: List<String> = emptyList()
)

@Serializable
data class AngleView(
    val view_name: String,
    val condition: String
)

@Serializable
data class Defect(
    val item: String,
    val description: String,
    val impact: String? = null
)

class MainActivity : ComponentActivity() {

    private val jsonParser = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    InspectionAppScreen()
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun InspectionAppScreen() {
        val context = LocalContext.current

        val imageUris = remember { mutableStateListOf<Uri>() }
        val imageBitmaps = remember { mutableStateListOf<Bitmap>() }
        var inspectionResult by remember { mutableStateOf<InspectionResult?>(null) }
        var isLoading by remember { mutableStateOf(false) }
        var tempCameraUri by remember { mutableStateOf<Uri?>(null) }

        val multiImagePicker = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetMultipleContents()
        ) { uris: List<Uri> ->
            uris.forEach { uri ->
                if (!imageUris.contains(uri)) {
                    val bitmap = loadBitmapFromUri(uri)
                    if (bitmap != null) {
                        imageUris.add(uri)
                        imageBitmaps.add(bitmap)
                    }
                }
            }
        }

        val cameraLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.TakePicture()
        ) { success: Boolean ->
            if (success && tempCameraUri != null) {
                val uri = tempCameraUri!!
                val bitmap = loadBitmapFromUri(uri)
                if (bitmap != null) {
                    imageUris.add(uri)
                    imageBitmaps.add(bitmap)
                }
            }
        }

        fun resetSurvey() {
            imageUris.clear()
            imageBitmaps.clear()
            inspectionResult = null
            isLoading = false
        }

        fun loadBenchmarkScenario(scenario: CargoBenchmarkScenario) {
            resetSurvey()
            val bmp = CargoBenchmarks.createScenarioBitmap(scenario)
            imageBitmaps.add(bmp)
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF1E3A8A)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DirectionsBoat,
                                    contentDescription = null,
                                    tint = Color(0xFF38BDF8),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Cargo Defect Inspector", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                                Text("Maritime & Intermodal Surveyor", fontSize = 11.sp, color = Color(0xFF64748B))
                            }
                        }
                    },
                    actions = {
                        if (imageBitmaps.isNotEmpty() || inspectionResult != null) {
                            IconButton(onClick = { resetSurvey() }) {
                                Icon(Icons.Default.Refresh, contentDescription = "New Survey / Clear All")
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .padding(16.dp)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "ONE-TAP BENCHMARK TEST PRESETS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF64748B),
                    letterSpacing = 0.5.sp,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CargoBenchmarks.scenarios.forEach { scenario ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFE2E8F0))
                                .clickable { loadBenchmarkScenario(scenario) }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                .testTag("preset_${scenario.id}")
                        ) {
                            Text(
                                text = scenario.title,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF1E293B)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Survey Photos & Angles (${imageBitmaps.size})",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp
                    )
                    if (imageBitmaps.isNotEmpty()) {
                        TextButton(onClick = { resetSurvey() }) {
                            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Clear All", fontSize = 12.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    item {
                        Box(
                            modifier = Modifier
                                .width(115.dp)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(12.dp))
                                .border(1.5.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.05f))
                                .clickable { multiImagePicker.launch("image/*") }
                                .testTag("add_gallery_angle_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.AddAPhoto,
                                    contentDescription = "Add Gallery Angle",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    "Add Angle",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    "Gallery",
                                    fontSize = 10.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                        }
                    }

                    item {
                        Box(
                            modifier = Modifier
                                .width(115.dp)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(12.dp))
                                .border(1.5.dp, Color(0xFF0F2642), RoundedCornerShape(12.dp))
                                .background(Color(0xFF0F2642).copy(alpha = 0.05f))
                                .clickable {
                                    val file = File(context.cacheDir, "camera_angle_${System.currentTimeMillis()}.jpg")
                                    val uri = FileProvider.getUriForFile(
                                        context,
                                        "${context.packageName}.fileprovider",
                                        file
                                    )
                                    tempCameraUri = uri
                                    cameraLauncher.launch(uri)
                                }
                                .testTag("add_camera_angle_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.CameraAlt,
                                    contentDescription = "Capture Angle",
                                    tint = Color(0xFF0F2642)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    "Snap Photo",
                                    fontSize = 12.sp,
                                    color = Color(0xFF0F2642),
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    "Camera",
                                    fontSize = 10.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                        }
                    }

                    itemsIndexed(imageBitmaps) { index, bmp ->
                        Box(
                            modifier = Modifier
                                .width(130.dp)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF0F172A))
                        ) {
                            Image(
                                bitmap = bmp.asImageBitmap(),
                                contentDescription = "Angle #$index",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )

                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(6.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color.Black.copy(alpha = 0.7f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "View #${index + 1}",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }

                            IconButton(
                                onClick = {
                                    if (index < imageBitmaps.size) imageBitmaps.removeAt(index)
                                    if (index < imageUris.size) imageUris.removeAt(index)
                                },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(4.dp)
                                    .size(24.dp)
                                    .background(Color.Black.copy(alpha = 0.65f), CircleShape)
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Remove",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("inspect_cargo_button"),
                    enabled = !isLoading && imageBitmaps.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F2642)),
                    onClick = {
                        isLoading = true
                        inspectionResult = null
                        val currentBitmaps = imageBitmaps.toList()
                        lifecycleScope.launch {
                            val rawResponse = runMultiInspection(currentBitmaps)
                            try {
                                inspectionResult = jsonParser.decodeFromString<InspectionResult>(rawResponse)
                            } catch (e: Exception) {
                                Toast.makeText(context, "Parse error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                            } finally {
                                isLoading = false
                            }
                        }
                    }
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Correlating ${imageBitmaps.size} Perspectives...", color = Color.White)
                    } else {
                        Text(
                            text = if (imageBitmaps.isEmpty()) "Add Photos to Inspect" else "Analyze Cargo (${imageBitmaps.size} Views)",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                inspectionResult?.let { result ->
                    val statusColor = when (result.status.lowercase()) {
                        "pass" -> Color(0xFF2E7D32)
                        "warn" -> Color(0xFFE65100)
                        else -> Color(0xFFC62828)
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("inspection_result_card"),
                        colors = CardDefaults.cardColors(containerColor = statusColor.copy(alpha = 0.08f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = result.status.uppercase(),
                                    fontWeight = FontWeight.Black,
                                    fontSize = 22.sp,
                                    color = statusColor
                                )
                                Text(
                                    text = "Severity: ${result.severity}",
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.DarkGray
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Category: ${result.damage_category}",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (result.container_seal_visible) Icons.Default.Lock else Icons.Default.LockOpen,
                                        contentDescription = null,
                                        tint = if (result.container_seal_visible) Color(0xFF0284C7) else Color(0xFF64748B),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (result.container_seal_visible) "Seal Detected" else "No Seal in Views",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = if (result.container_seal_visible) Color(0xFF0369A1) else Color(0xFF64748B)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = result.findings_summary,
                                fontSize = 14.sp,
                                lineHeight = 19.sp
                            )

                            if (result.angle_breakdown.isNotEmpty()) {
                                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                                Text("Angle & Perspective Breakdown:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Spacer(modifier = Modifier.height(6.dp))
                                result.angle_breakdown.forEach { angle ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 3.dp),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Text("▸ ", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                        Text(
                                            text = "${angle.view_name}: ",
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 13.sp,
                                            color = Color(0xFF0F172A)
                                        )
                                        Text(
                                            text = angle.condition,
                                            fontSize = 13.sp,
                                            color = Color(0xFF334155)
                                        )
                                    }
                                }
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                            Text("Defects Across All Angles:", fontWeight = FontWeight.Bold)
                            if (result.visible_defects.isEmpty()) {
                                Text("• No structural, barrier, or safety defects observed.", color = Color(0xFF16A34A), fontSize = 13.sp)
                            } else {
                                result.visible_defects.forEach { defect ->
                                    Text("• ${defect.item}: ${defect.description}", fontSize = 13.sp)
                                    defect.impact?.let {
                                        Text("  Impact: $it", fontSize = 12.sp, color = Color(0xFF991B1B))
                                    }
                                }
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                            Text("Recommended Actions:", fontWeight = FontWeight.Bold)
                            result.recommended_actions.forEach { action ->
                                Text("✔ $action", fontSize = 13.sp, modifier = Modifier.padding(vertical = 2.dp))
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            OutlinedButton(
                                onClick = { resetSurvey() },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Clear & Start New Survey")
                            }
                        }
                    }
                }
            }
        }
    }

    private fun loadBitmapFromUri(uri: Uri): Bitmap? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(contentResolver, uri)
                ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                    decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                    decoder.isMutableRequired = true
                }
            } else {
                @Suppress("DEPRECATION")
                val bmp = MediaStore.Images.Media.getBitmap(contentResolver, uri)
                bmp.copy(Bitmap.Config.ARGB_8888, true)
            }
        } catch (_: Exception) {
            null
        }
    }

    private suspend fun runMultiInspection(bitmaps: List<Bitmap>): String = withContext(Dispatchers.IO) {
        val apiKey = com.example.BuildConfig.GEMINI_API_KEY

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            val angleViews = bitmaps.mapIndexed { idx, _ ->
                """{"view_name":"Angle ${idx + 1}","condition":"Visual evaluation pending API key in Secrets panel"}"""
            }.joinToString(",")

            return@withContext """
            {
                "status": "Warn",
                "severity": "Medium",
                "damage_category": "Configuration",
                "findings_summary": "Gemini API key is not set. Please configure GEMINI_API_KEY in the Secrets panel in AI Studio to enable live multimodal survey.",
                "container_seal_visible": false,
                "angle_breakdown": [$angleViews],
                "visible_defects": [{"item": "API Key", "description": "Placeholder GEMINI_API_KEY detected in runtime configuration", "impact": "Live AI analysis paused"}],
                "recommended_actions": ["Add Gemini API Key in Secrets panel", "Use preset benchmark buttons above to test"]
            }
            """.trimIndent()
        }

        val result = GeminiSurveyorService.inspectMultipleImages(
            bitmaps = bitmaps,
            cargoId = "CONSIGNMENT-${System.currentTimeMillis() % 100000}",
            cargoType = "ISO Shipping Container / Intermodal Freight"
        )

        if (result.isSuccess) {
            val report = result.getOrThrow()
            val anglesJson = report.angleBreakdown.joinToString(",") {
                """{"view_name":"${it.viewName.replace("\"", "'")}","condition":"${it.condition.replace("\"", "'")}"}"""
            }
            val defectsJson = report.visibleDefects.joinToString(",") {
                """{"item":"${it.item.replace("\"", "'")}","description":"${it.description.replace("\"", "'")}","impact":"${it.impact?.replace("\"", "'") ?: ""}"}"""
            }
            val actionsJson = report.recommendedActions.joinToString(",") { "\"${it.replace("\"", "'")}\"" }

            """
            {
                "status": "${report.status}",
                "severity": "${report.severity}",
                "damage_category": "${report.damageCategory}",
                "findings_summary": "${report.findingsSummary.replace("\"", "'")}",
                "container_seal_visible": ${report.containerSealVisible},
                "angle_breakdown": [$anglesJson],
                "visible_defects": [$defectsJson],
                "recommended_actions": [$actionsJson]
            }
            """.trimIndent()
        } else {
            val errorMsg = result.exceptionOrNull()?.message ?: "Unknown survey failure"
            """
            {
                "status": "Fail",
                "severity": "High",
                "damage_category": "Error",
                "findings_summary": "Survey correlation error: ${errorMsg.replace("\"", "'")}",
                "container_seal_visible": false,
                "angle_breakdown": [],
                "visible_defects": [],
                "recommended_actions": ["Check internet connection", "Verify image formats and retry"]
            }
            """.trimIndent()
        }
    }
}

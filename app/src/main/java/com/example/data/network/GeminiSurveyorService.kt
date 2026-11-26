package com.example.data.network

import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import com.example.BuildConfig
import com.example.data.model.AngleBreakdownItem
import com.example.data.model.InspectionReport
import com.example.data.model.VisibleDefect
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

object GeminiSurveyorService {
    private const val TAG = "GeminiSurveyorService"
    private const val PRIMARY_MODEL = "gemini-3.8-flash"
    private const val FALLBACK_MODEL = "gemini-3.5-flash"

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    suspend fun inspectCargoImage(
        bitmap: Bitmap,
        cargoId: String,
        cargoType: String,
        additionalSurveyorNotes: String = ""
    ): Result<InspectionReport> = inspectMultipleImages(
        bitmaps = listOf(bitmap),
        cargoId = cargoId,
        cargoType = cargoType,
        additionalSurveyorNotes = additionalSurveyorNotes
    )

    suspend fun inspectMultipleImages(
        bitmaps: List<Bitmap>,
        cargoId: String,
        cargoType: String,
        additionalSurveyorNotes: String = ""
    ): Result<InspectionReport> = withContext(Dispatchers.IO) {
        if (bitmaps.isEmpty()) {
            return@withContext Result.failure(IllegalArgumentException("No photos provided for inspection"))
        }

        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext Result.failure(
                IllegalStateException("Gemini API key is not configured. Please enter your API key in the Secrets panel in AI Studio under GEMINI_API_KEY.")
            )
        }

        val base64Images = bitmaps.map { bmp ->
            val scaled = scaleDownBitmap(bmp, maxDimension = 1024)
            scaled.toJpegBase64(quality = 82)
        }

        val promptText = buildMultiAnglePrompt(bitmaps.size, cargoId, cargoType, additionalSurveyorNotes)
        val requestJson = buildGeminiRequestBody(promptText, base64Images)

        val primaryResult = executeGenerateContent(PRIMARY_MODEL, apiKey, requestJson)
        if (primaryResult.isSuccess) {
            return@withContext primaryResult
        }

        Log.w(TAG, "Primary model failed: ${primaryResult.exceptionOrNull()?.message}, attempting fallback model $FALLBACK_MODEL")
        executeGenerateContent(FALLBACK_MODEL, apiKey, requestJson)
    }

    private fun executeGenerateContent(
        modelName: String,
        apiKey: String,
        requestJson: JSONObject
    ): Result<InspectionReport> {
        val url = "https://generativelanguage.googleapis.com/v1beta/models/$modelName:generateContent?key=$apiKey"
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val body = requestJson.toString().toRequestBody(mediaType)

        val httpRequest = Request.Builder()
            .url(url)
            .post(body)
            .build()

        return try {
            val response = okHttpClient.newCall(httpRequest).execute()
            val responseBody = response.body?.string().orEmpty()

            if (!response.isSuccessful) {
                Log.e(TAG, "API error: HTTP ${response.code} -> $responseBody")
                return Result.failure(Exception("Gemini API returned HTTP ${response.code}: $responseBody"))
            }

            val parsedReport = parseGeminiResponse(responseBody)
            Result.success(parsedReport)
        } catch (e: Exception) {
            Log.e(TAG, "Network or parsing exception", e)
            Result.failure(e)
        }
    }

    private fun buildMultiAnglePrompt(
        imageCount: Int,
        cargoId: String,
        cargoType: String,
        additionalNotes: String
    ): String {
        return buildString {
            appendLine("You are an expert Maritime, Intermodal, and Cargo Logistics Surveyor.")
            appendLine("Your task is to analyze $imageCount images representing different angles/views of a single consignment or container.")
            appendLine("Cargo Identifier: $cargoId | Equipment Type: $cargoType")
            if (additionalNotes.isNotBlank()) {
                appendLine("Surveyor Field Notes: $additionalNotes")
            }
            appendLine()
            appendLine("SURVEY GUIDELINES:")
            appendLine("1. Multi-Angle Correlation:")
            appendLine("   - Correlate observations across all $imageCount images provided in the request.")
            appendLine("   - Cross-reference visible container numbers or markings across angles to ensure consistency.")
            appendLine("   - If any single angle exhibits a critical defect (e.g. punctured panel, bowed door, missing seal, or bent corner casting), the overall status MUST reflect that defect.")
            appendLine("2. Structural & Integrity Assessment:")
            appendLine("   - Wall Panels: Look for punctures, deep indentations, torn corten steel, or weld separations.")
            appendLine("   - Locking Gear & Seals: Inspect rear door locking bars, cam keepers, handles, and whether a High-Security Bolt Seal (HSS) is intact.")
            appendLine("   - Roof & Floor: Check for outward roof buckling or floor board rot/moisture.")
            appendLine("3. Severity Grading:")
            appendLine("   - Pass: Normal wear and tear, superficial paint scratches, surface rust. Fit for loading/transport.")
            appendLine("   - Warn: Minor dents, partially torn hazard placards, or non-structural blemishes that require monitoring.")
            appendLine("   - Fail: Breached walls/roof, unseated door gaskets, absent/broken security seals, major post buckling, or damaged corner castings.")
            appendLine()
            appendLine("Output strictly valid JSON matching the schema. In 'angle_breakdown', provide an entry for each viewpoint observed (e.g. 'Front Wall', 'Rear Doors', 'Left Side', 'Right Side', 'Roof', 'Security Seal', 'Interior').")
        }
    }

    private fun buildGeminiRequestBody(promptText: String, base64Images: List<String>): JSONObject {
        val root = JSONObject()

        val contentsArray = JSONArray()
        val contentObj = JSONObject()
        val partsArray = JSONArray()

        val textPart = JSONObject()
        textPart.put("text", promptText)
        partsArray.put(textPart)

        for (imgBase64 in base64Images) {
            val imagePart = JSONObject()
            val inlineData = JSONObject()
            inlineData.put("mime_type", "image/jpeg")
            inlineData.put("data", imgBase64)
            imagePart.put("inline_data", inlineData)
            partsArray.put(imagePart)
        }

        contentObj.put("parts", partsArray)
        contentsArray.put(contentObj)
        root.put("contents", contentsArray)

        val generationConfig = JSONObject()
        generationConfig.put("response_mime_type", "application/json")
        generationConfig.put("temperature", 0.1)

        val responseSchema = JSONObject()
        responseSchema.put("type", "OBJECT")

        val properties = JSONObject()

        val statusProp = JSONObject().apply {
            put("type", "STRING")
            put("enum", JSONArray().apply { put("Pass"); put("Warn"); put("Fail") })
        }
        properties.put("status", statusProp)

        val severityProp = JSONObject().apply {
            put("type", "STRING")
            put("enum", JSONArray().apply { put("None"); put("Low"); put("Medium"); put("High") })
        }
        properties.put("severity", severityProp)

        val damageCatProp = JSONObject().apply {
            put("type", "STRING")
            put("enum", JSONArray().apply {
                put("None"); put("Structural"); put("Packaging"); put("Cargo Shifting")
                put("Seal/Security"); put("Hazard Labeling"); put("Environmental/Moisture"); put("Multiple")
            })
        }
        properties.put("damage_category", damageCatProp)

        val findingsProp = JSONObject().apply { put("type", "STRING") }
        properties.put("findings_summary", findingsProp)

        val sealProp = JSONObject().apply { put("type", "BOOLEAN") }
        properties.put("container_seal_visible", sealProp)

        val angleBreakdownProp = JSONObject().apply {
            put("type", "ARRAY")
            val angleItemObj = JSONObject().apply {
                put("type", "OBJECT")
                val angleProps = JSONObject().apply {
                    put("view_name", JSONObject().apply { put("type", "STRING") })
                    put("condition", JSONObject().apply { put("type", "STRING") })
                }
                put("properties", angleProps)
                put("required", JSONArray().apply { put("view_name"); put("condition") })
            }
            put("items", angleItemObj)
        }
        properties.put("angle_breakdown", angleBreakdownProp)

        val visibleDefectsProp = JSONObject().apply {
            put("type", "ARRAY")
            val defectItemObj = JSONObject().apply {
                put("type", "OBJECT")
                val defectProps = JSONObject().apply {
                    put("item", JSONObject().apply { put("type", "STRING") })
                    put("description", JSONObject().apply { put("type", "STRING") })
                    put("impact", JSONObject().apply { put("type", "STRING") })
                }
                put("properties", defectProps)
                put("required", JSONArray().apply { put("item"); put("description") })
            }
            put("items", defectItemObj)
        }
        properties.put("visible_defects", visibleDefectsProp)

        val actionsProp = JSONObject().apply {
            put("type", "ARRAY")
            put("items", JSONObject().apply { put("type", "STRING") })
        }
        properties.put("recommended_actions", actionsProp)

        responseSchema.put("properties", properties)
        responseSchema.put("required", JSONArray().apply {
            put("status")
            put("severity")
            put("damage_category")
            put("findings_summary")
            put("container_seal_visible")
            put("angle_breakdown")
            put("visible_defects")
            put("recommended_actions")
        })

        generationConfig.put("response_schema", responseSchema)
        root.put("generationConfig", generationConfig)

        return root
    }

    private fun parseGeminiResponse(rawResponseBody: String): InspectionReport {
        val root = JSONObject(rawResponseBody)
        val candidates = root.optJSONArray("candidates")
            ?: throw IllegalStateException("Gemini response contains no candidates: $rawResponseBody")

        if (candidates.length() == 0) {
            throw IllegalStateException("Empty candidates in response")
        }

        val firstCandidate = candidates.getJSONObject(0)
        val content = firstCandidate.getJSONObject("content")
        val parts = content.getJSONArray("parts")
        val jsonText = parts.getJSONObject(0).getString("text").trim()

        val reportJson = JSONObject(jsonText)

        val status = reportJson.optString("status", "Warn")
        val severity = reportJson.optString("severity", "Medium")
        val damageCategory = reportJson.optString("damage_category", "Multiple")
        val findingsSummary = reportJson.optString("findings_summary", "Multi-angle inspection completed.")
        val containerSealVisible = reportJson.optBoolean("container_seal_visible", false)

        val angleList = mutableListOf<AngleBreakdownItem>()
        val angleArray = reportJson.optJSONArray("angle_breakdown")
        if (angleArray != null) {
            for (i in 0 until angleArray.length()) {
                val a = angleArray.getJSONObject(i)
                angleList.add(
                    AngleBreakdownItem(
                        viewName = a.optString("view_name", "View ${i + 1}"),
                        condition = a.optString("condition", "")
                    )
                )
            }
        }

        val defectsList = mutableListOf<VisibleDefect>()
        val defectsArray = reportJson.optJSONArray("visible_defects")
        if (defectsArray != null) {
            for (i in 0 until defectsArray.length()) {
                val d = defectsArray.getJSONObject(i)
                defectsList.add(
                    VisibleDefect(
                        item = d.optString("item", "Observed Defect"),
                        description = d.optString("description", ""),
                        impact = if (d.has("impact")) d.optString("impact") else null
                    )
                )
            }
        }

        val actionsList = mutableListOf<String>()
        val actionsArray = reportJson.optJSONArray("recommended_actions")
        if (actionsArray != null) {
            for (i in 0 until actionsArray.length()) {
                actionsList.add(actionsArray.getString(i))
            }
        }

        return InspectionReport(
            status = status,
            severity = severity,
            damageCategory = damageCategory,
            findingsSummary = findingsSummary,
            containerSealVisible = containerSealVisible,
            angleBreakdown = angleList,
            visibleDefects = defectsList,
            recommendedActions = actionsList
        )
    }

    private fun scaleDownBitmap(bitmap: Bitmap, maxDimension: Int = 1024): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        if (width <= maxDimension && height <= maxDimension) return bitmap

        val ratio = width.toFloat() / height.toFloat()
        val targetWidth: Int
        val targetHeight: Int

        if (ratio > 1) {
            targetWidth = maxDimension
            targetHeight = (maxDimension / ratio).toInt()
        } else {
            targetHeight = maxDimension
            targetWidth = (maxDimension * ratio).toInt()
        }

        return Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true)
    }

    private fun Bitmap.toJpegBase64(quality: Int = 82): String {
        val outputStream = ByteArrayOutputStream()
        this.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
        return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
    }
}

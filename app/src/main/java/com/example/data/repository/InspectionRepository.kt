package com.example.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.example.data.db.CargoDatabase
import com.example.data.db.InspectionDao
import com.example.data.db.InspectionEntity
import com.example.data.model.InspectionReport
import com.example.data.network.GeminiSurveyorService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class InspectionRepository(private val context: Context) {
    private val database = CargoDatabase.getInstance(context)
    private val dao: InspectionDao = database.inspectionDao()

    fun getAllInspections(): Flow<List<InspectionEntity>> = dao.getAllInspections()

    fun getInspectionById(id: Long): Flow<InspectionEntity?> = dao.getInspectionById(id)

    suspend fun runGeminiInspection(
        bitmap: Bitmap,
        cargoId: String,
        cargoType: String,
        notes: String
    ): Result<InspectionReport> {
        return GeminiSurveyorService.inspectCargoImage(
            bitmap = bitmap,
            cargoId = cargoId,
            cargoType = cargoType,
            additionalSurveyorNotes = notes
        )
    }

    suspend fun saveInspection(
        report: InspectionReport,
        cargoId: String,
        cargoType: String,
        imageUri: String?,
        notes: String,
        location: String = "Port Logistics Terminal"
    ): Long = withContext(Dispatchers.IO) {
        val entity = InspectionEntity.fromReport(
            report = report,
            cargoIdentifier = cargoId,
            cargoType = cargoType,
            imageUri = imageUri,
            surveyorNotes = notes,
            location = location
        )
        dao.insertInspection(entity)
    }

    suspend fun updateInspection(entity: InspectionEntity) = withContext(Dispatchers.IO) {
        dao.updateInspection(entity)
    }

    suspend fun deleteInspection(entity: InspectionEntity) = withContext(Dispatchers.IO) {
        dao.deleteInspection(entity)
    }

    suspend fun clearHistory() = withContext(Dispatchers.IO) {
        dao.clearAll()
    }

    suspend fun saveBitmapToFile(bitmap: Bitmap, prefix: String = "survey"): String = withContext(Dispatchers.IO) {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss_SSS", Locale.US).format(Date())
        val imagesDir = File(context.filesDir, "inspections").apply { if (!exists()) mkdirs() }
        val imageFile = File(imagesDir, "${prefix}_$timeStamp.jpg")

        FileOutputStream(imageFile).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
        }
        imageFile.absolutePath
    }

    suspend fun loadBitmapFromUri(uri: Uri): Bitmap? = withContext(Dispatchers.IO) {
        try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            inputStream?.use { BitmapFactory.decodeStream(it) }
        } catch (_: Exception) {
            null
        }
    }
}

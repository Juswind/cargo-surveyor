package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.InspectionReport
import com.example.data.model.VisibleDefect
import org.json.JSONArray
import org.json.JSONObject

@Entity(tableName = "inspections")
data class InspectionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val cargoIdentifier: String,
    val cargoType: String,
    val imageUri: String? = null,
    val status: String,
    val severity: String,
    val damageCategory: String,
    val findingsSummary: String,
    val visibleDefectsJson: String,
    val recommendedActionsJson: String,
    val containerSealVisible: Boolean,
    val surveyorNotes: String = "",
    val location: String = "Port Logistics Terminal"
) {
    fun toReport(): InspectionReport {
        val defects = mutableListOf<VisibleDefect>()
        try {
            val array = JSONArray(visibleDefectsJson)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                defects.add(
                    VisibleDefect(
                        item = obj.optString("item", "Unidentified Item"),
                        description = obj.optString("description", ""),
                        impact = if (obj.has("impact")) obj.optString("impact") else null
                    )
                )
            }
        } catch (_: Exception) { }

        val actions = mutableListOf<String>()
        try {
            val array = JSONArray(recommendedActionsJson)
            for (i in 0 until array.length()) {
                actions.add(array.getString(i))
            }
        } catch (_: Exception) { }

        return InspectionReport(
            status = status,
            severity = severity,
            damageCategory = damageCategory,
            findingsSummary = findingsSummary,
            visibleDefects = defects,
            recommendedActions = actions,
            containerSealVisible = containerSealVisible
        )
    }

    companion object {
        fun fromReport(
            report: InspectionReport,
            cargoIdentifier: String,
            cargoType: String,
            imageUri: String?,
            surveyorNotes: String = "",
            location: String = "Port Logistics Terminal"
        ): InspectionEntity {
            val defectsArray = JSONArray()
            for (defect in report.visibleDefects) {
                val obj = JSONObject()
                obj.put("item", defect.item)
                obj.put("description", defect.description)
                defect.impact?.let { obj.put("impact", it) }
                defectsArray.put(obj)
            }

            val actionsArray = JSONArray()
            for (action in report.recommendedActions) {
                actionsArray.put(action)
            }

            return InspectionEntity(
                cargoIdentifier = cargoIdentifier,
                cargoType = cargoType,
                imageUri = imageUri,
                status = report.status,
                severity = report.severity,
                damageCategory = report.damageCategory,
                findingsSummary = report.findingsSummary,
                visibleDefectsJson = defectsArray.toString(),
                recommendedActionsJson = actionsArray.toString(),
                containerSealVisible = report.containerSealVisible,
                surveyorNotes = surveyorNotes,
                location = location
            )
        }
    }
}

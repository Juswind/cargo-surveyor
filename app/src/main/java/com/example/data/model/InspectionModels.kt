package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class InspectionReport(
    @param:Json(name = "status")
    val status: String = "Pass",
    @param:Json(name = "severity")
    val severity: String = "None",
    @param:Json(name = "damage_category")
    val damageCategory: String = "None",
    @param:Json(name = "findings_summary")
    val findingsSummary: String = "",
    @param:Json(name = "container_seal_visible")
    val containerSealVisible: Boolean = false,
    @param:Json(name = "angle_breakdown")
    val angleBreakdown: List<AngleBreakdownItem> = emptyList(),
    @param:Json(name = "visible_defects")
    val visibleDefects: List<VisibleDefect> = emptyList(),
    @param:Json(name = "recommended_actions")
    val recommendedActions: List<String> = emptyList()
) {
    val isPass: Boolean get() = status.equals("Pass", ignoreCase = true)
    val isWarn: Boolean get() = status.equals("Warn", ignoreCase = true)
    val isFail: Boolean get() = status.equals("Fail", ignoreCase = true)
}

@JsonClass(generateAdapter = true)
data class AngleBreakdownItem(
    @param:Json(name = "view_name")
    val viewName: String,
    @param:Json(name = "condition")
    val condition: String
)

@JsonClass(generateAdapter = true)
data class VisibleDefect(
    @param:Json(name = "item")
    val item: String,
    @param:Json(name = "description")
    val description: String,
    @param:Json(name = "impact")
    val impact: String? = null
)

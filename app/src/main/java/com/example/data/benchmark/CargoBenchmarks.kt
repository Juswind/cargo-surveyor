package com.example.data.benchmark

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import com.example.data.model.AngleBreakdownItem
import com.example.data.model.InspectionReport
import com.example.data.model.VisibleDefect

data class CargoBenchmarkScenario(
    val id: String,
    val title: String,
    val subtitle: String,
    val cargoIdentifier: String,
    val cargoType: String,
    val expectedStatus: String,
    val expectedSeverity: String,
    val expectedCategory: String,
    val baselineReport: InspectionReport,
    val drawDescription: String
)

object CargoBenchmarks {
    val scenarios: List<CargoBenchmarkScenario> = listOf(
        CargoBenchmarkScenario(
            id = "structural_buckle",
            title = "Corner Casting & Post Buckling",
            subtitle = "40ft ISO Dry Van with crushed bottom corner & puncture hole",
            cargoIdentifier = "MSKU 782019-4",
            cargoType = "40ft High Cube Container",
            expectedStatus = "Fail",
            expectedSeverity = "High",
            expectedCategory = "Structural",
            baselineReport = InspectionReport(
                status = "Fail",
                severity = "High",
                damageCategory = "Structural",
                findingsSummary = "Critical structural failure identified on bottom left corner post and lower side rail. Significant mechanical impact buckling exceeds ISO 1496-1 dimensional tolerance limits, with a 15cm jagged puncture breach.",
                containerSealVisible = false,
                angleBreakdown = listOf(
                    AngleBreakdownItem("Left Front Corner Post", "Severe inward longitudinal buckling (>35mm)"),
                    AngleBreakdownItem("Lower Side Corrugated Panel", "15cm jagged puncture tear breach")
                ),
                visibleDefects = listOf(
                    VisibleDefect(
                        item = "Corner Post Bottom Casting",
                        description = "Deformed corner post with inward longitudinal buckle exceeding 35mm deflection.",
                        impact = "Loss of stacking integrity; high risk of container collapse if loaded into cell guides."
                    ),
                    VisibleDefect(
                        item = "Corrugated Side Panel",
                        description = "15cm jagged puncture hole through steel panel with torn metal edges.",
                        impact = "Weather-tight integrity destroyed; water and contaminant ingress guaranteed."
                    )
                ),
                recommendedActions = listOf(
                    "DO NOT LOAD: Immediate rejection for vessel stowage or rail transport.",
                    "Tag unit OUT-OF-SERVICE pursuant to CSC regulations.",
                    "Transfer cargo contents to replacement certified sound container."
                )
            ),
            drawDescription = "Shows crushed corner post with metallic fracture and torn steel panel"
        ),
        CargoBenchmarkScenario(
            id = "compromised_seal",
            title = "Compromised High-Security Seal",
            subtitle = "Rear locking door bar with sheared bolt seal and missing cable",
            cargoIdentifier = "CMAU 491204-1",
            cargoType = "ISO Shipping Container Rear Doors",
            expectedStatus = "Fail",
            expectedSeverity = "High",
            expectedCategory = "Seal/Security",
            baselineReport = InspectionReport(
                status = "Fail",
                severity = "High",
                damageCategory = "Seal/Security",
                findingsSummary = "High-Security Bolt Seal (ISO 17712 HSS) has been severed at the locking cam hasp. Right door latch mechanism is loose with evidence of mechanical pry-bar tampering.",
                containerSealVisible = true,
                angleBreakdown = listOf(
                    AngleBreakdownItem("Rear Right Door Leaf", "Locking handle loose, pry scratch marks on paint"),
                    AngleBreakdownItem("Door Cam Keeper & Seal Hasp", "Bolt pin sheared cleanly through shank, locking barrel missing")
                ),
                visibleDefects = listOf(
                    VisibleDefect(
                        item = "ISO 17712 High-Security Bolt Seal",
                        description = "Bolt pin sheared cleanly through shank; bottom locking barrel missing from keeper aperture.",
                        impact = "Chain of custody broken; cargo theft, contraband introduction, or cargo contamination risk."
                    )
                ),
                recommendedActions = listOf(
                    "HALT DISPATCH: Quarantine container in secure customs inspection holding pen.",
                    "Notify Port Security and Customs Border Protection immediately.",
                    "Perform 100% unstuffing tally and manifest inventory audit."
                )
            ),
            drawDescription = "Shows rear container doors with severed bolt seal and pry marks"
        ),
        CargoBenchmarkScenario(
            id = "cargo_shift_straps",
            title = "Loose Drums & Broken Straps",
            subtitle = "Tilted pallet load with snapped 50mm polyester lashing inside container",
            cargoIdentifier = "TGHU 902184-7",
            cargoType = "Palletized Chemical Drums (200L)",
            expectedStatus = "Fail",
            expectedSeverity = "High",
            expectedCategory = "Cargo Shifting",
            baselineReport = InspectionReport(
                status = "Fail",
                severity = "High",
                damageCategory = "Cargo Shifting",
                findingsSummary = "Severe cargo shift observed inside container. Composite strapping has severed against drum chime edges. Drums tilted 24 degrees with impending pallet collapse.",
                containerSealVisible = false,
                angleBreakdown = listOf(
                    AngleBreakdownItem("Interior Bay View", "Two 200L chemical drums tilted 24 degrees transverse"),
                    AngleBreakdownItem("Lashing Anchor Points", "50mm polyester straps snapped and frayed")
                ),
                visibleDefects = listOf(
                    VisibleDefect(
                        item = "Polyester Lashing Straps (50mm)",
                        description = "Two primary transverse tie-down straps snapped clean due to dynamic shock loading.",
                        impact = "Zero restraint remaining against vessel roll or truck brake deceleration."
                    )
                ),
                recommendedActions = listOf(
                    "DO NOT MOVE CONTAINER: Extreme tip hazard upon trailer coupling.",
                    "Re-block and brace load with heavy timber dunnage and certified ratchet lashing belts."
                )
            ),
            drawDescription = "Shows leaning steel drums with broken ratchet straps"
        ),
        CargoBenchmarkScenario(
            id = "weathered_placard",
            title = "Damaged Flammable Placard",
            subtitle = "Torn UN 1993 Flammable Liquid placard with legible numbers",
            cargoIdentifier = "HLXU 523190-2",
            cargoType = "ISO Tank / Hazardous Freight Box",
            expectedStatus = "Warn",
            expectedSeverity = "Medium",
            expectedCategory = "Hazard Labeling",
            baselineReport = InspectionReport(
                status = "Warn",
                severity = "Medium",
                damageCategory = "Hazard Labeling",
                findingsSummary = "IMO Class 3 Flammable Liquid diamond placard is partially peeled along the lower corner. UN 1993 four-digit identification remains legible.",
                containerSealVisible = false,
                angleBreakdown = listOf(
                    AngleBreakdownItem("Side Panel Hazard Area", "Class 3 diamond placard lower-right quadrant peeled 40mm")
                ),
                visibleDefects = listOf(
                    VisibleDefect(
                        item = "IMO Class 3 Hazard Placard",
                        description = "Diamond placard (250mm x 250mm) has lower right quadrant peeled back 40mm.",
                        impact = "Non-compliance with IMDG Code Chapter 5.3; risk of full detachment in heavy weather."
                    )
                ),
                recommendedActions = listOf(
                    "Clean metal backing plate and apply new heavy-duty Class 3 placard overlay.",
                    "Ensure UN 1993 is 100% visible from a distance of 15 meters."
                )
            ),
            drawDescription = "Shows red flammable liquid diamond placard with torn edge"
        ),
        CargoBenchmarkScenario(
            id = "certified_sound_pass",
            title = "Sound ISO 40ft Container",
            subtitle = "Clean corrugated panels, tight door gaskets, and intact bolt seal",
            cargoIdentifier = "MEDU 318902-8",
            cargoType = "40ft High Cube ISO Dry Van",
            expectedStatus = "Pass",
            expectedSeverity = "None",
            expectedCategory = "None",
            baselineReport = InspectionReport(
                status = "Pass",
                severity = "None",
                damageCategory = "None",
                findingsSummary = "Container is in excellent seaworthy condition. Zero structural deformation, no holes, tears, or buckling observed. Door seals, locking handles, and CSC safety plate are intact and fully compliant.",
                containerSealVisible = true,
                angleBreakdown = listOf(
                    AngleBreakdownItem("Exterior Profile", "Clean corrugated side panels without punctures or dents"),
                    AngleBreakdownItem("Rear Door Hardware", "All 4 locking bars engage snugly; ISO 17712 bolt seal locked")
                ),
                visibleDefects = emptyList(),
                recommendedActions = listOf(
                    "PASSED: Clear container for immediate loading, vessel stowage, or intermodal dispatch.",
                    "Verify seal number matches bill of lading before departure gate out."
                )
            ),
            drawDescription = "Shows pristine shipping container doors with high-security seal locked"
        )
    )

    fun createScenarioBitmap(scenario: CargoBenchmarkScenario, width: Int = 1080, height: Int = 720): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        when (scenario.id) {
            "structural_buckle" -> {
                paint.color = Color.rgb(38, 45, 54)
                canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
                paint.color = Color.rgb(22, 78, 140)
                canvas.drawRect(width * 0.08f, height * 0.2f, width * 0.92f, height * 0.85f, paint)
                // Buckle
                paint.color = Color.rgb(18, 55, 95)
                val path = Path().apply {
                    moveTo(width * 0.08f, height * 0.2f)
                    lineTo(width * 0.15f, height * 0.2f)
                    lineTo(width * 0.2f, height * 0.6f)
                    lineTo(width * 0.05f, height * 0.75f)
                    close()
                }
                canvas.drawPath(path, paint)
                // Puncture hole
                paint.color = Color.rgb(15, 18, 22)
                canvas.drawRect(width * 0.18f, height * 0.5f, width * 0.32f, height * 0.68f, paint)
            }
            "compromised_seal" -> {
                paint.color = Color.rgb(220, 160, 30)
                canvas.drawRect(width * 0.1f, height * 0.1f, width * 0.9f, height * 0.9f, paint)
                paint.color = Color.rgb(180, 190, 200)
                canvas.drawRect(width * 0.45f, height * 0.1f, width * 0.48f, height * 0.9f, paint)
                canvas.drawRect(width * 0.52f, height * 0.1f, width * 0.55f, height * 0.9f, paint)
                // Severed seal in red
                paint.color = Color.rgb(220, 40, 40)
                canvas.drawCircle(width * 0.56f, height * 0.55f, 20f, paint)
            }
            "cargo_shift_straps" -> {
                paint.color = Color.rgb(28, 35, 42)
                canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
                paint.color = Color.rgb(160, 120, 75)
                canvas.drawRect(0f, height * 0.75f, width.toFloat(), height.toFloat(), paint)
                // Leaning drum
                canvas.save()
                canvas.rotate(24f, width * 0.6f, height * 0.7f)
                paint.color = Color.rgb(28, 85, 155)
                canvas.drawRoundRect(RectF(width * 0.5f, height * 0.3f, width * 0.7f, height * 0.7f), 15f, 15f, paint)
                canvas.restore()
                // Broken strap
                paint.color = Color.rgb(255, 60, 30)
                paint.strokeWidth = 10f
                canvas.drawLine(width * 0.1f, height * 0.45f, width * 0.38f, height * 0.52f, paint)
            }
            "weathered_placard" -> {
                paint.color = Color.rgb(155, 55, 40)
                canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
                val cx = width * 0.5f
                val cy = height * 0.5f
                val size = 200f
                paint.color = Color.rgb(225, 30, 25)
                val diamond = Path().apply {
                    moveTo(cx, cy - size)
                    lineTo(cx + size, cy)
                    lineTo(cx, cy + size)
                    lineTo(cx - size, cy)
                    close()
                }
                canvas.drawPath(diamond, paint)
                paint.color = Color.WHITE
                paint.textSize = 36f
                paint.textAlign = Paint.Align.CENTER
                canvas.drawText("UN 1993", cx, cy + 20f, paint)
                canvas.drawText("3", cx, cy + size - 30f, paint)
            }
            else -> {
                // Sound Container
                paint.color = Color.rgb(18, 92, 168)
                canvas.drawRect(width * 0.12f, height * 0.15f, width * 0.88f, height * 0.85f, paint)
                paint.color = Color.rgb(16, 185, 129)
                canvas.drawCircle(width * 0.82f, height * 0.75f, 22f, paint)
            }
        }

        // Overlay Identifier text
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 28f
            isFakeBoldText = true
        }
        canvas.drawText(scenario.cargoIdentifier, width * 0.12f, height * 0.12f, textPaint)

        return bitmap
    }
}

package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.benchmark.CargoBenchmarks
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read app_name string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("CargoSurveyor", appName)
    }

    @Test
    fun `verify benchmark scenarios conform to inspection schema`() {
        val scenarios = CargoBenchmarks.scenarios
        assertTrue(scenarios.isNotEmpty())

        val validStatuses = setOf("Pass", "Warn", "Fail")
        val validSeverities = setOf("None", "Low", "Medium", "High")
        val validCategories = setOf(
            "None", "Structural", "Packaging", "Cargo Shifting",
            "Seal/Security", "Hazard Labeling", "Environmental/Moisture", "Multiple"
        )

        for (scenario in scenarios) {
            val report = scenario.baselineReport
            assertTrue("Status must be Pass, Warn, or Fail", validStatuses.contains(report.status))
            assertTrue("Severity must be valid", validSeverities.contains(report.severity))
            assertTrue("Category must be valid", validCategories.contains(report.damageCategory))
            assertTrue("Findings summary cannot be empty", report.findingsSummary.isNotBlank())
            assertTrue("Recommended actions cannot be empty", report.recommendedActions.isNotEmpty())
        }
    }
}

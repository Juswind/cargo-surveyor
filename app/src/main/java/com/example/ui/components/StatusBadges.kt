package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SeverityHigh
import com.example.ui.theme.SeverityLow
import com.example.ui.theme.SeverityMedium
import com.example.ui.theme.SeverityNone
import com.example.ui.theme.StatusFailContainer
import com.example.ui.theme.StatusFailRed
import com.example.ui.theme.StatusFailText
import com.example.ui.theme.StatusPassContainer
import com.example.ui.theme.StatusPassGreen
import com.example.ui.theme.StatusPassText
import com.example.ui.theme.StatusWarnAmber
import com.example.ui.theme.StatusWarnContainer
import com.example.ui.theme.StatusWarnText

@Composable
fun InspectionStatusBadge(
    status: String,
    modifier: Modifier = Modifier,
    isLarge: Boolean = false
) {
    val (bgColor, textColor, icon, label) = when (status.uppercase()) {
        "PASS" -> Quad(StatusPassContainer, StatusPassText, Icons.Default.CheckCircle, "PASS - FIT FOR TRANSPORT")
        "WARN" -> Quad(StatusWarnContainer, StatusWarnText, Icons.Default.Warning, "WARN - DEFECT OBSERVED")
        else -> Quad(StatusFailContainer, StatusFailText, Icons.Default.Error, "FAIL - REJECT / REPAIR")
    }

    val paddingH = if (isLarge) 16.dp else 10.dp
    val paddingV = if (isLarge) 8.dp else 4.dp
    val fontSize = if (isLarge) 15.sp else 12.sp
    val iconSize = if (isLarge) 20.dp else 16.dp

    Box(
        modifier = modifier
            .testTag("status_badge_${status.lowercase()}")
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .padding(horizontal = paddingH, vertical = paddingV),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = textColor,
                modifier = Modifier.size(iconSize)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = if (isLarge) label else status.uppercase(),
                color = textColor,
                fontSize = fontSize,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        }
    }
}

@Composable
fun SeverityBadge(
    severity: String,
    modifier: Modifier = Modifier
) {
    val (color, label) = when (severity.uppercase()) {
        "HIGH" -> Pair(SeverityHigh, "HIGH RISK")
        "MEDIUM" -> Pair(SeverityMedium, "MEDIUM RISK")
        "LOW" -> Pair(SeverityLow, "LOW RISK")
        else -> Pair(SeverityNone, "NO SEVERITY")
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun DamageCategoryBadge(
    category: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = category.uppercase(),
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun SealStatusBadge(
    sealVisible: Boolean,
    modifier: Modifier = Modifier
) {
    val bgColor = if (sealVisible) Color(0xFFE0F2FE) else Color(0xFFF1F5F9)
    val contentColor = if (sealVisible) Color(0xFF0369A1) else Color(0xFF64748B)
    val text = if (sealVisible) "SEAL / LATCH VISIBLE" else "NO SEAL IN FRAME"
    val icon = if (sealVisible) Icons.Default.Lock else Icons.Default.LockOpen

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = contentColor,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = text,
                color = contentColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

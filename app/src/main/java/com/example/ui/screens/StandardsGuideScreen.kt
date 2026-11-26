package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Dangerous
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.InspectionViewModel
import com.example.ui.Screen
import com.example.ui.theme.Navy900
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate700
import com.example.ui.theme.StatusFailRed
import com.example.ui.theme.StatusPassGreen
import com.example.ui.theme.StatusWarnAmber
import com.example.ui.theme.SteelBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StandardsGuideScreen(
    viewModel: InspectionViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler {
        viewModel.navigateTo(Screen.HUB)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Surveyor Inspection Criteria",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateTo(Screen.HUB) },
                        modifier = Modifier.testTag("standards_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to Yard Hub",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Navy900,
                    titleContentColor = Color.White
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(
                text = "SURVEY EVALUATION PROTOCOL",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Slate400,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Pass / Warn / Fail Rules Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(10.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    EvaluationVerdictRow(
                        title = "PASS",
                        color = StatusPassGreen,
                        icon = Icons.Default.Check,
                        criteria = "Zero damage or safety/hazard defects found. All panels, corner posts, and seals intact."
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    EvaluationVerdictRow(
                        title = "WARN",
                        color = StatusWarnAmber,
                        icon = Icons.Default.Warning,
                        criteria = "Minor issues such as light surface scratches, paint scuffs, or partially torn (but still legible) placards."
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    EvaluationVerdictRow(
                        title = "FAIL",
                        color = StatusFailRed,
                        icon = Icons.Default.Close,
                        criteria = "Visible physical damage (dents, holes, buckles), loose loads, broken security seals, or missing hazmat warnings."
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "FOUR MANDATORY INSPECTION PILLARS",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Slate400,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            InspectionPillarCard(
                number = "01",
                title = "Physical Damage",
                icon = Icons.Default.Build,
                subtitle = "CSC 1972 & IICL-6 Structural Tolerances",
                items = listOf(
                    "Corner Posts & Castings: Denting exceeding 25mm, cracks, weld tears.",
                    "Corrugated Panels: Punctures, corrosion holes, tearing.",
                    "Roof & Floor: Crossmember buckles, broken bottom floorboards, moisture ingress.",
                    "Side Rails: Lateral deformation, sagging."
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            InspectionPillarCard(
                number = "02",
                title = "Securing & Block / Brace",
                icon = Icons.Default.Dangerous,
                subtitle = "IMO/ILO/UNECE CTU Code of Practice",
                items = listOf(
                    "Lashing Straps: Severed, frayed, loose composite or steel strapping.",
                    "Cargo Shifting: Tilted drum pallets, leaning structures, collapsed tiers.",
                    "Dunnage & Wedges: Missing chocks, broken timbers, void gaps."
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            InspectionPillarCard(
                number = "03",
                title = "Labeling & Markings",
                icon = Icons.AutoMirrored.Filled.Label,
                subtitle = "IMDG Code & IATA Dangerous Goods Regulations",
                items = listOf(
                    "Hazard Placards: Class 1-9 diamonds (250mm x 250mm) torn or missing.",
                    "UN Numbers: 4-digit substance numbers obscured or weathered.",
                    "Orientation Arrows: Up-arrows inverted or missing from liquid freight."
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            InspectionPillarCard(
                number = "04",
                title = "Barrier Breaches & Seals",
                icon = Icons.Default.Lock,
                subtitle = "ISO 17712 High-Security Seals (HSS) & C-TPAT",
                items = listOf(
                    "Bolt / Cable Seals: Sheared shanks, loose barrels, tampered locking cam.",
                    "Door Hardware: Bent locking rods, missing keeper cams, torn door gaskets.",
                    "Rust Breaches: Pinholes or daylight visible through roof or walls."
                )
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun EvaluationVerdictRow(
    title: String,
    color: Color,
    icon: ImageVector,
    criteria: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(color),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(14.dp)
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = criteria,
                fontSize = 12.sp,
                color = Color(0xFF334155),
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
fun InspectionPillarCard(
    number: String,
    title: String,
    icon: ImageVector,
    subtitle: String,
    items: List<String>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(SteelBlue.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = SteelBlue,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "$number. $title",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = subtitle,
                            fontSize = 10.sp,
                            color = Slate400,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            items.forEach { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = "•",
                        fontSize = 14.sp,
                        color = SteelBlue,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                    Text(
                        text = item,
                        fontSize = 12.sp,
                        color = Color(0xFF475569),
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}

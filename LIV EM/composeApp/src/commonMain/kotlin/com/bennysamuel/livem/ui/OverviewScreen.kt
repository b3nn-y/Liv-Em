package com.bennysamuel.livem.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun OverviewContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
    ) {
        Text(
            text = "Welcome back, User",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(Modifier.height(24.dp))

        Row(modifier = Modifier.fillMaxWidth().height(160.dp)) {
            BentoCard(
                modifier = Modifier.weight(1.5f),
                title = "Latest Story",
                content = "A walk in the park...",
                color = MaterialTheme.colorScheme.surfaceVariant
            )
            Spacer(Modifier.width(12.dp))
            BentoCard(
                modifier = Modifier.weight(1f),
                title = "Mood",
                content = "Reflective",
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
            )
        }

        Spacer(Modifier.height(12.dp))

        BentoCard(
            modifier = Modifier.fillMaxWidth().height(120.dp),
            title = "Daily Todo",
            content = "3 tasks remaining",
            color = MaterialTheme.colorScheme.surface
        )

        Spacer(Modifier.height(12.dp))

        Row(modifier = Modifier.fillMaxWidth().height(180.dp)) {
            BentoCard(
                modifier = Modifier.weight(1f),
                title = "Workout",
                content = "Upper Body",
                color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.2f)
            )
            Spacer(Modifier.width(12.dp))
            BentoCard(
                modifier = Modifier.weight(1f),
                title = "Journaling",
                content = "Day 12 Streak",
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            )
        }

        Spacer(Modifier.height(120.dp))
    }
}

@Composable
fun BentoCard(
    modifier: Modifier,
    title: String,
    content: String,
    color: Color
) {
    Surface(
        modifier = modifier,
        color = color,
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.05f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(title, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.weight(1f))
            Text(content, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
        }
    }
}
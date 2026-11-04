package pe.edu.upc.predictivemaintain.app.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun StatusChip(
    text: String,
    modifier: Modifier = Modifier
) {
    AssistChip(
        onClick = { },
        label = { Text(text = text) },
        modifier = modifier
    )
}

enum class StatusTone(val background: Color, val content: Color) {
    DANGER(Color(0xFFFEE2E2), Color(0xFFB91C1C)),
    NEUTRAL(Color(0xFFE2E8F0), Color(0xFF475569)),
    SUCCESS(Color(0xFFDCFCE7), Color(0xFF15803D))
}

/** Pill-shaped colored status label (light background, dark text). */
@Composable
fun ColoredStatusChip(
    text: String,
    tone: StatusTone,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        color = tone.content,
        style = MaterialTheme.typography.labelMedium,
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(tone.background)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    )
}

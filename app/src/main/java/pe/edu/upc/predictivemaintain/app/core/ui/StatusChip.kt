package pe.edu.upc.predictivemaintain.app.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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

/**
 * Colors of a status. [background]/[content] are the chip (text contrast >= 5:1 on its background);
 * [accent] is the stronger color used for the chip border and the card's side bar.
 * The app only has a light theme today; if a dark scheme is added, this enum is the single place to adapt.
 */
enum class StatusTone(val background: Color, val content: Color, val accent: Color) {
    DANGER(Color(0xFFFECACA), Color(0xFF991B1B), Color(0xFFDC2626)),
    SUCCESS(Color(0xFFBBF7D0), Color(0xFF166534), Color(0xFF22C55E)),
    WARNING(Color(0xFFFDE68A), Color(0xFF78350F), Color(0xFFF59E0B)),
    INFO(Color(0xFFBAE6FD), Color(0xFF075985), Color(0xFF0EA5E9)),
    NEUTRAL(Color(0xFFE2E8F0), Color(0xFF334155), Color(0xFF94A3B8))
}

/** Pill-shaped colored status label (tinted background, dark text, thin accent border). */
@Composable
fun ColoredStatusChip(
    text: String,
    tone: StatusTone,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(50)
    Text(
        text = text,
        color = tone.content,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold,
        modifier = modifier
            .clip(shape)
            .background(tone.background)
            .border(1.dp, tone.accent, shape)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    )
}

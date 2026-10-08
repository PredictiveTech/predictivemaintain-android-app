package pe.edu.upc.predictivemaintain.app.core.ui

import androidx.compose.material3.AssistChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

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

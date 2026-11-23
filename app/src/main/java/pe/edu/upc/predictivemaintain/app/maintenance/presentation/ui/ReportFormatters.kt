package pe.edu.upc.predictivemaintain.app.maintenance.presentation.ui

import pe.edu.upc.predictivemaintain.app.core.ui.StatusTone
import java.util.Locale

/** "1 d 2 h 3 min", "45 min", "30 s". Seconds only show when the whole value is under a minute. */
internal fun formatDuration(totalSeconds: Long): String {
    if (totalSeconds < 60) return "$totalSeconds s"
    val days = totalSeconds / 86_400
    val hours = totalSeconds % 86_400 / 3_600
    val minutes = totalSeconds % 3_600 / 60
    return buildList {
        if (days > 0) add("$days d")
        if (hours > 0) add("$hours h")
        if (minutes > 0 || isEmpty()) add("$minutes min")
    }.joinToString(" ")
}

internal fun formatPercent(percent: Double): String = String.format(Locale.getDefault(), "%.2f%%", percent)

/** Green from 99%, amber from 95%, red below. The thresholds are a display choice, not a backend rule. */
internal fun availabilityTone(percent: Double): StatusTone = when {
    percent >= 99.0 -> StatusTone.SUCCESS
    percent >= 95.0 -> StatusTone.WARNING
    else -> StatusTone.DANGER
}

package pe.edu.upc.predictivemaintain.app.maintenance.presentation.viewmodel

import pe.edu.upc.predictivemaintain.app.core.error.AppError

internal fun AppError.toReportMessage(): String = when (this) {
    is AppError.Api -> detail
    is AppError.Network -> "Network error. Please check your connection."
    is AppError.InvalidResponse -> "Unexpected server response."
    is AppError.SessionExpired -> "Session expired."
    is AppError.Unknown -> "An unexpected error occurred."
}

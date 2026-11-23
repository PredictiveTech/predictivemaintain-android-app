package pe.edu.upc.predictivemaintain.app.maintenance.infrastructure.local

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import pe.edu.upc.predictivemaintain.app.core.error.AppError
import pe.edu.upc.predictivemaintain.app.core.error.Outcome
import pe.edu.upc.predictivemaintain.app.maintenance.domain.entity.ExportedReport
import java.io.File
import java.io.IOException
import javax.inject.Inject

/**
 * Saves a downloaded CSV in the public Downloads folder.
 * Android 10+ goes through MediaStore (no permission needed). Android 8/9 would need the legacy storage
 * permission to write to the public folder, so there the file goes to the app's own Downloads folder and
 * is shared through the FileProvider.
 */
class CsvFileSaver @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    suspend fun save(fileName: String, body: ResponseBody): Outcome<ExportedReport> =
        withContext(Dispatchers.IO) {
            body.use {
                try {
                    Outcome.Success(
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) saveWithMediaStore(fileName, it)
                        else saveInAppFolder(fileName, it)
                    )
                } catch (e: IOException) {
                    Outcome.Failure(AppError.Unknown(e))
                } catch (e: SecurityException) {
                    Outcome.Failure(AppError.Unknown(e))
                }
            }
        }

    private fun saveWithMediaStore(fileName: String, body: ResponseBody): ExportedReport {
        val resolver = context.contentResolver
        val values = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, fileName)
            put(MediaStore.Downloads.MIME_TYPE, MIME_TYPE)
            put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            put(MediaStore.Downloads.IS_PENDING, 1)
        }
        val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
            ?: throw IOException("Could not create the file in Downloads")
        try {
            resolver.openOutputStream(uri)?.use { out -> body.byteStream().use { it.copyTo(out) } }
                ?: throw IOException("Could not open the file in Downloads")
            resolver.update(uri, ContentValues().apply { put(MediaStore.Downloads.IS_PENDING, 0) }, null, null)
        } catch (e: Exception) {
            resolver.delete(uri, null, null) // do not leave a half written file behind
            throw e
        }
        // MediaStore may rename the file when the name already exists; the share sheet shows the stored name.
        return ExportedReport(uri.toString(), fileName, MIME_TYPE)
    }

    private fun saveInAppFolder(fileName: String, body: ResponseBody): ExportedReport {
        val dir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: context.cacheDir
        dir.mkdirs()
        val file = File(dir, fileName)
        try {
            file.outputStream().use { out -> body.byteStream().use { it.copyTo(out) } }
        } catch (e: IOException) {
            file.delete()
            throw e
        }
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        return ExportedReport(uri.toString(), fileName, MIME_TYPE)
    }

    private companion object {
        const val MIME_TYPE = "text/csv"
    }
}

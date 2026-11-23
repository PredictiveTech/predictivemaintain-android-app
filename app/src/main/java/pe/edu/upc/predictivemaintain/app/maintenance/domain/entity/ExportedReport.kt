package pe.edu.upc.predictivemaintain.app.maintenance.domain.entity

/** A report saved on the device. [uri] is a content URI that other apps can be granted to read. */
data class ExportedReport(
    val uri: String,
    val fileName: String,
    val mimeType: String
)

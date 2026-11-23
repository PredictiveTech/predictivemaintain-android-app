package pe.edu.upc.predictivemaintain.app.maintenance.infrastructure.local

import androidx.room.Database
import androidx.room.RoomDatabase

/** The app's SQLite database: the offline queue (US-28) and the local copy of work orders. */
@Database(
    entities = [PendingOperationEntity::class, WorkOrderEntity::class],
    version = 1,
    exportSchema = true
)
abstract class MaintenanceDatabase : RoomDatabase() {
    abstract fun pendingOperationDao(): PendingOperationDao
    abstract fun workOrderDao(): WorkOrderDao

    companion object {
        const val NAME = "predictivemaintain.db"
    }
}

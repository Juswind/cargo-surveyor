package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface InspectionDao {
    @Query("SELECT * FROM inspections ORDER BY timestamp DESC")
    fun getAllInspections(): Flow<List<InspectionEntity>>

    @Query("SELECT * FROM inspections WHERE id = :id LIMIT 1")
    fun getInspectionById(id: Long): Flow<InspectionEntity?>

    @Query("SELECT * FROM inspections WHERE status = :status ORDER BY timestamp DESC")
    fun getInspectionsByStatus(status: String): Flow<List<InspectionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInspection(entity: InspectionEntity): Long

    @Update
    suspend fun updateInspection(entity: InspectionEntity)

    @Delete
    suspend fun deleteInspection(entity: InspectionEntity)

    @Query("DELETE FROM inspections")
    suspend fun clearAll()

    @Query("SELECT COUNT(*) FROM inspections")
    suspend fun getCount(): Int
}

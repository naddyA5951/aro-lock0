package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.model.Trek
import kotlinx.coroutines.flow.Flow

@Dao
interface TrekDao {

    @Query("SELECT * FROM treks ORDER BY startTimeMillis DESC")
    fun getAllTreks(): Flow<List<Trek>>

    @Query("SELECT * FROM treks ORDER BY startTimeMillis DESC LIMIT 5")
    fun getRecentTreks(): Flow<List<Trek>>

    @Query("SELECT * FROM treks WHERE id = :id LIMIT 1")
    suspend fun getTrekById(id: Long): Trek?

    @Query("SELECT * FROM treks WHERE id = :id LIMIT 1")
    fun getTrekByIdFlow(id: Long): Flow<Trek?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrek(trek: Trek): Long

    @Update
    suspend fun updateTrek(trek: Trek)

    @Query("DELETE FROM treks WHERE id = :id")
    suspend fun deleteTrekById(id: Long)

    @Query("DELETE FROM treks")
    suspend fun deleteAllTreks()

    @Query("SELECT COUNT(*) FROM treks")
    fun getTreksCountFlow(): Flow<Int>

    @Query("SELECT SUM(distanceMeters) FROM treks")
    fun getTotalDistanceFlow(): Flow<Double?>

    @Query("SELECT SUM(elevationGainMeters) FROM treks")
    fun getTotalElevationGainFlow(): Flow<Double?>

    @Query("SELECT SUM(durationSeconds) FROM treks")
    fun getTotalDurationFlow(): Flow<Long?>

    @Query("SELECT SUM(estimatedCalories) FROM treks")
    fun getTotalCaloriesFlow(): Flow<Int?>

    @Query("SELECT SUM(steps) FROM treks")
    fun getTotalStepsFlow(): Flow<Int?>
}

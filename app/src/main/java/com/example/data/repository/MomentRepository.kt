package com.example.data.repository

import com.example.data.local.MomentDao
import com.example.data.model.Moment
import kotlinx.coroutines.flow.Flow

class MomentRepository(private val momentDao: MomentDao) {

    val allMoments: Flow<List<Moment>> = momentDao.getAllMoments()
    val allDates: Flow<List<String>> = momentDao.getAllDates()

    fun getMomentsForDate(dateString: String): Flow<List<Moment>> {
        return momentDao.getMomentsForDate(dateString)
    }

    fun getMomentsForDateDesc(dateString: String): Flow<List<Moment>> {
        return momentDao.getMomentsForDateDesc(dateString)
    }

    suspend fun insertMoment(moment: Moment): Long {
        return momentDao.insertMoment(moment)
    }

    suspend fun deleteMoment(moment: Moment) {
        momentDao.deleteMoment(moment)
    }

    suspend fun deleteById(id: Long) {
        momentDao.deleteById(id)
    }

    suspend fun clearAll() {
        momentDao.clearAll()
    }
}

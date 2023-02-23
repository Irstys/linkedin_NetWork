        package ru.netology.linkedin_network.repository.job

import androidx.lifecycle.MutableLiveData
import ru.netology.linkedin_network.api.ApiService
import ru.netology.linkedin_network.dao.JobDao
import ru.netology.linkedin_network.dto.Job
import ru.netology.linkedin_network.entity.JobEntity
import ru.netology.linkedin_network.entity.toEntity
import ru.netology.linkedin_network.error.ApiError
import ru.netology.linkedin_network.error.NetworkError
import java.io.IOException
import javax.inject.Inject

val emptyJobList = emptyList<Job>()

class JobRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val jobDao: JobDao
): JobRepository {

    override val data: MutableLiveData<List<Job>> = MutableLiveData(emptyJobList)

    override suspend fun getUserJobs(id: Int) {
        try {
            val response = apiService.getUserJobs(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            data.postValue(response.body())
            jobDao.insert(body.toEntity())
        } catch (e: IOException) {
            throw NetworkError
        }
    }

    override suspend fun saveJob(job: Job) {
        try {
            val response = apiService.saveJob(job)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            jobDao.insert(JobEntity.fromDto(body))
        } catch (e: IOException) {
            throw NetworkError
        }
    }

    override suspend fun removeJobById(id: Int) {
        try {
            val response = apiService.removeJobById(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            jobDao.removeJobById(id)
        } catch (e: IOException) {
            throw NetworkError
        }
    }

    override suspend fun getMyJobs() {
        try {
            val response = apiService.getMyJobs()
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            data.postValue(body)
            jobDao.insert(body.toEntity())
        } catch (e: IOException) {
            throw NetworkError
        }
    }
}
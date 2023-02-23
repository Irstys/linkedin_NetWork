package ru.netology.linkedin_network.repository.job

import androidx.lifecycle.MutableLiveData
import ru.netology.linkedin_network.dto.Job

interface JobRepository {
    val data: MutableLiveData<List<Job>>
    suspend fun getUserJobs(id: Int)
    suspend fun saveJob(job: Job)
    suspend fun removeJobById(id: Int)
    suspend fun getMyJobs()
}
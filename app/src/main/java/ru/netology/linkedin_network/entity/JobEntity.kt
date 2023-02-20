package ru.netology.linkedin_network.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.netology.linkedin_network.dto.Job

@Entity(tableName = "jobs")
data class JobEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val name: String,
    val position: String,
    val start: String,
    val finish: String?,
    val link: String? = null,
    val ownedByMe: Boolean=false,
) {
        fun toDto(): Job {
            val job = Job(
                id = id,
                name = name,
                position = position,
                start = start,
                finish = finish,
                link = link,
                ownedByMe = ownedByMe
            )
            return job
        }

        companion object {
            fun fromDto(dto: Job) =
                JobEntity(
                    dto.id,
                    dto.name,
                    dto.position,
                    dto.start,
                    dto.finish,
                    dto.link,
                    dto.ownedByMe
                )

        }
    }
    fun List<JobEntity>.toDto(): List<Job> = map(JobEntity::toDto)
    fun List<Job>.toEntity(): List<JobEntity> = map(JobEntity.Companion::fromDto)
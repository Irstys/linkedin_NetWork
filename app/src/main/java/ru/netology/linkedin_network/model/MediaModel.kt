package ru.netology.linkedin_network.model

import android.net.Uri
import ru.netology.linkedin_network.enumeration.AttachmentType
import java.io.File

data class MediaModel(
    val uri: Uri? = null,
    val file: File? = null,
    val type: AttachmentType? = null
)
package com.example.data.dto.image

import io.ktor.http.content.*

interface ImageStorageService {
    //    fun saveImage(productId: Long, part: PartData.FileItem): String // возвращает путь
    fun saveImage(prefix: String, id: Long, part: PartData.FileItem): String

    fun deleteImage(path: String): Boolean
}

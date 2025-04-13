package com.example.data.dto.image

import io.ktor.http.content.*

interface ImageStorageService {
    fun saveImage(productId: Long, part: PartData.FileItem): String // возвращает путь
    fun deleteImage(path: String): Boolean
}

package com.example

import com.example.data.dto.image.ImageStorageService
import io.ktor.http.content.*
import java.io.File

class LocalImageStorageService(
    private val uploadDir: String = "uploads/images"
) : ImageStorageService {

    init {
        File(uploadDir).mkdirs()
    }

    override fun saveImage(productId: Long, part: PartData.FileItem): String {
        val originalFileName = part.originalFileName ?: "image.jpg"
        val extension = File(originalFileName).extension.ifBlank { "jpg" }
        val filename = "product_${productId}_${System.currentTimeMillis()}.$extension"
        val file = File("$uploadDir/$filename")

        part.streamProvider().use { input ->
            file.outputStream().buffered().use { output ->
                input.copyTo(output)
            }
        }

        return file.path // путь сохраняется в БД
    }

    override fun deleteImage(path: String): Boolean {
        val file = File(path)
        return file.exists() && file.delete()
    }
}

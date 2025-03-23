package com.example.utils

import kotlinx.serialization.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*
import java.time.Instant

object InstantSerializer : KSerializer<Instant> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("Instant", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Instant) {
        encoder.encodeString(value.toString()) // Сериализация в строку
    }

    override fun deserialize(decoder: Decoder): Instant {
        return Instant.parse(decoder.decodeString()) // Парсим обратно
    }
}

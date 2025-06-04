package com.example.data.error.addres

class AddressValidationException(
    val code: String,
    override val message: String
) : Exception(message)

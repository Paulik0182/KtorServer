package com.example.data.dto.counterparty

import com.example.data.dto.product.CurrencyResponse
import kotlinx.serialization.Serializable

@Serializable
data class BankAccountResponse(
    val id: Long? = null,
    val accountNumber: String?,
    val bankName: String,
    val swiftCode: String?,
    val code: String,
    val symbol: String?,
    val currencyName: String,
    val currency: CurrencyResponse,
    val currencyId: Long,
)
package com.example.data.error.addres

import com.example.data.dto.counterparty.CounterpartyAddressRequest

object AddressValidation {
    // Regex шаблоны
    private val FULL_NAME_REGEX = Regex("^[A-Za-z0-9\\-./ ]{1,100}$")
    private val POSTAL_CODE_REGEX = Regex("^[0-9\\-_/ ]{2,9}$")
    private val STREET_NAME_REGEX = Regex("^[A-Za-z0-9.,_\\-\\/ ]{4,60}$")
    private val HOUSE_NUMBER_REGEX = Regex("^[A-Za-z0-9\\-/ ]{1,5}$")
    private val LOCATION_NUMBER_REGEX = Regex("^[A-Za-z0-9\\-/ ]{1,5}$")
    private val ENTRANCE_NUMBER_REGEX = Regex("^[A-Za-z0-9\\-/ ]{1,5}$")
    private val FLOOR_REGEX = Regex("^[A-Za-z0-9\\-/ ]{1,5}$")
    private val NUMBER_INTERCOM_REGEX = Regex("^[A-Za-z0-9\\-/# ]{1,15}$")

    private fun hasOnlySingleSpaces(text: String): Boolean = !text.contains("  ")
    private fun noEmojisOrNewLines(text: String): Boolean =
        text.codePoints().noneMatch { Character.getType(it) == Character.SURROGATE.toInt() || Character.getType(it) == Character.OTHER_SYMBOL.toInt() } &&
                !text.contains("\n") && !text.contains("\r")

    fun validateFullName(value: String) {
        if (value.isBlank()) throw AddressValidationException("full_name_required", "Укажите имя получателя")
        if (value.length > 100) throw AddressValidationException("full_name_too_long", "Имя слишком длинное")
        if (!value.matches(FULL_NAME_REGEX)) throw AddressValidationException("full_name_invalid", "Недопустимые символы в имени")
        if (!hasOnlySingleSpaces(value) || !noEmojisOrNewLines(value)) throw AddressValidationException("full_name_invalid", "Недопустимые символы")
    }

    fun validatePostalCode(value: String) {
        if (value.isBlank()) throw AddressValidationException("postal_code_required", "Укажите почтовый код")
        if (value.length !in 2..9) throw AddressValidationException("postal_code_length", "Код должен состоять от 2 до 9 символов")
        if (!value.matches(POSTAL_CODE_REGEX)) throw AddressValidationException("postal_code_invalid", "Недопустимые символы в почтовом коде")
        if (!hasOnlySingleSpaces(value) || !noEmojisOrNewLines(value)) throw AddressValidationException("postal_code_invalid", "Недопустимые символы")
    }

    fun validateStreetName(value: String) {
        if (value.isBlank()) throw AddressValidationException("street_required", "Укажите улицу")
        if (value.length !in 4..60) throw AddressValidationException("street_length", "Улица должна быть от 4 до 60 символов")
        if (!value.matches(STREET_NAME_REGEX)) throw AddressValidationException("street_invalid", "Недопустимые символы в улице")
        if (!hasOnlySingleSpaces(value) || !noEmojisOrNewLines(value)) throw AddressValidationException("street_invalid", "Недопустимые символы")
    }

    fun validateOptionalField(value: String?, field: String, max: Int, regex: Regex) {
        if (value == null) return
        if (value.length > max) throw AddressValidationException("${field}_too_long", "Слишком длинное значение")
        if (!value.matches(regex)) throw AddressValidationException("${field}_invalid", "Недопустимые символы")
        if (!hasOnlySingleSpaces(value) || !noEmojisOrNewLines(value)) throw AddressValidationException("${field}_invalid", "Недопустимые символы")
    }

    fun validateAddressFields(address: CounterpartyAddressRequest) {
        address.fullName?.let { validateFullName(it) }
        address.postalCode?.let { validatePostalCode(it) }
        validateStreetName(address.streetName)
        validateOptionalField(address.houseNumber, "house_number", 5, HOUSE_NUMBER_REGEX)
        validateOptionalField(address.locationNumber, "location_number", 5, LOCATION_NUMBER_REGEX)
        validateOptionalField(address.entranceNumber, "entrance_number", 5, ENTRANCE_NUMBER_REGEX)
        validateOptionalField(address.floor, "floor", 5, FLOOR_REGEX)
        validateOptionalField(address.numberIntercom, "number_intercom", 15, NUMBER_INTERCOM_REGEX)
    }

    fun validatePatch(patch: Map<String, Any?>) {
        patch["fullName"]?.let { validateFullName(it as String) }
        patch["postalCode"]?.let { validatePostalCode(it as String) }
        patch["streetName"]?.let { validateStreetName(it as String) }
        patch["houseNumber"]?.let { validateOptionalField(it as String?, "house_number", 5, HOUSE_NUMBER_REGEX) }
        patch["locationNumber"]?.let { validateOptionalField(it as String?, "location_number", 5, LOCATION_NUMBER_REGEX) }
        patch["entranceNumber"]?.let { validateOptionalField(it as String?, "entrance_number", 5, ENTRANCE_NUMBER_REGEX) }
        patch["floor"]?.let { validateOptionalField(it as String?, "floor", 5, FLOOR_REGEX) }
        patch["numberIntercom"]?.let { validateOptionalField(it as String?, "number_intercom", 15, NUMBER_INTERCOM_REGEX) }
    }
}

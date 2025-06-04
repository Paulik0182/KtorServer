package validation


import com.example.data.error.addres.AddressValidation
import com.example.data.error.addres.AddressValidationException
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertEquals

class AddressValidationTest {

    @Test
    fun `valid fullName should pass`() {
        AddressValidation.validateFullName("John Doe")
    }

    @Test
    fun `invalid fullName with emoji should throw`() {
        val exception = assertFailsWith<AddressValidationException> {
            AddressValidation.validateFullName("John 💩")
        }
        assertEquals("full_name_invalid", exception.code)
    }

    @Test
    fun `postal code too short should throw`() {
        val exception = assertFailsWith<AddressValidationException> {
            AddressValidation.validatePostalCode("1")
        }
        assertEquals("postal_code_length", exception.code)
    }

    @Test
    fun `valid patch should pass`() {
        val patch = mapOf(
            "fullName" to "Jane Doe",
            "postalCode" to "00-123",
            "streetName" to "Main Street",
            "houseNumber" to "12B"
        )
        AddressValidation.validatePatch(patch)
    }

    @Test
    fun `patch with invalid field should throw`() {
        val patch = mapOf(
            "streetName" to "💥💥💥"
        )
        val exception = assertFailsWith<AddressValidationException> {
            AddressValidation.validatePatch(patch)
        }
        assertEquals("street_invalid", exception.code)
    }
}

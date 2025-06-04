package dao

import com.example.Cities
import com.example.CounterpartyAddresses
import com.example.Countries
import com.example.data.AddressDao
import com.example.data.error.addres.AddressValidationException
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.SchemaUtils.create
import org.jetbrains.exposed.sql.SchemaUtils.drop
import org.junit.jupiter.api.*
import kotlin.test.assertFailsWith
import kotlin.test.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AddressDaoTest {

    @BeforeAll
    fun setup() {
        Database.connect("jdbc:h2:mem:test_db;DB_CLOSE_DELAY=-1;", driver = "org.h2.Driver")
        transaction {
            create(CounterpartyAddresses, Countries, Cities)
            // при необходимости — заполнить заглушками страну и город
            Countries.insert {
                it[id] = 1L
                it[name] = "Poland"
                it[isoCode] = "PL"
                it[phoneCode] = "+48"
            }
            Cities.insert {
                it[id] = 1L
                it[name] = "Warsaw"
                it[countryId] = 1L
            }
            CounterpartyAddresses.insert {
                it[id] = 100L
                it[counterpartyId] = 19L
                it[countryId] = 1L
                it[cityId] = 1L
                it[postalCode] = "00-001"
                it[streetName] = "Testowa"
                it[houseNumber] = "1"
                it[fullName] = "John Doe"
                it[isMain] = false
            }
        }
    }

    @AfterAll
    fun teardown() {
        transaction {
            drop(CounterpartyAddresses, Countries, Cities)
        }
    }

    @Test
    fun `valid patch should update address`() {
        transaction {
            val patch = mapOf<String, Any?>(
                "fullName" to "Jane Updated",
                "postalCode" to "11-111"
            )

            AddressDao.patchAddress(19L, 100L, patch)

            val updated = CounterpartyAddresses.selectAll().where {
                CounterpartyAddresses.id eq 100L
            }.first()

            Assertions.assertEquals("Jane Updated", updated[CounterpartyAddresses.fullName])
            Assertions.assertEquals("11-111", updated[CounterpartyAddresses.postalCode])
        }
    }

    @Test
    fun `invalid patch with emoji should throw`() {
        transaction {
            val patch = mapOf<String, Any?>(
                "fullName" to "💩💩💩"
            )

            val ex = assertFailsWith<AddressValidationException> {
                AddressDao.patchAddress(19L, 100L, patch)
            }

            assertEquals("full_name_invalid", ex.code)
        }
    }
}
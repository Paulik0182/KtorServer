package com.example.data

import com.example.*
import com.example.data.AddressDao.formatAddressString
import com.example.data.AddressDao.getCounterpartyAddresses
import com.example.data.AddressDao.getCountryIsoCode
import com.example.data.AddressDao.getCountryName
import com.example.data.AddressDao.getCountryPhoneCode
import com.example.data.AddressDao.validateCityBelongsToCountry
import com.example.data.ProductDao.getCounterpartyName
import com.example.data.dto.counterparty.*
import com.example.data.dto.order.OrderItemResponse
import com.example.data.dto.order.OrderResponse
import com.example.data.dto.product.CurrencyResponse
import com.example.data.dto.product.LinkResponse
import com.example.data.dto.product.ProductCounterpartyResponse
import com.example.data.dto.product.UrlsResponse
import com.example.data.error.AuthException
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory

// ✅ DAO и маршруты для Counterparty (контрагента)

object CounterpartyDao {
    fun getAll(languageCode: String = "ru"): List<CounterpartyResponse> = transaction {
        try {
            val counterpartyRows = Counterparties.selectAll().toList()
            println("✅ Получено продуктов: ${counterpartyRows.size}")

            counterpartyRows.map { row ->

                val counterpartyId = row[Counterparties.id]

                val reps = getCounterpartyRepresentatives(counterpartyId)
                val contacts = getCounterpartyContacts(counterpartyId)
                val bankAccounts = getCounterpartyBankAccounts(counterpartyId)
                val addresses = getCounterpartyAddresses(counterpartyId, languageCode)
                val orders = getOrders(counterpartyId)
                val productCounterparties = getProductCounterpartiesForCounterparty(counterpartyId)
                val productLinks = getCounterpartyLinks(counterpartyId)
                val productSuppliers = ProductSupplierDao.getProductSuppliersForCounterparty(counterpartyId)

                CounterpartyResponse(
                    id = counterpartyId,
                    shortName = row[Counterparties.shortName],
                    companyName = row[Counterparties.companyName],
                    type = row[Counterparties.type],
                    isSupplierOld = row[Counterparties.isSupplierOld],
                    productCountOld = row[Counterparties.productCountOld],
                    isSupplier = row[Counterparties.isSupplier],
                    isWarehouse = row[Counterparties.isWarehouse],
                    isCustomer = row[Counterparties.isCustomer],
                    isLegalEntity = row[Counterparties.isLegalEntity],
                    imagePath = row[Counterparties.imagePath],
                    nip = row[Counterparties.nip],
                    krs = row[Counterparties.krs],
                    firstName = row[Counterparties.firstName],
                    lastName = row[Counterparties.lastName],

                    counterpartyRepresentatives = reps,
                    representativesIds = reps.mapNotNull { it.id },
                    representativesName = reps.joinToString { it.fullName },

                    representativesContact = reps
                        .flatMap { it.contacts ?: emptyList() }
                        .map { formatContactString(it) },

                    counterpartyContacts = contacts,

                    contactIds = contacts.mapNotNull { it.id },
                    // это если мы захотим получить информацию одной строкой
//                    counterpartyContact = listOf(
//                        contacts.joinToString(separator = ", ") { formatContactString(it) }
//                    ),
                    counterpartyContact = contacts.map { formatContactString(it) },


                    counterpartyBankAccounts = bankAccounts,
                    bankAccountIds = bankAccounts.mapNotNull { it.id },
                    bankAccountInformation = bankAccounts.map { formatBankString(it) },

                    counterpartyAddresses = addresses,
                    addressesIds = addresses.mapNotNull { it.id },
                    addressesInformation = addresses.map { formatAddressString(it) },

                    orders = orders,
                    orderIds = orders.mapNotNull { it.id },

                    productCounterparties = productCounterparties,
                    counterpartyLinks = productLinks,
                    counterpartyLinkIds = productLinks.mapNotNull { it.id },
                    productSuppliers = productSuppliers,
                    productSupplierIds = productSuppliers.mapNotNull { it.id },
                )
            }
        } catch (e: Exception) {
            println("❌ Ошибка в getAll() - Counterparty: ${e.localizedMessage}")
            throw e
        }
    }

    fun getById(counterpartyId: Long, languageCode: String = "ru"): CounterpartyResponse? = transaction {
        try {
            val row = Counterparties
                .selectAll()
                .where { Counterparties.id eq counterpartyId }
                .firstOrNull() ?: return@transaction null

            val reps = getCounterpartyRepresentatives(counterpartyId)
            val contacts = getCounterpartyContacts(counterpartyId)
            val bankAccounts = getCounterpartyBankAccounts(counterpartyId)
            val addresses = getCounterpartyAddresses(counterpartyId, languageCode)
            val orders = getOrders(counterpartyId)
            val productCounterparties = getProductCounterpartiesForCounterparty(counterpartyId)
            val productLinks = getCounterpartyLinks(counterpartyId)
            val productSuppliers = ProductSupplierDao.getProductSuppliersForCounterparty(counterpartyId)

            CounterpartyResponse(
                id = row[Counterparties.id],
                shortName = row[Counterparties.shortName],
                companyName = row[Counterparties.companyName],
                type = row[Counterparties.type],
                isSupplierOld = row[Counterparties.isSupplierOld],
                productCountOld = row[Counterparties.productCountOld],
                isSupplier = row[Counterparties.isSupplier],
                isWarehouse = row[Counterparties.isWarehouse],
                isCustomer = row[Counterparties.isCustomer],
                isLegalEntity = row[Counterparties.isLegalEntity],
                imagePath = row[Counterparties.imagePath],
                nip = row[Counterparties.nip],
                krs = row[Counterparties.krs],
                firstName = row[Counterparties.firstName],
                lastName = row[Counterparties.lastName],

                counterpartyRepresentatives = reps,
                representativesIds = reps.mapNotNull { it.id },
                representativesName = reps.joinToString { it.fullName },
                representativesContact = reps
                    .flatMap { it.contacts ?: emptyList() }
                    .map { formatContactString(it) },

                counterpartyContacts = contacts,
                contactIds = contacts.mapNotNull { it.id },
                // это если мы захотим получить информацию одной строкой
//                    counterpartyContact = listOf(
//                        contacts.joinToString(separator = ", ") { formatContactString(it) }
//                    ),
                counterpartyContact = contacts.map { formatContactString(it) },

                counterpartyBankAccounts = bankAccounts,
                bankAccountIds = bankAccounts.mapNotNull { it.id },
                bankAccountInformation = bankAccounts.map { formatBankString(it) },

                counterpartyAddresses = addresses,
                addressesIds = addresses.mapNotNull { it.id },
                addressesInformation = addresses.map { formatAddressString(it) },

                orders = orders,
                orderIds = orders.mapNotNull { it.id },

                productCounterparties = productCounterparties,
                counterpartyLinks = productLinks,
                counterpartyLinkIds = productLinks.mapNotNull { it.id },
                productSuppliers = productSuppliers,
                productSupplierIds = productSuppliers.mapNotNull { it.id },
            )
//        }
        } catch (e: Exception) {
            println("Ошибка в getById($counterpartyId): ${e.localizedMessage}")
            throw e
        }
    }

    fun insert(counterparty: CounterpartyRequest): Long = transaction {
        // 🔁 Проверка на дубликат
        val exists = Counterparties
            .selectAll()
            .where { Counterparties.shortName eq counterparty.shortName }
            .firstOrNull()

        if (exists != null) {
            error("Контрагент с названием '${counterparty.shortName}' уже существует")
        }

        val id = Counterparties.insert {
            it[shortName] = counterparty.shortName
            it[companyName] = counterparty.companyName ?: counterparty.shortName
            it[type] = counterparty.type
            it[isSupplier] = counterparty.isSupplier
            it[isWarehouse] = counterparty.isWarehouse
            it[isCustomer] = counterparty.isCustomer
            it[isLegalEntity] = counterparty.isLegalEntity
            it[imagePath] = counterparty.imagePath
            it[nip] = counterparty.nip
            it[krs] = counterparty.krs
            it[firstName] = counterparty.firstName
            it[lastName] = counterparty.lastName
        } get Counterparties.id
        println("Created counterparty $id, isCustomer = ${Counterparties.selectAll().where { Counterparties.id eq id }.first()[Counterparties.isCustomer]}")

        insertCounterpartyDependencies(id, counterparty)
        return@transaction id
    }

    /**
     * Формируем уникальное ShortName при создании новой учетной записи.
     */
    fun insertDefaultCounterparty(email: String): Long = transaction {
        val rawBaseName = email.substringBefore("@")

        // Очистка baseName от запрещённых символов и лишних пробелов (Соответствует валидации на стороне Приложения)
        val baseNameCleaned = rawBaseName
            .replace("\n", "")
            .replace(Regex(" {2,}"), " ")
            .trim()
            .replace(Regex("[^a-zA-Z0-9@#/_\\-.\\s]"), "") // соответствует ADVANCED_INVALID_CHARACTERS
            .replace(Regex("[+!]"), "")                   // соответствует INVALID_CHARACTERS
            .take(15)                                     // ограничение длины имени без суффикса

        val maxAttempts = 4
        val random = kotlin.random.Random

        repeat(maxAttempts) { attempt ->
            val suffix = if (attempt == 0) "" else (1..4)
                .map { ('0'..'9').random(random) }
                .joinToString("")
            val candidateName = "$baseNameCleaned$suffix"

            // Проверка уникальности
            val isUnique = Counterparties
                .selectAll().where { Counterparties.shortName eq candidateName }
                .empty()

            if (isUnique) {
                LoggerFactory.getLogger("InsertDebug").info("Insert counterparty: isCustomer = true")

                return@transaction Counterparties.insert {
                    it[shortName] = candidateName
                    it[companyName] = null
                    it[type] = "customer"
                    it[isCustomer] = true
                    it[isLegalEntity] = false
                    it[imagePath] = null
                    it[nip] = null
                    it[krs] = null
                    it[firstName] = null
                    it[lastName] = null
                } get Counterparties.id
            }
        }

        throw AuthException("registration_server_error", "Ошибка сервера регистрации. Попробуйте позже.")
    }

    fun update(id: Long, counterparty: CounterpartyRequest) = transaction {
        Counterparties.update({ Counterparties.id eq id }) {
            it[shortName] = counterparty.shortName
            it[companyName] = counterparty.companyName ?: counterparty.shortName
            it[type] = counterparty.type
            it[isSupplier] = counterparty.isSupplier
            it[isWarehouse] = counterparty.isWarehouse
            it[isCustomer] = counterparty.isCustomer
            it[isLegalEntity] = counterparty.isLegalEntity
            it[imagePath] = counterparty.imagePath
            it[nip] = counterparty.nip
            it[krs] = counterparty.krs
            it[firstName] = counterparty.firstName
            it[lastName] = counterparty.lastName
        }

        deleteCounterpartyDependencies(id)
        insertCounterpartyDependencies(id, counterparty)
    }

    fun delete(id: Long) = transaction {
        deleteCounterpartyDependencies(id)
        Counterparties.deleteWhere { Counterparties.id eq id }
    }

// --- ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ---

    private fun insertCounterpartyDependencies(id: Long, data: CounterpartyRequest) = transaction {
        data.representatives?.forEach { rep ->
            val repId = CounterpartyRepresentatives
                .insert {
                    it[counterpartyId] = id
                    it[fullName] = rep.fullName ?: "Без имени"
                    it[position] = rep.position
                } get CounterpartyRepresentatives.id

            rep.contacts.forEach { contact ->
                CounterpartyContacts.insert {
                    it[counterpartyId] = id
                    it[contactType] = contact.contactType ?: ""
                    it[contactValue] = contact.contactValue ?: ""
                    it[countryCodeId] = contact.countryCodeId
                    it[representativeId] = repId
                }
            }
        }

        data.contacts?.forEach { contact ->
            CounterpartyContacts.insert {
                it[counterpartyId] = id
                it[contactType] = contact.contactType ?: ""
                it[contactValue] = contact.contactValue ?: ""
                it[countryCodeId] = contact.countryCodeId
                it[representativeId] = contact.representativeId
            }
        }

        data.bankAccounts?.forEach { acc ->
            CounterpartyBankAccounts.insert {
                it[counterpartyId] = id
                it[accountNumber] = acc.accountNumber ?: ""
                it[bankName] = acc.bankName
                it[swiftCode] = acc.swiftCode ?: ""
                it[currencyId] = acc.currencyId
            }
        }

        data.addresses?.forEach { addr ->
            // ✅ ВАЛИДАЦИЯ: перед вставкой адреса
            validateCityBelongsToCountry(addr.cityId, addr.countryId)

            CounterpartyAddresses.insert {
                it[counterpartyId] = id
                it[countryId] = addr.countryId
                it[cityId] = addr.cityId
                it[postalCode] = addr.postalCode
                it[streetName] = addr.streetName
                it[houseNumber] = addr.houseNumber
                it[locationNumber] = addr.locationNumber
                it[latitude] = addr.latitude?.toBigDecimal()
                it[longitude] = addr.longitude?.toBigDecimal()
                it[entranceNumber] = addr.entranceNumber
                it[floor] = addr.floor
                it[numberIntercom] = addr.numberIntercom
            }
        }

        data.productCounterparties.forEach { pc ->
            // ✅ ПРОВЕРКА: существует ли продукт с таким id
            val productExists = Products.selectAll().where { Products.id eq pc.productId }.count() > 0
            if (!productExists) {
                error("Продукт с id=${pc.productId} не найден — невозможно создать связанного контрагента")
            }

            ProductCounterparties.insert {
                it[counterpartyId] = id
                it[productId] = pc.productId
                it[stockQuantity] = pc.stockQuantity
                it[role] = pc.role
                it[minStockQuantity] = pc.minStockQuantity
                it[warehouseLocationCodes] = pc.warehouseLocationCodes
                it[measurementUnitId] = pc.measurementUnitId
            }
        }


        data.productLinks?.forEach { link ->
            // Пытаемся найти URL
            val existing = Urls.selectAll()
                .where { Urls.url eq link.url }
                .firstOrNull()

            val urlId = existing?.get(Urls.id) ?: Urls.insertReturning(listOf(Urls.id)) {
                it[Urls.url] = link.url
            }.single()[Urls.id]

            // Добавляем запись в Links
            Links.insert {
                it[Links.counterpartyId] = id
                it[Links.urlId] = urlId
            }
        }

        data.productSuppliers?.forEach { supplier ->
            ProductSuppliers.insert {
                it[counterpartyId] = id
                it[productId] = supplier.productId
            }
        }
    }

    private fun deleteCounterpartyDependencies(id: Long) = transaction {
        ProductSuppliers.deleteWhere { ProductSuppliers.counterpartyId eq id }
        println("🧹 Удалены productSuppliers для контрагента $id")

        ProductCounterparties.deleteWhere { ProductCounterparties.counterpartyId eq id }
        println("🧹 Удалены productCounterparties для контрагента $id")

        Links.deleteWhere { Links.counterpartyId eq id }
        println("🧹 Удалены links для контрагента $id")

        Orders.deleteWhere { Orders.counterpartyId eq id }
        println("🧹 Удалены заказы для контрагента $id")

        CounterpartyAddresses.deleteWhere { CounterpartyAddresses.counterpartyId eq id }
        println("🧹 Удалены адреса для контрагента $id")

        CounterpartyBankAccounts.deleteWhere { CounterpartyBankAccounts.counterpartyId eq id }
        println("🧹 Удалены банковские счета для контрагента $id")

        CounterpartyContacts.deleteWhere { CounterpartyContacts.counterpartyId eq id }
        println("🧹 Удалены контакты для контрагента $id")

        CounterpartyRepresentatives.deleteWhere { CounterpartyRepresentatives.counterpartyId eq id }
        println("🧹 Удалены представители для контрагента $id")
    }

    private fun formatContactString(it: CounterpartyContactResponse): String {
        return when (it.contactType) {
            "phone" -> "Tel. ${it.countryPhoneCode} ${it.contactValue}"
            "fax" -> "Fax: ${it.countryPhoneCode} ${it.contactValue}"
            "email" -> "Email: ${it.contactValue}"
            else -> "${it.contactType}: ${it.contactValue}"
        }
    }

    private fun formatBankString(it: BankAccountResponse): String =
        "${it.bankName}, ${it.accountNumber} (${it.currency.code})"



    fun getCounterpartyRepresentatives(counterpartyId: Long): List<RepresentativeResponse> = transaction {
        val reps = CounterpartyRepresentatives.selectAll()
            .where { CounterpartyRepresentatives.counterpartyId eq counterpartyId }.map {
                val representativeId = it[CounterpartyRepresentatives.id]
                RepresentativeResponse(
                    id = representativeId,
                    counterpartyId = it[CounterpartyRepresentatives.counterpartyId],
                    fullName = it[CounterpartyRepresentatives.fullName],
                    position = it[CounterpartyRepresentatives.position],
                    contacts = getContactsByRepresentativeId(representativeId),
                    contactsIds = getContactsByRepresentativeId(representativeId).mapNotNull { c -> c.id }
                )
            }
        reps
    }

    fun getContactsByRepresentativeId(repId: Long): List<CounterpartyContactResponse> = transaction {
        CounterpartyContacts
            .selectAll()
            .where { CounterpartyContacts.representativeId eq repId }
            .map {
                val countryId = it[CounterpartyContacts.countryCodeId]
                CounterpartyContactResponse(
                    id = it[CounterpartyContacts.id],
                    counterpartyId = it[CounterpartyContacts.counterpartyId],
                    counterpartyName = it[CounterpartyContacts.counterpartyId]?.let { getCounterpartyName(repId) },
                    contactType = it[CounterpartyContacts.contactType] ?: "",
                    contactValue = it[CounterpartyContacts.contactValue] ?: "",
                    countryCodeId = countryId,
                    representativeId = it[CounterpartyContacts.representativeId],
                    countryPhoneCode = countryId?.let { getCountryPhoneCode(it) },
                    countryIsoCode = countryId?.let { getCountryIsoCode(it) },
                    countryName = countryId?.let { getCountryName(it) },
                    representativeName = it[CounterpartyContacts.representativeId]?.let { getRepresentativeName(it) },

                    )
            }
    }

    fun getCounterpartyContacts(counterpartyId: Long): List<CounterpartyContactResponse> = transaction {
        CounterpartyContacts
            .selectAll()
            .where { CounterpartyContacts.counterpartyId eq counterpartyId }
            .map {
                val repId = it[CounterpartyContacts.representativeId]
                val countryId = it[CounterpartyContacts.countryCodeId]

                CounterpartyContactResponse(
                    id = it[CounterpartyContacts.id],
                    counterpartyId = counterpartyId,
                    counterpartyName = it[CounterpartyContacts.counterpartyId]?.let { getCounterpartyName(counterpartyId) },
                    contactType = it[CounterpartyContacts.contactType] ?: "",
                    contactValue = it[CounterpartyContacts.contactValue] ?: "",
                    countryCodeId = countryId,
                    countryPhoneCode = countryId?.let { getCountryPhoneCode(it) },
                    countryIsoCode = countryId?.let { getCountryIsoCode(it) },
                    countryName = countryId?.let { getCountryName(it) },
                    representativeId = repId,
                    representativeName = repId?.let { getRepresentativeName(it) }
                )
            }
    }

    fun getCounterpartyBankAccounts(counterpartyId: Long): List<BankAccountResponse> = transaction {
        (CounterpartyBankAccounts innerJoin Currencies)
            .selectAll().where { CounterpartyBankAccounts.counterpartyId eq counterpartyId }
            .map {
                BankAccountResponse(
                    id = it[CounterpartyBankAccounts.id],
                    accountNumber = it[CounterpartyBankAccounts.accountNumber],
                    bankName = it[CounterpartyBankAccounts.bankName],
                    swiftCode = it[CounterpartyBankAccounts.swiftCode],
                    code = it[Currencies.code],
                    symbol = it[Currencies.symbol],
                    currencyName = it[Currencies.name],
                    currencyId = it[Currencies.id],
                    currency = CurrencyResponse(
                        id = it[Currencies.id],
                        code = it[Currencies.code],
                        symbol = it[Currencies.symbol],
                        name = it[Currencies.name]
                    )
                )
            }
    }

    fun getOrders(counterpartyId: Long): List<OrderResponse> = transaction {
        Orders.selectAll().where { Orders.counterpartyId eq counterpartyId }.map {
            OrderResponse(
                id = it[Orders.id],
                counterpartyId = it[Orders.counterpartyId],
                counterpartyName = getCounterpartyName(it[Orders.counterpartyId]),
                orderStatus = it[Orders.orderStatus],
                createdAt = it[Orders.createdAt],
                updatedAt = it[Orders.updatedAt],
                acceptedAt = it[Orders.acceptedAt],
                completedAt = it[Orders.completedAt],
                items = getOrderItems(it[Orders.id])
            )
        }
    }

    // Получение ссылок на товар
    fun getCounterpartyLinks(counterpartyId: Long): List<LinkResponse> = transaction {
        (Links innerJoin Urls).selectAll().where {
            Links.counterpartyId eq counterpartyId
        }.map {
            val urlId = it[Links.urlId]
            val urlText = it[Urls.url]

            LinkResponse(
                id = it[Links.id],
                productId = it[Links.productId],
                counterpartyId = it[Links.counterpartyId],
                urlId = urlId,
                urlName = urlText,
                url = listOf(UrlsResponse(id = urlId, url = urlText))
            )
        }
    }

    fun getProductCounterpartiesForCounterparty(counterpartyId: Long): List<ProductCounterpartyResponse> = transaction {
        ProductCounterparties
            .selectAll()
            .where { ProductCounterparties.counterpartyId eq counterpartyId }
            .map {
                val unit = ProductDao.getMeasurementUnit(it[ProductCounterparties.measurementUnitId], "ru")
                val (unitName, unitAbbr) = ProductDao.getMeasurementUnitLocalized(
                    it[ProductCounterparties.measurementUnitId],
                    "ru"
                )

                ProductCounterpartyResponse(
                    productId = it[ProductCounterparties.productId],
                    productName = ProductDao.getProductName(it[ProductCounterparties.productId]),
                    counterpartyId = it[ProductCounterparties.counterpartyId],
                    counterpartyName = ProductDao.getCounterpartyName(it[ProductCounterparties.counterpartyId]),
                    stockQuantity = it[ProductCounterparties.stockQuantity],
                    role = it[ProductCounterparties.role],
                    minStockQuantity = it[ProductCounterparties.minStockQuantity],
                    warehouseLocationCodes = it[ProductCounterparties.warehouseLocationCodes],
                    measurementUnitId = it[ProductCounterparties.measurementUnitId],
                    measurementUnitList = unit,
                    measurementUnit = unitName,
                    measurementUnitAbbreviation = unitAbbr,
                )
            }
    }





    fun getRepresentativeName(id: Long): String = transaction {
        CounterpartyRepresentatives
            .selectAll()
            .where { CounterpartyRepresentatives.id eq id }
            .map { it[CounterpartyRepresentatives.fullName] }
            .firstOrNull() ?: "Без имени"
    }

    fun getCounterpartyFullName(counterpartyId: Long): String = transaction {
        val row = Counterparties
            .selectAll()
            .where { Counterparties.id eq counterpartyId }
            .firstOrNull()

        if (row != null) {
            val firstName = row[Counterparties.firstName] ?: ""
            val lastName = row[Counterparties.lastName] ?: ""
            listOf(firstName, lastName).filter { it.isNotBlank() }.joinToString(" ")
        } else {
            "Неизвестный пользователь"
        }
    }

    fun getOrderItems(counterpartyId: Long): List<OrderItemResponse> = transaction {
        (Orders innerJoin OrderItems)
            .selectAll()
            .where { Orders.counterpartyId eq counterpartyId }
            .map {
                val (unitName, unitAbbr) = ProductDao.getMeasurementUnitLocalized(
                    it[OrderItems.measurementUnitId],
                    "ru"
                )
                OrderItemResponse(
                    id = it[OrderItems.id],
                    orderId = it[OrderItems.orderId],
                    productId = it[OrderItems.productId],
                    productName = ProductDao.getProductName(it[OrderItems.productId]),
                    quantity = it[OrderItems.quantity],
                    measurementUnitId = it[OrderItems.measurementUnitId],
                    measurementUnitList = ProductDao.getMeasurementUnit(it[OrderItems.measurementUnitId], "ru"),
                    measurementUnit = unitName,
                    measurementUnitAbbreviation = unitAbbr
                )
            }
    }


    fun updateContacts(counterpartyId: Long, contacts: List<CounterpartyContactRequest>) = transaction {
        // Удаляем старые
        CounterpartyContacts.deleteWhere { CounterpartyContacts.counterpartyId eq counterpartyId }

        val allowedTypes = setOf("phone", "email", "fax", "other")
        contacts.forEach { contact ->
            if (contacts.any { it.contactType !in allowedTypes }) {
                error("Обнаружены контакты с недопустимыми типами")
            }
        }

        // Добавляем новые
        contacts.forEach { contact ->
            CounterpartyContacts.insert {
                it[this.counterpartyId] = counterpartyId
                it[contactType] = contact.contactType ?: ""
                it[contactValue] = contact.contactValue ?: ""
                it[countryCodeId] = contact.countryCodeId
                it[representativeId] = contact.representativeId
            }
        }
    }

    fun updateBasicFields(id: Long, patchRequest: CounterpartyPatchRequest) = transaction {
        Counterparties.update({ Counterparties.id eq id }) {
            it[shortName] = patchRequest.shortName
            it[companyName] = patchRequest.companyName ?: patchRequest.shortName
            it[type] = patchRequest.type
            it[isSupplier] = patchRequest.isSupplier
            it[isWarehouse] = patchRequest.isWarehouse
            it[isCustomer] = patchRequest.isCustomer
            it[isLegalEntity] = patchRequest.isLegalEntity
            it[nip] = patchRequest.nip
            it[krs] = patchRequest.krs
            it[firstName] = patchRequest.firstName
            it[lastName] = patchRequest.lastName
        }
    }

    fun updateRepresentatives(counterpartyId: Long, patchRequest: RepresentativeRequest) = transaction {
        // Удаляем старых представителей и их контакты
        val repIds = CounterpartyRepresentatives
            .selectAll().where { CounterpartyRepresentatives.counterpartyId eq counterpartyId }
            .map { it[CounterpartyRepresentatives.id] }

        // Удаляем их контакты и самих представителей
        CounterpartyContacts.deleteWhere { CounterpartyContacts.representativeId inList repIds }
        CounterpartyRepresentatives.deleteWhere { CounterpartyRepresentatives.counterpartyId eq counterpartyId }

        // Вставляем нового представителя
        val repId = CounterpartyRepresentatives.insert {
            it[this.counterpartyId] = counterpartyId
            it[fullName] = patchRequest.fullName ?: "Без имени"
            it[position] = patchRequest.position
        } get CounterpartyRepresentatives.id

        patchRequest.contacts.forEach { contact ->
            CounterpartyContacts.insert {
                it[this.counterpartyId] = counterpartyId
                it[contactType] = contact.contactType ?: ""
                it[contactValue] = contact.contactValue ?: ""
                it[countryCodeId] = contact.countryCodeId
                it[representativeId] = repId
            }
        }
    }

    fun updateBankAccounts(counterpartyId: Long, patchRequest: BankAccountRequest) = transaction {
        // Удаляем старые банковские счета
        CounterpartyBankAccounts.deleteWhere { CounterpartyBankAccounts.counterpartyId eq counterpartyId }

        // Вставляем новый
        CounterpartyBankAccounts.insert {
            it[this.counterpartyId] = counterpartyId
            it[accountNumber] = patchRequest.accountNumber ?: ""
            it[bankName] = patchRequest.bankName
            it[swiftCode] = patchRequest.swiftCode ?: ""
            it[currencyId] = patchRequest.currencyId
        }
    }

    fun updateImagePath(counterpartyId: Long, imagePath: String?) = transaction {
        Counterparties.update({ Counterparties.id eq counterpartyId }) {
            it[Counterparties.imagePath] = imagePath
        }
    }
}

package com.example.routing

enum class UserRole {
    SYSTEM_ADMIN,        // доступ ко всему
    WAREHOUSE_ADMIN,     // редактирует только склад
    SHOP_ADMIN,          // редактирует только свои товары, лимит на товары
    SUPPLIER,            // поставляет товары
    CUSTOMER             // обычный покупатель
}
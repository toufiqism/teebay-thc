package com.teebay.appname.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Product(
    val id: String,
    val title: String,
    val categories: List<Category>,
    val description: String,
    val imagePath: String? = null,
    val price: Double,
    val rentDuration: RentDuration,
    val userId: String, // Owner of the product
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Serializable
enum class RentDuration(val displayName: String) {
    PER_DAY("Per Day"),
    PER_WEEK("Per Week"),
    PER_MONTH("Per Month");

    companion object {
        fun getAll(): List<RentDuration> = values().toList()
        
        fun fromDisplayName(displayName: String): RentDuration? {
            return values().find { it.displayName == displayName }
        }
    }
}

data class CreateProductRequest(
    val title: String,
    val categories: List<Category>,
    val description: String,
    val imagePath: String? = null,
    val price: Double,
    val rentDuration: RentDuration
)

data class UpdateProductRequest(
    val id: String,
    val title: String,
    val categories: List<Category>,
    val description: String,
    val imagePath: String? = null,
    val price: Double,
    val rentDuration: RentDuration
) 
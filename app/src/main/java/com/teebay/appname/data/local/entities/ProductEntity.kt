package com.teebay.appname.data.local.entities

import com.teebay.appname.domain.model.Category
import com.teebay.appname.domain.model.RentDuration
import kotlinx.serialization.Serializable

@Serializable
data class ProductEntity(
    val id: String,
    val title: String,
    val categories: List<Category>,
    val description: String,
    val imagePath: String? = null,
    val price: Double,
    val rentDuration: RentDuration,
    val userId: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) 
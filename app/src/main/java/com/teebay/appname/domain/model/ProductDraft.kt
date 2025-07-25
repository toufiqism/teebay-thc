package com.teebay.appname.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class ProductDraft(
    val id: String = "",
    val userId: String,
    val title: String = "",
    val categories: List<Category> = emptyList(),
    val description: String = "",
    val imagePath: String? = null,
    val price: String = "", // Keep as string for form input
    val rentDuration: RentDuration? = null,
    val currentStep: Int = 1,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) 
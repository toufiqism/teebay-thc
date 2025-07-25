package com.teebay.appname.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class Category(val displayName: String) {
    ELECTRONICS("Electronics"),
    FURNITURE("Furniture"),
    HOME_APPLIANCES("Home Appliances"),
    SPORTING_GOODS("Sporting Goods"),
    OUTDOOR("Outdoor"),
    TOYS("Toys");

    companion object {
        fun getAll(): List<Category> = values().toList()
        
        fun fromDisplayName(displayName: String): Category? {
            return values().find { it.displayName == displayName }
        }
    }
} 
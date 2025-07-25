package com.teebay.appname.data.mappers

import com.teebay.appname.data.local.entities.ProductEntity
import com.teebay.appname.domain.model.CreateProductRequest
import com.teebay.appname.domain.model.Product
import com.teebay.appname.domain.model.UpdateProductRequest
import java.util.UUID

fun ProductEntity.toDomain(): Product {
    return Product(
        id = id,
        title = title,
        categories = categories,
        description = description,
        imagePath = imagePath,
        price = price,
        rentDuration = rentDuration,
        userId = userId,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun Product.toEntity(): ProductEntity {
    return ProductEntity(
        id = id,
        title = title,
        categories = categories,
        description = description,
        imagePath = imagePath,
        price = price,
        rentDuration = rentDuration,
        userId = userId,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun CreateProductRequest.toEntity(userId: String): ProductEntity {
    return ProductEntity(
        id = UUID.randomUUID().toString(),
        title = title,
        categories = categories,
        description = description,
        imagePath = imagePath,
        price = price,
        rentDuration = rentDuration,
        userId = userId,
        createdAt = System.currentTimeMillis(),
        updatedAt = System.currentTimeMillis()
    )
}

fun UpdateProductRequest.toEntity(userId: String, createdAt: Long): ProductEntity {
    return ProductEntity(
        id = id,
        title = title,
        categories = categories,
        description = description,
        imagePath = imagePath,
        price = price,
        rentDuration = rentDuration,
        userId = userId,
        createdAt = createdAt,
        updatedAt = System.currentTimeMillis()
    )
} 
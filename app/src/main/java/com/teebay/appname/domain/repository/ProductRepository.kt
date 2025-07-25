package com.teebay.appname.domain.repository

import com.teebay.appname.domain.model.CreateProductRequest
import com.teebay.appname.domain.model.Product
import com.teebay.appname.domain.model.ProductDraft
import com.teebay.appname.domain.model.UpdateProductRequest
import kotlinx.coroutines.flow.Flow

interface ProductRepository {
    suspend fun createProduct(request: CreateProductRequest, userId: String): Result<Product>
    suspend fun updateProduct(request: UpdateProductRequest, userId: String): Result<Product>
    suspend fun deleteProduct(productId: String, userId: String): Result<Unit>
    suspend fun getProductById(productId: String): Product?
    fun getUserProducts(userId: String): Flow<List<Product>>
    suspend fun getAllProducts(): List<Product>
    
    // Draft management
    suspend fun saveDraft(draft: ProductDraft): Result<Unit>
    suspend fun getDraft(userId: String): ProductDraft?
    suspend fun deleteDraft(userId: String): Result<Unit>
    suspend fun hasDraft(userId: String): Boolean
} 
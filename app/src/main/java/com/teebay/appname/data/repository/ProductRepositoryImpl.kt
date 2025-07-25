package com.teebay.appname.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.teebay.appname.data.local.entities.ProductEntity
import com.teebay.appname.data.mappers.toDomain
import com.teebay.appname.data.mappers.toEntity
import com.teebay.appname.domain.model.CreateProductRequest
import com.teebay.appname.domain.model.Product
import com.teebay.appname.domain.model.ProductDraft
import com.teebay.appname.domain.model.UpdateProductRequest
import com.teebay.appname.domain.repository.ProductRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class ProductRepositoryImpl(private val context: Context) : ProductRepository {

    private object PreferencesKeys {
        val PRODUCTS_DATA = stringPreferencesKey("products_data")
        val PRODUCT_DRAFTS_DATA = stringPreferencesKey("product_drafts_data")
    }

    override suspend fun createProduct(request: CreateProductRequest, userId: String): Result<Product> {
        return try {
            val products = getStoredProducts().toMutableList()
            val newProduct = request.toEntity(userId)
            products.add(newProduct)
            saveProducts(products)
            Result.success(newProduct.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateProduct(request: UpdateProductRequest, userId: String): Result<Product> {
        return try {
            val products = getStoredProducts().toMutableList()
            val productIndex = products.indexOfFirst { it.id == request.id && it.userId == userId }
            
            if (productIndex == -1) {
                return Result.failure(Exception("Product not found or access denied"))
            }

            val existingProduct = products[productIndex]
            val updatedProduct = request.toEntity(userId, existingProduct.createdAt)
            products[productIndex] = updatedProduct
            saveProducts(products)
            Result.success(updatedProduct.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteProduct(productId: String, userId: String): Result<Unit> {
        return try {
            val products = getStoredProducts().toMutableList()
            val removed = products.removeIf { it.id == productId && it.userId == userId }
            
            if (!removed) {
                return Result.failure(Exception("Product not found or access denied"))
            }

            saveProducts(products)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getProductById(productId: String): Product? {
        return try {
            getStoredProducts().find { it.id == productId }?.toDomain()
        } catch (e: Exception) {
            null
        }
    }

    override fun getUserProducts(userId: String): Flow<List<Product>> = flow {
        try {
            val products = getStoredProducts()
                .filter { it.userId == userId }
                .map { it.toDomain() }
                .sortedByDescending { it.updatedAt }
            emit(products)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    override suspend fun getAllProducts(): List<Product> {
        return try {
            getStoredProducts().map { it.toDomain() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private suspend fun getStoredProducts(): List<ProductEntity> {
        return try {
            val productsJson = context.dataStore.data.first()[PreferencesKeys.PRODUCTS_DATA]
            if (productsJson.isNullOrEmpty()) {
                emptyList()
            } else {
                Json.decodeFromString<List<ProductEntity>>(productsJson)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private suspend fun saveProducts(products: List<ProductEntity>) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.PRODUCTS_DATA] = Json.encodeToString(products)
        }
    }

    // Draft management implementation
    override suspend fun saveDraft(draft: ProductDraft): Result<Unit> {
        return try {
            val drafts = getStoredDrafts().toMutableMap()
            drafts[draft.userId] = draft.copy(updatedAt = System.currentTimeMillis())
            saveDrafts(drafts)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getDraft(userId: String): ProductDraft? {
        return try {
            getStoredDrafts()[userId]
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun deleteDraft(userId: String): Result<Unit> {
        return try {
            val drafts = getStoredDrafts().toMutableMap()
            drafts.remove(userId)
            saveDrafts(drafts)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun hasDraft(userId: String): Boolean {
        return try {
            getStoredDrafts().containsKey(userId)
        } catch (e: Exception) {
            false
        }
    }

    private suspend fun getStoredDrafts(): Map<String, ProductDraft> {
        return try {
            val draftsJson = context.dataStore.data.first()[PreferencesKeys.PRODUCT_DRAFTS_DATA]
            if (draftsJson.isNullOrEmpty()) {
                emptyMap()
            } else {
                Json.decodeFromString<Map<String, ProductDraft>>(draftsJson)
            }
        } catch (e: Exception) {
            emptyMap()
        }
    }

    private suspend fun saveDrafts(drafts: Map<String, ProductDraft>) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.PRODUCT_DRAFTS_DATA] = Json.encodeToString(drafts)
        }
    }
} 
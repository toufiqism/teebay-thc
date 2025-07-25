package com.teebay.appname.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teebay.appname.domain.model.Product
import com.teebay.appname.domain.repository.AuthRepository
import com.teebay.appname.domain.repository.ProductRepository
import kotlinx.coroutines.launch

class MyProductsViewModel(
    private val productRepository: ProductRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _productsState = MutableLiveData<ProductsState>()
    val productsState: LiveData<ProductsState> = _productsState

    private val _currentUser = MutableLiveData<String?>()
    val currentUser: LiveData<String?> = _currentUser

    init {
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            val user = authRepository.getCurrentUser()
            _currentUser.value = user?.id
            user?.id?.let { userId ->
                loadUserProducts(userId)
            }
        }
    }

    fun loadUserProducts(userId: String) {
        viewModelScope.launch {
            _productsState.value = ProductsState.Loading
            try {
                productRepository.getUserProducts(userId).collect { products ->
                    _productsState.value = ProductsState.Success(products)
                }
            } catch (e: Exception) {
                _productsState.value = ProductsState.Error("Failed to load products: ${e.message}")
            }
        }
    }

    fun deleteProduct(productId: String) {
        val userId = _currentUser.value ?: return
        viewModelScope.launch {
            val result = productRepository.deleteProduct(productId, userId)
            if (result.isFailure) {
                _productsState.value = ProductsState.Error("Failed to delete product: ${result.exceptionOrNull()?.message}")
            }
            // Products will auto-refresh through the Flow
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }

    fun refreshProducts() {
        _currentUser.value?.let { userId ->
            loadUserProducts(userId)
        }
    }
}

sealed class ProductsState {
    object Loading : ProductsState()
    data class Success(val products: List<Product>) : ProductsState()
    data class Error(val message: String) : ProductsState()
} 
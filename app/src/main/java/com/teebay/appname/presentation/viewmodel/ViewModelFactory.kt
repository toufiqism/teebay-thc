package com.teebay.appname.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.teebay.appname.domain.repository.AuthRepository
import com.teebay.appname.domain.repository.ProductRepository

class ViewModelFactory(
    private val authRepository: AuthRepository,
    private val productRepository: ProductRepository? = null
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(LoginViewModel::class.java) ->
                LoginViewModel(authRepository) as T
            modelClass.isAssignableFrom(RegisterViewModel::class.java) ->
                RegisterViewModel(authRepository) as T
            modelClass.isAssignableFrom(MyProductsViewModel::class.java) -> {
                requireNotNull(productRepository) { "ProductRepository is required for MyProductsViewModel" }
                MyProductsViewModel(productRepository, authRepository) as T
            }
            modelClass.isAssignableFrom(CreateProductViewModel::class.java) -> {
                requireNotNull(productRepository) { "ProductRepository is required for CreateProductViewModel" }
                CreateProductViewModel(productRepository, authRepository) as T
            }
            modelClass.isAssignableFrom(EditProductViewModel::class.java) -> {
                requireNotNull(productRepository) { "ProductRepository is required for EditProductViewModel" }
                EditProductViewModel(productRepository, authRepository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
} 
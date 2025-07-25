package com.teebay.appname.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teebay.appname.domain.model.Category
import com.teebay.appname.domain.model.Product
import com.teebay.appname.domain.model.RentDuration
import com.teebay.appname.domain.model.UpdateProductRequest
import com.teebay.appname.domain.repository.AuthRepository
import com.teebay.appname.domain.repository.ProductRepository
import kotlinx.coroutines.launch

class EditProductViewModel(
    private val productRepository: ProductRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _editProductState = MutableLiveData<EditProductState>()
    val editProductState: LiveData<EditProductState> = _editProductState

    private val _currentStep = MutableLiveData<EditProductStep>()
    val currentStep: LiveData<EditProductStep> = _currentStep

    // Form data
    private val _productId = MutableLiveData<String>()
    val productId: LiveData<String> = _productId

    private val _title = MutableLiveData<String>()
    val title: LiveData<String> = _title

    private val _selectedCategories = MutableLiveData<List<Category>>()
    val selectedCategories: LiveData<List<Category>> = _selectedCategories

    private val _description = MutableLiveData<String>()
    val description: LiveData<String> = _description

    private val _imagePath = MutableLiveData<String?>()
    val imagePath: LiveData<String?> = _imagePath

    private val _price = MutableLiveData<String>()
    val price: LiveData<String> = _price

    private val _rentDuration = MutableLiveData<RentDuration>()
    val rentDuration: LiveData<RentDuration> = _rentDuration

    init {
        _currentStep.value = EditProductStep.EDIT_DETAILS
        _selectedCategories.value = emptyList()
        _price.value = ""
        _title.value = ""
        _description.value = ""
        _rentDuration.value = RentDuration.PER_DAY
    }

    fun loadProduct(productId: String) {
        _productId.value = productId
        viewModelScope.launch {
            _editProductState.value = EditProductState.Loading
            try {
                val product = productRepository.getProductById(productId)
                if (product != null) {
                    populateForm(product)
                    _editProductState.value = EditProductState.Loaded(product)
                } else {
                    _editProductState.value = EditProductState.Error("Product not found")
                }
            } catch (e: Exception) {
                _editProductState.value = EditProductState.Error("Failed to load product: ${e.message}")
            }
        }
    }

    private fun populateForm(product: Product) {
        _title.value = product.title
        _selectedCategories.value = product.categories
        _description.value = product.description
        _imagePath.value = product.imagePath
        _price.value = product.price.toString()
        _rentDuration.value = product.rentDuration
    }

    fun setTitle(title: String) {
        _title.value = title
    }

    fun setCategories(categories: List<Category>) {
        _selectedCategories.value = categories
    }

    fun setDescription(description: String) {
        _description.value = description
    }

    fun setImagePath(imagePath: String?) {
        _imagePath.value = imagePath
    }

    fun setPrice(price: String) {
        _price.value = price
    }

    fun setRentDuration(duration: RentDuration) {
        _rentDuration.value = duration
    }

    fun goToNextStep(): Boolean {
        val current = _currentStep.value ?: return false
        
        if (current == EditProductStep.EDIT_DETAILS && validateDetailsStep()) {
            _currentStep.value = EditProductStep.EDIT_PRICE
            return true
        }
        return false
    }

    fun goToPreviousStep(): Boolean {
        val current = _currentStep.value ?: return false
        
        if (current == EditProductStep.EDIT_PRICE) {
            _currentStep.value = EditProductStep.EDIT_DETAILS
            return true
        }
        return false
    }

    private fun validateDetailsStep(): Boolean {
        return !_title.value.isNullOrBlank() &&
                !_selectedCategories.value.isNullOrEmpty() &&
                !_description.value.isNullOrBlank()
    }

    private fun validatePriceStep(): Boolean {
        val price = _price.value?.toDoubleOrNull()
        return price != null && price > 0
    }

    fun updateProduct() {
        if (!validateAllSteps()) {
            _editProductState.value = EditProductState.Error("Please fill all required fields")
            return
        }

        val productId = _productId.value ?: return
        _editProductState.value = EditProductState.Loading
        
        viewModelScope.launch {
            try {
                val user = authRepository.getCurrentUser()
                if (user == null) {
                    _editProductState.value = EditProductState.Error("User not authenticated")
                    return@launch
                }

                val request = UpdateProductRequest(
                    id = productId,
                    title = _title.value!!,
                    categories = _selectedCategories.value!!,
                    description = _description.value!!,
                    imagePath = _imagePath.value,
                    price = _price.value!!.toDouble(),
                    rentDuration = _rentDuration.value!!
                )

                val result = productRepository.updateProduct(request, user.id)
                if (result.isSuccess) {
                    _editProductState.value = EditProductState.Success(result.getOrThrow())
                } else {
                    _editProductState.value = EditProductState.Error(
                        result.exceptionOrNull()?.message ?: "Failed to update product"
                    )
                }
            } catch (e: Exception) {
                _editProductState.value = EditProductState.Error("Failed to update product: ${e.message}")
            }
        }
    }

    private fun validateAllSteps(): Boolean {
        return validateDetailsStep() && validatePriceStep()
    }
}

enum class EditProductStep {
    EDIT_DETAILS,
    EDIT_PRICE
}

sealed class EditProductState {
    object Loading : EditProductState()
    data class Loaded(val product: Product) : EditProductState()
    data class Success(val product: Product) : EditProductState()
    data class Error(val message: String) : EditProductState()
} 
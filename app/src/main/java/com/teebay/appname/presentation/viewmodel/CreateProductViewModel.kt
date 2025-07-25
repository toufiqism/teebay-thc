package com.teebay.appname.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teebay.appname.domain.model.Category
import com.teebay.appname.domain.model.CreateProductRequest
import com.teebay.appname.domain.model.RentDuration
import com.teebay.appname.domain.repository.AuthRepository
import com.teebay.appname.domain.repository.ProductRepository
import kotlinx.coroutines.launch

class CreateProductViewModel(
    private val productRepository: ProductRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _createProductState = MutableLiveData<CreateProductState>()
    val createProductState: LiveData<CreateProductState> = _createProductState

    private val _currentStep = MutableLiveData<CreateProductStep>()
    val currentStep: LiveData<CreateProductStep> = _currentStep

    // Form data
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
        _currentStep.value = CreateProductStep.TITLE
        _selectedCategories.value = emptyList()
        _price.value = ""
        _title.value = ""
        _description.value = ""
        _rentDuration.value = RentDuration.PER_DAY
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
        val next = getNextStep(current)
        
        if (next != null && validateCurrentStep()) {
            _currentStep.value = next
            return true
        }
        return false
    }

    fun goToPreviousStep(): Boolean {
        val current = _currentStep.value ?: return false
        val previous = getPreviousStep(current)
        
        if (previous != null) {
            _currentStep.value = previous
            return true
        }
        return false
    }

    fun goToStep(step: CreateProductStep) {
        _currentStep.value = step
    }

    private fun validateCurrentStep(): Boolean {
        return when (_currentStep.value) {
            CreateProductStep.TITLE -> !_title.value.isNullOrBlank()
            CreateProductStep.CATEGORY -> !_selectedCategories.value.isNullOrEmpty()
            CreateProductStep.DESCRIPTION -> !_description.value.isNullOrBlank()
            CreateProductStep.UPLOAD_PICTURE -> true // Optional
            CreateProductStep.PRICE -> {
                val price = _price.value?.toDoubleOrNull()
                price != null && price > 0
            }
            CreateProductStep.SUMMARY -> true
            null -> false
        }
    }

    private fun getNextStep(current: CreateProductStep): CreateProductStep? {
        return when (current) {
            CreateProductStep.TITLE -> CreateProductStep.CATEGORY
            CreateProductStep.CATEGORY -> CreateProductStep.DESCRIPTION
            CreateProductStep.DESCRIPTION -> CreateProductStep.UPLOAD_PICTURE
            CreateProductStep.UPLOAD_PICTURE -> CreateProductStep.PRICE
            CreateProductStep.PRICE -> CreateProductStep.SUMMARY
            CreateProductStep.SUMMARY -> null
        }
    }

    private fun getPreviousStep(current: CreateProductStep): CreateProductStep? {
        return when (current) {
            CreateProductStep.TITLE -> null
            CreateProductStep.CATEGORY -> CreateProductStep.TITLE
            CreateProductStep.DESCRIPTION -> CreateProductStep.CATEGORY
            CreateProductStep.UPLOAD_PICTURE -> CreateProductStep.DESCRIPTION
            CreateProductStep.PRICE -> CreateProductStep.UPLOAD_PICTURE
            CreateProductStep.SUMMARY -> CreateProductStep.PRICE
        }
    }

    fun submitProduct() {
        if (!validateAllSteps()) {
            _createProductState.value = CreateProductState.Error("Please fill all required fields")
            return
        }

        _createProductState.value = CreateProductState.Loading
        
        viewModelScope.launch {
            try {
                val user = authRepository.getCurrentUser()
                if (user == null) {
                    _createProductState.value = CreateProductState.Error("User not authenticated")
                    return@launch
                }

                val request = CreateProductRequest(
                    title = _title.value!!,
                    categories = _selectedCategories.value!!,
                    description = _description.value!!,
                    imagePath = _imagePath.value,
                    price = _price.value!!.toDouble(),
                    rentDuration = _rentDuration.value!!
                )

                val result = productRepository.createProduct(request, user.id)
                if (result.isSuccess) {
                    _createProductState.value = CreateProductState.Success(result.getOrThrow())
                } else {
                    _createProductState.value = CreateProductState.Error(
                        result.exceptionOrNull()?.message ?: "Failed to create product"
                    )
                }
            } catch (e: Exception) {
                _createProductState.value = CreateProductState.Error("Failed to create product: ${e.message}")
            }
        }
    }

    private fun validateAllSteps(): Boolean {
        return !_title.value.isNullOrBlank() &&
                !_selectedCategories.value.isNullOrEmpty() &&
                !_description.value.isNullOrBlank() &&
                _price.value?.toDoubleOrNull() != null &&
                _price.value!!.toDouble() > 0
    }

    fun resetForm() {
        _currentStep.value = CreateProductStep.TITLE
        _title.value = ""
        _selectedCategories.value = emptyList()
        _description.value = ""
        _imagePath.value = null
        _price.value = ""
        _rentDuration.value = RentDuration.PER_DAY
        _createProductState.value = CreateProductState.Idle
    }
}

enum class CreateProductStep {
    TITLE,
    CATEGORY,
    DESCRIPTION,
    UPLOAD_PICTURE,
    PRICE,
    SUMMARY
}

sealed class CreateProductState {
    object Idle : CreateProductState()
    object Loading : CreateProductState()
    data class Success(val product: com.teebay.appname.domain.model.Product) : CreateProductState()
    data class Error(val message: String) : CreateProductState()
} 
package com.teebay.appname.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teebay.appname.domain.model.Category
import com.teebay.appname.domain.model.CreateProductRequest
import com.teebay.appname.domain.model.ProductDraft
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

    // Draft-related LiveData
    private val _draftState = MutableLiveData<DraftState>()
    val draftState: LiveData<DraftState> = _draftState

    private var currentUserId: String? = null

    init {
        _currentStep.value = CreateProductStep.TITLE
        _selectedCategories.value = emptyList()
        _price.value = ""
        _title.value = ""
        _description.value = ""
        _rentDuration.value = RentDuration.PER_DAY
        _draftState.value = DraftState.NoDraft
        
        // Get current user and check for drafts
        checkForExistingDraft()
    }

    fun setTitle(title: String) {
        _title.value = title
        autoSaveDraft()
    }

    fun setCategories(categories: List<Category>) {
        _selectedCategories.value = categories
        autoSaveDraft()
    }

    fun setDescription(description: String) {
        _description.value = description
        autoSaveDraft()
    }

    fun setImagePath(imagePath: String?) {
        _imagePath.value = imagePath
        autoSaveDraft()
    }

    fun setPrice(price: String) {
        _price.value = price
        autoSaveDraft()
    }

    fun setRentDuration(duration: RentDuration) {
        _rentDuration.value = duration
        autoSaveDraft()
    }

    fun goToNextStep(): Boolean {
        val current = _currentStep.value ?: return false
        val next = getNextStep(current)
        
        if (next != null && validateCurrentStep()) {
            _currentStep.value = next
            autoSaveDraft()
            return true
        }
        return false
    }

    fun goToPreviousStep(): Boolean {
        val current = _currentStep.value ?: return false
        val previous = getPreviousStep(current)
        
        if (previous != null) {
            _currentStep.value = previous
            autoSaveDraft()
            return true
        }
        return false
    }

    fun goToStep(step: CreateProductStep) {
        _currentStep.value = step
        autoSaveDraft()
    }

    // Draft management methods
    private fun checkForExistingDraft() {
        viewModelScope.launch {
            try {
                val user = authRepository.getCurrentUser()
                if (user != null) {
                    currentUserId = user.id
                    val hasDraft = productRepository.hasDraft(user.id)
                    if (hasDraft) {
                        _draftState.value = DraftState.DraftAvailable
                    }
                }
            } catch (e: Exception) {
                // Handle error silently for now
            }
        }
    }

    fun loadDraftData() {
        viewModelScope.launch {
            try {
                currentUserId?.let { userId ->
                    val draft = productRepository.getDraft(userId)
                    if (draft != null) {
                        _title.value = draft.title
                        _selectedCategories.value = draft.categories
                        _description.value = draft.description
                        _imagePath.value = draft.imagePath
                        _price.value = draft.price
                        _rentDuration.value = draft.rentDuration ?: RentDuration.PER_DAY
                        _currentStep.value = getCurrentStepFromInt(draft.currentStep)
                        _draftState.value = DraftState.DraftLoaded
                    }
                }
            } catch (e: Exception) {
                _draftState.value = DraftState.Error("Failed to load draft")
            }
        }
    }

    fun discardDraft() {
        viewModelScope.launch {
            try {
                currentUserId?.let { userId ->
                    productRepository.deleteDraft(userId)
                    _draftState.value = DraftState.NoDraft
                }
            } catch (e: Exception) {
                // Handle error silently
            }
        }
    }

    private fun autoSaveDraft() {
        viewModelScope.launch {
            try {
                currentUserId?.let { userId ->
                    val draft = ProductDraft(
                        userId = userId,
                        title = _title.value ?: "",
                        categories = _selectedCategories.value ?: emptyList(),
                        description = _description.value ?: "",
                        imagePath = _imagePath.value,
                        price = _price.value ?: "",
                        rentDuration = _rentDuration.value,
                        currentStep = getCurrentStepInt()
                    )
                    productRepository.saveDraft(draft)
                }
            } catch (e: Exception) {
                // Handle error silently for auto-save
            }
        }
    }

    private fun getCurrentStepInt(): Int {
        return when (_currentStep.value) {
            CreateProductStep.TITLE -> 1
            CreateProductStep.CATEGORY -> 2
            CreateProductStep.DESCRIPTION -> 3
            CreateProductStep.UPLOAD_PICTURE -> 4
            CreateProductStep.PRICE -> 5
            CreateProductStep.SUMMARY -> 6
            null -> 1
        }
    }

    private fun getCurrentStepFromInt(step: Int): CreateProductStep {
        return when (step) {
            1 -> CreateProductStep.TITLE
            2 -> CreateProductStep.CATEGORY
            3 -> CreateProductStep.DESCRIPTION
            4 -> CreateProductStep.UPLOAD_PICTURE
            5 -> CreateProductStep.PRICE
            6 -> CreateProductStep.SUMMARY
            else -> CreateProductStep.TITLE
        }
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
                    // Clean up draft on successful submission
                    productRepository.deleteDraft(user.id)
                    _draftState.value = DraftState.NoDraft
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
        
        // Clear draft when resetting form
        discardDraft()
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

sealed class DraftState {
    object NoDraft : DraftState()
    object DraftAvailable : DraftState()
    object DraftLoaded : DraftState()
    data class Error(val message: String) : DraftState()
} 
package com.teebay.appname.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teebay.appname.domain.model.AuthResult
import com.teebay.appname.domain.model.RegisterRequest
import com.teebay.appname.domain.model.User
import com.teebay.appname.domain.repository.AuthRepository
import kotlinx.coroutines.launch

class RegisterViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val _registerState = MutableLiveData<RegisterState>()
    val registerState: LiveData<RegisterState> = _registerState

    fun register(
        firstName: String,
        lastName: String,
        email: String,
        password: String,
        confirmPassword: String,
        phoneNumber: String,
        address: String,
        dateOfBirth: String
    ) {
        // Validation
        val validationError = validateRegistrationData(
            firstName, lastName, email, password, confirmPassword, phoneNumber, address, dateOfBirth
        )
        
        if (validationError != null) {
            _registerState.value = RegisterState.Error(validationError)
            return
        }

        _registerState.value = RegisterState.Loading
        
        viewModelScope.launch {
            val request = RegisterRequest(
                firstName = firstName.trim(),
                lastName = lastName.trim(),
                email = email.trim().lowercase(),
                password = password,
                confirmPassword = confirmPassword,
                phoneNumber = phoneNumber.trim(),
                address = address.trim(),
                dateOfBirth = dateOfBirth
            )
            
            val result = authRepository.register(request)
            when (result) {
                is AuthResult.Success -> {
                    _registerState.value = RegisterState.Success(result.user)
                }
                is AuthResult.Error -> {
                    _registerState.value = RegisterState.Error(result.message)
                }
                else -> {
                    _registerState.value = RegisterState.Error("Unexpected registration result")
                }
            }
        }
    }

    private fun validateRegistrationData(
        firstName: String,
        lastName: String,
        email: String,
        password: String,
        confirmPassword: String,
        phoneNumber: String,
        address: String,
        dateOfBirth: String
    ): String? {
        return when {
            firstName.isBlank() -> "First name is required"
            lastName.isBlank() -> "Last name is required"
            email.isBlank() -> "Email is required"
            !isValidEmail(email) -> "Please enter a valid email address"
            password.isBlank() -> "Password is required"
            password.length < 6 -> "Password must be at least 6 characters long"
            confirmPassword.isBlank() -> "Please confirm your password"
            password != confirmPassword -> "Passwords do not match"
            phoneNumber.isBlank() -> "Phone number is required"
            !isValidPhoneNumber(phoneNumber) -> "Please enter a valid phone number"
            address.isBlank() -> "Address is required"
            dateOfBirth.isBlank() -> "Date of birth is required"
            else -> null
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun isValidPhoneNumber(phoneNumber: String): Boolean {
        // Basic phone number validation - adjust regex as needed for your requirements
        val phoneRegex = "^[+]?[0-9]{10,15}$".toRegex()
        return phoneRegex.matches(phoneNumber.replace("\\s".toRegex(), ""))
    }
}

sealed class RegisterState {
    object Idle : RegisterState()
    object Loading : RegisterState()
    data class Success(val user: User) : RegisterState()
    data class Error(val message: String) : RegisterState()
} 
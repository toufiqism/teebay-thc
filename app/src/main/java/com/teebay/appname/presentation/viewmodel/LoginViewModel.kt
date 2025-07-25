package com.teebay.appname.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teebay.appname.domain.model.AuthResult
import com.teebay.appname.domain.model.LoginRequest
import com.teebay.appname.domain.model.User
import com.teebay.appname.domain.repository.AuthRepository
import kotlinx.coroutines.launch

class LoginViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val _loginState = MutableLiveData<LoginState>()
    val loginState: LiveData<LoginState> = _loginState

    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?> = _user

    private val _biometricState = MutableLiveData<BiometricState>()
    val biometricState: LiveData<BiometricState> = _biometricState

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _loginState.value = LoginState.Error("Please fill in all fields")
            return
        }

        if (!isValidEmail(email)) {
            _loginState.value = LoginState.Error("Please enter a valid email address")
            return
        }

        _loginState.value = LoginState.Loading
        
        viewModelScope.launch {
            val result = authRepository.login(LoginRequest(email, password))
            when (result) {
                is AuthResult.Success -> {
                    _user.value = result.user
                    if (result.user.biometricEnabled) {
                        _loginState.value = LoginState.BiometricRequired(result.user)
                    } else {
                        _loginState.value = LoginState.Success(result.user)
                    }
                }
                is AuthResult.Error -> {
                    _loginState.value = LoginState.Error(result.message)
                }
                else -> {
                    _loginState.value = LoginState.Error("Unexpected authentication result")
                }
            }
        }
    }

    fun onBiometricSuccess() {
        val currentUser = _user.value
        if (currentUser != null) {
            _loginState.value = LoginState.Success(currentUser)
            _biometricState.value = BiometricState.Success
        }
    }

    fun onBiometricError(message: String) {
        _biometricState.value = BiometricState.Error(message)
    }

    fun onBiometricCancel() {
        _biometricState.value = BiometricState.Cancelled
        // User cancelled biometric, but they can still proceed with regular login
        val currentUser = _user.value
        if (currentUser != null) {
            _loginState.value = LoginState.Success(currentUser)
        }
    }

    fun resetBiometricState() {
        _biometricState.value = BiometricState.Idle
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}

sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    data class Success(val user: User) : LoginState()
    data class Error(val message: String) : LoginState()
    data class BiometricRequired(val user: User) : LoginState()
}

sealed class BiometricState {
    object Idle : BiometricState()
    object Success : BiometricState()
    data class Error(val message: String) : BiometricState()
    object Cancelled : BiometricState()
} 
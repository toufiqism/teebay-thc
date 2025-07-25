package com.teebay.appname.presentation.ui.auth.login

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.teebay.appname.R
import com.teebay.appname.data.repository.AuthRepositoryImpl
import com.teebay.appname.databinding.FragmentLoginBinding
import com.teebay.appname.presentation.viewmodel.BiometricState
import com.teebay.appname.presentation.viewmodel.LoginState
import com.teebay.appname.presentation.viewmodel.LoginViewModel
import com.teebay.appname.presentation.viewmodel.ViewModelFactory
import com.teebay.appname.utils.BiometricAuthenticationHelper
import com.teebay.appname.utils.BiometricStatus
import java.util.Calendar

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private lateinit var biometricHelper: BiometricAuthenticationHelper
    private val viewModel: LoginViewModel by viewModels {
        ViewModelFactory(AuthRepositoryImpl(requireContext()))
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        biometricHelper = BiometricAuthenticationHelper(requireContext())
        setupUI()
        observeViewModel()
        checkBiometricAvailability()
    }

    private fun setupUI() {
        binding.apply {
            btnLogin.setOnClickListener {
                val email = etEmail.text.toString()
                val password = etPassword.text.toString()
                viewModel.login(email, password)
            }

            btnBiometric.setOnClickListener {
                showBiometricPrompt()
            }

            tvSignUp.setOnClickListener {
                findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
            }
        }
    }

    private fun observeViewModel() {
        viewModel.loginState.observe(viewLifecycleOwner) { state ->
            handleLoginState(state)
        }

        viewModel.biometricState.observe(viewLifecycleOwner) { state ->
            handleBiometricState(state)
        }
    }

    private fun handleLoginState(state: LoginState) {
        binding.apply {
            when (state) {
                is LoginState.Idle -> {
                    showLoading(false)
                }
                is LoginState.Loading -> {
                    showLoading(true)
                }
                is LoginState.Success -> {
                    showLoading(false)
                    Toast.makeText(
                        requireContext(),
                        "Welcome, ${state.user.firstName}!",
                        Toast.LENGTH_SHORT
                    ).show()
                    // Navigate to main screen (placeholder for now)
                    Toast.makeText(requireContext(), "Login Successful!", Toast.LENGTH_LONG).show()
                }
                is LoginState.Error -> {
                    showLoading(false)
                    showError(state.message)
                }
                is LoginState.BiometricRequired -> {
                    showLoading(false)
                    showBiometricPrompt()
                }
            }
        }
    }

    private fun handleBiometricState(state: BiometricState) {
        when (state) {
            is BiometricState.Success -> {
                viewModel.onBiometricSuccess()
            }
            is BiometricState.Error -> {
                viewModel.onBiometricError(state.message)
                showError(state.message)
            }
            is BiometricState.Cancelled -> {
                viewModel.onBiometricCancel()
            }
            else -> { /* Do nothing */ }
        }
    }

    private fun checkBiometricAvailability() {
        val biometricStatus = biometricHelper.isBiometricAvailable()
        binding.btnBiometric.visibility = when (biometricStatus) {
            BiometricStatus.AVAILABLE -> View.VISIBLE
            BiometricStatus.NOT_ENROLLED -> {
                binding.btnBiometric.text = "Set up biometric authentication"
                View.VISIBLE
            }
            else -> View.GONE
        }
    }

    private fun showBiometricPrompt() {
        val biometricStatus = biometricHelper.isBiometricAvailable()
        
        when (biometricStatus) {
            BiometricStatus.AVAILABLE -> {
                biometricHelper.authenticateWithBiometric(
                    activity = requireActivity(),
                    title = "Biometric Login",
                    subtitle = "Use your fingerprint or face to login to Teebay",
                    onSuccess = {
                        viewModel.onBiometricSuccess()
                    },
                    onError = { error ->
                        viewModel.onBiometricError(error)
                    },
                    onCancel = {
                        viewModel.onBiometricCancel()
                    }
                )
            }
            BiometricStatus.NOT_ENROLLED -> {
                showError("Please set up biometric authentication in your device settings first")
            }
            BiometricStatus.NOT_AVAILABLE -> {
                showError("Biometric authentication is not available on this device")
            }
            BiometricStatus.TEMPORARILY_NOT_AVAILABLE -> {
                showError("Biometric authentication is temporarily unavailable")
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.apply {
            if (isLoading) {
                btnLogin.text = ""
                progressBar.visibility = View.VISIBLE
                btnLogin.isEnabled = false
                btnBiometric.isEnabled = false
            } else {
                btnLogin.text = "LOGIN"
                progressBar.visibility = View.GONE
                btnLogin.isEnabled = true
                btnBiometric.isEnabled = true
            }
        }
    }

    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 
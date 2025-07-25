package com.teebay.appname.presentation.ui.auth.register

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
import com.teebay.appname.databinding.FragmentRegisterBinding
import com.teebay.appname.presentation.viewmodel.RegisterState
import com.teebay.appname.presentation.viewmodel.RegisterViewModel
import com.teebay.appname.presentation.viewmodel.ViewModelFactory
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    private val viewModel: RegisterViewModel by viewModels {
        ViewModelFactory(AuthRepositoryImpl(requireContext()))
    }

    private val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupUI()
        observeViewModel()
    }

    private fun setupUI() {
        binding.apply {
            btnRegister.setOnClickListener {
                register()
            }

            etDateOfBirth.setOnClickListener {
                showDatePicker()
            }

            tvSignIn.setOnClickListener {
                findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
            }
        }
    }

    private fun observeViewModel() {
        viewModel.registerState.observe(viewLifecycleOwner) { state ->
            handleRegisterState(state)
        }
    }

    private fun register() {
        binding.apply {
            val firstName = etFirstName.text.toString()
            val lastName = etLastName.text.toString()
            val email = etEmail.text.toString()
            val phoneNumber = etPhoneNumber.text.toString()
            val address = etAddress.text.toString()
            val dateOfBirth = etDateOfBirth.text.toString()
            val password = etPassword.text.toString()
            val confirmPassword = etConfirmPassword.text.toString()

            viewModel.register(
                firstName = firstName,
                lastName = lastName,
                email = email,
                password = password,
                confirmPassword = confirmPassword,
                phoneNumber = phoneNumber,
                address = address,
                dateOfBirth = dateOfBirth
            )
        }
    }

    private fun handleRegisterState(state: RegisterState) {
        binding.apply {
            when (state) {
                is RegisterState.Idle -> {
                    showLoading(false)
                }
                is RegisterState.Loading -> {
                    showLoading(true)
                }
                is RegisterState.Success -> {
                    showLoading(false)
                    Toast.makeText(
                        requireContext(),
                        "Registration successful! Welcome, ${state.user.firstName}!",
                        Toast.LENGTH_LONG
                    ).show()
                    // Navigate to main screen or login screen
                    findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
                }
                is RegisterState.Error -> {
                    showLoading(false)
                    showError(state.message)
                }
            }
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentDay = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                val formattedDate = dateFormatter.format(calendar.time)
                binding.etDateOfBirth.setText(formattedDate)
            },
            currentYear - 18, // Default to 18 years ago
            currentMonth,
            currentDay
        )

        // Set maximum date to today
        datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
        
        // Set minimum date to 100 years ago
        calendar.add(Calendar.YEAR, -100)
        datePickerDialog.datePicker.minDate = calendar.timeInMillis

        datePickerDialog.show()
    }

    private fun showLoading(isLoading: Boolean) {
        binding.apply {
            if (isLoading) {
                btnRegister.text = ""
                progressBar.visibility = View.VISIBLE
                btnRegister.isEnabled = false
                setFormEnabled(false)
            } else {
                btnRegister.text = "REGISTER"
                progressBar.visibility = View.GONE
                btnRegister.isEnabled = true
                setFormEnabled(true)
            }
        }
    }

    private fun setFormEnabled(enabled: Boolean) {
        binding.apply {
            etFirstName.isEnabled = enabled
            etLastName.isEnabled = enabled
            etEmail.isEnabled = enabled
            etPhoneNumber.isEnabled = enabled
            etAddress.isEnabled = enabled
            etDateOfBirth.isEnabled = enabled
            etPassword.isEnabled = enabled
            etConfirmPassword.isEnabled = enabled
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
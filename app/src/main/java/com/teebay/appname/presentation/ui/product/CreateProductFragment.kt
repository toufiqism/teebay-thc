package com.teebay.appname.presentation.ui.product

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.teebay.appname.R
import com.teebay.appname.data.repository.AuthRepositoryImpl
import com.teebay.appname.data.repository.ProductRepositoryImpl
import com.teebay.appname.databinding.FragmentCreateProductBinding
import com.teebay.appname.databinding.StepProductCategoryBinding
import com.teebay.appname.databinding.StepProductDescriptionBinding
import com.teebay.appname.databinding.StepProductPriceBinding
import com.teebay.appname.databinding.StepProductSummaryBinding
import com.teebay.appname.databinding.StepProductTitleBinding
import com.teebay.appname.databinding.StepProductUploadPictureBinding
import com.teebay.appname.domain.model.Category
import com.teebay.appname.domain.model.RentDuration
import com.teebay.appname.presentation.adapter.CategoryAdapter
import com.teebay.appname.presentation.adapter.CategoryItem
import com.teebay.appname.presentation.viewmodel.CreateProductState
import com.teebay.appname.presentation.viewmodel.CreateProductStep
import com.teebay.appname.presentation.viewmodel.CreateProductViewModel
import com.teebay.appname.presentation.viewmodel.DraftState
import com.teebay.appname.presentation.viewmodel.ViewModelFactory
import java.text.NumberFormat
import java.util.Locale

class CreateProductFragment : Fragment() {

    private var _binding: FragmentCreateProductBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CreateProductViewModel by viewModels {
        ViewModelFactory(
            authRepository = AuthRepositoryImpl(requireContext()),
            productRepository = ProductRepositoryImpl(requireContext())
        )
    }

    private lateinit var categoryAdapter: CategoryAdapter
    private var currentStepBinding: Any? = null

    // Camera and Gallery permissions
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions[Manifest.permission.CAMERA] == true -> {
                launchCamera()
            }
            permissions[Manifest.permission.READ_EXTERNAL_STORAGE] == true -> {
                launchGallery()
            }
            else -> {
                showError("Permissions are required to access camera and gallery")
            }
        }
    }

    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                handleImageSelected(uri)
            }
        }
    }

    private val selectImageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                handleImageSelected(uri)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateProductBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupUI()
        observeViewModel()
    }

    private fun setupUI() {
        binding.apply {
            ivClose.setOnClickListener {
                findNavController().navigateUp()
            }

            btnBack.setOnClickListener {
                viewModel.goToPreviousStep()
            }

            btnNext.setOnClickListener {
                handleNextStep()
            }
        }
    }

    private fun observeViewModel() {
        viewModel.currentStep.observe(viewLifecycleOwner) { step ->
            updateStepUI(step)
        }

        viewModel.createProductState.observe(viewLifecycleOwner) { state ->
            handleCreateProductState(state)
        }

        viewModel.draftState.observe(viewLifecycleOwner) { state ->
            handleDraftState(state)
        }
    }

    private fun updateStepUI(step: CreateProductStep) {
        val stepNumber = step.ordinal + 1
        val totalSteps = CreateProductStep.values().size

        binding.apply {
            tvStepIndicator.text = "$stepNumber/$totalSteps"
            progressIndicator.progress = (stepNumber * 100) / totalSteps

            // Update button visibility and text
            btnBack.visibility = if (step == CreateProductStep.TITLE) View.GONE else View.VISIBLE
            btnNext.text = if (step == CreateProductStep.SUMMARY) "Submit" else "Next"
        }

        // Clear previous step content
        binding.contentContainer.removeAllViews()
        currentStepBinding = null

        // Load step content
        when (step) {
            CreateProductStep.TITLE -> loadTitleStep()
            CreateProductStep.CATEGORY -> loadCategoryStep()
            CreateProductStep.DESCRIPTION -> loadDescriptionStep()
            CreateProductStep.UPLOAD_PICTURE -> loadUploadPictureStep()
            CreateProductStep.PRICE -> loadPriceStep()
            CreateProductStep.SUMMARY -> loadSummaryStep()
        }
    }

    private fun loadTitleStep() {
        val stepBinding = StepProductTitleBinding.inflate(layoutInflater)
        binding.contentContainer.addView(stepBinding.root)
        currentStepBinding = stepBinding

        // Observe title changes
        viewModel.title.observe(viewLifecycleOwner) { title ->
            if (stepBinding.etTitle.text.toString() != title) {
                stepBinding.etTitle.setText(title)
            }
        }

        stepBinding.etTitle.setOnFocusChangeListener { _, _ ->
            viewModel.setTitle(stepBinding.etTitle.text.toString())
        }
    }

    private fun loadCategoryStep() {
        val stepBinding = StepProductCategoryBinding.inflate(layoutInflater)
        binding.contentContainer.addView(stepBinding.root)
        currentStepBinding = stepBinding

        setupCategoryRecyclerView(stepBinding)
    }

    private fun setupCategoryRecyclerView(stepBinding: StepProductCategoryBinding) {
        categoryAdapter = CategoryAdapter { category, isSelected ->
            val currentCategories = viewModel.selectedCategories.value?.toMutableList() ?: mutableListOf()
            
            if (isSelected) {
                if (!currentCategories.contains(category)) {
                    currentCategories.add(category)
                }
            } else {
                currentCategories.remove(category)
            }
            
            viewModel.setCategories(currentCategories)
            updateCategoryList()
        }

        stepBinding.rvCategories.apply {
            adapter = categoryAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        updateCategoryList()
    }

    private fun updateCategoryList() {
        val selectedCategories = viewModel.selectedCategories.value ?: emptyList()
        val categoryItems = Category.getAll().map { category ->
            CategoryItem(category, selectedCategories.contains(category))
        }
        categoryAdapter.submitList(categoryItems)
    }

    private fun loadDescriptionStep() {
        val stepBinding = StepProductDescriptionBinding.inflate(layoutInflater)
        binding.contentContainer.addView(stepBinding.root)
        currentStepBinding = stepBinding

        viewModel.description.observe(viewLifecycleOwner) { description ->
            if (stepBinding.etDescription.text.toString() != description) {
                stepBinding.etDescription.setText(description)
            }
        }

        stepBinding.etDescription.setOnFocusChangeListener { _, _ ->
            viewModel.setDescription(stepBinding.etDescription.text.toString())
        }
    }

    private fun loadUploadPictureStep() {
        val stepBinding = StepProductUploadPictureBinding.inflate(layoutInflater)
        binding.contentContainer.addView(stepBinding.root)
        currentStepBinding = stepBinding

        stepBinding.apply {
            cvTakePhoto.setOnClickListener {
                checkCameraPermissionAndTakePhoto()
            }

            cvGallery.setOnClickListener {
                checkGalleryPermissionAndSelectImage()
            }

            ivRemoveImage.setOnClickListener {
                viewModel.setImagePath(null)
                updateImagePreview(stepBinding)
            }
        }

        viewModel.imagePath.observe(viewLifecycleOwner) {
            updateImagePreview(stepBinding)
        }
    }

    private fun loadPriceStep() {
        val stepBinding = StepProductPriceBinding.inflate(layoutInflater)
        binding.contentContainer.addView(stepBinding.root)
        currentStepBinding = stepBinding

        // Setup rent duration dropdown
        val rentDurations = RentDuration.getAll().map { it.displayName }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, rentDurations)
        stepBinding.actvRentDuration.setAdapter(adapter)

        // Observe price changes
        viewModel.price.observe(viewLifecycleOwner) { price ->
            if (stepBinding.etPrice.text.toString() != price) {
                stepBinding.etPrice.setText(price)
            }
        }

        // Observe rent duration changes
        viewModel.rentDuration.observe(viewLifecycleOwner) { duration ->
            stepBinding.actvRentDuration.setText(duration.displayName, false)
        }

        stepBinding.etPrice.setOnFocusChangeListener { _, _ ->
            viewModel.setPrice(stepBinding.etPrice.text.toString())
        }

        stepBinding.actvRentDuration.setOnItemClickListener { _, _, position, _ ->
            val selectedDuration = RentDuration.getAll()[position]
            viewModel.setRentDuration(selectedDuration)
        }
    }

    private fun loadSummaryStep() {
        val stepBinding = StepProductSummaryBinding.inflate(layoutInflater)
        binding.contentContainer.addView(stepBinding.root)
        currentStepBinding = stepBinding

        updateSummaryContent(stepBinding)

        stepBinding.apply {
            btnEditTitle.setOnClickListener {
                viewModel.goToStep(CreateProductStep.TITLE)
            }

            btnEditCategories.setOnClickListener {
                viewModel.goToStep(CreateProductStep.CATEGORY)
            }

            btnEditPrice.setOnClickListener {
                viewModel.goToStep(CreateProductStep.PRICE)
            }
        }
    }

    private fun updateSummaryContent(stepBinding: StepProductSummaryBinding) {
        stepBinding.apply {
            tvProductTitle.text = viewModel.title.value ?: ""
            tvCategories.text = viewModel.selectedCategories.value?.joinToString(", ") { it.displayName } ?: ""
            tvDescription.text = viewModel.description.value ?: ""

            val price = viewModel.price.value?.toDoubleOrNull() ?: 0.0
            val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US)
            val rentDuration = viewModel.rentDuration.value?.displayName ?: ""
            tvPrice.text = "${currencyFormat.format(price)} $rentDuration"

            // TODO: Load actual image
            ivProductImage.setImageResource(android.R.drawable.ic_menu_gallery)
        }
    }

    private fun handleNextStep() {
        when (viewModel.currentStep.value) {
            CreateProductStep.TITLE -> {
                val titleBinding = currentStepBinding as? StepProductTitleBinding
                viewModel.setTitle(titleBinding?.etTitle?.text.toString() ?: "")
            }
            CreateProductStep.DESCRIPTION -> {
                val descBinding = currentStepBinding as? StepProductDescriptionBinding
                viewModel.setDescription(descBinding?.etDescription?.text.toString() ?: "")
            }
            CreateProductStep.PRICE -> {
                val priceBinding = currentStepBinding as? StepProductPriceBinding
                viewModel.setPrice(priceBinding?.etPrice?.text.toString() ?: "")
            }
            CreateProductStep.SUMMARY -> {
                viewModel.submitProduct()
                return
            }
            else -> { /* No input to save */ }
        }

        if (!viewModel.goToNextStep()) {
            showError("Please fill all required fields")
        }
    }

    private fun handleCreateProductState(state: CreateProductState) {
        binding.apply {
            when (state) {
                is CreateProductState.Loading -> {
                    loadingOverlay.visibility = View.VISIBLE
                    btnNext.isEnabled = false
                    btnBack.isEnabled = false
                }
                is CreateProductState.Success -> {
                    loadingOverlay.visibility = View.GONE
                    showSuccess("Product created successfully!")
                    findNavController().navigateUp()
                }
                is CreateProductState.Error -> {
                    loadingOverlay.visibility = View.GONE
                    btnNext.isEnabled = true
                    btnBack.isEnabled = true
                    showError(state.message)
                }
                else -> {
                    loadingOverlay.visibility = View.GONE
                    btnNext.isEnabled = true
                    btnBack.isEnabled = true
                }
            }
        }
    }

    private fun handleDraftState(state: DraftState) {
        when (state) {
            is DraftState.DraftAvailable -> {
                showDraftResumeDialog()
            }
            is DraftState.DraftLoaded -> {
                // Draft loaded successfully, no action needed
                // The UI will update automatically through data binding
            }
            is DraftState.Error -> {
                showError("Draft error: ${state.message}")
            }
            is DraftState.NoDraft -> {
                // No draft available, normal flow
            }
        }
    }

    private fun showDraftResumeDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Resume Draft")
            .setMessage("You have a saved draft. Would you like to resume or start a new entry?")
            .setPositiveButton("Resume") { _, _ ->
                viewModel.loadDraftData()
            }
            .setNegativeButton("Start New") { _, _ ->
                viewModel.discardDraft()
            }
            .setCancelable(false)
            .show()
    }

    private fun checkCameraPermissionAndTakePhoto() {
        when {
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == 
                PackageManager.PERMISSION_GRANTED -> {
                launchCamera()
            }
            else -> {
                requestPermissionLauncher.launch(arrayOf(Manifest.permission.CAMERA))
            }
        }
    }

    private fun checkGalleryPermissionAndSelectImage() {
        when {
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == 
                PackageManager.PERMISSION_GRANTED -> {
                launchGallery()
            }
            else -> {
                requestPermissionLauncher.launch(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE))
            }
        }
    }

    private fun launchCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        takePictureLauncher.launch(intent)
    }

    private fun launchGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        selectImageLauncher.launch(intent)
    }

    private fun handleImageSelected(uri: Uri) {
        viewModel.setImagePath(uri.toString())
    }

    private fun updateImagePreview(stepBinding: StepProductUploadPictureBinding) {
        val imagePath = viewModel.imagePath.value
        stepBinding.apply {
            if (imagePath != null) {
                cvImagePreview.visibility = View.VISIBLE
                // TODO: Load actual image from URI
                ivPreview.setImageResource(android.R.drawable.ic_menu_gallery)
            } else {
                cvImagePreview.visibility = View.GONE
            }
        }
    }

    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }

    private fun showSuccess(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        currentStepBinding = null
    }
} 
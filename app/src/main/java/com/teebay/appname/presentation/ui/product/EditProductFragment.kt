package com.teebay.appname.presentation.ui.product

import android.Manifest
import android.app.Activity
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
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.teebay.appname.R
import com.teebay.appname.data.repository.AuthRepositoryImpl
import com.teebay.appname.data.repository.ProductRepositoryImpl
import com.teebay.appname.databinding.FragmentEditProductBinding
import com.teebay.appname.databinding.StepProductCategoryBinding
import com.teebay.appname.databinding.StepProductDescriptionBinding
import com.teebay.appname.databinding.StepProductPriceBinding
import com.teebay.appname.databinding.StepProductTitleBinding
import com.teebay.appname.databinding.StepProductUploadPictureBinding
import com.teebay.appname.domain.model.Category
import com.teebay.appname.domain.model.RentDuration
import com.teebay.appname.presentation.adapter.CategoryAdapter
import com.teebay.appname.presentation.adapter.CategoryItem
import com.teebay.appname.presentation.viewmodel.EditProductState
import com.teebay.appname.presentation.viewmodel.EditProductStep
import com.teebay.appname.presentation.viewmodel.EditProductViewModel
import com.teebay.appname.presentation.viewmodel.ViewModelFactory
import java.text.NumberFormat
import java.util.Locale

class EditProductFragment : Fragment() {

    private var _binding: FragmentEditProductBinding? = null
    private val binding get() = _binding!!

    private val args: EditProductFragmentArgs by navArgs()

    private val viewModel: EditProductViewModel by viewModels {
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
        _binding = FragmentEditProductBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupUI()
        observeViewModel()
        loadProduct()
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

    private fun loadProduct() {
        viewModel.loadProduct(args.productId)
    }

    private fun observeViewModel() {
        viewModel.currentStep.observe(viewLifecycleOwner) { step ->
            updateStepUI(step)
        }

        viewModel.editProductState.observe(viewLifecycleOwner) { state ->
            handleEditProductState(state)
        }
    }

    private fun updateStepUI(step: EditProductStep) {
        val stepNumber = step.ordinal + 1
        val totalSteps = EditProductStep.values().size

        binding.apply {
            tvStepIndicator.text = "$stepNumber/$totalSteps"
            progressIndicator.progress = (stepNumber * 100) / totalSteps

            // Update button visibility and text
            btnBack.visibility = if (step == EditProductStep.EDIT_DETAILS) View.GONE else View.VISIBLE
            btnNext.text = if (step == EditProductStep.EDIT_PRICE) "Update Product" else "Next"
        }

        // Clear previous step content
        binding.contentContainer.removeAllViews()
        currentStepBinding = null

        // Load step content
        when (step) {
            EditProductStep.EDIT_DETAILS -> loadDetailsStep()
            EditProductStep.EDIT_PRICE -> loadPriceStep()
        }
    }

    private fun loadDetailsStep() {
        // Create a container for all detail fields
        val detailsContainer = layoutInflater.inflate(R.layout.step_product_title, null) as ViewGroup
        
        // Title section
        val titleBinding = StepProductTitleBinding.bind(detailsContainer)
        
        // Categories section
        val categoryContainer = layoutInflater.inflate(R.layout.step_product_category, null)
        val categoryBinding = StepProductCategoryBinding.bind(categoryContainer)
        
        // Description section
        val descriptionContainer = layoutInflater.inflate(R.layout.step_product_description, null)
        val descriptionBinding = StepProductDescriptionBinding.bind(descriptionContainer)
        
        // Upload picture section
        val uploadContainer = layoutInflater.inflate(R.layout.step_product_upload_picture, null)
        val uploadBinding = StepProductUploadPictureBinding.bind(uploadContainer)

        // Create a vertical LinearLayout to hold all sections
        val mainContainer = android.widget.LinearLayout(requireContext()).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            addView(titleBinding.root)
            addView(categoryBinding.root)
            addView(descriptionBinding.root)
            addView(uploadBinding.root)
        }

        binding.contentContainer.addView(mainContainer)
        currentStepBinding = mapOf(
            "title" to titleBinding,
            "category" to categoryBinding,
            "description" to descriptionBinding,
            "upload" to uploadBinding
        )

        setupTitleSection(titleBinding)
        setupCategorySection(categoryBinding)
        setupDescriptionSection(descriptionBinding)
        setupUploadSection(uploadBinding)
    }

    private fun setupTitleSection(titleBinding: StepProductTitleBinding) {
        // Observe title changes
        viewModel.title.observe(viewLifecycleOwner) { title ->
            if (titleBinding.etTitle.text.toString() != title) {
                titleBinding.etTitle.setText(title)
            }
        }

        titleBinding.etTitle.setOnFocusChangeListener { _, _ ->
            viewModel.setTitle(titleBinding.etTitle.text.toString())
        }
    }

    private fun setupCategorySection(categoryBinding: StepProductCategoryBinding) {
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

        categoryBinding.rvCategories.apply {
            adapter = categoryAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        viewModel.selectedCategories.observe(viewLifecycleOwner) {
            updateCategoryList()
        }
    }

    private fun updateCategoryList() {
        val selectedCategories = viewModel.selectedCategories.value ?: emptyList()
        val categoryItems = Category.getAll().map { category ->
            CategoryItem(category, selectedCategories.contains(category))
        }
        categoryAdapter.submitList(categoryItems)
    }

    private fun setupDescriptionSection(descriptionBinding: StepProductDescriptionBinding) {
        viewModel.description.observe(viewLifecycleOwner) { description ->
            if (descriptionBinding.etDescription.text.toString() != description) {
                descriptionBinding.etDescription.setText(description)
            }
        }

        descriptionBinding.etDescription.setOnFocusChangeListener { _, _ ->
            viewModel.setDescription(descriptionBinding.etDescription.text.toString())
        }
    }

    private fun setupUploadSection(uploadBinding: StepProductUploadPictureBinding) {
        uploadBinding.apply {
            cvTakePhoto.setOnClickListener {
                checkCameraPermissionAndTakePhoto()
            }

            cvGallery.setOnClickListener {
                checkGalleryPermissionAndSelectImage()
            }

            ivRemoveImage.setOnClickListener {
                viewModel.setImagePath(null)
                updateImagePreview(uploadBinding)
            }
        }

        viewModel.imagePath.observe(viewLifecycleOwner) {
            updateImagePreview(uploadBinding)
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

    private fun handleNextStep() {
        when (viewModel.currentStep.value) {
            EditProductStep.EDIT_DETAILS -> {
                val bindings = currentStepBinding as? Map<String, *>
                bindings?.let { bindingMap ->
                    val titleBinding = bindingMap["title"] as? StepProductTitleBinding
                    val descriptionBinding = bindingMap["description"] as? StepProductDescriptionBinding
                    
                    titleBinding?.let { viewModel.setTitle(it.etTitle.text.toString()) }
                    descriptionBinding?.let { viewModel.setDescription(it.etDescription.text.toString()) }
                }
            }
            EditProductStep.EDIT_PRICE -> {
                val priceBinding = currentStepBinding as? StepProductPriceBinding
                priceBinding?.let { viewModel.setPrice(it.etPrice.text.toString()) }
                viewModel.updateProduct()
                return
            }
            else -> { /* No input to save */ }
        }

        if (!viewModel.goToNextStep()) {
            showError("Please fill all required fields")
        }
    }

    private fun handleEditProductState(state: EditProductState) {
        binding.apply {
            when (state) {
                is EditProductState.Loading -> {
                    loadingOverlay.visibility = View.VISIBLE
                    btnNext.isEnabled = false
                    btnBack.isEnabled = false
                }
                is EditProductState.Loaded -> {
                    loadingOverlay.visibility = View.GONE
                    btnNext.isEnabled = true
                    btnBack.isEnabled = true
                    showSuccess("Product loaded successfully!")
                }
                is EditProductState.Success -> {
                    loadingOverlay.visibility = View.GONE
                    showSuccess("Product updated successfully!")
                    findNavController().navigateUp()
                }
                is EditProductState.Error -> {
                    loadingOverlay.visibility = View.GONE
                    btnNext.isEnabled = true
                    btnBack.isEnabled = true
                    showError(state.message)
                }
            }
        }
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

    private fun updateImagePreview(uploadBinding: StepProductUploadPictureBinding) {
        val imagePath = viewModel.imagePath.value
        uploadBinding.apply {
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
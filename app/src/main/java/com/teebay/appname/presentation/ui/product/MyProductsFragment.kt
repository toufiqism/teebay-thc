package com.teebay.appname.presentation.ui.product

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.teebay.appname.R
import com.teebay.appname.data.repository.AuthRepositoryImpl
import com.teebay.appname.data.repository.ProductRepositoryImpl
import com.teebay.appname.databinding.FragmentMyProductsBinding
import com.teebay.appname.domain.model.Product
import com.teebay.appname.presentation.adapter.ProductAdapter
import com.teebay.appname.presentation.viewmodel.MyProductsViewModel
import com.teebay.appname.presentation.viewmodel.ProductsState
import com.teebay.appname.presentation.viewmodel.ViewModelFactory

class MyProductsFragment : Fragment() {

    private var _binding: FragmentMyProductsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MyProductsViewModel by viewModels {
        ViewModelFactory(
            authRepository = AuthRepositoryImpl(requireContext()),
            productRepository = ProductRepositoryImpl(requireContext())
        )
    }

    private lateinit var productAdapter: ProductAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyProductsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        setupUI()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        productAdapter = ProductAdapter(
            onProductClick = { product ->
                showDeleteConfirmationDialog(product)
            },
            onEditClick = { product ->
                // Navigate to edit product screen
                val action = MyProductsFragmentDirections
                    .actionMyProductsFragmentToEditProductFragment(product.id)
                findNavController().navigate(action)
            }
        )

        binding.rvProducts.apply {
            adapter = productAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun setupUI() {
        binding.apply {
            fabAddProduct.setOnClickListener {
                findNavController().navigate(R.id.action_myProductsFragment_to_createProductFragment)
            }

            btnLogout.setOnClickListener {
                showLogoutConfirmationDialog()
            }
        }
    }

    private fun observeViewModel() {
        viewModel.productsState.observe(viewLifecycleOwner) { state ->
            handleProductsState(state)
        }
    }

    private fun handleProductsState(state: ProductsState) {
        binding.apply {
            when (state) {
                is ProductsState.Loading -> {
                    progressBar.visibility = View.VISIBLE
                    rvProducts.visibility = View.GONE
                    llEmptyState.visibility = View.GONE
                }
                is ProductsState.Success -> {
                    progressBar.visibility = View.GONE
                    if (state.products.isEmpty()) {
                        rvProducts.visibility = View.GONE
                        llEmptyState.visibility = View.VISIBLE
                    } else {
                        rvProducts.visibility = View.VISIBLE
                        llEmptyState.visibility = View.GONE
                        productAdapter.submitList(state.products)
                    }
                }
                is ProductsState.Error -> {
                    progressBar.visibility = View.GONE
                    rvProducts.visibility = View.GONE
                    llEmptyState.visibility = View.GONE
                    showError(state.message)
                }
            }
        }
    }

    private fun showDeleteConfirmationDialog(product: Product) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Product")
            .setMessage("Are you sure you want to delete this product?")
            .setPositiveButton("Yes") { _, _ ->
                viewModel.deleteProduct(product.id)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showLogoutConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Yes") { _, _ ->
                viewModel.logout()
                // Navigate back to login screen
                findNavController().navigate(R.id.action_myProductsFragment_to_loginFragment)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshProducts()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 
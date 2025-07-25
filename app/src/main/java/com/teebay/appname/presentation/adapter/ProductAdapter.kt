package com.teebay.appname.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.teebay.appname.databinding.ItemProductBinding
import com.teebay.appname.domain.model.Product
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

class ProductAdapter(
    private val onProductClick: (Product) -> Unit,
    private val onEditClick: (Product) -> Unit
) : ListAdapter<Product, ProductAdapter.ProductViewHolder>(ProductDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = ItemProductBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ProductViewHolder(
        private val binding: ItemProductBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(product: Product) {
            binding.apply {
                // Set product title
                tvProductTitle.text = product.title

                // Set categories
                tvCategories.text = product.categories.joinToString(", ") { it.displayName }

                // Set price with currency formatting
                val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US)
                val formattedPrice = "${currencyFormat.format(product.price)} ${product.rentDuration.displayName}"
                tvPrice.text = formattedPrice

                // Set description
                tvDescription.text = product.description

                // Load product image (placeholder for now)
                if (product.imagePath != null) {
                    // TODO: Load actual image from path
                    ivProductImage.setImageResource(android.R.drawable.ic_menu_gallery)
                } else {
                    ivProductImage.setImageResource(android.R.drawable.ic_menu_gallery)
                }

                // Click listeners
                root.setOnClickListener {
                    onProductClick(product)
                }

                ivEdit.setOnClickListener {
                    onEditClick(product)
                }
            }
        }
    }

    private class ProductDiffCallback : DiffUtil.ItemCallback<Product>() {
        override fun areItemsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem == newItem
        }
    }
} 
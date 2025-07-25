package com.teebay.appname.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.teebay.appname.databinding.ItemCategoryBinding
import com.teebay.appname.domain.model.Category

class CategoryAdapter(
    private val onCategoryToggled: (Category, Boolean) -> Unit
) : ListAdapter<CategoryItem, CategoryAdapter.CategoryViewHolder>(CategoryDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding = ItemCategoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CategoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class CategoryViewHolder(
        private val binding: ItemCategoryBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(categoryItem: CategoryItem) {
            binding.apply {
                tvCategoryName.text = categoryItem.category.displayName
                cbCategory.isChecked = categoryItem.isSelected

                // Set card appearance based on selection
                root.strokeColor = if (categoryItem.isSelected) {
                    root.context.getColor(android.R.color.holo_blue_light)
                } else {
                    root.context.getColor(com.teebay.appname.R.color.design_default_color_outline)
                }

                // Handle clicks on the entire card
                root.setOnClickListener {
                    val newState = !categoryItem.isSelected
                    cbCategory.isChecked = newState
                    onCategoryToggled(categoryItem.category, newState)
                }

                // Handle checkbox clicks
                cbCategory.setOnCheckedChangeListener { _, isChecked ->
                    onCategoryToggled(categoryItem.category, isChecked)
                }
            }
        }
    }

    private class CategoryDiffCallback : DiffUtil.ItemCallback<CategoryItem>() {
        override fun areItemsTheSame(oldItem: CategoryItem, newItem: CategoryItem): Boolean {
            return oldItem.category == newItem.category
        }

        override fun areContentsTheSame(oldItem: CategoryItem, newItem: CategoryItem): Boolean {
            return oldItem == newItem
        }
    }
}

data class CategoryItem(
    val category: Category,
    val isSelected: Boolean = false
) 
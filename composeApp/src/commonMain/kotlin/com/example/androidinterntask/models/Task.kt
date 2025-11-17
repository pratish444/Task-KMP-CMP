package com.example.androidinterntask.models


import kotlinx.serialization.Serializable

@Serializable
data class Task(
    val id: String,
    val taskType: TaskType,
    val timestamp: String,
    val durationSec: Int,
    val text: String? = null,
    val imageUrl: String? = null,
    val imagePath: String? = null,
    val audioPath: String? = null,
    val textDescription: String? = null
)

enum class TaskType {
    TEXT_READING,
    IMAGE_DESCRIPTION,
    PHOTO_CAPTURE
}

@Serializable
data class Product(
    val id: Int,
    val title: String,
    val description: String,
    val price: Double,
    val discountPercentage: Double,
    val rating: Double,
    val stock: Int,
    val brand: String? = null, // Made nullable to handle missing field
    val category: String,
    val thumbnail: String,
    val images: List<String>
)

@Serializable
data class ProductsResponse(
    val products: List<Product>,
    val total: Int,
    val skip: Int,
    val limit: Int
)
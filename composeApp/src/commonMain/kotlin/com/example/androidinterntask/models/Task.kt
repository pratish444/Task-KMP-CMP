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
    val audioPath: String
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
    val images: List<String>
)

@Serializable
data class ProductsResponse(
    val products: List<Product>
)
package com.example.androidinterntask.data

import com.example.androidinterntask.models.Product
import com.example.androidinterntask.models.ProductsResponse
import com.example.androidinterntask.models.Task
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

object TaskRepository {
    private val tasks = mutableListOf<Task>()

    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }

    fun addTask(task: Task) {
        tasks.add(task)
    }

    fun getAllTasks(): List<Task> = tasks.toList()

    fun getTotalDuration(): Int = tasks.sumOf { it.durationSec }

    suspend fun fetchProduct(): Product? {
        return try {
            val response: ProductsResponse = client.get("https://dummyjson.com/products").body()
            response.products.firstOrNull()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
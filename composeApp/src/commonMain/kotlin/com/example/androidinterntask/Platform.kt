package com.example.androidinterntask

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
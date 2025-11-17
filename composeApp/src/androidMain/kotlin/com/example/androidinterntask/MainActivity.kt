package com.example.androidinterntask

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import com.example.androidinterntask.utils.PermissionHandler

// Create a CompositionLocal for MainActivity
val LocalMainActivity = compositionLocalOf<MainActivity> {
    error("No MainActivity provided")
}

class MainActivity : ComponentActivity() {

    lateinit var permissionHandler: PermissionHandler
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        permissionHandler = PermissionHandler(this)

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CompositionLocalProvider(LocalMainActivity provides this) {
                        App()
                    }
                }
            }
        }
    }
}
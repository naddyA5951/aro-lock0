package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.ui.navigation.MainAppScaffold
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.TrekViewModel

class MainActivity : ComponentActivity() {

    private val trekViewModel: TrekViewModel by viewModels {
        TrekViewModel.Factory((application as ArolockApp).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppScaffold(viewModel = trekViewModel)
            }
        }
    }
}

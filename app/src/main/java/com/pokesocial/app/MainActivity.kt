package com.pokesocial.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.pokesocial.app.ui.navigation.AppNavGraph
import com.pokesocial.app.ui.theme.IgWhite
import com.pokesocial.app.ui.theme.PokeSocialTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PokeSocialTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = IgWhite) {
                    AppNavGraph()
                }
            }
        }
    }
}

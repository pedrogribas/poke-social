package com.pokesocial.app.ui.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.pokesocial.app.core.AppConstants
import com.pokesocial.app.ui.theme.IgBlack
import com.pokesocial.app.ui.theme.IgBlue
import com.pokesocial.app.ui.theme.IgGray
import com.pokesocial.app.ui.theme.IgWhite

@Composable
fun SplashScreen(
    viewModel: SplashViewModel,
    onReady: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(state.ready) {
        if (state.ready) onReady()
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(IgWhite),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {
            Text(
                "PokeSocial",
                fontFamily = FontFamily.Cursive,
                fontWeight = FontWeight.Bold,
                fontSize = 42.sp,
                color = IgBlack
            )
            Spacer(Modifier.height(24.dp))
            AsyncImage(
                model = AppConstants.artworkUrl(AppConstants.ME_POKEMON_ID),
                contentDescription = "Lucario",
                modifier = Modifier.size(120.dp)
            )
            Spacer(Modifier.height(8.dp))
            Text("@${AppConstants.ME_USERNAME}", color = IgGray, fontSize = 14.sp)
            Spacer(Modifier.height(28.dp))
            LinearProgressIndicator(
                progress = { state.progress.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp),
                color = IgBlue,
                trackColor = IgGray.copy(alpha = 0.2f),
            )
            Spacer(Modifier.height(12.dp))
            Text(state.message, color = IgGray, fontSize = 13.sp)
            state.error?.let {
                Spacer(Modifier.height(16.dp))
                Text(it, color = IgBlack, fontSize = 13.sp)
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = viewModel::retry,
                    colors = ButtonDefaults.buttonColors(containerColor = IgBlue),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Tentar de novo")
                }
            }
        }
    }
}

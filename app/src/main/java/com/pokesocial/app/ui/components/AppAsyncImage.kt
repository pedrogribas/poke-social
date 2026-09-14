package com.pokesocial.app.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.pokesocial.app.core.LocalMedia

/** Carrega http(s) ou assets locais (`file:///android_asset/...`). */
@Composable
fun AppAsyncImage(
    data: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    filterQuality: FilterQuality = FilterQuality.Low
) {
    val context = LocalContext.current
    val isGif = data?.endsWith(".gif", ignoreCase = true) == true
    val modelData: Any? = when {
        data.isNullOrBlank() -> null
        data.startsWith("file:///android_asset/") -> data
        else -> LocalMedia.forCoil(data)
    }
    AsyncImage(
        model = ImageRequest.Builder(context)
            .data(modelData)
            // crossfade força bitmap estático e mata a animação do GIF
            .crossfade(!isGif)
            .build(),
        contentDescription = contentDescription,
        contentScale = contentScale,
        filterQuality = filterQuality,
        modifier = modifier
    )
}

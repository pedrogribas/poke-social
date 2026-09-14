package com.pokesocial.app.ui.post

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pokesocial.app.data.local.ContentThemes
import com.pokesocial.app.data.repository.SocialRepository
import com.pokesocial.app.ui.components.AppAsyncImage
import com.pokesocial.app.ui.components.IgSimpleTopBar
import com.pokesocial.app.ui.theme.IgBlack
import com.pokesocial.app.ui.theme.IgBlue
import com.pokesocial.app.ui.theme.IgGray
import com.pokesocial.app.ui.theme.IgWhite
import kotlinx.coroutines.launch

data class CreateMediaOption(
    val url: String,
    val type: String,
    val label: String
)

class CreatePostViewModel(private val repo: SocialRepository) : ViewModel() {
    val mediaOptions: List<CreateMediaOption> = buildList {
        ContentThemes.all.forEach { theme ->
            theme.images.forEachIndexed { i, path ->
                add(
                    CreateMediaOption(
                        url = ContentThemes.assetUri(path),
                        type = "IMAGE",
                        label = "${theme.id} ${i + 1}"
                    )
                )
            }
        }
        ContentThemes.extraImages.forEachIndexed { i, path ->
            add(
                CreateMediaOption(
                    url = ContentThemes.assetUri(path),
                    type = "IMAGE",
                    label = "extra ${i + 1}"
                )
            )
        }
        ContentThemes.videos.forEachIndexed { i, path ->
            add(
                CreateMediaOption(
                    url = ContentThemes.assetUri(path),
                    type = "VIDEO",
                    label = "video ${i + 1}"
                )
            )
        }
    }

    fun publish(
        caption: String,
        media: CreateMediaOption,
        onDone: () -> Unit
    ) {
        viewModelScope.launch {
            repo.createPost(caption, media.url, media.type)
            onDone()
        }
    }
}

@Composable
fun CreatePostScreen(
    viewModel: CreatePostViewModel,
    onBack: () -> Unit,
    onPublished: () -> Unit
) {
    var caption by remember { mutableStateOf("") }
    var selected by remember { mutableStateOf<CreateMediaOption?>(null) }
    var publishing by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            IgSimpleTopBar(
                title = "Nova publicação",
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = IgBlack)
                    }
                },
                actions = {
                    Button(
                        onClick = {
                            val media = selected ?: return@Button
                            publishing = true
                            viewModel.publish(caption, media) {
                                publishing = false
                                onPublished()
                            }
                        },
                        enabled = selected != null && !publishing,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = IgBlue,
                            contentColor = IgWhite,
                            disabledContainerColor = IgBlue.copy(alpha = 0.35f)
                        ),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp)
                    ) {
                        if (publishing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = IgWhite,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Publicar", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            OutlinedTextField(
                value = caption,
                onValueChange = { caption = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Escreva uma legenda…", color = IgGray) },
                minLines = 2,
                maxLines = 4
            )
            Text(
                "Escolha a mídia",
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = IgBlack,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(1.dp),
                horizontalArrangement = Arrangement.spacedBy(1.dp),
                verticalArrangement = Arrangement.spacedBy(1.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(viewModel.mediaOptions, key = { it.url }) { option ->
                    val isSelected = selected?.url == option.url
                    val thumbUrl = if (option.type == "VIDEO") {
                        ContentThemes.assetUri(
                            ContentThemes.extraImages[
                                option.url.hashCode().floorMod(ContentThemes.extraImages.size)
                            ]
                        )
                    } else {
                        option.url
                    }
                    Box(
                        Modifier
                            .aspectRatio(1f)
                            .clickable { selected = option }
                            .then(
                                if (isSelected) Modifier.border(2.dp, IgBlue)
                                else Modifier
                            )
                    ) {
                        AppAsyncImage(
                            data = thumbUrl,
                            contentDescription = option.label,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        if (option.type == "VIDEO") {
                            Icon(
                                Icons.Filled.PlayCircleFilled,
                                null,
                                tint = IgWhite,
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .size(28.dp)
                            )
                        }
                        if (isSelected) {
                            Icon(
                                Icons.Filled.CheckCircle,
                                null,
                                tint = IgBlue,
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(6.dp)
                                    .size(22.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun Int.floorMod(m: Int): Int {
    val r = this % m
    return if (r >= 0) r else r + m
}

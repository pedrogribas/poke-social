package com.pokesocial.app.ui.profile

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.AddBox
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pokesocial.app.domain.model.Highlight
import com.pokesocial.app.ui.components.AppAsyncImage
import com.pokesocial.app.ui.components.IgAvatar
import com.pokesocial.app.ui.components.IgSimpleTopBar
import com.pokesocial.app.ui.components.PokemonTypeChips
import com.pokesocial.app.ui.theme.IgBg
import com.pokesocial.app.ui.theme.IgBlack
import com.pokesocial.app.ui.theme.IgBlue
import com.pokesocial.app.ui.theme.IgGray
import com.pokesocial.app.ui.theme.IgLightGray
import com.pokesocial.app.ui.theme.IgWhite
import com.pokesocial.app.ui.util.formatCount

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onBack: (() -> Unit)? = null,
    onMessage: (() -> Unit)? = null,
    onOpenPost: (String) -> Unit = {},
    onOpenFollowers: () -> Unit = {},
    onOpenFollowing: () -> Unit = {},
    onOpenHighlight: (Highlight) -> Unit = {},
    onCreatePost: (() -> Unit)? = null
) {
    val user by viewModel.user.collectAsStateWithLifecycle()
    val posts by viewModel.posts.collectAsStateWithLifecycle()
    val loading by viewModel.loading.collectAsStateWithLifecycle()
    val isFollowing by viewModel.isFollowing.collectAsStateWithLifecycle()
    val highlights by viewModel.highlights.collectAsStateWithLifecycle()
    val followedBy by viewModel.followedBy.collectAsStateWithLifecycle()
    val followedByExtra by viewModel.followedByExtra.collectAsStateWithLifecycle()
    val isMe = user?.isMe == true
    var selectedTab by remember { mutableIntStateOf(0) }
    var editingProfile by remember { mutableStateOf(false) }
    var notifyOn by remember { mutableStateOf(true) }
    val context = LocalContext.current

    val gridPosts = remember(posts, selectedTab) {
        when (selectedTab) {
            1 -> posts.filter { it.mediaType == "VIDEO" }
            else -> posts.filter { it.mediaType != "VIDEO" }
        }
    }

    Scaffold(
        topBar = {
            IgSimpleTopBar(
                title = user?.username ?: "",
                titleBold = true,
                navigationIcon = onBack?.let { back ->
                    {
                        IconButton(onClick = back) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = IgBlack)
                        }
                    }
                },
                actions = {
                    if (isMe) {
                        if (onCreatePost != null) {
                            IconButton(onClick = onCreatePost) {
                                Icon(Icons.Outlined.AddBox, contentDescription = "Criar", tint = IgBlack)
                            }
                        }
                        IconButton(onClick = {
                            val send = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, "@${user?.username.orEmpty()}")
                            }
                            context.startActivity(Intent.createChooser(send, "Compartilhar perfil"))
                        }) {
                            Icon(Icons.Outlined.Menu, contentDescription = "Menu", tint = IgBlack)
                        }
                    } else {
                        IconButton(onClick = { notifyOn = !notifyOn }) {
                            Icon(
                                if (notifyOn) Icons.Filled.Notifications else Icons.Outlined.NotificationsNone,
                                contentDescription = "Notificações",
                                tint = IgBlack
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (loading || user == null) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = IgBlue)
            }
        } else {
            val u = user!!
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(IgWhite),
                contentPadding = PaddingValues(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(1.dp),
                verticalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                item(span = { GridItemSpan(3) }) {
                    Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IgAvatar(u.avatarUrl, 86.dp)
                            Spacer(Modifier.width(28.dp))
                            Row(
                                Modifier.weight(1f),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                Stat(formatCount(posts.size), "posts")
                                Stat(formatCount(u.followers), "seguidores", onOpenFollowers)
                                Stat(formatCount(u.following), "seguindo", onOpenFollowing)
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                        Text(
                            u.displayName,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = IgBlack,
                            letterSpacing = 0.sp
                        )
                        PokemonTypeChips(u.types)
                        if (u.bio.isNotBlank()) {
                            Text(
                                u.bio,
                                color = IgBlack,
                                fontSize = 14.sp,
                                lineHeight = 18.sp,
                                modifier = Modifier.padding(top = 1.dp)
                            )
                        }
                        if (!isMe && followedBy.isNotEmpty()) {
                            val names = followedBy.joinToString(", ") { it.username }
                            val extra = if (followedByExtra > 0) {
                                " e outras ${formatCount(followedByExtra)} pessoas"
                            } else ""
                            Row(
                                Modifier.padding(top = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    Modifier
                                        .width((12 * (followedBy.size - 1) + 22).dp)
                                        .height(22.dp)
                                ) {
                                    followedBy.forEachIndexed { index, person ->
                                        IgAvatar(
                                            person.avatarUrl,
                                            22.dp,
                                            modifier = Modifier
                                                .offset(x = (index * 12).dp)
                                                .border(1.5.dp, IgWhite, CircleShape)
                                        )
                                    }
                                }
                                Text(
                                    "Seguido(a) por $names$extra",
                                    color = IgBlack,
                                    fontSize = 12.sp,
                                    lineHeight = 15.sp,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (isMe) {
                                ProfilePillButton(
                                    "Editar perfil",
                                    Modifier.weight(1f),
                                    onClick = { editingProfile = true }
                                )
                                ProfilePillButton(
                                    "Compartilhar perfil",
                                    Modifier.weight(1f),
                                    onClick = {
                                        val send = Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(Intent.EXTRA_TEXT, "@${u.username}")
                                        }
                                        context.startActivity(Intent.createChooser(send, "Compartilhar perfil"))
                                    }
                                )
                            } else {
                                ProfilePillButton(
                                    if (isFollowing) "Seguindo" else "Seguir",
                                    Modifier.weight(1f),
                                    filledBlue = !isFollowing,
                                    trailingIcon = if (isFollowing) Icons.Filled.KeyboardArrowDown else null,
                                    onClick = { viewModel.toggleFollow() }
                                )
                                if (onMessage != null) {
                                    ProfilePillButton(
                                        "Mensagem",
                                        Modifier.weight(1f),
                                        onClick = onMessage
                                    )
                                }
                            }
                            ProfileIconButton(onClick = onOpenFollowing) {
                                Icon(
                                    Icons.Outlined.PersonAdd,
                                    contentDescription = "Sugestões",
                                    tint = IgBlack,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        if (highlights.isNotEmpty()) {
                            Spacer(Modifier.height(18.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                items(highlights, key = { it.id }) { hl ->
                                    HighlightCircle(hl) { onOpenHighlight(hl) }
                                }
                            }
                        }
                    }
                }
                item(span = { GridItemSpan(3) }) {
                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = IgWhite,
                        contentColor = IgBlack,
                        divider = {},
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                                color = IgBlack,
                                height = 1.dp
                            )
                        }
                    ) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            icon = {
                                Icon(
                                    Icons.Filled.GridOn,
                                    contentDescription = "Publicações",
                                    tint = if (selectedTab == 0) IgBlack else IgBlack.copy(alpha = 0.35f)
                                )
                            }
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            icon = {
                                Icon(
                                    Icons.Filled.PlayArrow,
                                    contentDescription = "Reels",
                                    tint = if (selectedTab == 1) IgBlack else IgBlack.copy(alpha = 0.35f)
                                )
                            }
                        )
                    }
                }
                items(gridPosts, key = { it.id }) { post ->
                    Box(
                        Modifier
                            .aspectRatio(1f)
                            .background(IgBg)
                            .clickable { onOpenPost(post.id) }
                    ) {
                        AppAsyncImage(
                            data = post.media.firstOrNull()?.url,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        if (post.mediaType == "VIDEO" || post.media.size > 1) {
                            Icon(
                                Icons.Filled.PlayArrow,
                                contentDescription = null,
                                tint = IgWhite,
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(6.dp)
                                    .size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    if (editingProfile && user != null) {
        var nameDraft by remember(user!!.id) { mutableStateOf(user!!.displayName) }
        var bioDraft by remember(user!!.id) { mutableStateOf(user!!.bio) }
        AlertDialog(
            onDismissRequest = { editingProfile = false },
            title = { Text("Editar perfil") },
            text = {
                Column {
                    OutlinedTextField(
                        value = nameDraft,
                        onValueChange = { nameDraft = it },
                        label = { Text("Nome") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = bioDraft,
                        onValueChange = { bioDraft = it },
                        label = { Text("Bio") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.updateProfile(nameDraft, bioDraft)
                        editingProfile = false
                    }
                ) { Text("Salvar") }
            },
            dismissButton = {
                TextButton(onClick = { editingProfile = false }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
private fun ProfilePillButton(
    label: String,
    modifier: Modifier = Modifier,
    filledBlue: Boolean = false,
    trailingIcon: ImageVector? = null,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(32.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (filledBlue) IgBlue else IgBg,
            contentColor = if (filledBlue) IgWhite else IgBlack
        ),
        shape = RoundedCornerShape(8.dp),
        contentPadding = PaddingValues(horizontal = 8.dp),
        elevation = ButtonDefaults.buttonElevation(0.dp)
    ) {
        Text(label, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        if (trailingIcon != null) {
            Icon(
                trailingIcon,
                contentDescription = null,
                modifier = Modifier
                    .padding(start = 2.dp)
                    .size(16.dp)
            )
        }
    }
}

@Composable
private fun ProfileIconButton(
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .height(32.dp)
            .width(36.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = IgBg,
            contentColor = IgBlack
        ),
        shape = RoundedCornerShape(8.dp),
        contentPadding = PaddingValues(0.dp),
        elevation = ButtonDefaults.buttonElevation(0.dp)
    ) {
        content()
    }
}

@Composable
private fun HighlightCircle(highlight: Highlight, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(72.dp)
            .clickable(onClick = onClick)
    ) {
        Box(
            Modifier
                .size(64.dp)
                .border(1.dp, IgLightGray, CircleShape)
                .padding(3.dp)
                .clip(CircleShape)
                .background(IgBg)
        ) {
            AppAsyncImage(
                data = highlight.coverUrl,
                contentDescription = highlight.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
        Text(
            highlight.title,
            fontSize = 12.sp,
            color = IgBlack,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 6.dp)
        )
    }
}

@Composable
private fun Stat(value: String, label: String, onClick: (() -> Unit)? = null) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
    ) {
        Text(value, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = IgBlack)
        Text(label, fontSize = 13.sp, color = IgBlack)
    }
}

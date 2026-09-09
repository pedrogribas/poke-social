package com.pokesocial.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pokesocial.app.domain.model.Story
import com.pokesocial.app.ui.theme.IgBlack
import com.pokesocial.app.ui.theme.IgGray
import com.pokesocial.app.ui.theme.IgLightGray
import com.pokesocial.app.ui.theme.IgWhite

data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector = icon
)

@Composable
fun IgHomeTopBar(
    onActivity: () -> Unit = {},
    onMessages: () -> Unit = {},
    activityIcon: ImageVector,
    messagesIcon: ImageVector
) {
    Column(Modifier.fillMaxWidth().background(IgWhite)) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "PokeSocial",
                fontFamily = FontFamily.Cursive,
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
                color = IgBlack,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onActivity) {
                Icon(activityIcon, contentDescription = "Atividade", tint = IgBlack)
            }
            IconButton(onClick = onMessages) {
                Icon(messagesIcon, contentDescription = "Mensagens", tint = IgBlack)
            }
        }
        HorizontalDivider(color = IgLightGray, thickness = 0.5.dp)
    }
}

@Composable
fun IgSimpleTopBar(
    title: String,
    navigationIcon: (@Composable () -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    Column(Modifier.fillMaxWidth().background(IgWhite)) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            navigationIcon?.invoke()
            Text(
                title,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                color = IgBlack,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp)
            )
            actions()
        }
        HorizontalDivider(color = IgLightGray, thickness = 0.5.dp)
    }
}

@Composable
fun IgBottomBar(
    items: List<BottomNavItem>,
    currentRoute: String?,
    onNavigate: (String) -> Unit
) {
    NavigationBar(containerColor = IgWhite, tonalElevation = 0.dp) {
        items.forEach { item ->
            val selected = currentRoute == item.route
            NavigationBarItem(
                selected = selected,
                onClick = { onNavigate(item.route) },
                icon = {
                    Icon(
                        if (selected) item.selectedIcon else item.icon,
                        contentDescription = item.label,
                        tint = IgBlack
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = IgWhite,
                    selectedIconColor = IgBlack,
                    unselectedIconColor = IgBlack
                )
            )
        }
    }
}

@Composable
fun StoryRail(
    stories: List<Story>,
    myStoryFirst: Story?,
    onStoryClick: (Story) -> Unit
) {
    val ordered = buildList {
        myStoryFirst?.let { add(it) }
        addAll(stories.filter { it.id != myStoryFirst?.id })
    }
    LazyRow(
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxWidth().background(IgWhite)
    ) {
        items(ordered, key = { it.id }) { story ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .width(74.dp)
                    .clickable { onStoryClick(story) }
            ) {
                IgAvatar(
                    imageUrl = story.author.avatarUrl,
                    size = 66.dp,
                    showStoryRing = true,
                    seen = story.seenByMe
                )
                Text(
                    if (story.author.isMe) "Seu story" else story.author.username,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = IgBlack,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}
